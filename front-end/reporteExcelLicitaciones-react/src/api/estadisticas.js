import { API_BASE_URL } from './config'
import { authFetch } from '../auth/authFetch'

const BASE_URL = `${API_BASE_URL}/estadisticas`
const EXCEL_URL = `${API_BASE_URL}/excel`

// Arma el query string a partir de un objeto de params, ignorando valores
// vacíos/undefined/null. Soporta arrays (ej. motivos=A&motivos=B).
function buildQuery(params) {
  if (!params) return ''
  const usp = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    if (Array.isArray(value)) {
      if (value.length === 0) return
      value.forEach((v) => usp.append(key, v))
    } else {
      usp.append(key, value)
    }
  })
  const qs = usp.toString()
  return qs ? `?${qs}` : ''
}

async function get(path, params) {
  const res = await authFetch(`${BASE_URL}${path}${buildQuery(params)}`)
  if (!res.ok) {
    throw new Error(`Error ${res.status} al consultar ${path}`)
  }
  // Cuando no hay datos para el rango filtrado, el backend puede devolver
  // 200 OK con el body completamente vacío (pasa con los endpoints que
  // devuelven un objeto único, no una lista). Lo tratamos como "sin datos"
  // en vez de romper: el resto de la UI ya sabe mostrar ceros/paneles vacíos
  // ante null/undefined.
  const texto = await res.text()
  if (!texto) return null
  return JSON.parse(texto)
}

// "rango" es un objeto opcional { fechaDesde, fechaHasta } (strings 'yyyy-MM-dd').
export const EstadisticasAPI = {
  totalLicitaciones: (rango) => get('/total-licitaciones', rango),
  winrateGlobal: (rango) => get('/winrate-global', rango),
  evolucionMensual: (rango) => get('/evolucion-mensual', rango),
  topRiesgos: (rango) => get('/top-riesgos', rango),
  topClientesTasaExito: (rango) => get('/top-clientes-tasa-exito', rango),
  estadoLicitaciones: (rango) => get('/estado-licitaciones', rango),
  rentabilidadGlobal: (rango) => get('/rentabilidad-global', rango),
  rentabilidadMensual: (motivos, rango) => get('/rentabilidad-mensual', { motivos, ...rango }),
  totalAdjudicadoGanadas: (rango) => get('/total-adjudicado-ganadas', rango),
  sobreprecioPromedio: (rango) => get('/sobreprecio-promedio', rango),
  fugasCompetidor: (rango) => get('/fugas-competidor', rango),
  perdidasMotivo: (rango) => get('/perdidas-motivo', rango),
  desvioPrecioMotivo: (rango) => get('/desvio-precio-motivo', rango),
  motivoGanada: (rango) => get('/motivo-ganada', rango),
  rankingRiesgosGanados: (rango) => get('/ranking-riesgos-ganados', rango),
  rentabilidadResidualPerdidas: (rango) => get('/rentabilidad-residual-perdidas', rango),
  totalDesistidas: (rango) => get('/total-desistidas', rango),
  topMotivosDesistidas: (rango) => get('/top-motivos-desistidas', rango),
  montoAdjudicadoDesistido: (rango) => get('/monto-adjudicado-desistido', rango),
  renglonesDesistidos: (rango) => get('/renglones-desistidos', rango),
  motivosDisponibles: (rango) => get('/motivos-disponibles', rango),
  cantidadLicitacionesMensual: (motivos, rango) => get('/cantidad-licitaciones-mensual', { motivos, ...rango }),
  rentabilidadPorRiesgo: (rango) => get('/rentabilidad-riesgo', rango),
}

export const ExcelAPI = {
  importar: async (archivo) => {
    const formData = new FormData()
    formData.append('archivo', archivo)
    const res = await authFetch(`${EXCEL_URL}/importar`, {
      method: 'POST',
      body: formData,
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`)
    return data
  },
  // Audita el Excel contra lo que ya está en la base de datos (no inserta ni
  // modifica nada). Devuelve { exitosa, renglonesOk, renglonesFaltantes,
  // renglonesMalMonto, renglonesMalCotizado, incidencias: string[] }.
  verificar: async (archivo) => {
    const formData = new FormData()
    formData.append('archivo', archivo)
    const res = await authFetch(`${EXCEL_URL}/verificar`, {
      method: 'POST',
      body: formData,
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`)
    return data
  },
}
