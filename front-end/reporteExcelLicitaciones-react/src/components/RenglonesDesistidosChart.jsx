import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { num } from '../utils/formato'

const PALETTE = ['#F5C26B', '#FF5C5C', '#9B7EDE', '#5B8DEF', '#4FD1C5', '#3DDC84']

function TickY({ x, y, payload }) {
  const words = String(payload.value).split(' ')
  const lineHeight = 14
  const lines = []
  let current = ''
  for (const word of words) {
    const candidate = current ? `${current} ${word}` : word
    if (candidate.length > 18 && current) {
      lines.push(current)
      current = word
    } else {
      current = candidate
    }
  }
  if (current) lines.push(current)

  const totalHeight = lines.length * lineHeight
  const startY = y - totalHeight / 2 + lineHeight / 2

  return (
    <g>
      {lines.map((line, i) => (
        <text
          key={i}
          x={x}
          y={startY + i * lineHeight}
          textAnchor="end"
          fill="var(--text-dim)"
          fontSize={11}
          fontFamily="var(--font-mono)"
          dominantBaseline="middle"
        >
          {line}
        </text>
      ))}
    </g>
  )
}

const tooltipStyle = {
  contentStyle: {
    background: 'var(--panel-bg)',
    border: '1px solid var(--grid-line)',
    borderRadius: 8,
    fontFamily: 'var(--font-mono)',
    fontSize: 13,
    color: 'var(--text)',
  },
  itemStyle: { color: 'var(--text)' },
  labelStyle: { color: 'var(--text-dim)' },
}

export default function RenglonesDesistidosChart({ data }) {
  const rows = (data ?? [])
    .map((row) => ({
      riesgo: row.riesgo_renglon,
      cantidad: num(row.cantidad_desistidos),
    }))
    .sort((a, b) => b.cantidad - a.cantidad)

  const barHeight = 42
  const chartHeight = Math.max(420, rows.length * barHeight + 60)

  return (
    <ResponsiveContainer width="100%" height={chartHeight}>
      <BarChart
        data={rows}
        layout="vertical"
        margin={{ top: 8, right: 40, left: 0, bottom: 8 }}
        barSize={22}
      >
        <CartesianGrid stroke="var(--grid-line)" horizontal={false} />
        <XAxis type="number" stroke="var(--text-dim)" fontSize={12} allowDecimals={false} tickLine={false} />
        <YAxis dataKey="riesgo" type="category" width={185} tickLine={false} axisLine={false} tick={<TickY />} />
        <Tooltip
          contentStyle={tooltipStyle.contentStyle}
          itemStyle={tooltipStyle.itemStyle}
          labelStyle={tooltipStyle.labelStyle}
          formatter={(value) => [value, 'Desistidas']}
        />
        <Bar dataKey="cantidad" radius={[0, 4, 4, 0]}>
          {rows.map((entry, i) => (
            <Cell key={entry.riesgo} fill={PALETTE[i % PALETTE.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
