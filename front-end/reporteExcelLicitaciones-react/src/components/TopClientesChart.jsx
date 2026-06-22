import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { num, formatoPorcentaje } from '../utils/formato'

export default function TopClientesChart({ data }) {
  const rows = (data ?? [])
    .map((row) => ({
      cliente: row.cliente,
      tasa: num(row.tasa_exito_porcentaje),
      total: row.total_compulsas_participadas,
    }))
    .slice(0, 10)

  return (
    <ResponsiveContainer width="100%" height={320}>
      <BarChart data={rows} margin={{ top: 8, right: 16, left: 0, bottom: 24 }}>
        <CartesianGrid stroke="var(--grid-line)" vertical={false} />
        <XAxis
          dataKey="cliente"
          stroke="var(--text-dim)"
          fontSize={11}
          angle={-25}
          textAnchor="end"
          interval={0}
          tickLine={false}
        />
        <YAxis
          stroke="var(--text-dim)"
          fontSize={12}
          tickLine={false}
          axisLine={false}
          tickFormatter={(v) => `${v}%`}
        />
        <Tooltip
          contentStyle={{
            background: 'var(--panel-bg)',
            border: '1px solid var(--grid-line)',
            borderRadius: 8,
            fontFamily: 'var(--font-mono)',
            fontSize: 13,
          }}
          formatter={(value, _name, props) => [
            `${formatoPorcentaje(value)} (${props.payload.total} compulsas)`,
            'Tasa de éxito',
          ]}
        />
        <Bar dataKey="tasa" fill="var(--accent-green)" radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}
