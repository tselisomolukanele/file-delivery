const apiBaseUrl = 'http://file-delivery-service:8081';
const authBaseUrl = 'http://keycloak:8080';
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
