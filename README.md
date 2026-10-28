# file-delivery
A system for storing customer account statements as PDF files and providing secure, time-limited download links to customers.

The system is a collection of microservices containerized with Docker and orchestrated using Docker Compose.

## Requirements
### Compiling and building
#### Backend
- Java 21
- Apache Maven 3.9.9

#### Frontend
- Node.js 24
- npm 11

To build the Docker images, run the following commands:
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
#### Prepare the Docker runtime by specifying the following
1. An `.env` file. There is an `env.example` file with the various secrets used by Docker Compose.
2. The relevant certificates for the UI and Keycloak must be placed in their respective locations. Alternatively, edit `compose.yml` to point to the correct file paths.
    1. `compose/secrets/keycloak.crt`
    2. `compose/secrets/keycloak.key`
    3. `compose/secrets/ui.crt`
    4. `compose/secrets/ui.key`
    5. `compose/secrets/keycloak.p12` — this is a truststore containing the certificate in file 1, `compose/secrets/keycloak.crt`

The solution can be started with its dependencies using the following commands from the project root:
- `source .env`
- `cd compose`
- `docker compose up --build`

### Usage
#### Preparation
1. On Keycloak, configure the following:
    - realm named `file-delivery`
    - client named `file-delivery-ui`
    - 2 realm roles: `file-uploader` and `file-owner`, with the respective users `admin:admin` and `client:client`
    - at least one user for each of the roles above
2. On S3, set up a bucket named `quarkus.s3.file-delivery-bucket`

After preparation, access the UI and log in with the user who has `file-uploader` rights. On the Accounts page, this user can add clients and their documents. Ensure the client username matches the login name in Keycloak.

After adding the users, you can log out and log in again using the newly created users and their relevant Keycloak credentials.

On the Account page, documents will be listed and can be downloaded or emailed through a simple HTML email simulator. The simulator requires an OTP, which can be retrieved from the Accounts page for simulation purposes.

The document links expire according to the configured property `token.ttl` and can be used only once. This property is configurable in the `application.properties` file.

## Project development documentation
An architecture diagram has been added for reference: ![Architecture Design](design.svg)
