package raiz.componentes;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raiz.Repositories.*;
import raiz.dominio.*;
import raiz.dtos.output.VerificacionCargaResultadoDtoOutput;
import raiz.dtos.output.ImportacionResultadoDtoOutput;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.*;

// header = [Seccion, Mes, Fecha, Numero, Cliente, Riesgo, Status, Motivo, AdjudicadoA, MontoAdjudicado]

@Service
public class InsertorDatos {

    @Value("#{'${excel.header}'.replace('[','').replace(']','').split('\\s*,\\s*')}")
    private List<String> headersTemplate;

    @Value("#{'${excel.renglon.riesgo}'.replace('[','').replace(']','').split('\\s*,\\s*')}")
    private List<String> renglonRiesgos;

    private final IClienteRepository clienteRepository;
    private final IEntidadAdjudicada entidadAdjudicadaRepository;
    private final ILicitacion entidadLicitacionRepository;
    private final IMes entidadMesRepository;
    private final IRiesgo entidadRiesgoRepository;
    private final IStatus entidadStatusRepository;
    private final ILicitacionRiesgoRepository licitacionRiesgoRepository;
    private final ITipoAdjudicacion entidadTipoAdjudicacionRepository;
    private final IRamoRepository entidadRamoRepository;
    private final IMonedaRepository entidadMonedaRepository;
    private final LectorCeldas lectorCeldas = new LectorCeldas();
    private GestorRiesgos gestorRiesgos;

    public InsertorDatos(IClienteRepository clienteRepository, IEntidadAdjudicada entidadAdjudicadaRepository, ILicitacion entidadLicitacionRepository, IMes entidadMesRepository, IRiesgo entidadRiesgoRepository, IStatus entidadStatusRepository, ILicitacionRiesgoRepository licitacionRiesgoRepository, ITipoAdjudicacion entidadTipoAdjudicacionRepository, IRamoRepository entidadRamoRepository, IMonedaRepository monedaRepository) {

        this.clienteRepository = clienteRepository;
        this.entidadAdjudicadaRepository = entidadAdjudicadaRepository;
        this.entidadLicitacionRepository = entidadLicitacionRepository;
        this.entidadMesRepository = entidadMesRepository;
        this.entidadRiesgoRepository = entidadRiesgoRepository;
        this.entidadStatusRepository = entidadStatusRepository;
        this.licitacionRiesgoRepository = licitacionRiesgoRepository;
        this.entidadTipoAdjudicacionRepository = entidadTipoAdjudicacionRepository;
        this.entidadRamoRepository = entidadRamoRepository;
        this.entidadMonedaRepository = monedaRepository;
    }

    @PostConstruct
    public void init() {
        this.gestorRiesgos = new GestorRiesgos(this.entidadRiesgoRepository, this.renglonRiesgos);
    }

