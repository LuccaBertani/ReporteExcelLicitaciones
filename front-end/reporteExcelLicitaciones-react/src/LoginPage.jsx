import { useState } from 'react'
import { useNavigate, useLocation, Navigate } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import './pages.css'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [cargando, setCargando] = useState(false)

  // Si ya está logueado y entra a /login manualmente, lo mandamos al home.
  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const onSubmit = async (e) => {
    e.preventDefault()

    if (!email.trim() || !password.trim()) {
      setError('Completá el email y la contraseña.')
      return
    }

    setError('')
    setCargando(true)
    try {
      await login(email.trim(), password)
      const destino = location.state?.from?.pathname || '/'
      navigate(destino, { replace: true })
    } catch (err) {
      setError(err.message || 'No se pudo iniciar sesión.')
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={onSubmit}>
        <div className="auth-card__eyebrow">Reporte Excel Licitaciones · GCBA</div>
        <h1 className="auth-card__title">Iniciar sesión</h1>
        <p className="auth-card__subtitle">Ingresá con tu cuenta para acceder al panel.</p>

        <label className="auth-field">
          <span className="auth-field__label">Email</span>
          <input
            type="email"
            className="auth-field__input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="username"
            autoFocus
          />
        </label>

        <label className="auth-field">
          <span className="auth-field__label">Contraseña</span>
          <input
            type="password"
            className="auth-field__input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>

        {error && <div className="auth-error">{error}</div>}

        <button className="auth-submit" type="submit" disabled={cargando}>
          {cargando ? (<><span className="auth-spinner" /> Ingresando…</>) : 'Ingresar'}
        </button>
      </form>
    </div>
  )
}
