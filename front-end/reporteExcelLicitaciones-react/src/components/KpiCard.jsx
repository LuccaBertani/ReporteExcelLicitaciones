import { useEffect, useRef, useState } from 'react'

/**
 * Tarjeta de indicador grande con animación de conteo al montar.
 * "value" debe ser numérico; "format" recibe el valor animado y lo formatea.
 */
export default function KpiCard({ label, value, format, accent = 'neutral', sublabel }) {
  const [display, setDisplay] = useState(0)
  const rafRef = useRef(null)

  useEffect(() => {
    const target = Number.isFinite(value) ? value : 0
    const duration = 900
    const start = performance.now()

    function tick(now) {
      const elapsed = now - start
      const progress = Math.min(elapsed / duration, 1)
      const eased = 1 - Math.pow(1 - progress, 3)
      setDisplay(target * eased)
      if (progress < 1) {
        rafRef.current = requestAnimationFrame(tick)
      } else {
        setDisplay(target)
      }
    }

    rafRef.current = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(rafRef.current)
  }, [value])

  return (
    <div className={`kpi-card kpi-card--${accent}`}>
      <span className="kpi-card__label">{label}</span>
      <span className="kpi-card__value">{format ? format(display) : Math.round(display)}</span>
      {sublabel ? <span className="kpi-card__sublabel">{sublabel}</span> : null}
    </div>
  )
}
