import { useState, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { ExcelAPI } from './api/estadisticas'
import './pages.css'

const HEADERS_ESPERADOS = [
  { col: 'Seccion',          desc: 'Sección o área del organismo' },
  { col: 'Ramo',             desc: 'Ramo de seguro (ej. Patrimonial)' },
  { col: 'Fecha',            desc: 'Fecha de la licitación (dd/mm/aaaa)' },
  { col: 'Numero',           desc: 'Número de compulsa único' },
  { col: 'Cliente',          desc: 'Nombre del cliente / organismo' },
  { col: 'Riesgo',           desc: 'Riesgo o renglón cotizado' },
  { col: 'Status',           desc: 'Estado: Ganada, Perdida, Desistida…' },
  { col: 'Estado Motivo',    desc: 'Detalle del estado (ej. Por precio)' },
  { col: 'Motivo',           desc: 'Motivo libre de la resolución' },
  { col: 'AdjudicadoA',      desc: 'Empresa adjudicataria' },
  { col: 'Moneda',           desc: 'Moneda del monto (ej. ARS)' },
  { col: 'MontoAdjudicado',  desc: 'Monto adjudicado (numérico)' },
  { col: 'MontoCotizado',    desc: 'Monto cotizado por La Caja (numérico)' },
  { col: 'CotizadoCosto1',   desc: 'Costo cotizado renglón 1' },
  { col: 'CotizadoCosto2',   desc: 'Costo cotizado renglón 2' },
  { col: 'AdjudicadoCosto1', desc: 'Costo adjudicado renglón 1' },
  { col: 'AdjudicadoCosto2', desc: 'Costo adjudicado renglón 2' },
  { col: 'AdjudicadoCosto3', desc: 'Costo adjudicado renglón 3' },
]

const RIESGOS_SOPORTADOS = [
  'RC COMPRENSIVA', 'RC ASCENSORES', 'RC CALDERAS', 'RC GUARDA/DEPOSITO',
  'RC CARTELES', 'INCENDIO', 'TECNICO EQ. ELECTRONICOS', 'APC',
  'ROBO Y RIESGOS SIMILARES', 'VALORES EN TRANSITO', 'VALORES EN CAJA',
  'TR INSTRUMENTOS MUSICALES', 'TR OBRAS DE ARTE', 'DRONES', 'INTEGRAL',
  'AERONAVEACION', 'CAUCION', 'TRO', 'TRANSPORTE', 'SEPELIO',
  'VIDA', 'SALUD', 'FRANQUICIAS',
]

export default function UploadPage() {
  const navigate = useNavigate()
  const [archivo, setArchivo] = useState(null)
  const [dragging, setDragging] = useState(false)
  const [estado, setEstado] = useState(null) // null | 'cargando' | 'ok' | 'error'
  const [mensaje, setMensaje] = useState('')
  const inputRef = useRef(null)

  const seleccionarArchivo = (file) => {
    if (!file) return
    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      setEstado('error')
      setMensaje('El archivo debe ser .xlsx')
      return
    }
    setArchivo(file)
    setEstado(null)
    setMensaje('')
  }

  const onDrop = useCallback((e) => {
    e.preventDefault()
    setDragging(false)
    const file = e.dataTransfer.files[0]
    seleccionarArchivo(file)
  }, [])

  const onDragOver = (e) => { e.preventDefault(); setDragging(true) }
  const onDragLeave = () => setDragging(false)

  const onFileChange = (e) => seleccionarArchivo(e.target.files[0])

  const importar = async () => {
    if (!archivo) return
    setEstado('cargando')
    setMensaje('')
    try {
      const res = await ExcelAPI.importar(archivo)
      setEstado('ok')
      setMensaje(res.mensaje || 'Importación completada.')
    } catch (err) {
      setEstado('error')
      setMensaje(err.message || 'Error inesperado al importar.')
    }
  }

  const limpiar = () => {
    setArchivo(null)
    setEstado(null)
    setMensaje('')
    if (inputRef.current) inputRef.current.value = ''
  }

  return (
    <div className="upload-page">
      <header className="upload-header">
        <button className="back-btn" onClick={() => navigate('/')}>← Volver</button>
        <div className="upload-header__eyebrow">Carga de datos · Excel</div>
        <h1 className="upload-header__title">Importar nuevas licitaciones</h1>
        <p className="upload-header__desc">
          Subí el archivo .xlsx con los datos de licitaciones. El servidor lo procesa
          e inserta los registros en la base de datos automáticamente.
        </p>
      </header>

      <div className="upload-layout">
        {/* ── Panel izquierdo: zona de carga ── */}
        <section className="upload-card upload-card--main">
          <h2 className="upload-card__title">Cargar archivo</h2>

          {/* Dropzone */}
          <div
            className={`dropzone ${dragging ? 'dropzone--over' : ''} ${archivo ? 'dropzone--selected' : ''}`}
            onDrop={onDrop}
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onClick={() => !archivo && inputRef.current?.click()}
          >
            <input
              ref={inputRef}
              type="file"
              accept=".xlsx"
              style={{ display: 'none' }}
              onChange={onFileChange}
            />

            {archivo ? (
              <div className="dropzone__file">
                <span className="dropzone__file-icon">📄</span>
                <div className="dropzone__file-info">
                  <span className="dropzone__file-name">{archivo.name}</span>
                  <span className="dropzone__file-size">
                    {(archivo.size / 1024).toFixed(1)} KB
                  </span>
                </div>
                <button
                  className="dropzone__remove"
                  onClick={(e) => { e.stopPropagation(); limpiar() }}
                  title="Quitar archivo"
                >
                  ✕
                </button>
              </div>
            ) : (
              <div className="dropzone__placeholder">
                <span className="dropzone__icon">📥</span>
                <p className="dropzone__text">
                  Arrastrá el archivo acá o <span className="dropzone__link">hacé click para elegirlo</span>
                </p>
                <p className="dropzone__hint">Solo archivos .xlsx · Máx. 50 MB</p>
              </div>
            )}
          </div>

          {/* Botón de importar */}
          <button
            className={`import-btn ${!archivo || estado === 'cargando' ? 'import-btn--disabled' : ''}`}
            onClick={importar}
            disabled={!archivo || estado === 'cargando'}
          >
            {estado === 'cargando' ? (
              <><span className="import-btn__spinner" /> Importando…</>
            ) : (
              'Importar al sistema'
            )}
          </button>

          {/* Feedback */}
          {estado === 'ok' && (
            <div className="upload-feedback upload-feedback--ok">
              <span>✓</span> {mensaje}
            </div>
          )}
          {estado === 'error' && (
            <div className="upload-feedback upload-feedback--error">
              <span>✕</span> {mensaje}
            </div>
          )}

          <div className="upload-note" style={{ marginTop: 20 }}>
            <span className="upload-note__icon">⚠</span>
            Si una licitación ya existe en la base de datos, sus renglones se actualizan
            en lugar de duplicarse.
          </div>
        </section>

        {/* ── Panel derecho: referencia ── */}
        <aside className="upload-aside">
          <div className="upload-card">
            <h2 className="upload-card__title">Columnas esperadas</h2>
            <p className="upload-card__subtitle">El orden no importa, pero los nombres deben ser exactos (sensible a mayúsculas).</p>
            <div className="headers-table-wrapper">
              <table className="headers-table">
                <thead>
                  <tr>
                    <th>Columna</th>
                    <th>Descripción</th>
                  </tr>
                </thead>
                <tbody>
                  {HEADERS_ESPERADOS.map((h) => (
                    <tr key={h.col}>
                      <td><code>{h.col}</code></td>
                      <td>{h.desc}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="upload-card">
            <h2 className="upload-card__title">Riesgos / Renglones soportados</h2>
            <p className="upload-card__subtitle">
              Valores reconocidos en la columna <code>Riesgo</code>. El backend los normaliza automáticamente.
            </p>
            <div className="riesgos-grid">
              {RIESGOS_SOPORTADOS.map((r) => (
                <span key={r} className="riesgo-chip">{r}</span>
              ))}
            </div>
          </div>
        </aside>
      </div>
    </div>
  )
}
