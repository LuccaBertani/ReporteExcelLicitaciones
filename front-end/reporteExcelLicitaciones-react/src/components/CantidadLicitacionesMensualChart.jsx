import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { num } from '../utils/formato'

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

export default function CantidadLicitacionesMensualChart({ data }) {
  const rows = (data ?? []).map((row) => ({
    mes: row.mes,
    cantidad: num(row.cantidad_licitaciones),
  }))

  return (
    <ResponsiveContainer width="100%" height={320}>
      <BarChart data={rows} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid stroke="var(--grid-line)" vertical={false} />
        <XAxis dataKey="mes" stroke="var(--text-dim)" fontSize={12} tickLine={false} />
        <YAxis stroke="var(--text-dim)" fontSize={12} tickLine={false} axisLine={false} allowDecimals={false} />
        <Tooltip
          contentStyle={tooltipStyle.contentStyle}
          itemStyle={tooltipStyle.itemStyle}
          labelStyle={tooltipStyle.labelStyle}
          formatter={(value) => [value, 'Licitaciones']}
        />
        <Bar dataKey="cantidad" name="Licitaciones" fill="var(--accent-blue)" radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}
