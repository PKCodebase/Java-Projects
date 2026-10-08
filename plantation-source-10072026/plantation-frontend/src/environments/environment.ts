// Frontend configuration.
// NOTE: no secrets live here. The backend owns all credentials (JWT keys, DB
// passwords, payment keys, OAuth client secret) via ITS environment variables.
//
// Google sign-in is configured on the BACKEND with the GOOGLE_CLIENT_ID env
// var (a PUBLIC OAuth2 client id — never the client secret). The login page
// asks the backend for it at runtime (GET /auth/oauth2/google/config), so
// nothing has to be baked into this file and no client id is ever invented
// here.
export const environment = {
  production: false,

  // Backend base URL (Spring context-path = /plantation, port 8080)
  apiUrl: 'http://localhost:8080/plantation',
};
