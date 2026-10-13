# file-delivery
A system to store customer account statements as PDF files and provide secure, time-limited download links to customers.

The system is a collection of microservices conternerized with Docker and ochestrated usind Docker compose.

## Requiments
### Compiling and building
- Linux OS
- Docker
#### Backend
The following must be on the path
- Java 21
- Apache Maven 3.8.7
#### Frontend
- NodeJs 24
- NPM 11

To build respective docker images run the following commands
- `source .env`
- Backend
    - `cd document-service`
    - `mvn install -Dquarkus.container-image.build=true` resulting Docker image *<configured-docker-username>document-service:1.0.0-SNAPSHOT*
- Frontend
    - `cd UI`
    - `npm run build`
    - `docker build -t file-delivery-ui .` resulting Docker image *file-delivery-ui:latest*

### Running
Afer building the 2 images you can run the solution with dependencies using the following command on the commands in the project root folder
- `source .env`
- `cd compose`
-  `docker compose up`

## Project development documentation
For more information on how this project was completed please see [dev docs](development.md)
