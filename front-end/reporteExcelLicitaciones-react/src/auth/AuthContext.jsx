import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { getAccessToken, setTokens, clearTokens } from './tokenStorage'
import { loginRequest } from './authApi'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // Arranca leyendo lo que haya en localStorage, para no perder la sesión al recargar (F5).
  const [accessToken, setAccessToken] = useState(() => getAccessToken())
  const [unauthorized, setUnauthorized] = useState(false)

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
