# file-delivery
A system for storing customer account statements as PDF files and providing secure, time-limited download links to customers.

The system is composed of microservices containerized with Docker and orchestrated using Docker Compose.

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
  - `docker build -t filedelivery-api .` This produces a Docker image named *<configured-docker-username>document-service:1.0.0-SNAPSHOT*.
- Frontend
  - `cd UI`
  - `npm ci --legacy-peer-deps`
  - `npm run build`
  - `docker build -t file-delivery-ui .` This produces the Docker image *file-delivery-ui:latest*.

## Running with Docker Compose
### Prepare the Docker runtime by setting the following
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
    - a realm named `file-delivery`
    - a client named `file-delivery-ui`
    - two realm roles: `file-uploader` and `file-owner`, with the respective users `admin:admin` and `client:client`
    - at least one user for each of the roles above
2. On S3, set up a bucket named `quarkus.s3.file-delivery-bucket`

After preparation, access the UI and log in with a user who has `file-uploader` rights. On the Accounts page, this user can add clients and their documents. Ensure the client username matches the login name in Keycloak.

After adding the users, you can log out and log in again using the newly created users and their relevant Keycloak credentials.

On the Account page, documents will be listed and can be downloaded or emailed through a simple HTML email simulator. The simulator requires an OTP, which can be retrieved from the Accounts page for simulation purposes.

The document links expire according to the configured property `token.ttl` and can be used only once. This property is configurable in the `application.properties` file.

## How to test

**Assuming the loaded system properties are the same as those in `env.example`**

### Uploading documents
- Go to https://localhost:8443
- Log in as the `file-uploader` user `admin` with password `admin`
- Click the `Accounts` button at the top of the page. This is the main page for uploading documents.
- On the page, there is a form for adding customers who are the document owners and the users who will download the documents.
- Add a customer named `client-`. It is important that the customer name matches the username of a user added in Keycloak. A user named `client` has been preloaded.
- After adding the new user, they will appear in a list on the same `Accounts` page just above the add-customer form.
- Next to each customer entry is a form for uploading a document.
- After a successful upload, log out.

### Downloading documents
- Go to https://localhost:8443
- Log in as the `file-owner` user `client` with password `client`
- Click the `Account` button at the top of the page. This is the main page for downloading documents.
- On the page, there is a list of documents belonging to the logged-in user.
- For a standard authenticated download, click the `Download` button next to the file being downloaded.
- For a simulated link download, click `Email`. This starts the email simulation and showcases the single-use link functionality.
- The `Email` button opens a new window with a page that resembles an email inbox. The file download link can be found at the bottom of the page.
- Click the download button, which redirects to an OTP capture page to authenticate the owner of the document link.
- To retrieve the OTP for the owner of the link (`client` in this case), click `Generate OTP` in the session you logged in to earlier or in a new session. The OTP is displayed as a popup.
- Enter the OTP on the OTP capture page and the document will download.

### Adding users on Keycloak
- Log in to https://localhost:8082 as the Keycloak admin user with username `-security-` and password `-sEcur1ty-`.
- Switch to the `file-delivery` realm.
- Add users as required.
- Assign the `file-uploader` or `file-owner` role as required.

## Project development documentation
An architecture diagram has been added for reference: ![Architecture Design](design.svg)
