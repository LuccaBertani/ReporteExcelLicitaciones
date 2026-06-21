// El backend devuelve varios campos numéricos como String (a veces con "%").
// Estos helpers los normalizan para usar en gráficos y formateo.

export function num(value) {
  if (value === null || value === undefined) return 0
  if (typeof value === 'number') return value
  const directo = parseFloat(String(value).replace('%', ''))
  return Number.isNaN(directo) ? 0 : directo
}

export function formatoMoneda(value) {
  const n = num(value)
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(n)
}

export function formatoMonedaCorta(value) {
  const n = num(value)
  if (Math.abs(n) >= 1_000_000) return `$${(n / 1_000_000).toFixed(1)}M`
  if (Math.abs(n) >= 1_000) return `$${(n / 1_000).toFixed(0)}K`
  return `$${n.toFixed(0)}`
}

export function formatoPorcentaje(value, decimals = 1) {
  const n = num(value)
  return `${n.toFixed(decimals)}%`
}

export function formatoEntero(value) {
  const n = num(value)
  return new Intl.NumberFormat('es-AR').format(Math.round(n))
}
