package raiz.componentes;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raiz.Repositories.*;
import raiz.dominio.*;
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
    public void importarDesdeExcel(String rutaArchivo) {

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

            while (rows.hasNext()) {

                Row currentRow = rows.next();
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

                List<Integer> indicesAdjudicadoCosto = new ArrayList<>();

                Integer AdjudicadoCosto1Header = headerGestor.getHeaderIndex("AdjudicadoCosto1");
                Integer AdjudicadoCosto2Header = headerGestor.getHeaderIndex("AdjudicadoCosto2");
                Integer AdjudicadoCosto3Header = headerGestor.getHeaderIndex("AdjudicadoCosto3");

                if(AdjudicadoCosto1Header != null) {
                    indicesAdjudicadoCosto.add(AdjudicadoCosto1Header);
                }
                if(AdjudicadoCosto1Header != null) {
                    indicesAdjudicadoCosto.add(AdjudicadoCosto2Header);
                }
                if(AdjudicadoCosto1Header != null) {
                    indicesAdjudicadoCosto.add(AdjudicadoCosto3Header);
                }

                IndicesLicitacion indicesLicitacion = new IndicesLicitacion(indexNumeroCompulsa, indexRiesgo, indexFecha, indexMotivo, indexEstadoMotivo, indexMontoAdjudicado, indexMontoCotizado, indicesRiesgoCosto, indicesAdjudicadoCosto);

                this.almacenarLicitacion(currentRow, mes, cliente, moneda, status, adjudicada, indicesLicitacion);
            }

            System.out.println("¡Importación completada con éxito!");

        } catch (Exception e) {

            System.err.println("Error al procesar el Excel: " + e.getMessage());

            e.printStackTrace();
        }

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
     * Busca, dentro de los riesgos ya asignados a la licitación, uno que
     * coincida en riesgo + fecha + status. Esto evita crear un
     * LicitacionRiesgo duplicado al reimportar el mismo Excel: si ya existe
     * una coincidencia, se actualiza esa fila en lugar de insertar una nueva.
     */
    private LicitacionRiesgo buscarLicitacionRiesgoExistente(Licitacion licitacion, Riesgo riesgo, Date fecha, Status status, String motivo) {

        for (LicitacionRiesgo existente : licitacion.getRiesgosAsignados()) {

            boolean mismoRiesgo = existente.getRiesgo() != null
                    && riesgo != null
                    && existente.getRiesgo().getId().equals(riesgo.getId());

            boolean mismoStatus = (existente.getStatus() == null && status == null)
                    || (existente.getStatus() != null && status != null
                        && existente.getStatus().getId().equals(status.getId()));

            boolean mismaFecha = (existente.getFecha() == null && fecha == null)
                    || (existente.getFecha() != null && fecha != null
                        && existente.getFecha().compareTo(fecha) == 0);

            // Comparar motivo para distinguir renglones con mismo riesgo+status+fecha pero distinto motivo
            String motivoExistente = existente.getMotivo() == null ? "" : existente.getMotivo();
            String motivoNuevo    = motivo == null ? "" : motivo;
            boolean mismoMotivo   = motivoExistente.equals(motivoNuevo);

            if (mismoRiesgo && mismoStatus && mismaFecha && mismoMotivo) {
                return existente;
            }
        }

        return null;
    }

    private void almacenarLicitacion(Row row, Mes mes, Cliente cliente, Moneda moneda, Status status, EntidadAdjudicada adjudicada, IndicesLicitacion indicesLicitacion) {

        Licitacion licitacion;
        String numero_str = lectorCeldas.leerComoTexto(row, indicesLicitacion.getIndexNumeroCompulsa());

        System.out.println("#### Procesando licitacion num " +  numero_str);

        if(this.entidadLicitacionRepository.findByNumeroCompulsa(numero_str).isPresent()) {

            licitacion = this.entidadLicitacionRepository.findByNumeroCompulsa(numero_str).orElse(null);

            if(licitacion == null) {
                System.out.println("#### COMO ENTRAS A ACA ANIMAL");
                return;
            }

        } else {
            licitacion = new Licitacion();
            licitacion.setNumeroCompulsa(numero_str);
            licitacion.setCliente(cliente);
        }

        String riesgos_compactados_str = lectorCeldas.leerComoTexto(row, indicesLicitacion.getIndexRiesgo());

        List<String> riesgos_str = this.gestorRiesgos.obtenerTokens(riesgos_compactados_str);

        int indiceRiesgo = 0;

        System.out.println("Riesgos encontrados: " + riesgos_str);

        for(String riesgo_str : riesgos_str){

            System.out.println("Riesgo num " + indiceRiesgo + 1 + ": " + riesgo_str);

            if(riesgo_str.equalsIgnoreCase("SEGURO TECNICO")){
                indiceRiesgo++;
                continue;
            }

            Riesgo riesgo = this.gestorRiesgos.resolverRiesgo(riesgo_str);

            if(riesgo != null) {

                Date fechaRiesgo = lectorCeldas.leerFecha(row, indicesLicitacion.getIndexFecha());

                // Cada fila del Excel genera su propio LicitacionRiesgo
                LicitacionRiesgo licitacionRiesgo = new LicitacionRiesgo();

                licitacionRiesgo.setRiesgo(riesgo);
                licitacionRiesgo.setStatus(status);
                licitacionRiesgo.setAdjudicada(adjudicada);
                licitacionRiesgo.setFecha(fechaRiesgo);
                licitacionRiesgo.setMes(mes);
                licitacionRiesgo.setMoneda(moneda);
                licitacionRiesgo.setLicitacion(licitacion);

                Integer indexMotivo = indicesLicitacion.getIndexMotivo();

                Cell motivoCelda = (indexMotivo != null) ? row.getCell(indexMotivo) : null;

                if (motivoCelda == null || motivoCelda.getCellType() == CellType.BLANK) {
                    System.out.println("El motivo de la compulsa está vacío.");
                } else {
                    String motivo_str = LimpiadorTexto.capitalizar(lectorCeldas.leerCeldaRecortada(row, indexMotivo));
                    System.out.println("Motivo: " + motivo_str);
                    licitacionRiesgo.setMotivo(motivo_str);
                }

                Integer indexEstadoMotivo = indicesLicitacion.getIndexEstadoMotivo();

                Cell estadoMotivoCelda = (indexEstadoMotivo != null) ? row.getCell(indexEstadoMotivo) : null;

                if (estadoMotivoCelda == null || estadoMotivoCelda.getCellType() == CellType.BLANK) {
                    System.out.println("El estado del motivo de la compulsa está vacío.");
                } else {
                    String estadoMotivo_str = LimpiadorTexto.capitalizar(lectorCeldas.leerCeldaRecortada(row, indexEstadoMotivo));
                    System.out.println("Estado Motivo: " + estadoMotivo_str);
                    licitacionRiesgo.setEstadoMotivo(estadoMotivo_str);
                }

                Integer indexMontoAdjudicado = indicesLicitacion.getIndexMontoAdjudicado();

//monto adjudicado
                Cell celdaMontoAdjudicado = (indexMontoAdjudicado != null) ? row.getCell(indexMontoAdjudicado) : null;

                if (!lectorCeldas.esCeldaVaciaOInvalida(celdaMontoAdjudicado)) {

// Lógica de monto y tipo
                    if (celdaMontoAdjudicado.getCellType() == CellType.NUMERIC) {
                        licitacionRiesgo.setMontoAdjudicado(celdaMontoAdjudicado.getNumericCellValue());
                        licitacionRiesgo.setTipoAdjudicacion(this.entidadTipoAdjudicacionRepository.findByDetalle("CON MONTO"));
                    } else {
                        licitacionRiesgo.setTipoAdjudicacion(this.entidadTipoAdjudicacionRepository.findByDetalle("DEJADA SIN EFECTO"));
                    }
                } else {
                    licitacionRiesgo.setTipoAdjudicacion(this.entidadTipoAdjudicacionRepository.findByDetalle("DEJADA SIN EFECTO"));
                }

                Integer indexMontoCotizado = indicesLicitacion.getIndexMontoCotizado();

                //monto cotizado
                if (indexMontoCotizado != null) {

                    Cell celdaMontoCotizado = row.getCell(indexMontoCotizado);

                    if (!lectorCeldas.esCeldaVaciaOInvalida(celdaMontoCotizado) && celdaMontoCotizado.getCellType() == CellType.NUMERIC) {
                        licitacionRiesgo.setMontoCotizado(celdaMontoCotizado.getNumericCellValue());
                    }
                }

                List<Integer> indicesRiesgoCosto = indicesLicitacion.getIndicesRiesgoCosto();

// monto cotizado (RiesgoCosto1 -> primer riesgo, RiesgoCosto2 -> segundo riesgo)
                if(indicesRiesgoCosto != null) {
                    if (indiceRiesgo < indicesRiesgoCosto.size()) {
                        Integer indexCotizadoCosto = indicesRiesgoCosto.get(indiceRiesgo);

                        if (indexCotizadoCosto != null) {
                            Double montoCotizado = lectorCeldas.leerComoDouble(row.getCell(indexCotizadoCosto));
                            licitacionRiesgo.setMontoCotizado(montoCotizado);
                        }
                    }
                }

                List<Integer> indicesAdjudicadoCosto = indicesLicitacion.getIndicesAdjudicadoCosto();

                if(indicesAdjudicadoCosto != null) {

                    indicesAdjudicadoCosto.forEach(i -> System.out.println("IndiceAdjudicado: " + i));

// monto adjudicado por riesgo (AdjudicadoCosto1/2/3 -> primer, segundo y tercer riesgo)
                    if (indiceRiesgo < indicesAdjudicadoCosto.size()) {
                        Integer indexAdjudicadoCosto = indicesAdjudicadoCosto.get(indiceRiesgo);

                        System.out.println("IndiceAdjudicado: " + indexAdjudicadoCosto);

                        if (indexAdjudicadoCosto != null) {
                            Double montoAdjudicadoRiesgo = lectorCeldas.leerComoDouble(row.getCell(indexAdjudicadoCosto));

                            System.out.println("montoLeido:  " + montoAdjudicadoRiesgo);

                            if (montoAdjudicadoRiesgo != null) {
                                licitacionRiesgo.setMontoAdjudicado(montoAdjudicadoRiesgo);
                            }
                        }
                    }
                }

                licitacion.getRiesgosAsignados().add(licitacionRiesgo);

                indiceRiesgo++;
            }
        }


        this.entidadLicitacionRepository.save(licitacion);
    }

    public void verificarCarga(String rutaArchivo) {

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
            Integer idxCotCosto1       = hg.getHeaderIndex("CotizadoCosto1");
            Integer idxCotCosto2       = hg.getHeaderIndex("CotizadoCosto2");

            List<Integer> indicesAdjCosto = new ArrayList<>();
            if (idxAdjCosto1 != null) indicesAdjCosto.add(idxAdjCosto1);
            if (idxAdjCosto2 != null) indicesAdjCosto.add(idxAdjCosto2);

            List<Integer> indicesCotCosto = new ArrayList<>();
            if (idxCotCosto1 != null) indicesCotCosto.add(idxCotCosto1);
            if (idxCotCosto2 != null) indicesCotCosto.add(idxCotCosto2);

            System.out.println("--- INICIANDO AUDITORÍA DE DATOS ---");

            int filaExcel      = 1;
            int renglonesFaltantes = 0;
            int renglonesMalMonto  = 0;
            int renglonesMalCotizado = 0;
            int renglonOk      = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                filaExcel++;

                if (idxNumero == null || row.getCell(idxNumero) == null) continue;
                String numExcel = lectorCeldas.leerComoTexto(row, idxNumero);
                if (numExcel == null || numExcel.isBlank()) continue;

                Licitacion licitacion = this.entidadLicitacionRepository.findByNumeroCompulsa(numExcel).orElse(null);
                if (licitacion == null) {
                    System.out.println("[FALTA LICITACION] Fila " + filaExcel + " | Compulsa: " + numExcel);
                    renglonesFaltantes++;
                    continue;
                }

                if (idxCliente != null) {
                    String clienteExcel = LimpiadorTexto.limpiarTildes(lectorCeldas.leerComoTexto(row, idxCliente));
                    String clienteDB    = LimpiadorTexto.limpiarTildes(licitacion.getCliente().getDetalle());
                    if (!clienteDB.equals(clienteExcel)) {
                        System.out.println("[CLIENTE DISTINTO] Fila " + filaExcel + " | Compulsa: " + numExcel
                                + " | Excel: " + clienteExcel + " | DB: " + clienteDB);
                    }
                }

                List<LicitacionRiesgo> riesgosEnDB = this.licitacionRiesgoRepository.findByLicitacion(licitacion.getId());
                if (riesgosEnDB.isEmpty()) {
                    System.out.println("[SIN RENGLONES] Fila " + filaExcel + " | Compulsa: " + numExcel);
                    renglonesFaltantes++;
                    continue;
                }

                String riesgosRaw  = idxRiesgo != null ? lectorCeldas.leerComoTexto(row, idxRiesgo) : "";
                List<String> tokens = this.gestorRiesgos.obtenerTokens(riesgosRaw);
                String statusExcel = idxStatus != null ? lectorCeldas.leerCeldaRecortada(row, idxStatus) : "";
                String motivoExcel = idxMotivo != null ? lectorCeldas.leerCeldaRecortada(row, idxMotivo) : "";
                String motivoExcelCap = motivoExcel.isBlank() ? "" : LimpiadorTexto.capitalizar(motivoExcel);
                Date   fechaExcel  = idxFecha  != null ? lectorCeldas.leerFecha(row, idxFecha) : null;

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

                // Usamos una copia mutable de los riesgos en DB para marcar los ya matcheados
                // y no contar el mismo registro dos veces cuando hay duplicados legítimos
                List<LicitacionRiesgo> riesgosDisponibles = new ArrayList<>(riesgosEnDB);

                int indiceToken = 0;
                for (String token : tokens) {
                    if (token.equalsIgnoreCase("SEGURO TECNICO")) {
                        indiceToken++; // igual que en el insertor
                        continue;
                    }

                    Riesgo riesgoEsperado = this.gestorRiesgos.resolverRiesgo(token);
                    if (riesgoEsperado == null) {
                        System.out.println("[RIESGO NO RECONOCIDO] Fila " + filaExcel + " | Compulsa: " + numExcel + " | Token: " + token);
                        indiceToken++;
                        continue;
                    }

                    // Montos esperados para este token según su posición (igual que el insertor)
                    Double montoAdjEsperado = null;
                    if (!indicesAdjCosto.isEmpty() && indiceToken < indicesAdjCosto.size()) {
                        montoAdjEsperado = lectorCeldas.leerComoDouble(row.getCell(indicesAdjCosto.get(indiceToken)));
                    }
                    if (montoAdjEsperado == null) montoAdjEsperado = montoAdjExcel;

                    Double montoCotEsperado = null;
                    if (!indicesCotCosto.isEmpty() && indiceToken < indicesCotCosto.size()) {
                        montoCotEsperado = lectorCeldas.leerComoDouble(row.getCell(indicesCotCosto.get(indiceToken)));
                    }
                    if (montoCotEsperado == null) montoCotEsperado = montoCotExcel;

                    // Buscar en disponibles: primero por metadatos+montos exactos,
                    // luego solo por metadatos (para reportar monto distinto)
                    LicitacionRiesgo matchMetadatos = null;
                    LicitacionRiesgo matchCompleto  = null;

                    for (LicitacionRiesgo lr : riesgosDisponibles) {
                        boolean mismoRiesgo = lr.getRiesgo()  != null && lr.getRiesgo().getId().equals(riesgoEsperado.getId());
                        boolean mismoStatus = lr.getStatus()  != null && lr.getStatus().getDetalle().equals(statusExcel);
                        boolean mismaFecha  = lr.getFecha()   != null && fechaExcel != null && lr.getFecha().compareTo(fechaExcel) == 0;
                        String  motivoDB    = lr.getMotivo()  == null ? "" : lr.getMotivo();
                        boolean mismoMotivo = motivoDB.equals(motivoExcelCap);

                        if (!mismoRiesgo || !mismoStatus || !mismaFecha || !mismoMotivo) continue;

                        // Metadatos ok — verificar montos
                        boolean adjOk = montoAdjEsperado == null
                                ? (lr.getMontoAdjudicado() == null)
                                : (lr.getMontoAdjudicado() != null && Math.abs(lr.getMontoAdjudicado() - montoAdjEsperado) < 0.01);

                        boolean cotOk = montoCotEsperado == null
                                ? (lr.getMontoCotizado() == null)
                                : (lr.getMontoCotizado() != null && Math.abs(lr.getMontoCotizado() - montoCotEsperado) < 0.01);

                        if (adjOk && cotOk) {
                            matchCompleto = lr;
                            break;
                        } else if (matchMetadatos == null) {
                            matchMetadatos = lr; // guardar el primero con metadatos ok para reporte
                        }
                    }

                    if (matchCompleto != null) {
                        riesgosDisponibles.remove(matchCompleto);
                        renglonOk++;
                    } else if (matchMetadatos != null) {
                        riesgosDisponibles.remove(matchMetadatos);
                        String adjDB = matchMetadatos.getMontoAdjudicado() == null ? "null" : matchMetadatos.getMontoAdjudicado().toString();
                        String cotDB = matchMetadatos.getMontoCotizado()   == null ? "null" : matchMetadatos.getMontoCotizado().toString();
                        System.out.println("[MONTO DISTINTO] Fila " + filaExcel
                                + " | Compulsa: " + numExcel
                                + " | Riesgo: " + riesgoEsperado.getDetalle()
                                + " | Posicion: " + indiceToken
                                + " | AdjExcel: " + montoAdjEsperado + " -> DB: " + adjDB
                                + " | CotExcel: " + montoCotEsperado + " -> DB: " + cotDB);
                        if (montoAdjEsperado != null && matchMetadatos.getMontoAdjudicado() != null
                                && Math.abs(matchMetadatos.getMontoAdjudicado() - montoAdjEsperado) >= 0.01) {
                            renglonesMalMonto++;
                        }
                        if (montoCotEsperado != null && matchMetadatos.getMontoCotizado() != null
                                && Math.abs(matchMetadatos.getMontoCotizado() - montoCotEsperado) >= 0.01) {
                            renglonesMalCotizado++;
                        }
                    } else {
                        System.out.println("[RENGLON FALTANTE] Fila " + filaExcel
                                + " | Compulsa: " + numExcel
                                + " | Riesgo: " + riesgoEsperado.getDetalle()
                                + " | Posicion: " + indiceToken
                                + " | Status: " + statusExcel
                                + " | Motivo: '" + motivoExcelCap + "'"
                                + " | Fecha: " + fechaExcel
                                + " -> NO encontrado en BD");
                        renglonesFaltantes++;
                    }

                    indiceToken++;
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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
