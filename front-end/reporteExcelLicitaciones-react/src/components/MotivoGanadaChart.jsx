import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { num } from '../utils/formato'

const COLORES = ['#5B8DEF', '#3DDC84', '#F5C26B', '#9B7EDE', '#4FD1C5', '#FF5C5C']

function TooltipMotivo({ active, payload }) {
  if (!active || !payload?.length) return null
  const { motivo, cantidad } = payload[0].payload
  return (
    <div
      style={{
        background: 'var(--panel-bg)',
        border: '1px solid var(--grid-line)',
        borderRadius: 8,
        padding: '8px 14px',
        fontFamily: 'var(--font-mono)',
        fontSize: 13,
        color: 'var(--text)',
        display: 'flex',
        alignItems: 'center',
        gap: 8,
      }}
    >
      <span
        style={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          background: payload[0].payload.fill,
          flexShrink: 0,
        }}
      />
      {motivo}: <strong style={{ marginLeft: 4 }}>{cantidad}</strong>
    </div>
  )
}

export default function MotivoGanadaChart({ data }) {
  const rows = (data ?? []).map((row, i) => ({
    motivo: row.motivo_ganada,
    cantidad: num(row.cantidad_compulsas),
    fill: COLORES[i % COLORES.length],
  }))

  return (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie
          data={rows}
          dataKey="cantidad"
          nameKey="motivo"
          innerRadius={65}
          outerRadius={100}
          paddingAngle={2}
        >
          {rows.map((entry) => (
            <Cell key={entry.motivo} fill={entry.fill} stroke="var(--panel-bg)" />
          ))}
        </Pie>
        <Tooltip content={<TooltipMotivo />} />
        <Legend
          verticalAlign="bottom"
          height={36}
          wrapperStyle={{ fontSize: 12, fontFamily: 'var(--font-mono)' }}
        />
      </PieChart>
    </ResponsiveContainer>
  )
}
