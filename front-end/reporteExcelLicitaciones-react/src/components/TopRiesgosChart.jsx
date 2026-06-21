import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { num, formatoMonedaCorta } from '../utils/formato'

const PALETTE = ['#3DDC84', '#5B8DEF', '#F5C26B', '#FF5C5C', '#9B7EDE', '#4FD1C5']

export default function TopRiesgosChart({ data }) {
  // Agrupamos por riesgo sumando monto adjudicado, tomamos el top 8
  const acumulado = new Map()
  for (const row of data ?? []) {
    const key = row.riesgo ?? 'Sin riesgo'
    const monto = num(row.monto_adjudicado)
    acumulado.set(key, (acumulado.get(key) ?? 0) + monto)
  }

  const rows = Array.from(acumulado.entries())
    .map(([riesgo, monto]) => ({ riesgo, monto }))
    .sort((a, b) => b.monto - a.monto)
    .slice(0, 8)

  return (
    <ResponsiveContainer width="100%" height={320}>
      <BarChart data={rows} layout="vertical" margin={{ top: 8, right: 24, left: 8, bottom: 0 }}>
        <CartesianGrid stroke="var(--grid-line)" horizontal={false} />
        <XAxis type="number" stroke="var(--text-dim)" fontSize={12} tickFormatter={formatoMonedaCorta} />
        <YAxis
          dataKey="riesgo"
          type="category"
          stroke="var(--text-dim)"
          fontSize={12}
          width={140}
          tickLine={false}
        />
        <Tooltip
          contentStyle={{
            background: 'var(--panel-bg)',
            border: '1px solid var(--grid-line)',
            borderRadius: 8,
            fontFamily: 'var(--font-mono)',
            fontSize: 13,
          }}
          formatter={(value) => [formatoMonedaCorta(value), 'Monto adjudicado']}
        />
        <Bar dataKey="monto" radius={[0, 4, 4, 0]}>
          {rows.map((entry, index) => (
            <Cell key={entry.riesgo} fill={PALETTE[index % PALETTE.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
