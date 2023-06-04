# entfernungsrechner

Code as answer for the Case Study Sprintstarter-Programm (see pdf)

To run the app:
cd /path/to/db-entfernungsrechner
mvn clean install
./mvnw spring-boot:run


In Web Browser:
http://localhost:8080/api/v1/distance/{from}/{to}

from: DS100 code for a station part of the "FernVerkhehr" (Long distance train station)
e.g. http://localhost:8080/api/v1/distance/FF/BLS


If mvn does not work use .jar:
java -jar target/rest-service-app-0.0.1-SNAPSHOT.jar
