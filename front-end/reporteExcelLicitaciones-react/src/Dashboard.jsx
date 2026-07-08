import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useEstadisticas } from './hooks/useEstadisticas'
import KpiCard from './components/KpiCard'
import Panel from './components/Panel'
import EvolucionMensualChart from './components/EvolucionMensualChart'
import TopRiesgosChart from './components/TopRiesgosChart'
import EstadoLicitacionesChart from './components/EstadoLicitacionesChart'
import RentabilidadMensualChart from './components/RentabilidadMensualChart'
import CantidadLicitacionesMensualChart from './components/CantidadLicitacionesMensualChart'
import FugasCompetidorChart from './components/FugasCompetidorChart'
import MotivoGanadaChart from './components/MotivoGanadaChart'
import RankingRiesgosGanadosChart from './components/RankingRiesgosGanadosChart'
import RentabilidadResidualChart from './components/RentabilidadResidualChart'
import RenglonesDesistidosChart from './components/RenglonesDesistidosChart'
import DesvioPrecios from './components/DesvioPrecios'
import MotivosDesistidasList from './components/MotivosDesistidasList'
import RentabilidadPorRiesgoChart from './components/RentabilidadPorRiesgoChart'
import { PanelLoading, PanelEmpty } from './components/PanelStates'
import { num, formatoEntero, formatoPorcentaje, formatoMonedaCorta } from './utils/formato'
import './App.css'

function SectionTitle({ children }) {
  return <h2 className="section-title">{children}</h2>
}