    @Transactional
    public ImportacionResultadoDtoOutput importarDesdeExcel(String rutaArchivo) {

        List<String> errores = new ArrayList<>();
        int filasProcesadas = 0;
        int filasOk = 0;

        try (FileInputStream fis = new FileInputStream(rutaArchivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

        //Primera hoja del excel
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            Row headerRow = rows.next();

            List<String> headers = new ArrayList<>();

            for(int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell celda = headerRow.getCell(i);

                if (celda != null) {
                    headers.add(celda.getStringCellValue());
                } else {
                    headers.add("");
                }
            }

            HeaderGestor headerGestor = new HeaderGestor(headers, this.headersTemplate);

            this.almacenarTiposBase();

            int filaExcel = 1; // fila 1 = encabezado

            while (rows.hasNext()) {

                Row currentRow = rows.next();
                filaExcel++;
                filasProcesadas++;

                // Cada fila se procesa de forma aislada: si una fila falla (por el motivo
                // que sea: constraint de base, dato mal formado, etc.) se registra el error
                // y se sigue con la siguiente, en vez de abortar todo el resto del Excel en
                // silencio como pasaba antes.
                try {
                    // 1. Obtener y verificar el índice de Moneda
                    Integer indexMoneda = headerGestor.getHeaderIndex("moneda");
                    Moneda moneda = null;
                    if (indexMoneda != null) {
                        moneda = this.almacenarMoneda(currentRow, indexMoneda);
                    }

// 2. Obtener y verificar el índice de Ramo
                    Integer indexRamo = headerGestor.getHeaderIndex("ramo");
                    Ramo ramo = null;
                    if (indexRamo != null) {
                        ramo = this.almacenarRamo(currentRow, indexRamo);
                    }

// 3. Obtener y verificar el índice de Riesgo (depende de que 'ramo' también se haya procesado)
                    Integer indexRiesgo = headerGestor.getHeaderIndex("riesgo");
                    if (indexRiesgo != null) {
                        this.almacenarRiesgo(currentRow, indexRiesgo, ramo);
                    }

// 4. Obtener y verificar el índice de Fecha/Mes
                    Integer indexFecha = headerGestor.getHeaderIndex("Fecha");
                    Mes mes = null;
                    if (indexFecha != null) {
                        mes = this.almacenarMes(currentRow, indexFecha);
                    }

// 5. Obtener y verificar el índice de Cliente
                    Integer indexCliente = headerGestor.getHeaderIndex("cliente");
                    Cliente cliente = null;
                    if (indexCliente != null) {
                        cliente = this.almacenarCliente(currentRow, indexCliente);
                    }

// 6. Obtener y verificar el índice de Adjudicado
                    Integer indexAdjudicado = headerGestor.getHeaderIndex("adjudicadoA");
                    EntidadAdjudicada adjudicada = null;
                    if (indexAdjudicado != null) {
                        adjudicada = this.almacenarAdjudicadoA(currentRow, indexAdjudicado);
                    }

// 7. Obtener y verificar el índice de Status
                    Integer indexStatus = headerGestor.getHeaderIndex("status");
                    Status status = null;
                    if (indexStatus != null) {
                        status = this.almacenarStatus(currentRow, indexStatus);
                    }

                    Integer indexNumeroCompulsa = headerGestor.getHeaderIndex("Numero");
                    Integer indexMotivo = headerGestor.getHeaderIndex("Motivo");
                    Integer indexEstadoMotivo = headerGestor.getHeaderIndex("estadoMotivo");
                    Integer indexMontoAdjudicado = headerGestor.getHeaderIndex("MontoAdjudicado");
                    Integer indexMontoCotizado = headerGestor.getHeaderIndex("MontoCotizado");

                    // Búsqueda dinámica: no hay límite fijo de CotizadoCostoN/AdjudicadoCostoN.
                    // El tamaño de cada lista es el N más alto que exista en ESTE Excel puntual;
                    // si un N intermedio falta, esa posición queda null (el consumidor ya
                    // chequea null antes de usar cada valor), para que el índice de la lista se
                    // siga correspondiendo 1 a 1 con la posición del riesgo en la fila.
                    List<Integer> indicesRiesgoCosto = headerGestor.getIndicesPorPrefijoNumerado("CotizadoCosto");
                    List<Integer> indicesAdjudicadoCosto = headerGestor.getIndicesPorPrefijoNumerado("AdjudicadoCosto");

                    IndicesLicitacion indicesLicitacion = new IndicesLicitacion(indexNumeroCompulsa, indexRiesgo, indexFecha, indexMotivo, indexEstadoMotivo, indexMontoAdjudicado, indexMontoCotizado, indicesRiesgoCosto, indicesAdjudicadoCosto);

                    this.almacenarLicitacion(currentRow, mes, cliente, moneda, status, adjudicada, indicesLicitacion);

                    filasOk++;

                } catch (Exception filaEx) {

                    String numeroParaLog = null;
                    try {
                        Integer indexNumeroCompulsa = headerGestor.getHeaderIndex("Numero");
                        if (indexNumeroCompulsa != null) {
                            numeroParaLog = lectorCeldas.leerComoTexto(currentRow, indexNumeroCompulsa);
                        }
                    } catch (Exception ignorada) {
                        // Si ni siquiera se puede leer el número de compulsa para el mensaje,
                        // se reporta sin ese dato en vez de perder el error original.
                    }

                    String msg = "Fila " + filaExcel
                            + (numeroParaLog != null ? " | Compulsa: " + numeroParaLog : "")
                            + " | Error: " + filaEx.getMessage();

                    System.err.println("#### " + msg);
                    filaEx.printStackTrace();
                    errores.add(msg);
                }
            }

            System.out.println("¡Importación finalizada! Filas OK: " + filasOk + " / " + filasProcesadas);

        } catch (Exception e) {

            System.err.println("Error al procesar el Excel: " + e.getMessage());

            e.printStackTrace();

            errores.add("Error general al procesar el archivo: " + e.getMessage());
        }

        return ImportacionResultadoDtoOutput.builder()
                .exitosa(errores.isEmpty())
                .filasProcesadas(filasProcesadas)
                .filasOk(filasOk)
                .filasConError(errores.size())
                .errores(errores)
                .build();
    }

    private Cliente almacenarCliente(Row row, Integer indexCliente) {

        String detalle = lectorCeldas.leerCeldaRecortada(row, indexCliente);

        Cliente cliente = this.clienteRepository.findByDetalle(detalle);

        if (cliente == null) {

            cliente = this.clienteRepository.save(new Cliente(detalle));

        }

        return cliente;

    }

    private Mes almacenarMes(Row row, Integer indexFecha) {

        java.sql.Date fecha = lectorCeldas.leerFecha(row, indexFecha);

        if (fecha == null) {
            return null;
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(fecha);

        String[] nombresMeses = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };

        String detalle = nombresMeses[calendar.get(java.util.Calendar.MONTH)];

        Mes mes = this.entidadMesRepository.findByDetalle(detalle);

        if (mes == null) {

            mes = this.entidadMesRepository.save(new Mes(detalle));

        }

        return mes;

    }

    private Ramo almacenarRamo(Row row, Integer indexRamo) {

        String detalle = lectorCeldas.leerCeldaRecortada(row, indexRamo);

        Ramo ramo = this.entidadRamoRepository.findByDetalle(detalle);

        if (ramo == null) {
            ramo = this.entidadRamoRepository.save(new Ramo(detalle));
        }

        return ramo;
    }

    private void asociarRiesgoAlRamo(Riesgo riesgo, Ramo ramo) {

        if (riesgo.getRamo() != null && riesgo.getRamo().equals(ramo)) {
            return;
        }

        riesgo.setRamo(ramo);

        this.entidadRiesgoRepository.save(riesgo);
    }

    private void almacenarRiesgo(Row row, Integer indexRiesgo, Ramo ramo) {

        System.out.println("#### Cargando riesgo");

        String renglonRiesgo = lectorCeldas.leerComoTexto(row, indexRiesgo);

        List<String> tokens = this.gestorRiesgos.obtenerTokens(renglonRiesgo);

        for (String token : tokens) {

            if (token.equalsIgnoreCase("SEGURO TECNICO")) {
                continue;
            }

            Riesgo riesgo = this.gestorRiesgos.resolverRiesgo(token);

            if (riesgo == null) {

                String nombreOficial = this.gestorRiesgos.resolverNombreOficial(token);

                if (nombreOficial == null) {
                    continue;
                }

                long id = (long) (this.renglonRiesgos.indexOf(nombreOficial) + 1);

                if (id == 0L) {
                    continue;
                }

                riesgo = new Riesgo(id, nombreOficial);
            }

            this.asociarRiesgoAlRamo(riesgo, ramo);
        }
    }

    private Status almacenarStatus(Row row, Integer indexStatus) {

        String detalle = lectorCeldas.leerCeldaRecortada(row, indexStatus);

        Status status = this.entidadStatusRepository.findByDetalle(detalle);

        if (status == null) {

            status = this.entidadStatusRepository.save(new Status(detalle));

        }

        return status;

    }

    private Moneda almacenarMoneda(Row row, Integer indexMoneda) {
        String detalle = lectorCeldas.leerCeldaRecortada(row, indexMoneda);

        Moneda moneda = this.entidadMonedaRepository.findByDetalle(detalle);

        if (moneda == null) {

            moneda = this.entidadMonedaRepository.save(new Moneda(detalle));

        }

        return moneda;
    }

    private EntidadAdjudicada almacenarAdjudicadoA(Row row, Integer indexAdjudicadoA) {

        String detalle = lectorCeldas.leerCeldaRecortada(row, indexAdjudicadoA);

        EntidadAdjudicada entidad = this.entidadAdjudicadaRepository.findByDetalle(detalle);

        if (entidad == null) {

            entidad = this.entidadAdjudicadaRepository.save(new EntidadAdjudicada(detalle));

        }

        return entidad;

    }

    private void almacenarTiposBase() {

        if (!this.entidadTipoAdjudicacionRepository.existsByDetalle("DEJADA SIN EFECTO")) {

            this.entidadTipoAdjudicacionRepository.save(new TipoAdjudicacion("DEJADA SIN EFECTO"));

            this.entidadTipoAdjudicacionRepository.save(new TipoAdjudicacion("CON MONTO"));

        }

    }

    /**
     * Compara riesgo + fecha + status + motivo entre un LicitacionRiesgo ya
     * asignado a la licitación y los datos de la fila que se está procesando.
     * No compara montos: es la base tanto para detectar duplicados exactos
     * como renglones "parciales" (mismo concepto, distinto monto).
     */
    private boolean coincideRiesgoFechaStatusMotivo(LicitacionRiesgo existente, Riesgo riesgo, Date fecha, Status status, String motivo) {

        boolean mismoRiesgo = existente.getRiesgo() != null
                && riesgo != null
                && existente.getRiesgo().getId().equals(riesgo.getId());

        boolean mismoStatus = (existente.getStatus() == null && status == null)
                || (existente.getStatus() != null && status != null
                    && existente.getStatus().getId().equals(status.getId()));

        boolean mismaFecha = (existente.getFecha() == null && fecha == null)
                || (existente.getFecha() != null && fecha != null
                    && existente.getFecha().compareTo(fecha) == 0);

        String motivoExistente = existente.getMotivo() == null ? "" : existente.getMotivo();
        String motivoNuevo    = motivo == null ? "" : motivo;
        boolean mismoMotivo   = motivoExistente.equals(motivoNuevo);

        return mismoRiesgo && mismoStatus && mismaFecha && mismoMotivo;
    }

    private boolean mismoMonto(Double existente, Double nuevo) {

        if (existente == null && nuevo == null) {
            return true;
        }

        if (existente == null || nuevo == null) {
            return false;
        }

        return Math.abs(existente - nuevo) < 0.01;
    }

    /**
     * Busca, dentro de los riesgos ya asignados a la licitación, uno que
     * coincida en riesgo + fecha + status + motivo (sin importar los montos:
     * eso se resuelve a nivel de componentes, ver tieneComponenteIgual).
     */
    private LicitacionRiesgo buscarLicitacionRiesgoPorMetadatos(Licitacion licitacion, Riesgo riesgo, Date fecha, Status status, String motivo) {

        for (LicitacionRiesgo existente : licitacion.getRiesgosAsignados()) {

            if (coincideRiesgoFechaStatusMotivo(existente, riesgo, fecha, status, motivo)) {
                return existente;
            }
        }

        return null;
    }

    /**
     * true si este renglón ya tiene un componente con exactamente este
     * montoCotizado + montoAdjudicado. Es la clave para que sumar sea
     * idempotente: si el mismo Excel (completo o parcial) se vuelve a
     * cargar, la fila que ya aportó este monto no se vuelve a sumar; si el
     * monto es distinto, es un aporte nuevo y sí se suma.
     */
    private boolean tieneComponenteIgual(LicitacionRiesgo licitacionRiesgo, Double montoCotizado, Double montoAdjudicado) {

        for (LicitacionRiesgoComponente componente : licitacionRiesgo.getComponentes()) {
            if (mismoMonto(componente.getMontoCotizado(), montoCotizado)
                    && mismoMonto(componente.getMontoAdjudicado(), montoAdjudicado)) {
                return true;
            }
        }

        return false;
    }

    /** Recalcula montoCotizado/montoAdjudicado del renglón como la suma de todos sus componentes. */
    private void recalcularTotales(LicitacionRiesgo licitacionRiesgo) {

        Double totalCotizado = null;
        Double totalAdjudicado = null;

        for (LicitacionRiesgoComponente componente : licitacionRiesgo.getComponentes()) {
            totalCotizado = this.sumarMontos(totalCotizado, componente.getMontoCotizado());
            totalAdjudicado = this.sumarMontos(totalAdjudicado, componente.getMontoAdjudicado());
        }

        licitacionRiesgo.setMontoCotizado(totalCotizado);
        licitacionRiesgo.setMontoAdjudicado(totalAdjudicado);
    }

    private Double sumarMontos(Double a, Double b) {

        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        return a + b;
    }

    private void almacenarLicitacion(Row row, Mes mes, Cliente cliente, Moneda moneda, Status status, EntidadAdjudicada adjudicada, IndicesLicitacion indicesLicitacion) {

        Licitacion licitacion;
        String numero_str = lectorCeldas.leerComoTexto(row, indicesLicitacion.getIndexNumeroCompulsa());
        Date fecha = lectorCeldas.leerFecha(row, indicesLicitacion.getIndexFecha());

        if(numero_str == null || fecha == null) {
            System.out.println("#### Numero compulsa y/o fecha invalidos");
            return;
        }

        String anio = String.valueOf(fecha.toLocalDate().getYear());

        System.out.println("#### Procesando licitacion num " +  numero_str);

        if(this.entidadLicitacionRepository.findByNumeroCompulsaAndAnio(numero_str, anio).isPresent()) {

            System.out.println("LICITACION EXISTENTE");

            licitacion = this.entidadLicitacionRepository.findByNumeroCompulsaAndAnio(numero_str, anio).orElse(null);

            if(licitacion == null) {
                System.out.println("#### COMO ENTRAS A ACA ANIMAL");
                return;
            }

        } else {
            licitacion = new Licitacion();
            licitacion.setNumeroCompulsa(numero_str);
            licitacion.setCliente(cliente);
            licitacion.setAnio(anio);
        }

        String riesgos_compactados_str = lectorCeldas.leerComoTexto(row, indicesLicitacion.getIndexRiesgo());

        List<String> riesgos_str = this.gestorRiesgos.obtenerTokens(riesgos_compactados_str);

        int indiceRiesgo = 0;

        for(String riesgo_str : riesgos_str){

            if(riesgo_str.equalsIgnoreCase("SEGURO TECNICO")){
                indiceRiesgo++;
                continue;
            }

            Riesgo riesgo = this.gestorRiesgos.resolverRiesgo(riesgo_str);

            if(riesgo != null) {

                Date fechaRiesgo = lectorCeldas.leerFecha(row, indicesLicitacion.getIndexFecha());

                // El motivo forma parte de la clave de duplicado, así que se lee antes de buscar/crear el renglón
                Integer indexMotivo = indicesLicitacion.getIndexMotivo();

                Cell motivoCelda = (indexMotivo != null) ? row.getCell(indexMotivo) : null;

                String motivo_str = null;

                if (motivoCelda == null || motivoCelda.getCellType() == CellType.BLANK) {
                    System.out.println("El motivo de la compulsa está vacío.");
                } else {
                    motivo_str = LimpiadorTexto.capitalizar(lectorCeldas.leerCeldaRecortada(row, indexMotivo));
                    System.out.println("Motivo: " + motivo_str);
                }

                // Los montos también forman parte de la clave de duplicado exacto, así que
                // se calculan antes de buscar/crear el renglón.
                Integer indexMontoAdjudicado = indicesLicitacion.getIndexMontoAdjudicado();

//monto adjudicado (general de la fila)
                Cell celdaMontoAdjudicado = (indexMontoAdjudicado != null) ? row.getCell(indexMontoAdjudicado) : null;

                Double montoAdjudicadoCalculado = null;
                TipoAdjudicacion tipoAdjudicacionCalculado;

                if (!lectorCeldas.esCeldaVaciaOInvalida(celdaMontoAdjudicado)) {

// Lógica de monto y tipo
                    if (celdaMontoAdjudicado.getCellType() == CellType.NUMERIC) {
                        montoAdjudicadoCalculado = celdaMontoAdjudicado.getNumericCellValue();
                        tipoAdjudicacionCalculado = this.entidadTipoAdjudicacionRepository.findByDetalle("CON MONTO");
                    } else {
                        tipoAdjudicacionCalculado = this.entidadTipoAdjudicacionRepository.findByDetalle("DEJADA SIN EFECTO");
                    }
                } else {
                    tipoAdjudicacionCalculado = this.entidadTipoAdjudicacionRepository.findByDetalle("DEJADA SIN EFECTO");
                }

                Integer indexMontoCotizado = indicesLicitacion.getIndexMontoCotizado();

                //monto cotizado (general de la fila)
                Double montoCotizadoCalculado = null;

                if (indexMontoCotizado != null) {

                    Cell celdaMontoCotizado = row.getCell(indexMontoCotizado);

                    if (!lectorCeldas.esCeldaVaciaOInvalida(celdaMontoCotizado) && celdaMontoCotizado.getCellType() == CellType.NUMERIC) {
                        montoCotizadoCalculado = celdaMontoCotizado.getNumericCellValue();
                    }
                }

                List<Integer> indicesRiesgoCosto = indicesLicitacion.getIndicesRiesgoCosto();

// monto cotizado (RiesgoCosto1 -> primer riesgo, RiesgoCosto2 -> segundo riesgo)
// Igual que con AdjudicadoCosto: si la columna existe pero la celda de esta fila
// puntual está vacía, se conserva el monto general en vez de pisarlo con null.
                if(indicesRiesgoCosto != null) {
                    if (indiceRiesgo < indicesRiesgoCosto.size()) {
                        Integer indexCotizadoCosto = indicesRiesgoCosto.get(indiceRiesgo);

                        if (indexCotizadoCosto != null) {
                            Double montoCotizadoRiesgo = lectorCeldas.leerComoDouble(row.getCell(indexCotizadoCosto));

                            if (montoCotizadoRiesgo != null) {
                                montoCotizadoCalculado = montoCotizadoRiesgo;
                            }
                        }
                    }
                }

                List<Integer> indicesAdjudicadoCosto = indicesLicitacion.getIndicesAdjudicadoCosto();

                if(indicesAdjudicadoCosto != null) {

                    indicesAdjudicadoCosto.forEach(i -> System.out.println("IndiceAdjudicado: " + i));

// monto adjudicado por riesgo (AdjudicadoCosto1/2/3 -> primer, segundo y tercer riesgo)
                    if (indiceRiesgo < indicesAdjudicadoCosto.size()) {
                        Integer indexAdjudicadoCosto = indicesAdjudicadoCosto.get(indiceRiesgo);

                        if (indexAdjudicadoCosto != null) {
                            Double montoAdjudicadoRiesgo = lectorCeldas.leerComoDouble(row.getCell(indexAdjudicadoCosto));

                            if (montoAdjudicadoRiesgo != null) {
                                montoAdjudicadoCalculado = montoAdjudicadoRiesgo;
                            }
                        }
                    }
                }

                // Buscar el renglón por riesgo+fecha+status+motivo (sin importar montos).
                LicitacionRiesgo licitacionRiesgoExistente = this.buscarLicitacionRiesgoPorMetadatos(licitacion, riesgo, fechaRiesgo, status, motivo_str);
                boolean esRenglonNuevo = (licitacionRiesgoExistente == null);

                LicitacionRiesgo licitacionRiesgo;

                if (esRenglonNuevo) {
                    licitacionRiesgo = new LicitacionRiesgo();
                    licitacionRiesgo.setRiesgo(riesgo);
                    licitacionRiesgo.setFecha(fechaRiesgo);
                    licitacionRiesgo.setLicitacion(licitacion);
                } else {
                    licitacionRiesgo = licitacionRiesgoExistente;
                }

                // El aporte de ESTA fila (este monto puntual) puede ya estar contabilizado si
                // existe un componente idéntico -> se omite para que recargar el mismo Excel
                // (completo o parcial/delta) no sume dos veces. Si no existe, es un aporte
                // nuevo: se agrega como componente y se recalcula el total del renglón.
                if (this.tieneComponenteIgual(licitacionRiesgo, montoCotizadoCalculado, montoAdjudicadoCalculado)) {
                    System.out.println("#### Este aporte (mismo riesgo+fecha+status+motivo+monto) ya estaba contabilizado — se omite para no duplicar.");
                    indiceRiesgo++;
                    continue;
                }

                LicitacionRiesgoComponente nuevoComponente = new LicitacionRiesgoComponente(licitacionRiesgo, montoCotizadoCalculado, montoAdjudicadoCalculado);
                licitacionRiesgo.getComponentes().add(nuevoComponente);
                this.recalcularTotales(licitacionRiesgo);

                if (!esRenglonNuevo) {
                    System.out.println("#### Renglón existente: nuevo aporte detectado, se suma al total.");
                }

                licitacionRiesgo.setStatus(status);
                licitacionRiesgo.setAdjudicada(adjudicada);
                licitacionRiesgo.setMes(mes);
                licitacionRiesgo.setMoneda(moneda);
                licitacionRiesgo.setMotivo(motivo_str);
                licitacionRiesgo.setTipoAdjudicacion(tipoAdjudicacionCalculado);

                Integer indexEstadoMotivo = indicesLicitacion.getIndexEstadoMotivo();

                Cell estadoMotivoCelda = (indexEstadoMotivo != null) ? row.getCell(indexEstadoMotivo) : null;

                if (estadoMotivoCelda == null || estadoMotivoCelda.getCellType() == CellType.BLANK) {
                    System.out.println("El estado del motivo de la compulsa está vacío.");
                } else {
                    String estadoMotivo_str = LimpiadorTexto.capitalizar(lectorCeldas.leerCeldaRecortada(row, indexEstadoMotivo));
                    System.out.println("Estado Motivo: " + estadoMotivo_str);
                    licitacionRiesgo.setEstadoMotivo(estadoMotivo_str);
                }

                if (esRenglonNuevo) {
                    licitacion.getRiesgosAsignados().add(licitacionRiesgo);
                }

                indiceRiesgo++;
            }
        }


        this.entidadLicitacionRepository.save(licitacion);
    }

    /**
     * Un aporte esperado según UNA fila del Excel: riesgo + fecha + status +
     * motivo + el monto puntual de esa fila. A diferencia de una versión
     * anterior, ya no se suman varias filas entre sí acá: cada fila se
     * compara individualmente contra los componentes ya guardados en la
     * base (ver LicitacionRiesgoComponente), que es quien decide si esa
     * fila ya estaba contabilizada o no. Esto hace que la verificación
     * funcione igual con un Excel completo o con uno parcial/delta.
     */
    private static class AporteEsperado {
        int fila;
        Riesgo riesgo;
        Date fecha;
        String status;
        String motivo;
        Double montoAdjEsperado;
        Double montoCotEsperado;
    }

    /** Todo lo que el Excel espera para una compulsa: sus aportes y el cliente declarado. */
    private static class CompulsaEsperada {
        String numero;
        String anio;
        String clienteExcel;
        List<AporteEsperado> aportes = new ArrayList<>();
    }

    public VerificacionCargaResultadoDtoOutput verificarCarga(String rutaArchivo) {

        List<String> incidencias = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(rutaArchivo);
            Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell celda = headerRow.getCell(i);
                headers.add(celda != null ? celda.getStringCellValue() : "");
            }
            HeaderGestor hg = new HeaderGestor(headers, this.headersTemplate);

            Integer idxNumero          = hg.getHeaderIndex("Numero");
            Integer idxCliente         = hg.getHeaderIndex("cliente");
            Integer idxFecha           = hg.getHeaderIndex("Fecha");
            Integer idxRiesgo          = hg.getHeaderIndex("riesgo");
            Integer idxStatus          = hg.getHeaderIndex("status");
            Integer idxMotivo          = hg.getHeaderIndex("Motivo");
            Integer idxAdjudicado      = hg.getHeaderIndex("adjudicadoA");
            Integer idxMontoAdj        = hg.getHeaderIndex("MontoAdjudicado");
            Integer idxMontoCot        = hg.getHeaderIndex("MontoCotizado");

            // Búsqueda dinámica, igual que en el insertor: sin límite fijo de N, y con
            // null en las posiciones intermedias faltantes para no perder el alineamiento
            // con el riesgo correspondiente.
            List<Integer> indicesAdjCosto = hg.getIndicesPorPrefijoNumerado("AdjudicadoCosto");
            List<Integer> indicesCotCosto = hg.getIndicesPorPrefijoNumerado("CotizadoCosto");

            System.out.println("--- INICIANDO AUDITORÍA DE DATOS ---");

            int filaExcel      = 1;
            int renglonesFaltantes = 0;
            int renglonesMalMonto  = 0;
            int renglonesMalCotizado = 0;
            int renglonOk      = 0;

            // ------------------------------------------------------------------------
            // PASE 1: recorrer el Excel una sola vez y juntar, por compulsa, cada fila
            // como un aporte individual (SIN sumar entre filas acá: eso ahora lo
            // resuelve la comparación contra los componentes guardados en la base).
            // ------------------------------------------------------------------------
            Map<String, CompulsaEsperada> compulsasEsperadas = new LinkedHashMap<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                filaExcel++;

                if (idxNumero == null || row.getCell(idxNumero) == null) continue;
                String numExcel = lectorCeldas.leerComoTexto(row, idxNumero);
                if (numExcel == null || numExcel.isBlank()) continue;

                // Igual que en almacenarLicitacion: el numeroCompulsa por sí solo no es único
                // (se repite entre años), así que la clave de agrupación es numero + año.
                Date fechaExcel = idxFecha != null ? lectorCeldas.leerFecha(row, idxFecha) : null;
                String anioExcel = fechaExcel != null ? String.valueOf(fechaExcel.toLocalDate().getYear()) : null;
                String claveCompulsa = numExcel + "#" + (anioExcel == null ? "" : anioExcel);

                CompulsaEsperada compulsa = compulsasEsperadas.computeIfAbsent(claveCompulsa, k -> {
                    CompulsaEsperada nueva = new CompulsaEsperada();
                    nueva.numero = numExcel;
                    nueva.anio = anioExcel;
                    return nueva;
                });

                if (idxCliente != null && compulsa.clienteExcel == null) {
                    compulsa.clienteExcel = lectorCeldas.leerComoTexto(row, idxCliente);
                }

                String riesgosRaw  = idxRiesgo != null ? lectorCeldas.leerComoTexto(row, idxRiesgo) : "";
                List<String> tokens = this.gestorRiesgos.obtenerTokens(riesgosRaw);
                String statusExcel = idxStatus != null ? lectorCeldas.leerCeldaRecortada(row, idxStatus) : "";
                String motivoExcel = idxMotivo != null ? lectorCeldas.leerCeldaRecortada(row, idxMotivo) : "";
                String motivoExcelCap = motivoExcel.isBlank() ? "" : LimpiadorTexto.capitalizar(motivoExcel);

                // Monto adjudicado general de la fila
                Double montoAdjExcel = null;
                if (idxMontoAdj != null) {
                    Cell c = row.getCell(idxMontoAdj);
                    if (!lectorCeldas.esCeldaVaciaOInvalida(c) && c.getCellType() == CellType.NUMERIC) {
                        montoAdjExcel = c.getNumericCellValue();
                    }
                }

                // Monto cotizado general de la fila
                Double montoCotExcel = null;
                if (idxMontoCot != null) {
                    Cell c = row.getCell(idxMontoCot);
                    if (!lectorCeldas.esCeldaVaciaOInvalida(c) && c.getCellType() == CellType.NUMERIC) {
                        montoCotExcel = c.getNumericCellValue();
                    }
                }

                int indiceToken = 0;
                for (String token : tokens) {
                    if (token.equalsIgnoreCase("SEGURO TECNICO")) {
                        indiceToken++; // igual que en el insertor
                        continue;
                    }

                    Riesgo riesgoEsperado = this.gestorRiesgos.resolverRiesgo(token);
                    if (riesgoEsperado == null) {
                        String msg = "[RIESGO NO RECONOCIDO] Fila " + filaExcel + " | Compulsa: " + numExcel + " | Token: " + token;
                        System.out.println(msg);
                        incidencias.add(msg);
                        indiceToken++;
                        continue;
                    }

                    // Montos esperados para este token según su posición (igual que el insertor)
                    Double montoAdjEsperado = null;
                    if (!indicesAdjCosto.isEmpty() && indiceToken < indicesAdjCosto.size()) {
                        Integer indexAdjCosto = indicesAdjCosto.get(indiceToken);
                        if (indexAdjCosto != null) {
                            montoAdjEsperado = lectorCeldas.leerComoDouble(row.getCell(indexAdjCosto));
                        }
                    }
                    if (montoAdjEsperado == null) montoAdjEsperado = montoAdjExcel;

                    Double montoCotEsperado = null;
                    if (!indicesCotCosto.isEmpty() && indiceToken < indicesCotCosto.size()) {
                        Integer indexCotCosto = indicesCotCosto.get(indiceToken);
                        if (indexCotCosto != null) {
                            montoCotEsperado = lectorCeldas.leerComoDouble(row.getCell(indexCotCosto));
                        }
                    }
                    if (montoCotEsperado == null) montoCotEsperado = montoCotExcel;

                    AporteEsperado aporte = new AporteEsperado();
                    aporte.fila = filaExcel;
                    aporte.riesgo = riesgoEsperado;
                    aporte.fecha = fechaExcel;
                    aporte.status = statusExcel;
                    aporte.motivo = motivoExcelCap;
                    aporte.montoAdjEsperado = montoAdjEsperado;
                    aporte.montoCotEsperado = montoCotEsperado;
                    compulsa.aportes.add(aporte);

                    indiceToken++;
                }
            }

