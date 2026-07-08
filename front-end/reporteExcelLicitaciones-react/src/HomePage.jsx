import { useNavigate } from 'react-router-dom'
import './pages.css'

export default function HomePage() {
  const navigate = useNavigate()

  return (
    <div className="home-page">
      <div className="home-hero">
        <div className="home-hero__eyebrow">Reporte Excel Licitaciones · GCBA</div>
        <h1 className="home-hero__title">Panel de<br />Licitaciones</h1>
        <p className="home-hero__subtitle">
          Visualizá el rendimiento comercial de la cartera o cargá nuevos datos desde un Excel.
        </p>
      </div>

      <div className="home-cards">
        <button className="home-card home-card--dashboard" onClick={() => navigate('/dashboard')}>
          <span className="home-card__icon">📊</span>
          <h2 className="home-card__title">Ver estadísticas</h2>
          <p className="home-card__desc">
            Dashboard completo con winrate, evolución mensual, análisis competitivo, desistidas y más.
          </p>
          <div className="home-card__tags">
            <span className="home-tag home-tag--green">Winrate</span>
            <span className="home-tag home-tag--green">Fugas</span>
            <span className="home-tag home-tag--green">Desistidas</span>
            <span className="home-tag home-tag--green">Rentabilidad</span>
          </div>
          <span className="home-card__arrow">↗</span>
        </button>

        <button className="home-card home-card--upload" onClick={() => navigate('/upload')}>
          <span className="home-card__icon">📥</span>
          <h2 className="home-card__title">Cargar Excel</h2>
          <p className="home-card__desc">
            Consultá el formato esperado de columnas y riesgos soportados para importar nuevas licitaciones.
          </p>
          <div className="home-card__tags">
            <span className="home-tag home-tag--blue">18 columnas</span>
            <span className="home-tag home-tag--blue">23 riesgos</span>
            <span className="home-tag home-tag--blue">.xlsx</span>
          </div>
          <span className="home-card__arrow">↗</span>
        </button>
      </div>
    </div>
  )
}
