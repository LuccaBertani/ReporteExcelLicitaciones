import { Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import ProtectedRoute, { UnauthorizedRoute, CatchAllRedirect } from './auth/ProtectedRoute'
import LoginPage from './LoginPage'
import UnauthorizedPage from './UnauthorizedPage'
import HomePage from './HomePage'
import Dashboard from './Dashboard'
import UploadPage from './UploadPage'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/401" element={<UnauthorizedRoute><UnauthorizedPage /></UnauthorizedRoute>} />

        <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/upload" element={<ProtectedRoute><UploadPage /></ProtectedRoute>} />

        <Route path="*" element={<CatchAllRedirect />} />
      </Routes>
    </AuthProvider>
  )
}
