const BASE_URL = 'http://localhost:8080/api/v1/estadisticas'
const EXCEL_URL = 'http://localhost:8080/api/v1/excel'

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
  rentabilidadMensual: (motivos) => {
    if (!motivos || motivos.length === 0) return get('/rentabilidad-mensual')
    const params = motivos.map((m) => `motivos=${encodeURIComponent(m)}`).join('&')
    return get(`/rentabilidad-mensual?${params}`)
  },
  totalAdjudicadoGanadas: () => get('/total-adjudicado-ganadas'),
  sobreprecioPromedio: () => get('/sobreprecio-promedio'),
  fugasCompetidor: () => get('/fugas-competidor'),
  perdidasMotivo: () => get('/perdidas-motivo'),
  desvioPrecioMotivo: () => get('/desvio-precio-motivo'),
  motivoGanada: () => get('/motivo-ganada'),
  rankingRiesgosGanados: () => get('/ranking-riesgos-ganados'),
  rentabilidadResidualPerdidas: () => get('/rentabilidad-residual-perdidas'),
  totalDesistidas: () => get('/total-desistidas'),
  topMotivosDesistidas: () => get('/top-motivos-desistidas'),
  montoAdjudicadoDesistido: () => get('/monto-adjudicado-desistido'),
  renglonesDesistidos: () => get('/renglones-desistidos'),
  motivosDisponibles: () => get('/motivos-disponibles'),
  cantidadLicitacionesMensual: (motivos) => {
    if (!motivos || motivos.length === 0) return get('/cantidad-licitaciones-mensual')
    const params = motivos.map((m) => `motivos=${encodeURIComponent(m)}`).join('&')
    return get(`/cantidad-licitaciones-mensual?${params}`)
  },
  rentabilidadPorRiesgo: () => get('/rentabilidad-riesgo'),
}

export const ExcelAPI = {
  importar: async (archivo) => {
    const formData = new FormData()
    formData.append('archivo', archivo)
    const res = await fetch(`${EXCEL_URL}/importar`, {
      method: 'POST',
      body: formData,
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`)
    return data
  },
}
