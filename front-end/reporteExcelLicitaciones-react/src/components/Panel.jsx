import { useEffect, useRef, useState } from 'react'

export default function Panel({ title, subtitle, children, className = '', filtros, onFiltrosChange }) {
  // Inicializamos el estado como un array vacío para guardar múltiples selecciones
  const [filtrosActivos, setFiltrosActivos] = useState([])
  const [menuAbierto, setMenuAbierto] = useState(false)
  const menuRef = useRef(null)

  // Derivamos si "todos" está activo comparando cantidades, en vez de
  // mantener un estado aparte que se puede desincronizar
  let todosActivos = false;

  if(filtros && filtros.length > 0){
  todosActivos = filtrosActivos.length === filtros.length
  }

  const handleCheckboxChange = (filtro) => {
    setFiltrosActivos((prev) => {
      if (filtro === "todos") {
        // Si ya estaban todos seleccionados, deseleccionamos; si no, seleccionamos todos
        return prev.length === filtros.length ? [] : filtros;
      }
      // Si el filtro ya estaba seleccionado, lo quitamos del array
      if (prev.includes(filtro)) {
        return prev.filter((item) => item !== filtro);
      }
      // Si no estaba, lo agregamos al array
      return [...prev, filtro];
    });
  };

  // Cerramos el menú si se hace click fuera de él
  useEffect(() => {
    if (!menuAbierto) return

    const handleClickFuera = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuAbierto(false)
      }
    }

    document.addEventListener('mousedown', handleClickFuera)
    return () => document.removeEventListener('mousedown', handleClickFuera)
  }, [menuAbierto])

  // Avisamos al padre cada vez que cambia la selección (pero no en el montaje
  // inicial, para no disparar un fetch redundante apenas carga el Panel).
  const esPrimerRender = useRef(true)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (esPrimerRender.current) {
      esPrimerRender.current = false
      return
    }
    if (onFiltrosChange) onFiltrosChange(filtrosActivos)
  }, [filtrosActivos])

  return (
    <section className={`panel ${className}`}>
      <div className="panel__header">
        <div className="name-section">
          <h3 className="panel__title">{title}</h3>
          {subtitle ? <p className="panel__subtitle">{subtitle}</p> : null}
        </div>
        {filtros && filtros.length > 0 ? (
        <div className="filter-section">  
          <div className="filtros-dropdown" ref={menuRef}>
            <button
              type="button"
              className="filtros-dropdown__toggle"
              onClick={() => setMenuAbierto((prev) => !prev)}
              aria-expanded={menuAbierto}
            >
              Filtros
              {filtrosActivos.length > 0 ? (
                <span className="filtros-dropdown__badge">{filtrosActivos.length}</span>
              ) : null}
              <span className={`filtros-dropdown__chevron ${menuAbierto ? 'filtros-dropdown__chevron--abierto' : ''}`}>▾</span>
            </button>

            {menuAbierto ? (
              <ul className="filtros-dropdown__menu">
                <li key="todos">
                  <button
                    type="button"
                    className={`filtros-dropdown__item ${todosActivos ? 'filtros-dropdown__item--activo' : ''}`}
                    onClick={() => handleCheckboxChange("todos")}
                  >
                    <span className="filtros-dropdown__check">{todosActivos ? '✓' : ''}</span>
                    Todos
                  </button>
                </li>
                {filtros.map((filtro, index) => {
                  const activo = filtrosActivos.includes(filtro)
                  return (
                    <li key={index}>
                      <button
                        type="button"
                        className={`filtros-dropdown__item ${activo ? 'filtros-dropdown__item--activo' : ''}`}
                        onClick={() => handleCheckboxChange(filtro)}
                      >
                        <span className="filtros-dropdown__check">{activo ? '✓' : ''}</span>
                        {filtro}
                      </button>
                    </li>
                  )
                })}
              </ul>
            ) : null}
          </div>
        </div>
        ) : null}
      </div>
      <div className="panel__body">{children}</div>
    </section>
  )
}
