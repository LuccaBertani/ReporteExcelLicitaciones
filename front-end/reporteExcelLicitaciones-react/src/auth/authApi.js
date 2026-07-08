import { API_BASE_URL } from '../api/config'

// Estas dos llamadas NO pasan por authFetch a propósito:
// - login: todavía no hay ningún token.
// - refresh: la usa authFetch para renovar, así que no puede depender de sí misma.

export async function loginRequest(email, password) {
  const res = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })

  if (res.status === 404) {
    throw new Error('Email o contraseña incorrectos.')
  }
  if (res.status === 400) {
    throw new Error('Completá el email y la contraseña.')
  }
  if (!res.ok) {
    throw new Error(`No se pudo iniciar sesión (${res.status}).`)
  }

  return res.json() // { accessToken, refreshToken }
}

export async function refreshRequest(refreshToken) {
  const res = await fetch(`${API_BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${refreshToken}` },
  })

  if (!res.ok) {
    throw new Error('No se pudo renovar la sesión.')
  }

  return res.json() // { accessToken, refreshToken }
}
