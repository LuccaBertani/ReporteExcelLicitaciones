import {
  AreaChart,
  Area,
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

export default function RentabilidadMensualChart({ data }) {
  const rows = (data ?? []).map((row) => ({
    mes: row.mes,
    cotizada: num(row.cant_cotizada),
    ganada: num(row.cant_ganada),
  }))

  return (
    <ResponsiveContainer width="100%" height={320}>
      <AreaChart data={rows} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="gradCotizada" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--accent-blue)" stopOpacity={0.4} />
            <stop offset="100%" stopColor="var(--accent-blue)" stopOpacity={0} />
          </linearGradient>
          <linearGradient id="gradGanada" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--accent-green)" stopOpacity={0.5} />
            <stop offset="100%" stopColor="var(--accent-green)" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid stroke="var(--grid-line)" vertical={false} />
        <XAxis dataKey="mes" stroke="var(--text-dim)" fontSize={12} tickLine={false} />
        <YAxis stroke="var(--text-dim)" fontSize={12} tickLine={false} axisLine={false} tickFormatter={formatoMonedaCorta} />
        <Tooltip
          contentStyle={tooltipStyle.contentStyle}
          itemStyle={tooltipStyle.itemStyle}
          labelStyle={tooltipStyle.labelStyle}
          formatter={(value, name) => [formatoMonedaCorta(value), name]}
        />
        <Legend wrapperStyle={{ fontSize: 12, fontFamily: 'var(--font-mono)', color: 'var(--text-dim)' }} />
        <Area type="monotone" dataKey="cotizada" name="Cotizado" stroke="var(--accent-blue)" fill="url(#gradCotizada)" strokeWidth={2} />
        <Area type="monotone" dataKey="ganada" name="Ganado" stroke="var(--accent-green)" fill="url(#gradGanada)" strokeWidth={2} />
      </AreaChart>
    </ResponsiveContainer>
  )
}
