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

// "rango" es un objeto opcional { fechaDesde, fechaHasta } (strings 'yyyy-MM-dd').
// Cada vez que cambia, se vuelve a pedir todo el paquete de estadísticas filtrado.
export function useEstadisticas(rango = {}) {
  const { fechaDesde, fechaHasta } = rango

  const [data, setData] = useState(initialState)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelado = false

    async function cargarTodo() {
      setLoading(true)
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
          EstadisticasAPI.totalLicitaciones(rango),
          EstadisticasAPI.winrateGlobal(rango),
          EstadisticasAPI.evolucionMensual(rango),
          EstadisticasAPI.topRiesgos(rango),
          EstadisticasAPI.estadoLicitaciones(rango),
          EstadisticasAPI.rentabilidadGlobal(rango),
          EstadisticasAPI.rentabilidadMensual(null, rango),
          EstadisticasAPI.totalAdjudicadoGanadas(rango),
          EstadisticasAPI.sobreprecioPromedio(rango),
          EstadisticasAPI.fugasCompetidor(rango),
          EstadisticasAPI.perdidasMotivo(rango),
          EstadisticasAPI.desvioPrecioMotivo(rango),
          EstadisticasAPI.motivoGanada(rango),
          EstadisticasAPI.rankingRiesgosGanados(rango),
          EstadisticasAPI.rentabilidadResidualPerdidas(rango),
          EstadisticasAPI.totalDesistidas(rango),
          EstadisticasAPI.topMotivosDesistidas(rango),
          EstadisticasAPI.montoAdjudicadoDesistido(rango),
          EstadisticasAPI.renglonesDesistidos(rango),
          EstadisticasAPI.rentabilidadPorRiesgo(rango),
          EstadisticasAPI.motivosDisponibles(rango),
          EstadisticasAPI.cantidadLicitacionesMensual(null, rango),
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
          setError(null)
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
  }, [fechaDesde, fechaHasta])

  // Permite refrescar solo la rentabilidad mensual, filtrada por uno o varios motivos.
  // Si no se pasan motivos (o el array está vacío), vuelve a traer el total sin filtrar.
  // Respeta el rango de fechas actualmente aplicado.
  async function refetchRentabilidadMensual(motivos) {
    try {
      const rentabilidadMensual = await EstadisticasAPI.rentabilidadMensual(motivos, rango)
      setData((prev) => ({ ...prev, rentabilidadMensual }))
    } catch (err) {
      setError(err.message)
    }
  }

  // Igual que la anterior, pero para la cantidad de licitaciones por mes.
  async function refetchCantidadLicitacionesMensual(motivos) {
    try {
      const cantidadLicitacionesMensual = await EstadisticasAPI.cantidadLicitacionesMensual(motivos, rango)
      setData((prev) => ({ ...prev, cantidadLicitacionesMensual }))
    } catch (err) {
      setError(err.message)
    }
  }

  return { data, loading, error, refetchRentabilidadMensual, refetchCantidadLicitacionesMensual }
}
