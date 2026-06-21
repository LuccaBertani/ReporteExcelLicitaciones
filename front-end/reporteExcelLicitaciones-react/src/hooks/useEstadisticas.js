import { useEffect, useState } from 'react'
import { EstadisticasAPI } from '../api/estadisticas'

const initialState = {
  totalLicitaciones: null,
  winrateGlobal: null,
  evolucionMensual: null,
  topRiesgos: null,
  topClientesTasaExito: null,
  estadoLicitaciones: null,
  rentabilidadGlobal: null,
  rentabilidadMensual: null,
  totalAdjudicadoGanadas: null,
  sobreprecioPromedio: null,
}

export function useEstadisticas() {
  const [data, setData] = useState(initialState)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelado = false

    async function cargarTodo() {
      try {
        const [
          totalLicitaciones,
          winrateGlobal,
          evolucionMensual,
          topRiesgos,
          topClientesTasaExito,
          estadoLicitaciones,
          rentabilidadGlobal,
          rentabilidadMensual,
          totalAdjudicadoGanadas,
          sobreprecioPromedio,
        ] = await Promise.all([
          EstadisticasAPI.totalLicitaciones(),
          EstadisticasAPI.winrateGlobal(),
          EstadisticasAPI.evolucionMensual(),
          EstadisticasAPI.topRiesgos(),
          EstadisticasAPI.topClientesTasaExito(),
          EstadisticasAPI.estadoLicitaciones(),
          EstadisticasAPI.rentabilidadGlobal(),
          EstadisticasAPI.rentabilidadMensual(),
          EstadisticasAPI.totalAdjudicadoGanadas(),
          EstadisticasAPI.sobreprecioPromedio(),
        ])

        if (!cancelado) {
          setData({
            totalLicitaciones,
            winrateGlobal,
            evolucionMensual,
            topRiesgos,
            topClientesTasaExito,
            estadoLicitaciones,
            rentabilidadGlobal,
            rentabilidadMensual,
            totalAdjudicadoGanadas,
            sobreprecioPromedio,
          })
        }
      } catch (err) {
        if (!cancelado) setError(err.message)
      } finally {
        if (!cancelado) setLoading(false)
      }
    }

    cargarTodo()
    return () => {
      cancelado = true
    }
  }, [])

  return { data, loading, error }
}
