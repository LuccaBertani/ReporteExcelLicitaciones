import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { getAccessToken, setTokens, clearTokens } from './tokenStorage'
import { loginRequest } from './authApi'
import { authFetch } from './authFetch'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // Arranca leyendo lo que haya en localStorage, para no perder la sesión al recargar (F5).
  const [accessToken, setAccessToken] = useState(() => getAccessToken())
  const [unauthorized, setUnauthorized] = useState(false)

  // Si hay un token guardado, todavía no sabemos si es VÁLIDO (puede ser viejo
  // o de una sesión ya vencida) hasta que lo confirmamos contra el backend.
  // Mientras "checkingAuth" es true, ProtectedRoute no debe decidir nada:
  // ni mostrar la página protegida ni mandar a /login.
  const [checkingAuth, setCheckingAuth] = useState(() => !!getAccessToken())

  // authFetch dispara este evento global cuando el access token venció
  // y el refresh token no pudo renovarlo (o no existe).
  useEffect(() => {
    function onSesionVencida() {
      setAccessToken(null)
      setUnauthorized(true)
    }
    window.addEventListener('auth:unauthorized', onSesionVencida)
    return () => window.removeEventListener('auth:unauthorized', onSesionVencida)
  }, [])

  // Validación única al montar la app: si hay un token guardado, lo confirmamos
  // contra /auth/me. authFetch ya intenta refrescar solo si el access token venció,
  // así que esto no fuerza un nuevo login mientras el refresh token siga sirviendo.
  useEffect(() => {
    if (!getAccessToken()) {
      setCheckingAuth(false)
      return
    }

    let cancelado = false

    authFetch('/auth/me').finally(() => {
      if (!cancelado) setCheckingAuth(false)
    })

    return () => {
      cancelado = true
    }
  }, [])

  const login = useCallback(async (email, password) => {
    const data = await loginRequest(email, password)
    setTokens(data)
    setAccessToken(data.accessToken)
    setUnauthorized(false)
  }, [])

  const logout = useCallback(() => {
    clearTokens()
    setAccessToken(null)
    setUnauthorized(false)
  }, [])

  const value = {
    isAuthenticated: !!accessToken,
    unauthorized,
    checkingAuth,
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return ctx
}
