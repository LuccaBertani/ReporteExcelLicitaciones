import { useNavigate } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import './pages.css'

export default function UnauthorizedPage() {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const volverALogin = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="auth-page">
      <div className="auth-card auth-card--error">
        <div className="auth-card__code">401</div>
        <h1 className="auth-card__title">No autorizado</h1>
        <p className="auth-card__subtitle">
          Tu sesión expiró o no tenés permisos para acceder a este contenido.
          Volvé a iniciar sesión para continuar.
        </p>
        <button className="auth-submit" onClick={volverALogin}>Volver a iniciar sesión</button>
      </div>
    </div>
  )
}
