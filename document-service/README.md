# Document Service

This project exposes two JWT-protected REST endpoints behind Quarkus and validates the bearer token against a Keycloak realm at `http://localhost:8080`.

## Endpoints

- `GET http://localhost:8081/api/documents`
- `GET http://localhost:8081/api/profile`

Both endpoints require a valid JWT access token issued by the Keycloak realm `document-service`.

## Start Keycloak

```bash
cd document-service
docker compose up -d
```

Wait for Keycloak to finish starting, then confirm the realm is available:

```bash
curl http://localhost:8080/realms/document-service/.well-known/openid-configuration
```

## Start the Quarkus app

```bash
cd document-service
mvn quarkus:dev
```

The application listens on port `8081` to avoid conflicting with Keycloak on port `8080`.

## Get a token from Keycloak

```bash
curl -X POST 'http://localhost:8080/realms/document-service/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=alice' \
  -d 'password=password' \
  -d 'grant_type=password' \
  -d 'client_id=document-service' \
  -d 'client_secret=super-secret'
```

Then use the returned `access_token`:

```bash
curl -H 'Authorization: Bearer <ACCESS_TOKEN>' http://localhost:8081/api/documents
curl -H 'Authorization: Bearer <ACCESS_TOKEN>' http://localhost:8081/api/profile
```

## Default users

- `alice` / `password` with role `user`
- `bob` / `password` with role `admin`
