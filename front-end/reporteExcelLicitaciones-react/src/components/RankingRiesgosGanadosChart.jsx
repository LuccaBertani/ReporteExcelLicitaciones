import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { num } from '../utils/formato'

const PALETTE = ['#3DDC84', '#5B8DEF', '#F5C26B', '#9B7EDE', '#4FD1C5', '#FF5C5C']

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

export default function RankingRiesgosGanadosChart({ data }) {
  const rows = (data ?? [])
    .map((row) => ({
      riesgo: row.riesgo_renglon,
      ganadas: num(row.cantidad_renglones_ganados),
    }))
    .sort((a, b) => b.ganadas - a.ganadas)

  return (
    <ResponsiveContainer width="100%" height={280}>
      <BarChart data={rows} layout="vertical" margin={{ top: 8, right: 32, left: 8, bottom: 0 }}>
        <CartesianGrid stroke="var(--grid-line)" horizontal={false} />
        <XAxis type="number" stroke="var(--text-dim)" fontSize={12} allowDecimals={false} tickLine={false} />
        <YAxis dataKey="riesgo" type="category" stroke="var(--text-dim)" fontSize={12} width={150} tickLine={false} />
        <Tooltip
          contentStyle={tooltipStyle.contentStyle}
          itemStyle={tooltipStyle.itemStyle}
          labelStyle={tooltipStyle.labelStyle}
          formatter={(value) => [value, 'Compulsas ganadas']}
        />
        <Bar dataKey="ganadas" radius={[0, 4, 4, 0]}>
          {rows.map((entry, i) => (
            <Cell key={entry.riesgo} fill={PALETTE[i % PALETTE.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