            // ------------------------------------------------------------------------
            // PASE 2: por cada compulsa, resolver la licitación una sola vez y, para
            // cada aporte esperado, chequear si ya existe un componente idéntico en el
            // renglón correspondiente de la base (no si el TOTAL coincide: el total
            // puede incluir aportes de otras filas/otros imports que este Excel no
            // tiene por qué repetir).
            // ------------------------------------------------------------------------
            for (CompulsaEsperada compulsa : compulsasEsperadas.values()) {

                String filasCompulsa = compulsa.aportes.stream()
                        .map(a -> a.fila)
                        .distinct()
                        .sorted()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(","));

                Licitacion licitacion = this.entidadLicitacionRepository.findByNumeroCompulsaAndAnio(compulsa.numero, compulsa.anio).orElse(null);
                if (licitacion == null) {
                    String msg = "[FALTA LICITACION] Filas " + filasCompulsa + " | Compulsa: " + compulsa.numero;
                    System.out.println(msg);
                    incidencias.add(msg);
                    renglonesFaltantes += compulsa.aportes.size();
                    continue;
                }

                if (idxCliente != null && compulsa.clienteExcel != null) {
                    String clienteExcel = LimpiadorTexto.limpiarTildes(compulsa.clienteExcel);
                    String clienteDB    = LimpiadorTexto.limpiarTildes(licitacion.getCliente().getDetalle());
                    if (!clienteDB.equals(clienteExcel)) {
                        String msg = "[CLIENTE DISTINTO] Filas " + filasCompulsa + " | Compulsa: " + compulsa.numero
                                + " | Excel: " + compulsa.clienteExcel + " | DB: " + clienteDB;
                        System.out.println(msg);
                        incidencias.add(msg);
                    }
                }

