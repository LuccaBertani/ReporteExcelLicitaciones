import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

// Envuelve cualquier página que requiera sesión iniciada.
// - Si la sesión venció (401 detectado por authFetch) -> /401
// - Si no hay sesión -> /login (guardando desde dónde venía, para volver ahí después)
export default function ProtectedRoute({ children }) {
  const { isAuthenticated, unauthorized } = useAuth()
  const location = useLocation()

  if (unauthorized) {
    return <Navigate to="/401" replace />
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return children
}

// Envuelve la pantalla de 401: solo se puede ver si REALMENTE se venció una
// sesión (authFetch detectó un 401 y no pudo renovar con el refresh token).
// Si alguien no autenticado escribe /401 directo en la barra de direcciones,
// lo mandamos a /login: la única ruta navegable sin sesión es esa.
export function UnauthorizedRoute({ children }) {
  const { unauthorized } = useAuth()

  if (!unauthorized) {
    return <Navigate to="/login" replace />
  }

  return children
}

// Para cualquier ruta no reconocida: si no hay sesión, a /login (nunca a
// una pantalla protegida); si la sesión venció, a /401; si está todo bien, a /.
export function CatchAllRedirect() {
  const { isAuthenticated, unauthorized } = useAuth()

  if (unauthorized) return <Navigate to="/401" replace />
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <Navigate to="/" replace />
}
