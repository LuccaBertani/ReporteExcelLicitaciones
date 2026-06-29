import {
  RadarChart,
  Radar,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'
import { num, formatoPorcentaje, formatoMonedaCorta } from '../utils/formato'

function TooltipRiesgo({ active, payload }) {
  if (!active || !payload?.length) return null
  const d = payload[0].payload
  return (
    <div
      style={{
        background: 'var(--panel-bg)',
        border: '1px solid var(--grid-line)',
        borderRadius: 8,
        padding: '10px 14px',
        fontFamily: 'var(--font-mono)',
        fontSize: 12,
        color: 'var(--text)',
        lineHeight: 1.8,
      }}
    >
      <div style={{ fontWeight: 600, marginBottom: 4, color: 'var(--accent-blue)' }}>{d.riesgo}</div>
      <div>Winrate: <strong style={{ color: 'var(--accent-green)' }}>{formatoPorcentaje(d.winrate)}</strong></div>
      <div>Cotizado: <strong>{formatoMonedaCorta(d.cotizado)}</strong></div>
      <div>Ganado: <strong>{formatoMonedaCorta(d.ganado)}</strong></div>
      <div>% Beneficio: <strong>{formatoPorcentaje(d.beneficio)}</strong></div>
    </div>
  )
}

export default function RentabilidadPorRiesgoChart({ data }) {
  const rows = (data ?? []).map((row) => ({
    riesgo: row.riesgo,
    winrate: num(row.winrate),
    cotizado: num(row.cant_cotizada),
    ganado: num(row.cant_ganada),
    beneficio: num(row.porcentaje_beneficio),
  }))

  // Radar usa winrate como métrica principal
  return (
    <ResponsiveContainer width="100%" height={320}>
      <RadarChart data={rows} margin={{ top: 8, right: 32, left: 32, bottom: 8 }}>
        <PolarGrid stroke="var(--grid-line)" />
        <PolarAngleAxis
          dataKey="riesgo"
          tick={{ fill: 'var(--text-dim)', fontSize: 11, fontFamily: 'var(--font-mono)' }}
        />
        <PolarRadiusAxis
          angle={30}
          domain={[0, 100]}
          tick={{ fill: 'var(--text-dim)', fontSize: 10 }}
          tickFormatter={(v) => `${v}%`}
        />
        <Radar
          name="Winrate"
          dataKey="winrate"
          stroke="var(--accent-green)"
          fill="var(--accent-green)"
          fillOpacity={0.25}
          strokeWidth={2}
        />
        <Tooltip content={<TooltipRiesgo />} />
      </RadarChart>
    </ResponsiveContainer>
  )
}
