import { num, formatoPorcentaje, formatoMonedaCorta } from '../utils/formato'

/**
 * Tabla de desvío de precio por riesgo (una fila por riesgo).
 * Muestra: riesgo, perdidas, ganadas, cotizado, adjudicado, desvío %.
 */
export default function DesvioPrecios({ data }) {
  const rows = (data ?? []).map((row) => ({
    riesgo: row.riesgo,
    perdidas: row.licitaciones_perdidas_riesgo,
    ganadas: row.total_ganadas_riesgo,
    cotizado: num(row.monto_cotizado_total_riesgo),
    adjudicado: num(row.monto_adjudicado_total_riesgo),
    desvio: num(row.desvio_total_riesgo_porcentaje),
  }))

  const desvioColor = (v) => {
    if (v > 100) return 'var(--accent-red)'
    if (v > 30) return 'var(--accent-amber)'
    return 'var(--accent-green)'
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontFamily: 'var(--font-mono)', fontSize: 13 }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--grid-line)' }}>
            {['Riesgo / Renglón', 'Perdidas', 'Ganadas', 'Cotizado', 'Adjudicado', 'Desvío %'].map((h) => (
              <th
                key={h}
                style={{
                  padding: '8px 12px',
                  textAlign: h === 'Riesgo / Renglón' ? 'left' : 'right',
                  color: 'var(--text-dim)',
                  fontWeight: 500,
                  fontSize: 11,
                  textTransform: 'uppercase',
                  letterSpacing: '0.06em',
                  whiteSpace: 'nowrap',
                }}
              >
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, i) => (
            <tr
              key={row.riesgo}
              style={{
                borderBottom: '1px solid var(--grid-line)',
                background: i % 2 === 0 ? 'transparent' : 'rgba(255,255,255,0.02)',
              }}
            >
              <td style={{ padding: '9px 12px', color: 'var(--text)', fontWeight: 500 }}>{row.riesgo}</td>
              <td style={{ padding: '9px 12px', textAlign: 'right', color: 'var(--accent-red)' }}>{row.perdidas}</td>
              <td style={{ padding: '9px 12px', textAlign: 'right', color: 'var(--accent-green)' }}>{row.ganadas}</td>
              <td style={{ padding: '9px 12px', textAlign: 'right', color: 'var(--text-dim)' }}>{formatoMonedaCorta(row.cotizado)}</td>
              <td style={{ padding: '9px 12px', textAlign: 'right', color: 'var(--text-dim)' }}>{formatoMonedaCorta(row.adjudicado)}</td>
              <td style={{ padding: '9px 12px', textAlign: 'right', fontWeight: 600, color: desvioColor(row.desvio) }}>
                {formatoPorcentaje(row.desvio)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
