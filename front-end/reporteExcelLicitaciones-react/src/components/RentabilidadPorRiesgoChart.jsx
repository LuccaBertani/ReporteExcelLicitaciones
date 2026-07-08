import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { num, formatoPorcentaje, formatoMonedaCorta } from '../utils/formato'

const PALETTE = ['#FF5C5C', '#F5C26B', '#9B7EDE', '#5B8DEF', '#4FD1C5', '#3DDC84']

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
      <div>% Ganadas: <strong style={{ color: 'var(--accent-green)' }}>{formatoPorcentaje(d.winrate)}</strong></div>
      <div>Compulsas ganadas: <strong>{d.compulsasGanadas} / {d.compulsasTotales}</strong></div>
      <div>Cotizado: <strong>{formatoMonedaCorta(d.cotizado)}</strong></div>
      <div>Ganado: <strong>{formatoMonedaCorta(d.ganado)}</strong></div>
      <div>% Beneficio: <strong>{formatoPorcentaje(d.beneficio)}</strong></div>
    </div>
  )
}

export default function RentabilidadPorRiesgoChart({ data }) {
  const rows = (data ?? [])
    .map((row) => ({
      riesgo: row.riesgo,
      winrate: num(row.winrate),
      cotizado: num(row.cant_cotizada),
      ganado: num(row.cant_ganada),
      beneficio: num(row.porcentaje_beneficio),
      compulsasGanadas: row.compulsas_ganadas,
      compulsasTotales: row.compulsas_totales,
    }))
    .sort((a, b) => b.winrate - a.winrate)

  const altura = Math.max(320, rows.length * 36)

  return (
    <ResponsiveContainer width="100%" height={altura}>
      <BarChart data={rows} layout="vertical" margin={{ top: 8, right: 24, left: 8, bottom: 0 }}>
        <CartesianGrid stroke="var(--grid-line)" horizontal={false} />
        <XAxis type="number" domain={[0, 100]} stroke="var(--text-dim)" fontSize={12} tickFormatter={(v) => `${v}%`} />
        <YAxis dataKey="riesgo" type="category" stroke="var(--text-dim)" fontSize={12} width={160} tickLine={false} />
        <Tooltip content={<TooltipRiesgo />} />
        <Bar dataKey="winrate" name="% Ganadas" radius={[0, 4, 4, 0]}>
          {rows.map((entry, index) => (
            <Cell key={entry.riesgo} fill={PALETTE[index % PALETTE.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
