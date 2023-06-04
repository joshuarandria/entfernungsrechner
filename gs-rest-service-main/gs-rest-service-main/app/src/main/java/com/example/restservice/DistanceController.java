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

@RestController
public class DistanceController {

	public record BahnhofInfo(String name, Double laenge, Double breite) {}

	@GetMapping("/api/v1/distance/{from}/{to}")
	public Distance distance(@PathVariable("from") String from, @PathVariable("to") String to) {
		BahnhofInfo fromBhf = getBhfInfo(from);
		BahnhofInfo toBhf = getBhfInfo(to);
		long distance = computeDistance(fromBhf, toBhf);
	
		return new Distance(fromBhf.name(), toBhf.name(), distance, "km");
	}

	private BahnhofInfo getBhfInfo(String dscode) {
		System.out.println("getBhfInfo("+dscode+")");
		// String filePath = "D_test.csv";
		String filePath = "D_Bahnhof_2020_alle.csv";

		try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withDelimiter(';'))) {

			for (CSVRecord csvRecord : csvParser) {
				String ds100 = csvRecord.get("DS100");
				String verkehr = csvRecord.get("Verkehr");
				if (ds100.equals(dscode)) {
					System.out.println("dscode: "+dscode);
					System.out.println("ds100: "+ds100);
					if (verkehr.equals("FV")) { // interested only if Fernverkehr
						String name = csvRecord.get("NAME");
						double laenge = Double.parseDouble(csvRecord.get("Laenge").replace(",", "."));
						double breite = Double.parseDouble(csvRecord.get("Breite").replace(",", "."));
						System.out.println(name+", "+ laenge +", "+ breite);
						return new BahnhofInfo(name, laenge, breite);
					}
				}
			}

		} catch (IOException e) {
            e.printStackTrace();
        }
		return null;
    }

	private long computeDistance(BahnhofInfo fromBhf, BahnhofInfo toBhf) {
		System.out.println("computeDistance(");
		System.out.println(fromBhf.toString());
		System.out.println(toBhf.toString());
		System.out.println(")");

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

		long roundedDist=Math.round(distance);
		System.out.println("roundedDist= "+ roundedDist);
		return roundedDist;
    }
}
