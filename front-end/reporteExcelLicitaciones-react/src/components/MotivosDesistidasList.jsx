import { num, formatoMonedaCorta } from '../utils/formato'

/**
 * Lista de motivos de desistimiento con barra de progreso relativa.
 */
export default function MotivosDesistidasList({ data }) {

  const rows = (data ?? [])
    .map((row) => ({
      motivo: row.motivo_desistida,
      cantidad: num(row.cantidad_casos),
      monto: row.monto_adjudicado,
    }))
    .sort((a, b) => b.cantidad - a.cantidad)

  const max = rows[0]?.cantidad || 1

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {rows.map((row) => (
        <div key={row.motivo} style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <span
              style={{
                fontFamily: 'var(--font-mono)',
                fontSize: 12,
                color: 'var(--text)',
                maxWidth: '80%',
                lineHeight: 1.4,
              }}
            >
              {row.motivo}
            </span>
            <span
              style={{
                fontFamily: 'var(--font-mono)',
                fontSize: 13,
                fontWeight: 600,
                color: 'var(--accent-amber)',
                flexShrink: 0,
                marginLeft: 8,
              }}
            >
              {row.cantidad}
            </span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--text-dim)' }}>
              Monto adjudicado teórico
            </span>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, fontWeight: 600, color: 'var(--accent-blue)' }}>
              {formatoMonedaCorta(row.monto)}
            </span>
          </div>
          <div style={{ height: 4, background: 'var(--grid-line)', borderRadius: 2, overflow: 'hidden' }}>
            <div
              style={{
                height: '100%',
                width: `${(row.cantidad / max) * 100}%`,
                background: 'var(--accent-amber)',
                borderRadius: 2,
                transition: 'width 0.6s ease',
              }}
            />
          </div>
        </div>
      ))}
    </div>
  )
}
