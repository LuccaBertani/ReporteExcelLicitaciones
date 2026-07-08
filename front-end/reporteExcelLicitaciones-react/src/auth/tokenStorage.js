// Persistencia simple de los tokens en localStorage, para que la sesión
// sobreviva a un F5. No usa React: lo puede usar tanto el AuthContext
// (para el estado reactivo) como authFetch (que no es un componente).

const ACCESS_TOKEN_KEY = 'rel_access_token'
const REFRESH_TOKEN_KEY = 'rel_refresh_token'

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setTokens({ accessToken, refreshToken }) {
  if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}
