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

                    Integer CotizadoCosto1Header = headerGestor.getHeaderIndex("CotizadoCosto1");
                    Integer CotizadoCosto2Header = headerGestor.getHeaderIndex("CotizadoCosto2");

                    List<Integer> indicesRiesgoCosto = null;

                    if(CotizadoCosto1Header != null && CotizadoCosto2Header != null) {
                        indicesRiesgoCosto = List.of(
                                CotizadoCosto1Header,
                                CotizadoCosto2Header
                        );
                    }

                    Integer AdjudicadoCosto1Header = headerGestor.getHeaderIndex("AdjudicadoCosto1");
                    Integer AdjudicadoCosto2Header = headerGestor.getHeaderIndex("AdjudicadoCosto2");
                    Integer AdjudicadoCosto3Header = headerGestor.getHeaderIndex("AdjudicadoCosto3");

                    // Se agregan las 3 posiciones siempre (aunque alguna columna no exista en
                    // este Excel, en cuyo caso queda null) para que el índice de la lista se
                    // corresponda 1 a 1 con la posición del riesgo en la fila: posición 0 ->
                    // AdjudicadoCosto1, 1 -> AdjudicadoCosto2, 2 -> AdjudicadoCosto3. El consumidor
                    // ya chequea null antes de usar cada valor. Arrays.asList (a diferencia de
                    // List.of) sí admite elementos null.
                    List<Integer> indicesAdjudicadoCosto = new ArrayList<>(Arrays.asList(
                            AdjudicadoCosto1Header, AdjudicadoCosto2Header, AdjudicadoCosto3Header
                    ));

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
     * coincida EXACTAMENTE en riesgo + fecha + status + motivo + montoCotizado
     * + montoAdjudicado. Si existe, el registro ya está cargado tal cual en
     * el sistema (caso 1: mismo dato repetido) y no debe volver a insertarse
     * ni modificarse.
     */
    private LicitacionRiesgo buscarLicitacionRiesgoExistente(Licitacion licitacion, Riesgo riesgo, Date fecha, Status status, String motivo, Double montoCotizado, Double montoAdjudicado) {

        for (LicitacionRiesgo existente : licitacion.getRiesgosAsignados()) {

            if (!coincideRiesgoFechaStatusMotivo(existente, riesgo, fecha, status, motivo)) {
                continue;
            }

            if (mismoMonto(existente.getMontoCotizado(), montoCotizado)
                    && mismoMonto(existente.getMontoAdjudicado(), montoAdjudicado)) {
                return existente;
            }
        }

        return null;
    }

    /**
     * Busca, dentro de los riesgos ya asignados a la licitación, uno que
     * coincida en riesgo + fecha + status + motivo sin importar los montos.
     * Se usa para el caso 2: dos renglones "iguales" (mismo concepto) pero
     * con montoCotizado y/o montoAdjudicado distintos, que deben sumarse en
     * un único registro en lugar de generar un duplicado.
     */
    private LicitacionRiesgo buscarLicitacionRiesgoParcial(Licitacion licitacion, Riesgo riesgo, Date fecha, Status status, String motivo) {

        for (LicitacionRiesgo existente : licitacion.getRiesgosAsignados()) {

            if (coincideRiesgoFechaStatusMotivo(existente, riesgo, fecha, status, motivo)) {
                return existente;
            }
        }

        return null;
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
                if(indicesRiesgoCosto != null) {
                    if (indiceRiesgo < indicesRiesgoCosto.size()) {
                        Integer indexCotizadoCosto = indicesRiesgoCosto.get(indiceRiesgo);

                        if (indexCotizadoCosto != null) {
                            montoCotizadoCalculado = lectorCeldas.leerComoDouble(row.getCell(indexCotizadoCosto));
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

                // Caso 1: ya existe un renglón EXACTAMENTE igual (riesgo+fecha+status+motivo+
                // montoCotizado+montoAdjudicado) -> ya está cargado, se omite para no duplicar.
                LicitacionRiesgo licitacionRiesgoExacto = this.buscarLicitacionRiesgoExistente(licitacion, riesgo, fechaRiesgo, status, motivo_str, montoCotizadoCalculado, montoAdjudicadoCalculado);

                if (licitacionRiesgoExacto != null) {
                    System.out.println("#### Renglón ya cargado exactamente igual (incluidos los montos) — se omite para evitar duplicado.");
                    indiceRiesgo++;
                    continue;
                }

                // Caso 2: existe un renglón con el mismo riesgo+fecha+status+motivo pero con
                // montos distintos -> se suman los montos en ese mismo renglón en vez de crear uno nuevo.
                LicitacionRiesgo licitacionRiesgoParcial = this.buscarLicitacionRiesgoParcial(licitacion, riesgo, fechaRiesgo, status, motivo_str);
                boolean esRenglonNuevo = (licitacionRiesgoParcial == null);

                LicitacionRiesgo licitacionRiesgo;

                if (esRenglonNuevo) {
                    licitacionRiesgo = new LicitacionRiesgo();
                    licitacionRiesgo.setRiesgo(riesgo);
                    licitacionRiesgo.setFecha(fechaRiesgo);
                    licitacionRiesgo.setLicitacion(licitacion);
                    licitacionRiesgo.setMontoCotizado(montoCotizadoCalculado);
                    licitacionRiesgo.setMontoAdjudicado(montoAdjudicadoCalculado);
                } else {
                    licitacionRiesgo = licitacionRiesgoParcial;
                    System.out.println("#### Renglón con mismo riesgo+fecha+status+motivo pero monto distinto — se suman los montos.");
                    licitacionRiesgo.setMontoCotizado(this.sumarMontos(licitacionRiesgo.getMontoCotizado(), montoCotizadoCalculado));
                    licitacionRiesgo.setMontoAdjudicado(this.sumarMontos(licitacionRiesgo.getMontoAdjudicado(), montoAdjudicadoCalculado));
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
     * Un renglón "esperado" según el Excel: riesgo + fecha + status + motivo,
     * con los montos ya sumados si varias filas del Excel comparten esa misma
     * clave (mismo criterio que buscarLicitacionRiesgoParcial/sumarMontos en
     * el insertor), y la lista de filas que aportaron a esa suma, para poder
     * reportarlas todas juntas en un solo mensaje.
     */
    private static class GrupoRenglonEsperado {
        List<Integer> filas = new ArrayList<>();
        Riesgo riesgo;
        Date fecha;
        String status;
        String motivo;
        Double montoAdjEsperado;
        Double montoCotEsperado;
    }

    /** Todo lo que el Excel espera para una compulsa: sus renglones agrupados y el cliente declarado. */
    private static class CompulsaEsperada {
        String numero;
        String anio;
        String clienteExcel;
        List<GrupoRenglonEsperado> grupos = new ArrayList<>();
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
            Integer idxAdjCosto1       = hg.getHeaderIndex("AdjudicadoCosto1");
            Integer idxAdjCosto2       = hg.getHeaderIndex("AdjudicadoCosto2");
            Integer idxAdjCosto3       = hg.getHeaderIndex("AdjudicadoCosto3");
            Integer idxCotCosto1       = hg.getHeaderIndex("CotizadoCosto1");
            Integer idxCotCosto2       = hg.getHeaderIndex("CotizadoCosto2");

            // Igual que en el insertor: 3 posiciones fijas (una por riesgo posible en la
            // fila), con null donde la columna no exista en este Excel. Antes faltaba
            // AdjudicadoCosto3 acá, así que el tercer riesgo de una fila (posición 2)
            // siempre caía al monto general de la fila en vez de a su columna específica,
            // generando falsos [MONTO DISTINTO].
            List<Integer> indicesAdjCosto = new ArrayList<>(Arrays.asList(idxAdjCosto1, idxAdjCosto2, idxAdjCosto3));

            List<Integer> indicesCotCosto = new ArrayList<>();
            if (idxCotCosto1 != null) indicesCotCosto.add(idxCotCosto1);
            if (idxCotCosto2 != null) indicesCotCosto.add(idxCotCosto2);

            System.out.println("--- INICIANDO AUDITORÍA DE DATOS ---");

            int filaExcel      = 1;
            int renglonesFaltantes = 0;
            int renglonesMalMonto  = 0;
            int renglonesMalCotizado = 0;
            int renglonOk      = 0;

            // ------------------------------------------------------------------------
            // PASE 1: recorrer el Excel una sola vez y agrupar, por compulsa, los
            // renglones esperados por riesgo+fecha+status+motivo, sumando los montos
            // de todas las filas que compartan esa clave (mismo criterio que usa el
            // insertor para fusionar duplicados: buscarLicitacionRiesgoParcial +
            // sumarMontos). Sin esto, dos filas del Excel que se fusionan en un solo
            // renglón de la base se comparaban una por una contra el total ya sumado
            // y siempre daban "[MONTO DISTINTO]" aunque la fusión fuera correcta.
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
                        montoCotEsperado = lectorCeldas.leerComoDouble(row.getCell(indicesCotCosto.get(indiceToken)));
                    }
                    if (montoCotEsperado == null) montoCotEsperado = montoCotExcel;

                    // Buscar un grupo existente en esta compulsa con la misma clave
                    // riesgo+fecha+status+motivo para sumar en vez de duplicar.
                    GrupoRenglonEsperado grupo = null;
                    for (GrupoRenglonEsperado g : compulsa.grupos) {
                        boolean mismoRiesgo = g.riesgo.getId().equals(riesgoEsperado.getId());
                        boolean mismaFecha  = (g.fecha == null && fechaExcel == null)
                                || (g.fecha != null && fechaExcel != null && g.fecha.compareTo(fechaExcel) == 0);
                        boolean mismoStatus = LimpiadorTexto.limpiarTildes(g.status).equals(LimpiadorTexto.limpiarTildes(statusExcel));
                        boolean mismoMotivo = LimpiadorTexto.limpiarTildes(g.motivo).equals(LimpiadorTexto.limpiarTildes(motivoExcelCap));
                        if (mismoRiesgo && mismaFecha && mismoStatus && mismoMotivo) {
                            grupo = g;
                            break;
                        }
                    }

                    if (grupo == null) {
                        grupo = new GrupoRenglonEsperado();
                        grupo.riesgo = riesgoEsperado;
                        grupo.fecha = fechaExcel;
                        grupo.status = statusExcel;
                        grupo.motivo = motivoExcelCap;
                        grupo.montoAdjEsperado = montoAdjEsperado;
                        grupo.montoCotEsperado = montoCotEsperado;
                        grupo.filas.add(filaExcel);
                        compulsa.grupos.add(grupo);
                    } else {
                        grupo.montoAdjEsperado = this.sumarMontos(grupo.montoAdjEsperado, montoAdjEsperado);
                        grupo.montoCotEsperado = this.sumarMontos(grupo.montoCotEsperado, montoCotEsperado);
                        grupo.filas.add(filaExcel);
                    }

                    indiceToken++;
                }
            }

            // ------------------------------------------------------------------------
            // PASE 2: por cada compulsa, resolver la licitación una sola vez y comparar
            // cada grupo (ya con los montos sumados) contra los renglones de la base.
            // ------------------------------------------------------------------------
            for (CompulsaEsperada compulsa : compulsasEsperadas.values()) {

                String filasCompulsa = compulsa.grupos.stream()
                        .flatMap(g -> g.filas.stream())
                        .distinct()
                        .sorted()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(","));

                Licitacion licitacion = this.entidadLicitacionRepository.findByNumeroCompulsaAndAnio(compulsa.numero, compulsa.anio).orElse(null);
                if (licitacion == null) {
                    String msg = "[FALTA LICITACION] Filas " + filasCompulsa + " | Compulsa: " + compulsa.numero;
                    System.out.println(msg);
                    incidencias.add(msg);
                    renglonesFaltantes += compulsa.grupos.size();
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
                    renglonesFaltantes += compulsa.grupos.size();
                    continue;
                }

                // Copia mutable para no volver a matchear el mismo renglón de la base
                // contra dos grupos distintos del Excel.
                List<LicitacionRiesgo> riesgosDisponibles = new ArrayList<>(riesgosEnDB);

                for (GrupoRenglonEsperado grupo : compulsa.grupos) {

                    LicitacionRiesgo matchMetadatos = null;
                    LicitacionRiesgo matchCompleto  = null;

                    for (LicitacionRiesgo lr : riesgosDisponibles) {
                        boolean mismoRiesgo = lr.getRiesgo() != null && lr.getRiesgo().getId().equals(grupo.riesgo.getId());
                        // Comparación normalizada (sin tildes, mayúsculas): el status se guarda
                        // tal cual viene del Excel en cada import, así que "Desistida" y "DESISTIDA"
                        // pueden convivir en la base para el mismo concepto.
                        boolean mismoStatus = lr.getStatus() != null
                                && LimpiadorTexto.limpiarTildes(lr.getStatus().getDetalle()).equals(LimpiadorTexto.limpiarTildes(grupo.status));
                        boolean mismaFecha  = lr.getFecha()   != null && grupo.fecha != null && lr.getFecha().compareTo(grupo.fecha) == 0;
                        String  motivoDB    = lr.getMotivo()  == null ? "" : lr.getMotivo();
                        boolean mismoMotivo = LimpiadorTexto.limpiarTildes(motivoDB).equals(LimpiadorTexto.limpiarTildes(grupo.motivo));

                        if (!mismoRiesgo || !mismoStatus || !mismaFecha || !mismoMotivo) continue;

                        // Metadatos ok — verificar montos, ya sumados si el grupo venía de varias filas
                        boolean adjOk = grupo.montoAdjEsperado == null
                                ? (lr.getMontoAdjudicado() == null)
                                : (lr.getMontoAdjudicado() != null && Math.abs(lr.getMontoAdjudicado() - grupo.montoAdjEsperado) < 0.01);

                        boolean cotOk = grupo.montoCotEsperado == null
                                ? (lr.getMontoCotizado() == null)
                                : (lr.getMontoCotizado() != null && Math.abs(lr.getMontoCotizado() - grupo.montoCotEsperado) < 0.01);

                        if (adjOk && cotOk) {
                            matchCompleto = lr;
                            break;
                        } else if (matchMetadatos == null) {
                            matchMetadatos = lr; // guardar el primero con metadatos ok para reporte
                        }
                    }

                    String filasGrupo = grupo.filas.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));

                    if (matchCompleto != null) {
                        riesgosDisponibles.remove(matchCompleto);
                        renglonOk++;
                    } else if (matchMetadatos != null) {
                        riesgosDisponibles.remove(matchMetadatos);
                        String adjDB = matchMetadatos.getMontoAdjudicado() == null ? "null" : matchMetadatos.getMontoAdjudicado().toString();
                        String cotDB = matchMetadatos.getMontoCotizado()   == null ? "null" : matchMetadatos.getMontoCotizado().toString();
                        String msg = "[MONTO DISTINTO] Filas " + filasGrupo
                                + " | Compulsa: " + compulsa.numero
                                + " | Riesgo: " + grupo.riesgo.getDetalle()
                                + (grupo.filas.size() > 1 ? " | (suma de " + grupo.filas.size() + " filas)" : "")
                                + " | AdjExcel: " + grupo.montoAdjEsperado + " -> DB: " + adjDB
                                + " | CotExcel: " + grupo.montoCotEsperado + " -> DB: " + cotDB;
                        System.out.println(msg);
                        incidencias.add(msg);
                        if (grupo.montoAdjEsperado != null && matchMetadatos.getMontoAdjudicado() != null
                                && Math.abs(matchMetadatos.getMontoAdjudicado() - grupo.montoAdjEsperado) >= 0.01) {
                            renglonesMalMonto++;
                        }
                        if (grupo.montoCotEsperado != null && matchMetadatos.getMontoCotizado() != null
                                && Math.abs(matchMetadatos.getMontoCotizado() - grupo.montoCotEsperado) >= 0.01) {
                            renglonesMalCotizado++;
                        }
                    } else {
                        String msg = "[RENGLON FALTANTE] Filas " + filasGrupo
                                + " | Compulsa: " + compulsa.numero
                                + " | Riesgo: " + grupo.riesgo.getDetalle()
                                + " | Status: " + grupo.status
                                + " | Motivo: '" + grupo.motivo + "'"
                                + " | Fecha: " + grupo.fecha
                                + " -> NO encontrado en BD";
                        System.out.println(msg);
                        incidencias.add(msg);
                        renglonesFaltantes++;
                    }
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