                List<LicitacionRiesgo> riesgosEnDB = this.licitacionRiesgoRepository.findByLicitacion(licitacion.getId());
                if (riesgosEnDB.isEmpty()) {
                    String msg = "[SIN RENGLONES] Filas " + filasCompulsa + " | Compulsa: " + compulsa.numero;
                    System.out.println(msg);
                    incidencias.add(msg);
                    renglonesFaltantes += compulsa.aportes.size();
                    continue;
                }

                for (AporteEsperado aporte : compulsa.aportes) {

                    // Puede haber más de una fila del Excel para el mismo riesgo+fecha+
                    // status+motivo (eso es justamente lo que se suma como componentes
                    // distintos), así que el renglón de la base es siempre el mismo para
                    // todos los aportes que compartan esa clave.
                    LicitacionRiesgo renglonDB = null;
                    for (LicitacionRiesgo lr : riesgosEnDB) {
                        boolean mismoRiesgo = lr.getRiesgo() != null && lr.getRiesgo().getId().equals(aporte.riesgo.getId());
                        // Comparación normalizada (sin tildes, mayúsculas): el status se guarda
                        // tal cual viene del Excel en cada import, así que "Desistida" y "DESISTIDA"
                        // pueden convivir en la base para el mismo concepto.
                        boolean mismoStatus = lr.getStatus() != null
                                && LimpiadorTexto.limpiarTildes(lr.getStatus().getDetalle()).equals(LimpiadorTexto.limpiarTildes(aporte.status));
                        boolean mismaFecha  = lr.getFecha()   != null && aporte.fecha != null && lr.getFecha().compareTo(aporte.fecha) == 0;
                        String  motivoDB    = lr.getMotivo()  == null ? "" : lr.getMotivo();
                        boolean mismoMotivo = LimpiadorTexto.limpiarTildes(motivoDB).equals(LimpiadorTexto.limpiarTildes(aporte.motivo));

                        if (mismoRiesgo && mismoStatus && mismaFecha && mismoMotivo) {
                            renglonDB = lr;
                            break;
                        }
                    }

                    if (renglonDB == null) {
                        String msg = "[RENGLON FALTANTE] Fila " + aporte.fila
                                + " | Compulsa: " + compulsa.numero
                                + " | Riesgo: " + aporte.riesgo.getDetalle()
                                + " | Status: " + aporte.status
                                + " | Motivo: '" + aporte.motivo + "'"
                                + " | Fecha: " + aporte.fecha
                                + " -> NO encontrado en BD";
                        System.out.println(msg);
                        incidencias.add(msg);
                        renglonesFaltantes++;
                        continue;
                    }

                    boolean componenteExacto = this.tieneComponenteIgual(renglonDB, aporte.montoCotEsperado, aporte.montoAdjEsperado);

                    if (componenteExacto) {
                        renglonOk++;
                        continue;
                    }

                    // El renglón existe pero ningún componente tiene exactamente este
                    // monto -> esta fila puntual no está reflejada en la base. Se
                    // reporta por separado si es el cotizado, el adjudicado, o ambos,
                    // buscando entre TODOS los componentes existentes (no solo uno).
                    boolean existeComponenteConAdj = renglonDB.getComponentes().stream()
                            .anyMatch(c -> this.mismoMonto(c.getMontoAdjudicado(), aporte.montoAdjEsperado));
                    boolean existeComponenteConCot = renglonDB.getComponentes().stream()
                            .anyMatch(c -> this.mismoMonto(c.getMontoCotizado(), aporte.montoCotEsperado));

                    String componentesDB = renglonDB.getComponentes().isEmpty()
                            ? "(sin componentes)"
                            : renglonDB.getComponentes().stream()
                                    .map(c -> "cot=" + c.getMontoCotizado() + "/adj=" + c.getMontoAdjudicado())
                                    .collect(java.util.stream.Collectors.joining(" ; "));

                    String msg = "[APORTE FALTANTE] Fila " + aporte.fila
                            + " | Compulsa: " + compulsa.numero
                            + " | Riesgo: " + aporte.riesgo.getDetalle()
                            + " | AdjExcel: " + aporte.montoAdjEsperado
                            + " | CotExcel: " + aporte.montoCotEsperado
                            + " | Componentes en DB: " + componentesDB
                            + " | Total renglón en DB: cot=" + renglonDB.getMontoCotizado() + "/adj=" + renglonDB.getMontoAdjudicado();
                    System.out.println(msg);
                    incidencias.add(msg);

                    if (!existeComponenteConAdj) renglonesMalMonto++;
                    if (!existeComponenteConCot) renglonesMalCotizado++;
                }
            }

            System.out.println("\n--- RESUMEN AUDITORÍA ---");
            System.out.println("Renglones OK              : " + renglonOk);
            System.out.println("Renglones faltantes en BD : " + renglonesFaltantes);
            System.out.println("Renglones monto adj. mal  : " + renglonesMalMonto);
            System.out.println("Renglones monto cot. mal  : " + renglonesMalCotizado);

            int totalErrores = renglonesFaltantes + renglonesMalMonto + renglonesMalCotizado;
            if (totalErrores == 0) {
                System.out.println("VERIFICACION CORRECTA - Todos los renglones coinciden.");
            } else {
                System.out.println("VERIFICACION CON " + totalErrores + " ERROR(ES) - Ver detalle arriba.");
            }

            return VerificacionCargaResultadoDtoOutput.builder()
                    .exitosa(totalErrores == 0)
                    .renglonesOk(renglonOk)
                    .renglonesFaltantes(renglonesFaltantes)
                    .renglonesMalMonto(renglonesMalMonto)
                    .renglonesMalCotizado(renglonesMalCotizado)
                    .incidencias(incidencias)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
