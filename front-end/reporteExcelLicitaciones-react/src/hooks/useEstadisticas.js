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
  // Nuevos
  fugasCompetidor: null,
  perdidasMotivo: null,
  desvioPrecioMotivo: null,
  motivoGanada: null,
  rankingRiesgosGanados: null,
  rentabilidadResidualPerdidas: null,
  totalDesistidas: null,
  topMotivosDesistidas: null,
  montoAdjudicadoDesistido: null,
  renglonesDesistidos: null,
  rentabilidadPorRiesgo: null,
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
          fugasCompetidor,
          perdidasMotivo,
          desvioPrecioMotivo,
          motivoGanada,
          rankingRiesgosGanados,
          rentabilidadResidualPerdidas,
          totalDesistidas,
          topMotivosDesistidas,
          montoAdjudicadoDesistido,
          renglonesDesistidos,
          rentabilidadPorRiesgo,
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
          EstadisticasAPI.fugasCompetidor(),
          EstadisticasAPI.perdidasMotivo(),
          EstadisticasAPI.desvioPrecioMotivo(),
          EstadisticasAPI.motivoGanada(),
          EstadisticasAPI.rankingRiesgosGanados(),
          EstadisticasAPI.rentabilidadResidualPerdidas(),
          EstadisticasAPI.totalDesistidas(),
          EstadisticasAPI.topMotivosDesistidas(),
          EstadisticasAPI.montoAdjudicadoDesistido(),
          EstadisticasAPI.renglonesDesistidos(),
          EstadisticasAPI.rentabilidadPorRiesgo(),
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
            fugasCompetidor,
            perdidasMotivo,
            desvioPrecioMotivo,
            motivoGanada,
            rankingRiesgosGanados,
            rentabilidadResidualPerdidas,
            totalDesistidas,
            topMotivosDesistidas,
            montoAdjudicadoDesistido,
            renglonesDesistidos,
            rentabilidadPorRiesgo,
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
