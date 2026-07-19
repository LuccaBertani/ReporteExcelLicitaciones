import { useState, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { ExcelAPI } from './api/estadisticas'
import './pages.css'

const HEADERS_ESPERADOS = [
  { col: 'Seccion',          desc: 'Sección o área del organismo' },
  { col: 'Ramo',             desc: 'Ramo de seguro (ej. Patrimonial)' },
  { col: 'Fecha',            desc: 'Fecha de la licitación (dd/mm/aaaa)' },
  { col: 'Numero',           desc: 'Número de compulsa (se repite entre años; se identifica de forma única junto con el año de la Fecha)' },
  { col: 'Cliente',          desc: 'Nombre del cliente / organismo' },
  { col: 'Riesgo',           desc: 'Riesgo(s) cotizados en la fila, separados por "/", ";" o ",". Ej: "RC COMPRENSIVA / INCENDIO"' },
  { col: 'Status',           desc: 'Estado: Ganada, Perdida, Desistida…' },
  { col: 'Estado Motivo',    desc: 'Detalle del estado (ej. Por precio)' },
  { col: 'Motivo',           desc: 'Motivo libre de la resolución' },
  { col: 'AdjudicadoA',      desc: 'Empresa adjudicataria' },
  { col: 'MontoAdjudicado',  desc: 'Monto adjudicado general de la fila (numérico). Se usa como valor por defecto para cada riesgo si no hay una columna AdjudicadoCostoN específica para su posición' },
  { col: 'MontoCotizado',    desc: 'Monto cotizado general de la fila (numérico). Se usa como valor por defecto para cada riesgo si no hay una columna CotizadoCostoN específica para su posición' },
  { col: 'CotizadoCostoN',   desc: 'Monto cotizado específico para el riesgo en la posición N de la columna Riesgo (CotizadoCosto1 → primer riesgo, CotizadoCosto2 → segundo riesgo, y así sin límite). No hace falta que existan todas: donde falte alguna, ese riesgo toma el MontoCotizado general' },
  { col: 'AdjudicadoCostoN', desc: 'Monto adjudicado específico para el riesgo en la posición N de la columna Riesgo (AdjudicadoCosto1 → primer riesgo, AdjudicadoCosto2 → segundo, etc., sin límite). Donde falte alguna, ese riesgo toma el MontoAdjudicado general' },
]

// Debe reflejar exactamente los sinónimos definidos en GestorRiesgos.cargarSinonimos()
// (back-end/src/main/java/raiz/dominio/GestorRiesgos.java). Si se agrega o cambia un
// sinónimo ahí, hay que actualizarlo acá también.
const RIESGOS_SINONIMOS = [
  { oficial: 'RC COMPRENSIVA',            sinonimos: ['RCC', 'RC', 'RC Comprensiva', 'RC Canes'] },
  { oficial: 'RC ASCENSORES',             sinonimos: ['RC Ascendores', 'RC Ascensores'] },
  { oficial: 'RC CALDERAS',               sinonimos: ['RC Calderas', 'Calderas'] },
  { oficial: 'RC GUARDA/DEPOSITO',        sinonimos: ['RC Guarda/Deposito', 'RC Guarda y Deposito', 'Guarda y Deposito'] },
  { oficial: 'RC CARTELES',               sinonimos: ['RC Carteles', 'Carteles'] },
  { oficial: 'INCENDIO',                  sinonimos: ['Incendio'] },
  { oficial: 'TECNICO EQ. ELECTRONICOS',  sinonimos: ['ST EE', 'ST', 'ST TR', 'TR Equipos Electrónicos', 'ST Eq. Electrónicos'] },
  { oficial: 'APC',                       sinonimos: ['ACCIDENTES PERSONALES', 'APC'] },
  { oficial: 'ROBO Y RIESGOS SIMILARES',  sinonimos: ['Robo', 'Robo Drones'] },
  { oficial: 'VALORES EN TRANSITO',       sinonimos: ['ROBO DE VALORES', 'Valores en Transito'] },
  { oficial: 'VALORES EN CAJA',           sinonimos: ['Valores en Caja', 'Valores en Caja y Cofre'] },
  { oficial: 'TR INSTRUMENTOS MUSICALES', sinonimos: ['TR Instrumentos Musicales', 'TRIM'] },
  { oficial: 'TR OBRAS DE ARTE',          sinonimos: ['TR Obras de Arte', 'TROA'] },
  { oficial: 'DRONES',                    sinonimos: ['RC Drones', 'RC Vant'] },
  { oficial: 'INTEGRAL',                  sinonimos: ['INTEGRAL', 'ICO'] },
  { oficial: 'AERONAVEACION',             sinonimos: ['Aeronavegación', 'Aeronavegacion'] },
  { oficial: 'CAUCION',                   sinonimos: ['Caucion'] },
  { oficial: 'TRO',                       sinonimos: ['TRO'] },
  { oficial: 'TRANSPORTE',                sinonimos: ['Transporte'] },
  { oficial: 'SEPELIO',                   sinonimos: ['Sepelio'] },
  { oficial: 'VIDA',                      sinonimos: ['Vida'] },
  { oficial: 'SALUD',                     sinonimos: ['Salud'] },
  { oficial: 'FRANQUICIAS',               sinonimos: ['Franquicias'] },
]

export default function UploadPage() {
  const navigate = useNavigate()
  const [archivo, setArchivo] = useState(null)
  const [dragging, setDragging] = useState(false)
  const [estado, setEstado] = useState(null) // null | 'cargando' | 'ok' | 'parcial' | 'error'
  const [mensaje, setMensaje] = useState('')
  const [erroresImportacion, setErroresImportacion] = useState([])
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
    setErroresImportacion([])
    try {
      const res = await ExcelAPI.importar(archivo)
      setEstado(res.status === 'parcial' ? 'parcial' : 'ok')
      setMensaje(res.mensaje || 'Importación completada.')
      setErroresImportacion(res.errores || [])
    } catch (err) {
      setEstado('error')
      setMensaje(err.message || 'Error inesperado al importar.')
    }
  }

  const limpiar = () => {
    setArchivo(null)
    setEstado(null)
    setMensaje('')
    setErroresImportacion([])
    if (inputRef.current) inputRef.current.value = ''
  }

  // ── Verificar carga (auditoría de solo lectura, no inserta nada) ──
  const [archivoVerificar, setArchivoVerificar] = useState(null)
  const [draggingVerificar, setDraggingVerificar] = useState(false)
  const [estadoVerificar, setEstadoVerificar] = useState(null) // null | 'cargando' | 'ok' | 'error'
  const [mensajeVerificar, setMensajeVerificar] = useState('')
  const [resultadoVerificar, setResultadoVerificar] = useState(null)
  const inputRefVerificar = useRef(null)

  const seleccionarArchivoVerificar = (file) => {
    if (!file) return
    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      setEstadoVerificar('error')
      setMensajeVerificar('El archivo debe ser .xlsx')
      return
    }
    setArchivoVerificar(file)
    setEstadoVerificar(null)
    setMensajeVerificar('')
    setResultadoVerificar(null)
  }

  const onDropVerificar = useCallback((e) => {
    e.preventDefault()
    setDraggingVerificar(false)
    const file = e.dataTransfer.files[0]
    seleccionarArchivoVerificar(file)
  }, [])

  const onDragOverVerificar = (e) => { e.preventDefault(); setDraggingVerificar(true) }
  const onDragLeaveVerificar = () => setDraggingVerificar(false)
  const onFileChangeVerificar = (e) => seleccionarArchivoVerificar(e.target.files[0])

  const verificar = async () => {
    if (!archivoVerificar) return
    setEstadoVerificar('cargando')
    setMensajeVerificar('')
    setResultadoVerificar(null)
    try {
      const res = await ExcelAPI.verificar(archivoVerificar)
      setResultadoVerificar(res)
      setEstadoVerificar(res.exitosa ? 'ok' : 'error')
      setMensajeVerificar(
        res.exitosa
          ? 'Verificación correcta: todos los renglones coinciden.'
          : `Se encontraron incidencias (ver detalle abajo).`
      )
    } catch (err) {
      setEstadoVerificar('error')
      setMensajeVerificar(err.message || 'Error inesperado al verificar.')
    }
  }

  const limpiarVerificar = () => {
    setArchivoVerificar(null)
    setEstadoVerificar(null)
    setMensajeVerificar('')
    setResultadoVerificar(null)
    if (inputRefVerificar.current) inputRefVerificar.current.value = ''
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
          {estado === 'parcial' && (
            <div className="upload-feedback upload-feedback--error">
              <span>⚠</span> {mensaje}
            </div>
          )}
          {estado === 'error' && (
            <div className="upload-feedback upload-feedback--error">
              <span>✕</span> {mensaje}
            </div>
          )}

          {erroresImportacion.length > 0 && (
            <div className="verify-incidencias">
              {erroresImportacion.map((linea, i) => (
                <div key={i} className="verify-incidencias__row">{linea}</div>
              ))}
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
            <p className="upload-card__subtitle">
              El orden no importa, y la comparación de nombres no distingue mayúsculas/minúsculas,
              tildes ni espacios (p. ej. "Cotizado Costo 1" y "COTIZADOCOSTO1" matchean igual).
            </p>
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
              Valores reconocidos en la columna <code>Riesgo</code>. Cada renglón oficial acepta
              cualquiera de sus sinónimos (tampoco distingue mayúsculas/tildes/espacios); si un
              valor no coincide con ninguno de los dos, la fila se reporta como riesgo no reconocido.
            </p>
            <div className="headers-table-wrapper">
              <table className="headers-table">
                <thead>
                  <tr>
                    <th>Renglón oficial</th>
                    <th>Sinónimos aceptados</th>
                  </tr>
                </thead>
                <tbody>
                  {RIESGOS_SINONIMOS.map((r) => (
                    <tr key={r.oficial}>
                      <td><code>{r.oficial}</code></td>
                      <td>{r.sinonimos.join(', ')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </aside>
      </div>

      {/* ── Verificar carga ── */}
      <div className="upload-layout" style={{ marginTop: 24 }}>
        <section className="upload-card upload-card--main">
          <h2 className="upload-card__title">Verificar carga</h2>
          <p className="upload-card__subtitle">
            Subí un Excel para auditarlo contra lo que ya está en la base de datos:
            reporta licitaciones o renglones faltantes y montos que no coinciden.
            No inserta ni modifica nada.
          </p>

          {/* Dropzone */}
          <div
            className={`dropzone ${draggingVerificar ? 'dropzone--over' : ''} ${archivoVerificar ? 'dropzone--selected' : ''}`}
            onDrop={onDropVerificar}
            onDragOver={onDragOverVerificar}
            onDragLeave={onDragLeaveVerificar}
            onClick={() => !archivoVerificar && inputRefVerificar.current?.click()}
          >
            <input
              ref={inputRefVerificar}
              type="file"
              accept=".xlsx"
              style={{ display: 'none' }}
              onChange={onFileChangeVerificar}
            />

            {archivoVerificar ? (
              <div className="dropzone__file">
                <span className="dropzone__file-icon">📄</span>
                <div className="dropzone__file-info">
                  <span className="dropzone__file-name">{archivoVerificar.name}</span>
                  <span className="dropzone__file-size">
                    {(archivoVerificar.size / 1024).toFixed(1)} KB
                  </span>
                </div>
                <button
                  className="dropzone__remove"
                  onClick={(e) => { e.stopPropagation(); limpiarVerificar() }}
                  title="Quitar archivo"
                >
                  ✕
                </button>
              </div>
            ) : (
              <div className="dropzone__placeholder">
                <span className="dropzone__icon">🔎</span>
                <p className="dropzone__text">
                  Arrastrá el archivo acá o <span className="dropzone__link">hacé click para elegirlo</span>
                </p>
                <p className="dropzone__hint">Solo archivos .xlsx · Máx. 50 MB</p>
              </div>
            )}
          </div>

          {/* Botón de verificar */}
          <button
            className={`import-btn ${!archivoVerificar || estadoVerificar === 'cargando' ? 'import-btn--disabled' : ''}`}
            onClick={verificar}
            disabled={!archivoVerificar || estadoVerificar === 'cargando'}
          >
            {estadoVerificar === 'cargando' ? (
              <><span className="import-btn__spinner" /> Verificando…</>
            ) : (
              'Verificar carga'
            )}
          </button>

          {/* Feedback */}
          {estadoVerificar === 'ok' && (
            <div className="upload-feedback upload-feedback--ok">
              <span>✓</span> {mensajeVerificar}
            </div>
          )}
          {estadoVerificar === 'error' && (
            <div className="upload-feedback upload-feedback--error">
              <span>✕</span> {mensajeVerificar}
            </div>
          )}

          {/* Resultados */}
          {resultadoVerificar && (
            <div className="verify-results">
              <div className="verify-stats">
                <div className="verify-stat verify-stat--ok">
                  <span className="verify-stat__value">{resultadoVerificar.renglonesOk}</span>
                  <span className="verify-stat__label">OK</span>
                </div>
                <div className="verify-stat verify-stat--error">
                  <span className="verify-stat__value">{resultadoVerificar.renglonesFaltantes}</span>
                  <span className="verify-stat__label">Faltantes</span>
                </div>
                <div className="verify-stat verify-stat--warn">
                  <span className="verify-stat__value">{resultadoVerificar.renglonesMalMonto}</span>
                  <span className="verify-stat__label">Monto mal</span>
                </div>
                <div className="verify-stat verify-stat--warn">
                  <span className="verify-stat__value">{resultadoVerificar.renglonesMalCotizado}</span>
                  <span className="verify-stat__label">Cotizado mal</span>
                </div>
              </div>

              {resultadoVerificar.incidencias?.length > 0 && (
                <div className="verify-incidencias">
                  {resultadoVerificar.incidencias.map((linea, i) => (
                    <div key={i} className="verify-incidencias__row">{linea}</div>
                  ))}
                </div>
              )}
            </div>
          )}
        </section>

        <aside className="upload-aside">
          <div className="upload-card">
            <h2 className="upload-card__title">¿Qué hace la verificación?</h2>
            <p className="upload-card__subtitle">
              Compara, fila por fila, lo que hay en el Excel contra lo ya guardado en
              la base de datos. No importa datos nuevos, solo audita.
            </p>
            <ul style={{ margin: 0, paddingLeft: 18, fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--text-dim)', lineHeight: 1.8 }}>
              <li>Licitaciones o renglones que están en el Excel pero no en la base.</li>
              <li>Riesgos de la columna <code>Riesgo</code> que no se reconocen.</li>
              <li>Montos adjudicados o cotizados que difieren entre el Excel y la base.</li>
              <li>Clientes que difieren para la misma licitación.</li>
            </ul>
          </div>
        </aside>
      </div>
    </div>
  )
}
