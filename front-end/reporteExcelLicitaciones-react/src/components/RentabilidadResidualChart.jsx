import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { num, formatoMonedaCorta } from '../utils/formato'

const PALETTE = ['#FF5C5C', '#F5C26B', '#9B7EDE', '#5B8DEF', '#4FD1C5']

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

export default function RentabilidadResidualChart({ data }) {
  const rows = (data ?? [])
    .map((row) => ({
      riesgo: row.riesgo_renglon,
      perdidas: num(row.compulsas_perdidas),
      monto: num(row.total_monto_cotizado_perdido),
    }))
    .sort((a, b) => b.monto - a.monto)

  return (
    <ResponsiveContainer width="100%" height={280}>
      <BarChart data={rows} layout="vertical" margin={{ top: 8, right: 24, left: 8, bottom: 0 }}>
        <CartesianGrid stroke="var(--grid-line)" horizontal={false} />
        <XAxis type="number" stroke="var(--text-dim)" fontSize={12} tickFormatter={formatoMonedaCorta} />
        <YAxis dataKey="riesgo" type="category" stroke="var(--text-dim)" fontSize={12} width={150} tickLine={false} />
        <Tooltip
          contentStyle={tooltipStyle.contentStyle}
          itemStyle={tooltipStyle.itemStyle}
          labelStyle={tooltipStyle.labelStyle}
          formatter={(value, _name, props) => [
            `${formatoMonedaCorta(value)} (${props.payload.perdidas} compulsas)`,
            'Monto cotizado perdido',
          ]}
        />
        <Bar dataKey="monto" radius={[0, 4, 4, 0]}>
          {rows.map((entry, i) => (
            <Cell key={entry.riesgo} fill={PALETTE[i % PALETTE.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
