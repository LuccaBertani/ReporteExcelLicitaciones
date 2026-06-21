const BASE_URL = 'http://localhost:8080/api/v1/estadisticas'

async function get(path) {
  const res = await fetch(`${BASE_URL}${path}`)
  if (!res.ok) {
    throw new Error(`Error ${res.status} al consultar ${path}`)
  }
  return res.json()
}

export const EstadisticasAPI = {
  totalLicitaciones: () => get('/total-licitaciones'),
  winrateGlobal: () => get('/winrate-global'),
  evolucionMensual: () => get('/evolucion-mensual'),
  topRiesgos: () => get('/top-riesgos'),
  topClientesTasaExito: () => get('/top-clientes-tasa-exito'),
  estadoLicitaciones: () => get('/estado-licitaciones'),
  rentabilidadGlobal: () => get('/rentabilidad-global'),
  rentabilidadMensual: () => get('/rentabilidad-mensual'),
  totalAdjudicadoGanadas: () => get('/total-adjudicado-ganadas'),
  sobreprecioPromedio: () => get('/sobreprecio-promedio'),
}
