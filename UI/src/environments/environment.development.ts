const apiBaseUrl = 'http://localhost:8081';
const authBaseUrl = 'http://localhost:8080';
const keycloakRealm = 'file-delivery';
const keycloakClientId = 'file-delivery-ui';

export const environment = {
  production: true,
  apiBaseUrl,
  authBaseUrl,
  keycloak: {
    url: authBaseUrl,
    realm: keycloakRealm,
    clientId: keycloakClientId,
  },
};

// Read contents of hosted config file and replace environment values if present
const response = await fetch('/config.json')

if (!response.ok) {
  console.warn(`No external configs loaded on system ${response.status}, default configs applied.`)
} else {
  const responseBody = await response.json()
  console.log(`External configs loaded successfully ${JSON.stringify(responseBody)}`)
  if (responseBody.apiBaseUrl) {
    environment.apiBaseUrl = responseBody.apiBaseUrl
  }
  if (responseBody.authBaseUrl) {
    environment.authBaseUrl = responseBody.authBaseUrl
    environment.keycloak.url = responseBody.authBaseUrl
  }
  if (responseBody.keycloakRealm) {
    environment.keycloak.realm = responseBody.keycloakRealm
  }
  if (responseBody.keycloakClientId) {
    environment.keycloak.clientId = responseBody.keycloakClientId
  }
}
