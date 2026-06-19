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
                Moneda moneda = this.almacenarMoneda(currentRow, headerGestor.getHeaderIndex("moneda"));
                Ramo ramo = this.almacenarRamo(currentRow, headerGestor.getHeaderIndex("ramo"));
                this.almacenarRiesgo(currentRow, headerGestor.getHeaderIndex("riesgo"), ramo);
                Mes mes = this.almacenarMes(currentRow, headerGestor.getHeaderIndex("Fecha"));
                Cliente cliente = this.almacenarCliente(currentRow, headerGestor.getHeaderIndex("cliente"));
                EntidadAdjudicada adjudicada = this.almacenarAdjudicadoA(currentRow, headerGestor.getHeaderIndex("adjudicadoA"));
                Status status = this.almacenarStatus(currentRow, headerGestor.getHeaderIndex("status"));

                Integer indexNumeroCompulsa = headerGestor.getHeaderIndex("Numero");
                Integer indexRiesgo = headerGestor.getHeaderIndex("Riesgo");
                Integer indexFecha = headerGestor.getHeaderIndex("Fecha");
                Integer indexMotivo = headerGestor.getHeaderIndex("Motivo");
                Integer indexEstadoMotivo = headerGestor.getHeaderIndex("estadoMotivo");
                Integer indexMontoAdjudicado = headerGestor.getHeaderIndex("MontoAdjudicado");
                Integer indexMontoCotizado = headerGestor.getHeaderIndex("MontoCotizado");

                Integer riesgoCosto1Header = headerGestor.getHeaderIndex("RiesgoCosto1");
                Integer riesgoCosto2Header = headerGestor.getHeaderIndex("RiesgoCosto2");

                List<Integer> indicesRiesgoCosto = null;

                if(riesgoCosto1Header != null && riesgoCosto2Header != null) {
                    indicesRiesgoCosto = List.of(
                            riesgoCosto1Header,
                            riesgoCosto2Header
                    );
                }

                List<Integer> indicesAdjudicadoCosto = null;

                Integer AdjudicadoCosto1Header = headerGestor.getHeaderIndex("AdjudicadoCosto1");
                Integer AdjudicadoCosto2Header = headerGestor.getHeaderIndex("AdjudicadoCosto2");
                Integer AdjudicadoCosto3Header = headerGestor.getHeaderIndex("AdjudicadoCosto3");

                if(AdjudicadoCosto1Header != null &&  AdjudicadoCosto2Header != null &&  AdjudicadoCosto3Header != null) {
                    indicesAdjudicadoCosto = List.of(
                            AdjudicadoCosto1Header,
                            AdjudicadoCosto2Header,
                            AdjudicadoCosto3Header
                    );
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
    private LicitacionRiesgo buscarLicitacionRiesgoExistente(Licitacion licitacion, Riesgo riesgo, Date fecha, Status status) {

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

            if (mismoRiesgo && mismoStatus && mismaFecha) {
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
                continue;
            }

            Riesgo riesgo = this.gestorRiesgos.resolverRiesgo(riesgo_str);

            if(riesgo != null) {

                Date fechaRiesgo = lectorCeldas.leerFecha(row, indicesLicitacion.getIndexFecha());

                LicitacionRiesgo licitacionRiesgo = this.buscarLicitacionRiesgoExistente(licitacion, riesgo, fechaRiesgo, status);

                boolean esNuevo = (licitacionRiesgo == null);

                if (esNuevo) {
                    licitacionRiesgo = new LicitacionRiesgo();
                } else {
                    System.out.println("LicitacionRiesgo ya existente (riesgo=" + riesgo.getDetalle() + ", fecha=" + fechaRiesgo + ", status=" + (status != null ? status.getDetalle() : null) + ") -> se actualiza en lugar de duplicar.");
                }

                licitacionRiesgo.setRiesgo(riesgo);
                licitacionRiesgo.setStatus(status);
                licitacionRiesgo.setAdjudicada(adjudicada);
                licitacionRiesgo.setFecha(fechaRiesgo);
                licitacionRiesgo.setMes(mes);
                licitacionRiesgo.setMoneda(moneda);

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

                licitacionRiesgo.setLicitacion(licitacion);

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
                        Integer indexRiesgoCosto = indicesRiesgoCosto.get(indiceRiesgo);

                        if (indexRiesgoCosto != null) {
                            Double montoCotizado = lectorCeldas.leerComoDouble(row.getCell(indexRiesgoCosto));
                            licitacionRiesgo.setMontoCotizado(montoCotizado);
                        }
                    }
                }

                List<Integer> indicesAdjudicadoCosto = indicesLicitacion.getIndicesAdjudicadoCosto();

                if(indicesAdjudicadoCosto != null) {
// monto adjudicado por riesgo (AdjudicadoCosto1/2/3 -> primer, segundo y tercer riesgo)
                    if (indiceRiesgo < indicesAdjudicadoCosto.size()) {
                        Integer indexAdjudicadoCosto = indicesAdjudicadoCosto.get(indiceRiesgo);

                        if (indexAdjudicadoCosto != null) {
                            Double montoAdjudicadoRiesgo = lectorCeldas.leerComoDouble(row.getCell(indexAdjudicadoCosto));

                            if (montoAdjudicadoRiesgo != null) {
                                licitacionRiesgo.setMontoAdjudicado(montoAdjudicadoRiesgo);
                            }
                        }
                    }
                }

                if (esNuevo) {
                    licitacion.getRiesgosAsignados().add(licitacionRiesgo);
                }

                indiceRiesgo++;
            }
        }


        this.entidadLicitacionRepository.save(licitacion);
    }

    public void verificarCarga(String rutaArchivo) {

        try (FileInputStream fis = new FileInputStream(rutaArchivo);

            Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            System.out.println("--- INICIANDO AUDITORÍA DE DATOS ---");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null || row.getCell(3) == null) continue;

                String numExcel = lectorCeldas.leerComoTexto(row, 3);

                Licitacion licitacion = this.entidadLicitacionRepository.findByNumeroCompulsa(numExcel).orElse(null);

                if (licitacion == null) {
                    throw MensajesError.licitacionFaltante(numExcel);
                }

                String cliente_str_limpio = LimpiadorTexto.limpiarTildes(lectorCeldas.leerComoTexto(row, 4));
                String cliente_db_limpio = LimpiadorTexto.limpiarTildes(licitacion.getCliente().getDetalle());

                if (!cliente_db_limpio.equals(cliente_str_limpio)) {
                    MensajesError.errorComparacion(numExcel, cliente_str_limpio, licitacion.getCliente().getDetalle());
                }

                List<LicitacionRiesgo> licitacionesRiesgo = this.licitacionRiesgoRepository.findByLicitacion(licitacion.getId());

                if (licitacionesRiesgo.isEmpty()) {
                    throw MensajesError.sinLicitacionesRiesgo(numExcel);
                }

                int j = 0;
                boolean verificado = false;

                for (LicitacionRiesgo licitacionRiesgo : licitacionesRiesgo) {

                    System.out.println(j + " licitacionRiesgo a analizar");

                    Date date_excel = lectorCeldas.leerFecha(row, 2);

                    if (!(licitacionRiesgo.getFecha().compareTo(date_excel) == 0)) {
                        System.out.println("Fallo la fecha");
                        continue;
                    }

                    String motivoExcel = lectorCeldas.leerCeldaRecortada(row, 7);

                    String motivoDB = (licitacionRiesgo.getMotivo() == null) ? "" : licitacionRiesgo.getMotivo();

                    if (!motivoDB.equals(motivoExcel)) {
                        System.out.println("Fallo el motivo: DB[" + motivoDB + "] vs Excel[" + motivoExcel + "]");
                        continue;
                    }

                    String mes_str = lectorCeldas.leerCeldaRecortada(row, 1);

                    if (!licitacionRiesgo.getMes().getDetalle().equals(mes_str)) {
                        System.out.println("Fallo el mes: DB[" + licitacionRiesgo.getMes().getDetalle() + "] vs Excel[" + mes_str + "]");
                        continue;
                    }

                    String status_str = lectorCeldas.leerComoTexto(row, 6);

                    if (!licitacionRiesgo.getStatus().getDetalle().equals(status_str)) {
                        System.out.println("Fallo el status");
                        continue;
                    }

                    String celdaRiesgoRaw = lectorCeldas.leerComoTexto(row, 5);
                    List<String> tokensExcel = this.gestorRiesgos.obtenerTokens(celdaRiesgoRaw); // "RC / INCENDIO" -> ["RC", "INCENDIO"]

                    boolean riesgoEncontradoEnTokens = false;

                    for (String token : tokensExcel) {
                        Riesgo riesgoAsociadoAlToken = this.gestorRiesgos.resolverRiesgo(token);

                        if (riesgoAsociadoAlToken != null && riesgoAsociadoAlToken.getDetalle().equalsIgnoreCase(licitacionRiesgo.getRiesgo().getDetalle())) {
                            riesgoEncontradoEnTokens = true;
                        }
                    }


                    if (!riesgoEncontradoEnTokens) {
                        System.out.println("Fallo el riesgo: El riesgo en DB '" + licitacionRiesgo.getRiesgo().getDetalle() + "no se encuentra en la celda: " + celdaRiesgoRaw);
                        continue;
                    }

                    String adjudicada_str = lectorCeldas.leerComoTexto(row, 8);

                    if (!licitacionRiesgo.getAdjudicada().getDetalle().equals(adjudicada_str)) {
                        System.out.println("Fallo la adjudicada");
                        continue;
                    }

                    Cell celdaMontoAdjudicado = row.getCell(9);

                    if(!lectorCeldas.esCeldaVaciaOInvalida(celdaMontoAdjudicado)) {

                        if (celdaMontoAdjudicado.getCellType() == CellType.NUMERIC) {

                            Double montoAdjudicado = celdaMontoAdjudicado.getNumericCellValue();

                            if (!(licitacionRiesgo.getMontoAdjudicado().compareTo(montoAdjudicado) == 0)) {
                                System.out.println("Fallo el monto adjudicado");
                                continue;
                            }
                            if (!(licitacionRiesgo.getTipoAdjudicacion().getDetalle().equals("CON MONTO"))) {
                                System.out.println("Fallo el tipo adjudicacion");
                                continue;
                            }
                        } else {
                            if (licitacionRiesgo.getMontoAdjudicado() != null) {
                                System.out.println("Fallo el monto adjudicado");
                                continue;
                            }
                            if (!(licitacionRiesgo.getTipoAdjudicacion().getDetalle().equals("DEJADA SIN EFECTO"))) {
                                System.out.println("Fallo el tipo adjudicacion");
                                continue;
                            }
                        }
                    } else {
                        if(licitacionRiesgo.getMontoAdjudicado() != null || !licitacionRiesgo.getTipoAdjudicacion().getDetalle().equals("DEJADA SIN EFECTO")) {
                            System.out.println("Fallo el monto adjudicado y tipo adjudicacion");
                            continue;
                        }
                    }

                    verificado = true;
                    break;
                }
                if (!verificado) {
                    throw MensajesError.verificacionFallida(licitacion.getId());
                }
            }
            System.out.println("VERIFICACION FINALIZADA CORRECTAMENTE");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
