import {
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'
import { num, formatoMonedaCorta } from '../utils/formato'

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

export default function EvolucionMensualChart({ data }) {
  const rows = (data ?? []).map((d) => ({
    mes: d.mes,
    cantidad: num(d.cantidad_licitaciones),
    cotizado: num(d.total_cotizado),
    adjudicado: num(d.total_adjudicado),
  }))

  return (
    <ResponsiveContainer width="100%" height={320}>
      <ComposedChart data={rows} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid stroke="var(--grid-line)" vertical={false} />
        <XAxis dataKey="mes" stroke="var(--text-dim)" fontSize={12} tickLine={false} />
        <YAxis yAxisId="izq" stroke="var(--text-dim)" fontSize={12} tickLine={false} axisLine={false} tickFormatter={formatoMonedaCorta} />
        <YAxis yAxisId="der" orientation="right" stroke="var(--text-dim)" fontSize={12} tickLine={false} axisLine={false} allowDecimals={false} />
        <Tooltip
          contentStyle={tooltipStyle.contentStyle}
          itemStyle={tooltipStyle.itemStyle}
          labelStyle={tooltipStyle.labelStyle}
          formatter={(value, name) => {
            if (name === 'Licitaciones') return [value, name]
            return [formatoMonedaCorta(value), name]
          }}
        />
        <Legend wrapperStyle={{ fontSize: 12, fontFamily: 'var(--font-mono)', color: 'var(--text-dim)' }} />
        <Bar yAxisId="izq" dataKey="cotizado" name="Cotizado" fill="var(--accent-blue)" radius={[3, 3, 0, 0]} />
        <Bar yAxisId="izq" dataKey="adjudicado" name="Adjudicado" fill="var(--accent-green)" radius={[3, 3, 0, 0]} />
        <Line yAxisId="der" type="monotone" dataKey="cantidad" name="Licitaciones" stroke="var(--text)" strokeWidth={2} dot={{ r: 3 }} />
      </ComposedChart>
    </ResponsiveContainer>
  )
}
