import { useEstadisticas } from './hooks/useEstadisticas'
import KpiCard from './components/KpiCard'
import Panel from './components/Panel'
import EvolucionMensualChart from './components/EvolucionMensualChart'
import TopRiesgosChart from './components/TopRiesgosChart'
import TopClientesChart from './components/TopClientesChart'
import EstadoLicitacionesChart from './components/EstadoLicitacionesChart'
import RentabilidadMensualChart from './components/RentabilidadMensualChart'
import { PanelLoading, PanelError, PanelEmpty } from './components/PanelStates'
import { num, formatoEntero, formatoPorcentaje, formatoMonedaCorta } from './utils/formato'
import './App.css'

function App() {
  const { data, loading, error } = useEstadisticas()

  const winrate = num(data.winrateGlobal?.winrate)
  const totalLicitaciones = num(data.totalLicitaciones?.cant_licitaciones)
  const totalAdjudicado = num(data.totalAdjudicadoGanadas?.total_adjudicado_ganadas)
  const beneficio = num(data.rentabilidadGlobal?.porcentaje_beneficio)
  const sobreprecio = num(data.sobreprecioPromedio?.sobreprecio_promedio_porcentaje)

  return (
    <div className="dashboard">
      <header className="dashboard__hero">
        <div className="hero__eyebrow">Panel de licitaciones · Cartera de seguros</div>
        <h1 className="hero__title">
          {loading ? (
            <span className="hero__skeleton">— —%</span>
          ) : (
            <>
              <span className="hero__winrate">{formatoPorcentaje(winrate)}</span>
              <span className="hero__winrate-label">de winrate global</span>
            </>
          )}
        </h1>
        <p className="hero__description">
          Estado en vivo de la cartera: cuánto se cotiza, cuánto se gana y dónde está la
          rentabilidad, riesgo por riesgo, mes a mes.
        </p>
      </header>

      {error ? (
        <div className="dashboard__error">
          No se pudo conectar con la API en <code>localhost:8080</code>. Verificá que el
          backend esté corriendo. ({error})
        </div>
      ) : null}

      <section className="kpi-grid">
        <KpiCard
          label="Licitaciones totales"
          value={totalLicitaciones}
          format={(v) => formatoEntero(v)}
          accent="neutral"
        />
        <KpiCard
          label="Winrate global"
          value={winrate}
          format={(v) => formatoPorcentaje(v)}
          accent="green"
        />
        <KpiCard
          label="Adjudicado en ganadas"
          value={totalAdjudicado}
          format={(v) => formatoMonedaCorta(v)}
          accent="blue"
        />
        <KpiCard
          label="Beneficio sobre cotizado"
          value={beneficio}
          format={(v) => formatoPorcentaje(v)}
          accent="green"
        />
        <KpiCard
          label="Sobreprecio promedio"
          value={sobreprecio}
          format={(v) => formatoPorcentaje(v)}
          accent="amber"
        />
      </section>

      <section className="panel-grid">
        <Panel
          title="Evolución mensual"
          subtitle="Cantidad de licitaciones, cotizado y adjudicado por mes"
          className="panel--wide"
        >
          {loading ? (
            <PanelLoading />
          ) : data.evolucionMensual?.length ? (
            <EvolucionMensualChart data={data.evolucionMensual} />
          ) : (
            <PanelEmpty />
          )}
        </Panel>

        <Panel title="Estado de licitaciones" subtitle="Distribución por resultado">
          {loading ? (
            <PanelLoading />
          ) : data.estadoLicitaciones?.length ? (
            <EstadoLicitacionesChart data={data.estadoLicitaciones} />
          ) : (
            <PanelEmpty />
          )}
        </Panel>

        <Panel title="Top riesgos por monto adjudicado" subtitle="Los 8 riesgos con mayor volumen">
          {loading ? (
            <PanelLoading />
          ) : data.topRiesgos?.length ? (
            <TopRiesgosChart data={data.topRiesgos} />
          ) : (
            <PanelEmpty />
          )}
        </Panel>

        <Panel
          title="Top clientes por tasa de éxito"
          subtitle="Clientes con mejor relación ganadas / total"
          className="panel--wide"
        >
          {loading ? (
            <PanelLoading />
          ) : data.topClientesTasaExito?.length ? (
            <TopClientesChart data={data.topClientesTasaExito} />
          ) : (
            <PanelEmpty />
          )}
        </Panel>

        <Panel
          title="Rentabilidad mensual"
          subtitle="Monto cotizado vs. monto ganado por mes"
          className="panel--wide"
        >
          {loading ? (
            <PanelLoading />
          ) : data.rentabilidadMensual?.length ? (
            <RentabilidadMensualChart data={data.rentabilidadMensual} />
          ) : (
            <PanelEmpty />
          )}
        </Panel>
      </section>

      <footer className="dashboard__footer">
        ReporteExcelLicitaciones · datos servidos desde <code>/api/v1/estadisticas</code>
      </footer>
    </div>
  )
}

export default App
