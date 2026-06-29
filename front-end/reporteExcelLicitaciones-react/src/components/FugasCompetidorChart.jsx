import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
  LabelList,
} from 'recharts'
import { num, formatoMonedaCorta, formatoPorcentaje } from '../utils/formato'

const PALETTE = ['#FF5C5C', '#F5C26B', '#5B8DEF', '#9B7EDE', '#4FD1C5']

function TooltipFugas({ active, payload }) {
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
      <div style={{ fontWeight: 600, marginBottom: 4, color: d.fill }}>{d.competidor}</div>
      <div>Compulsas ganadas: <strong>{d.compulsas}</strong></div>
      <div>Monto ganado: <strong>{formatoMonedaCorta(d.monto)}</strong></div>
      <div>Share de volumen: <strong>{formatoPorcentaje(d.share)}</strong></div>
    </div>
  )
}

export default function FugasCompetidorChart({ data }) {
  const rows = (data ?? [])
    .map((row, i) => ({
      competidor: row.competidor,
      compulsas: row.compulsas_ganadas,
      monto: num(row.total_monto_ganado),
      share: num(row.porcentaje_tenencia_volumen),
      fill: PALETTE[i % PALETTE.length],
    }))
    .sort((a, b) => b.monto - a.monto)

  return (
    <ResponsiveContainer width="100%" height={320}>
      <BarChart data={rows} layout="vertical" margin={{ top: 8, right: 80, left: 8, bottom: 0 }}>
        <CartesianGrid stroke="var(--grid-line)" horizontal={false} />
        <XAxis type="number" stroke="var(--text-dim)" fontSize={12} tickFormatter={formatoMonedaCorta} />
        <YAxis
          dataKey="competidor"
          type="category"
          stroke="var(--text-dim)"
          fontSize={13}
          width={90}
          tickLine={false}
        />
        <Tooltip content={<TooltipFugas />} />
        <Bar dataKey="monto" radius={[0, 4, 4, 0]}>
          {rows.map((entry) => (
            <Cell key={entry.competidor} fill={entry.fill} />
          ))}
          <LabelList
            dataKey="share"
            position="right"
            formatter={(v) => `${num(v).toFixed(1)}%`}
            style={{ fill: 'var(--text-dim)', fontSize: 12, fontFamily: 'var(--font-mono)' }}
          />
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
