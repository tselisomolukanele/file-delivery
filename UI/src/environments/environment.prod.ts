const apiBaseUrl = 'http://file-delivery-service:8081';
const authBaseUrl = 'http://localhost:8084';
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
