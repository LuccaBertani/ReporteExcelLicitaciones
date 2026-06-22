import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import { num, formatoEntero } from '../utils/formato'

const COLOR_BY_ESTADO = {
  GANADA: '#3DDC84',
  PERDIDA: '#FF5C5C',
  ACTIVA: '#5B8DEF',
  DESISTIDA: '#F5C26B',
}
const FALLBACK = ['#9B7EDE', '#4FD1C5', '#E0739A']

function TooltipEstado({ active, payload }) {
  if (!active || !payload?.length) return null
  const { estado, cantidad } = payload[0].payload
  const color = payload[0].payload.fill ?? payload[0].color

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
          background: color,
          flexShrink: 0,
        }}
      />
      <span>
        {estado}: {formatoEntero(cantidad)}
      </span>
    </div>
  )
}

export default function EstadoLicitacionesChart({ data }) {
  const rows = (data ?? []).map((row) => ({
    estado: row.estado_licitacion,
    cantidad: num(row.cantidad_compulsas),
  }))

  const colorFor = (estado, index) =>
    COLOR_BY_ESTADO[String(estado).toUpperCase()] ?? FALLBACK[index % FALLBACK.length]

  return (
    <ResponsiveContainer width="100%" height={320}>
      <PieChart>
        <Pie
          data={rows}
          dataKey="cantidad"
          nameKey="estado"
          innerRadius={70}
          outerRadius={110}
          paddingAngle={2}
        >
          {rows.map((entry, index) => (
            <Cell key={entry.estado} fill={colorFor(entry.estado, index)} stroke="var(--panel-bg)" />
          ))}
        </Pie>
        <Tooltip content={<TooltipEstado />} />
        <Legend
          verticalAlign="bottom"
          height={36}
          wrapperStyle={{ fontSize: 12, fontFamily: 'var(--font-mono)' }}
        />
      </PieChart>
    </ResponsiveContainer>
  )
}
