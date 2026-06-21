export function PanelLoading() {
  return (
    <div className="panel__status">
      <div className="pulse-dot" />
      <span>Cargando datos…</span>
    </div>
  )
}

export function PanelError({ message }) {
  return (
    <div className="panel__status panel__status--error">
      <span>No se pudo cargar: {message}</span>
    </div>
  )
}

export function PanelEmpty() {
  return (
    <div className="panel__status">
      <span>Sin datos para mostrar todavía.</span>
    </div>
  )
}
