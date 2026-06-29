import { useState } from 'react'
import HomePage from './HomePage'
import Dashboard from './Dashboard'
import UploadPage from './UploadPage'

export default function App() {
  const [page, setPage] = useState('home')

  if (page === 'dashboard') return <Dashboard onNavigate={setPage} />
  if (page === 'upload')    return <UploadPage onNavigate={setPage} />
  return <HomePage onNavigate={setPage} />
}
