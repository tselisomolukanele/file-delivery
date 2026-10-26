# file-delivery
A system to store customer account statements as PDF files and provide secure, time-limited download links to customers.

The system is a collection of microservices conternerized with Docker and ochestrated usind Docker compose.

## Requiments
### Compiling and building 
#### Backend
- Java 21
- Apache Maven 3.9.9
#### Frontend
- NodeJs 24
- NPM 11

To build respective docker images run the following commands
- Backend
    - `cd document-service`
    - `./mvnw clean package`
    - `docker build -t filedelivery-api .` resulting Docker image *<configured-docker-username>document-service:1.0.0-SNAPSHOT*
- Frontend
    - `cd UI`
    - `npm ci --legacy-peer-deps`
    - `npm run build`
    - `docker build -t file-delivery-ui .` resulting Docker image *file-delivery-ui:latest*

### Running with Docker Compose
#### Prepare the docker runtime by specifying the following
1. .env file.  There is an env.example with the various secrets to used by Docker compose
2. The relevant certificates for the UI and Keycloak must be put in the respective locations.  Alternatively you can edit the compose.yml and edit the rererences to point to the correct file paths
    1. compose/secrets/keycloak.crt
    2. compose/secrets/keycloak.key
    3. compose/secrets/ui.crt
    4. compose/secrets/ui.key
Afer building the 2 images you can run the solution with dependencies using the following command on the commands in the project root folder
- `source .env`
- `cd compose`
- `docker compose up`

### Usage
##### Preparation
1. On Keycloak has the following pre set
    - realm named `file-delivery`
    - client named `file-delivery-ui`
    - 2 realm roles `file-uploader` and `file-owner` with respective users admin:admin and client:client 
    - At least a user for each of the roles above
2. On S3 set up bucket named quarkus.s3.file-delivery-bucket

After the preparation access the UI and login with the user having file-uploader rights.  With this user on the accounts page it is possible to add client and thier documents.  *Make sure the client name matched the login name in keycloak* 

After adding the respetive clients you can log out and log in using the users added above with their relavant Keycloak credentials.

On the account page the documents will be listed and can be downloaded or emailed (through a simple HTML simulator)

The documets link expire through a configured property `token.ttl` and is useable once only.  The property is configuratble in the application.properties file.

## Project development documentation
An architecture diagram has been added for reference ![Architechture Design](design.svg)
