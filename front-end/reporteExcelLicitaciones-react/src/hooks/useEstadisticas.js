import { useEffect, useState } from 'react'
import { EstadisticasAPI } from '../api/estadisticas'

const initialState = {
  totalLicitaciones: null,
  winrateGlobal: null,
  evolucionMensual: null,
  topRiesgos: null,
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
  motivosDisponibles: null,
  cantidadLicitacionesMensual: null,
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
          motivosDisponibles,
          cantidadLicitacionesMensual,
        ] = await Promise.all([
          EstadisticasAPI.totalLicitaciones(),
          EstadisticasAPI.winrateGlobal(),
          EstadisticasAPI.evolucionMensual(),
          EstadisticasAPI.topRiesgos(),
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
          EstadisticasAPI.motivosDisponibles(),
          EstadisticasAPI.cantidadLicitacionesMensual(),
        ])

        if (!cancelado) {
          setData({
            totalLicitaciones,
            winrateGlobal,
            evolucionMensual,
            topRiesgos,
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
            motivosDisponibles,
            cantidadLicitacionesMensual,
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

  // Permite refrescar solo la rentabilidad mensual, filtrada por uno o varios motivos.
  // Si no se pasan motivos (o el array está vacío), vuelve a traer el total sin filtrar.
  async function refetchRentabilidadMensual(motivos) {
    try {
      const rentabilidadMensual = await EstadisticasAPI.rentabilidadMensual(motivos)
      setData((prev) => ({ ...prev, rentabilidadMensual }))
    } catch (err) {
      setError(err.message)
    }
  }

  // Igual que la anterior, pero para la cantidad de licitaciones por mes.
  async function refetchCantidadLicitacionesMensual(motivos) {
    try {
      const cantidadLicitacionesMensual = await EstadisticasAPI.cantidadLicitacionesMensual(motivos)
      setData((prev) => ({ ...prev, cantidadLicitacionesMensual }))
    } catch (err) {
      setError(err.message)
    }
  }

  return { data, loading, error, refetchRentabilidadMensual, refetchCantidadLicitacionesMensual }
}
