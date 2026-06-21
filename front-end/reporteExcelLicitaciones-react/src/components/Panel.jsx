export default function Panel({ title, subtitle, children, className = '' }) {
  return (
    <section className={`panel ${className}`}>
      <header className="panel__header">
        <h3 className="panel__title">{title}</h3>
        {subtitle ? <p className="panel__subtitle">{subtitle}</p> : null}
      </header>
      <div className="panel__body">{children}</div>
    </section>
  )
}
