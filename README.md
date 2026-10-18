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
    - `docker build -t filedelivery-api .` resulting Docker image *<configured-docker-username>document-service:1.0.0-SNAPSHOT*
- Frontend
    - `cd UI`
    - `docker build -t file-delivery-ui .` resulting Docker image *file-delivery-ui:latest*

### Running (Dev mode)
After setting up the various dependencies the application can be ran in dev mode by following these commands
- `source .env`
- `cd document-service` //from root folder
- `./run.sh`
- `./cd UI` //from root folder
- `ng serve`

### Running (Docker Compose, experimental)
Afer building the 2 images you can run the solution with dependencies using the following command on the commands in the project root folder
- `source .env`
- `cd compose`
- `docker compose up`

### Usage
##### Preparation
1. Make sure all the values in `.env` and `file-delivery/UI/src/environments/environment.development.ts` OR `file-delivery/UI/src/environments/environment.production.ts` are consistent with installed dependencies
2. On Keycloak add the following
    - realm named `file-delivery`
    - client named `file-delivery-ui`
    - 2 realm roles `file-uploader` and `file-owner` 
    - At least a user for each of the roles above
3. On S3 set up bucket named quarkus.s3.file-delivery-bucket

After the preparation access the UI and login with the user having admin rights.  With this user on the accounts page it is possible to add client and thier documents.  *Make sure the client name matched the login name in keycloak* 

After adding the respetive clients you can log out and log in using the users added above with therelant Keycloak credentials.

On the account page the documents will be listed and can be downloaded or emailed (through a simple HTML simulator)

The documets link expire through a configured property `token.ttl` and is useable once only

## Project development documentation
For more information on how this project was completed please see [dev docs](development.md)
