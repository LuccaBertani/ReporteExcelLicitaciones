import { API_BASE_URL } from '../api/config'
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from './tokenStorage'
import { refreshRequest } from './authApi'

// Si varias peticiones fallan con 401 al mismo tiempo (p. ej. el dashboard
// dispara ~20 requests en paralelo), no queremos pedir 20 refresh en simultáneo:
// todas esperan la misma promesa.
let refreshEnCurso = null

function solicitarRefresh() {
  if (!refreshEnCurso) {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      return Promise.reject(new Error('No hay refresh token'))
    }
    refreshEnCurso = refreshRequest(refreshToken)
      .then((data) => {
        setTokens(data)
        return data.accessToken
      })
      .finally(() => {
        refreshEnCurso = null
      })
  }
  return refreshEnCurso
}

function avisarSesionVencida() {
  clearTokens()
  window.dispatchEvent(new Event('auth:unauthorized'))
}

/**
 * fetch autenticado: agrega el access token, y si el server responde 401
 * intenta renovar con el refresh token y reintenta la petición una vez.
 * Si el refresh también falla, limpia la sesión y avisa a la app (AuthContext)
 * para que muestre la pantalla de 401.
 */
export async function authFetch(pathOrUrl, options = {}) {
  const url = pathOrUrl.startsWith('http') ? pathOrUrl : `${API_BASE_URL}${pathOrUrl}`

  const hacerRequest = (token) =>
    fetch(url, {
      ...options,
      headers: {
        ...(options.headers || {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    })

  let res = await hacerRequest(getAccessToken())

  if (res.status === 401) {
    try {
      const nuevoAccessToken = await solicitarRefresh()
      res = await hacerRequest(nuevoAccessToken)
      if (res.status === 401) {
        avisarSesionVencida()
      }
    } catch {
      avisarSesionVencida()
    }
  }

  return res
}
