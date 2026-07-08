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
