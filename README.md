Receipt Processor API
This is a web service that processes receipts and calculates reward points based on specific rules.

Tech Stack
Java 17
Spring Boot
Maven
Docker

Running the Application
1. Clone the Repository
    git clone https://github.com/mayank145/receipt-processor.git
   
    cd receipt-processor
   
3. Build the Application
   
     mvn clean package
   
5. Run the Application Locally
   
    mvn spring-boot:run
   
    The server will start on http://localhost:8080.

7. Run with Docker
   
    docker build -t receipt-processor .
   
    docker run -p 8080:8080 receipt-processor