export default function Dashboard() {
  const navigate = useNavigate()

  // --- Filtro global de rango de fechas (aplica a TODAS las estadísticas) ---
  // Se eligen MESES (no días puntuales), y se calcula automáticamente el primer
  // y el último día de cada mes para que el rango incluya el mes completo.
  const [mesDesdeInput, setMesDesdeInput] = useState('')
  const [mesHastaInput, setMesHastaInput] = useState('')
  const [rango, setRango] = useState({ fechaDesde: '', fechaHasta: '' })

  const primerDiaDeMes = (yyyyMM) => `${yyyyMM}-01`

  const ultimoDiaDeMes = (yyyyMM) => {
    const [anio, mes] = yyyyMM.split('-').map(Number)
    const ultimoDia = new Date(anio, mes, 0).getDate() // día 0 del mes siguiente = último día del actual
    return `${yyyyMM}-${String(ultimoDia).padStart(2, '0')}`
  }

  const aplicarRango = () => {
    setRango({
      fechaDesde: mesDesdeInput ? primerDiaDeMes(mesDesdeInput) : '',
      fechaHasta: mesHastaInput ? ultimoDiaDeMes(mesHastaInput) : '',
    })
  }

  const limpiarRango = () => {
    setMesDesdeInput('')
    setMesHastaInput('')
    setRango({ fechaDesde: '', fechaHasta: '' })
  }

  const hayRangoAplicado = Boolean(rango.fechaDesde || rango.fechaHasta)

  const { data, loading, error, refetchRentabilidadMensual, refetchCantidadLicitacionesMensual } = useEstadisticas(rango)

  // --- Filtro de motivos para "Rentabilidad mensual" (recalcula en el back-end) ---
  const handleFiltroRentabilidadMensual = (motivos) => {
    refetchRentabilidadMensual(motivos)
  }

  // --- Filtro de motivos para "Cantidad de licitaciones por mes" (recalcula en el back-end) ---
  const handleFiltroCantidadLicitaciones = (motivos) => {
    refetchCantidadLicitacionesMensual(motivos)
  }

  // --- Filtro de motivos para "Motivos de desistimiento" (solo recorta lo ya cargado) ---
  const [motivosDesistidasFiltro, setMotivosDesistidasFiltro] = useState([])

  const opcionesMotivosDesistidas = useMemo(() => {
    const motivos = (data.topMotivosDesistidas ?? []).map((row) => row.motivo_desistida)
    return Array.from(new Set(motivos))
  }, [data.topMotivosDesistidas])

  const topMotivosDesistidasFiltrado = useMemo(() => {
    if (motivosDesistidasFiltro.length === 0) return data.topMotivosDesistidas
    return (data.topMotivosDesistidas ?? []).filter((row) =>
      motivosDesistidasFiltro.includes(row.motivo_desistida)
    )
  }, [data.topMotivosDesistidas, motivosDesistidasFiltro])

  const winrate          = num(data.winrateGlobal?.winrate)
  const totalLicitaciones = num(data.totalLicitaciones?.cant_licitaciones)
  const totalAdjudicado  = num(data.totalAdjudicadoGanadas?.total_adjudicado_ganadas)
  const beneficio        = num(data.rentabilidadGlobal?.porcentaje_beneficio)
  const sobreprecio      = num(data.sobreprecioPromedio?.sobreprecio_promedio_porcentaje)
  const totalDesistidas  = num(data.totalDesistidas?.total_compulsas_desistidas)
  const montoDesistido   = num(data.montoAdjudicadoDesistido?.cantidad_adjudicada_total_desistida)

  return (
    <div className="dashboard">
      <header className="dashboard__hero">
        <button className="back-btn" onClick={() => navigate('/')}>← Inicio</button>
        <div className="hero__eyebrow">Panel de licitaciones · Cartera de seguros</div>
        <h1 className="hero__title">
          {loading ? (
            <span className="hero__skeleton">— —%</span>
          ) : (
            <>
              <span className="hero__winrate">{formatoPorcentaje(winrate)}</span>
              <span className="hero__winrate-label">de tasa de ganadas global</span>
            </>
          )}
        </h1>
        <p className="hero__description">
          Estado en vivo de la cartera: cuánto se cotiza, cuánto se gana y dónde está la
          rentabilidad, riesgo por riesgo, mes a mes.
        </p>

        <div className="hero__filtro-fecha">
          <label className="hero__filtro-campo">
            <span>Desde</span>
            <input
              type="month"
              value={mesDesdeInput}
              onChange={(e) => setMesDesdeInput(e.target.value)}
              max={mesHastaInput || undefined}
            />
          </label>
          <label className="hero__filtro-campo">
            <span>Hasta</span>
            <input
              type="month"
              value={mesHastaInput}
              onChange={(e) => setMesHastaInput(e.target.value)}
              min={mesDesdeInput || undefined}
            />
          </label>
          <button className="hero__filtro-btn" onClick={aplicarRango}>Aplicar</button>
          {hayRangoAplicado ? (
            <button className="hero__filtro-btn hero__filtro-btn--ghost" onClick={limpiarRango}>
              Limpiar
            </button>
          ) : null}
        </div>
      </header>

      {error ? (
        <div className="dashboard__error">
          No se pudo conectar con la API en <code>localhost:8080</code>. Verificá que el
          backend esté corriendo. ({error})
        </div>
      ) : null}

      <SectionTitle>Indicadores Clave</SectionTitle>
      <section className="kpi-grid kpi-grid--7">
        <KpiCard label="Licitaciones totales"   value={totalLicitaciones} format={formatoEntero}      accent="neutral" />
        <KpiCard label="Tasa de ganadas global"  value={winrate}          format={formatoPorcentaje}   accent="green"   />
        <KpiCard label="Adjudicado en ganadas"   value={totalAdjudicado}  format={formatoMonedaCorta}  accent="blue"    />
        <KpiCard label="Beneficio sobre cotizado" value={beneficio}       format={formatoPorcentaje}   accent="green"   />
        <KpiCard label="Sobreprecio promedio"    value={sobreprecio}      format={formatoPorcentaje}   accent="amber"   />
        <KpiCard label="Compulsas desistidas"    value={totalDesistidas}  format={formatoEntero}       accent="amber"   />
        <KpiCard label="Masa desistida teórica"  value={montoDesistido}   format={formatoMonedaCorta}  accent="neutral" />
      </section>

      <SectionTitle>Evolución y Distribución</SectionTitle>
      <section className="panel-grid">
        <Panel title="Evolución mensual" subtitle="Cantidad de licitaciones, cotizado y adjudicado por mes" className="panel--wide">
          {loading ? <PanelLoading /> : data.evolucionMensual?.length ? <EvolucionMensualChart data={data.evolucionMensual} /> : <PanelEmpty />}
        </Panel>
        <Panel title="Estado de licitaciones" subtitle="Distribución por resultado">
          {loading ? <PanelLoading /> : data.estadoLicitaciones?.length ? <EstadoLicitacionesChart data={data.estadoLicitaciones} /> : <PanelEmpty />}
        </Panel>
        <Panel title="Top riesgos por monto adjudicado" subtitle="Los 8 riesgos con mayor volumen">
          {loading ? <PanelLoading /> : data.topRiesgos?.length ? <TopRiesgosChart data={data.topRiesgos} /> : <PanelEmpty />}
        </Panel>
        <Panel
          title="Cantidad de licitaciones por mes"
          subtitle="Cantidad de licitaciones cotizadas por mes"
          filtros={data.motivosDisponibles}
          onFiltrosChange={handleFiltroCantidadLicitaciones}
        >
          {loading ? <PanelLoading /> : data.cantidadLicitacionesMensual?.length ? <CantidadLicitacionesMensualChart data={data.cantidadLicitacionesMensual} /> : <PanelEmpty />}
        </Panel>
        <Panel
          title="Rentabilidad mensual"
          subtitle="Monto cotizado vs. monto ganado por mes"
          className="panel--wide"
          filtros={data.motivosDisponibles}
          onFiltrosChange={handleFiltroRentabilidadMensual}
        >
          {loading ? <PanelLoading /> : data.rentabilidadMensual?.length ? <RentabilidadMensualChart data={data.rentabilidadMensual} /> : <PanelEmpty />}
        </Panel>
      </section>

      <SectionTitle>Análisis de Ganadas</SectionTitle>
      <section className="panel-grid">
        <Panel title="Ranking de renglones ganados" subtitle="Compulsas ganadas por tipo de riesgo">
          {loading ? <PanelLoading /> : data.rankingRiesgosGanados?.length ? <RankingRiesgosGanadosChart data={data.rankingRiesgosGanados} /> : <PanelEmpty />}
        </Panel>
        <Panel title="Motivo de adjudicación" subtitle="Por qué se ganaron las compulsas">
          {loading ? <PanelLoading /> : data.motivoGanada?.length ? <MotivoGanadaChart data={data.motivoGanada} /> : <PanelEmpty />}
        </Panel>
        <Panel title="Porcentaje de ganadas por riesgo" subtitle="Proporción de éxito para cada renglón" className="panel--wide">
          {loading ? <PanelLoading /> : data.rentabilidadPorRiesgo?.length ? <RentabilidadPorRiesgoChart data={data.rentabilidadPorRiesgo} /> : <PanelEmpty />}
        </Panel>
      </section>

      <SectionTitle>Análisis Competitivo y Pérdidas</SectionTitle>
      <section className="panel-grid">
        <Panel title="Fugas por competidor" subtitle="Monto capturado por la competencia cuando perdemos" className="panel--wide">
          {loading ? <PanelLoading /> : data.fugasCompetidor?.length ? <FugasCompetidorChart data={data.fugasCompetidor} /> : <PanelEmpty />}
        </Panel>
        <Panel title="Rendimiento residual en pérdidas" subtitle="Monto cotizado perdido por riesgo / renglón">
          {loading ? <PanelLoading /> : data.rentabilidadResidualPerdidas?.length ? <RentabilidadResidualChart data={data.rentabilidadResidualPerdidas} /> : <PanelEmpty />}
        </Panel>
        <Panel title="Desvío de precio por renglón" subtitle="Correlación entre precio cotizado y adjudicado en pérdidas">
          {loading ? <PanelLoading /> : data.desvioPrecioMotivo?.length ? <DesvioPrecios data={data.desvioPrecioMotivo} /> : <PanelEmpty />}
        </Panel>
      </section>

      <SectionTitle>Radiografía de Desistidas</SectionTitle>
      <section className="panel-grid">
        <Panel title="Renglones más desistidos" subtitle="Cantidad de compulsas desistidas por tipo de riesgo">
          {loading ? <PanelLoading /> : data.renglonesDesistidos?.length ? <RenglonesDesistidosChart data={data.renglonesDesistidos} /> : <PanelEmpty />}
        </Panel>
        <Panel
          title="Motivos de desistimiento"
          subtitle="Causas por las que se descartaron compulsas"
          filtros={opcionesMotivosDesistidas}
          onFiltrosChange={setMotivosDesistidasFiltro}
        >
          {loading ? <PanelLoading /> : topMotivosDesistidasFiltrado?.length ? <MotivosDesistidasList data={topMotivosDesistidasFiltrado} /> : <PanelEmpty />}
        </Panel>
      </section>

      <footer className="dashboard__footer">
        ReporteExcelLicitaciones · datos servidos desde <code>/api/v1/estadisticas</code>
      </footer>
    </div>
  )
}
