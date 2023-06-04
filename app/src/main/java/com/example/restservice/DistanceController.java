package com.example.restservice;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DistanceController {

	public record BahnhofInfo(String name, Double laenge, Double breite) {}

	@GetMapping("/api/v1/distance/{from}/{to}")
	public Distance distance(@PathVariable("from") String from, @PathVariable("to") String to) throws Exception {
		BahnhofInfo fromBhf = getBhfInfo(from);
		BahnhofInfo toBhf = getBhfInfo(to);
		long distance = computeDistance(fromBhf, toBhf);
	
		return new Distance(fromBhf.name(), toBhf.name(), distance, "km");
	}

	/**
	Function getBhfInfo
	Input: String dscode corresponding to a specific Bahnhof
	Output: BahnHofInfo with relevant information on the associated Bahnhof
	*/
	private BahnhofInfo getBhfInfo(String dscode) throws Exception {
		//System.out.println("getBhfInfo("+dscode+")");
		String filePath = "D_Bahnhof_2020_alle.csv";

		// Fetch data in the csv file
		try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withDelimiter(';'))) {

			// Iterate on every row to find the corresponding DS100 code
			for (CSVRecord csvRecord : csvParser) {
				String ds100 = csvRecord.get("DS100");
				String verkehr = csvRecord.get("Verkehr");
				if (ds100.equals(dscode)) {
					if (verkehr.equals("FV")) { // filer on Fernverkehr
						String name = csvRecord.get("NAME");
						double laenge = Double.parseDouble(csvRecord.get("Laenge").replace(",", "."));
						double breite = Double.parseDouble(csvRecord.get("Breite").replace(",", "."));
						System.out.println(name+", "+ laenge +", "+ breite);
						return new BahnhofInfo(name, laenge, breite);
					}else{
						throw new Exception("DS100 "+ ds100 + " is not part of Fernverkehr");
					}
				}
			}
			System.out.println("DS100 "+ dscode + " not in database");
			throw new Exception("DS100 "+ dscode + " not in database");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }

	/**
	Function computeDistance
	Input: 2 BahnhofInfo record (e.g. BahnhofInfo[name=Frankfurt(Main)Hbf, laenge=8.663789, breite=50.107145])
	Output: long roundedDistance = computed 
	*/
	private long computeDistance(BahnhofInfo fromBhf, BahnhofInfo toBhf) {
		/*
		System.out.println("computeDistance(");
		System.out.println(fromBhf.toString());
		System.out.println(toBhf.toString());
		System.out.println(")");
		*/

		// Convert latitude and longitude from degrees to radians
		double latRad1 = Math.toRadians(fromBhf.breite());
		double lonRad1 = Math.toRadians(fromBhf.laenge());
		double latRad2 = Math.toRadians(toBhf.breite());
		double lonRad2 = Math.toRadians(toBhf.laenge());

		// Haversine formula with Mean Earth Radius
		double meanEarthRadius = 6371.0088; // in kilometers
		double dlat = latRad2 - latRad1;
		double dlon = lonRad2 - lonRad1;
		double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
				+ Math.cos(latRad1) * Math.cos(latRad2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = meanEarthRadius * c;

		// Round the computed distance (to whole km)
		long roundedDistance=Math.round(distance);
		System.out.println("roundedDistance= "+ roundedDistance+
			"\n_____________________________________");

		return roundedDistance;
    }

	/**
	Function handleException
	Input: caught Exception
	Output: Error message to be read on the web page
	*/
	@ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        String errorMessage = "An error occurred: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }
}
