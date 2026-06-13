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

    private final IClienteRepository clienteRepository;
    private final IEntidadAdjudicada entidadAdjudicadaRepository;
    private final ILicitacion entidadLicitacionRepository;
    private final IMes entidadMesRepository;
    private final IRiesgo entidadRiesgoRepository;
    private final IStatus entidadStatusRepository;
    private final ILicitacionRiesgoRepository licitacionRiesgoRepository;
    private final ITipoAdjudicacion entidadTipoAdjudicacionRepository;
    private final LectorCeldas lectorCeldas = new LectorCeldas();
    private GestorRiesgos gestorRiesgos;

    public InsertorDatos(IClienteRepository clienteRepository, IEntidadAdjudicada entidadAdjudicadaRepository, ILicitacion entidadLicitacionRepository, IMes entidadMesRepository, IRiesgo entidadRiesgoRepository, IStatus entidadStatusRepository, ILicitacionRiesgoRepository licitacionRiesgoRepository, ITipoAdjudicacion entidadTipoAdjudicacionRepository) {

        this.clienteRepository = clienteRepository;
        this.entidadAdjudicadaRepository = entidadAdjudicadaRepository;
        this.entidadLicitacionRepository = entidadLicitacionRepository;
        this.entidadMesRepository = entidadMesRepository;
        this.entidadRiesgoRepository = entidadRiesgoRepository;
        this.entidadStatusRepository = entidadStatusRepository;
        this.licitacionRiesgoRepository = licitacionRiesgoRepository;
        this.entidadTipoAdjudicacionRepository = entidadTipoAdjudicacionRepository;
    }

    @PostConstruct
    public void init() {
        this.almacenarRiesgo();
        this.gestorRiesgos = new GestorRiesgos(this.entidadRiesgoRepository);
    }

    @Transactional
    public void importarDesdeExcel(String rutaArchivo) {

        try (FileInputStream fis = new FileInputStream(rutaArchivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

        // Obtenemos la primera hoja del Excel
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
                Mes mes = this.almacenarMes(currentRow, headerGestor.getHeaderIndex("mes"));
                Cliente cliente = this.almacenarCliente(currentRow, headerGestor.getHeaderIndex("cliente"));
                EntidadAdjudicada adjudicada = this.almacenarAdjudicadoA(currentRow, headerGestor.getHeaderIndex("adjudicadoA"));
                Status status = this.almacenarStatus(currentRow, headerGestor.getHeaderIndex("status"));

                Integer indexNumeroCompulsa = headerGestor.getHeaderIndex("Numero");
                Integer indexRiesgo = headerGestor.getHeaderIndex("Riesgo");
                Integer indexFecha = headerGestor.getHeaderIndex("Fecha");
                Integer indexMotivo = headerGestor.getHeaderIndex("Motivo");
                Integer indexMontoAdjudicado = headerGestor.getHeaderIndex("MontoAdjudicado");

                List<Integer> indicesRiesgoCosto = List.of(
                        headerGestor.getHeaderIndex("RiesgoCosto1"),
                        headerGestor.getHeaderIndex("RiesgoCosto2")
                );

                List<Integer> indicesAdjudicadoCosto = List.of(
                        headerGestor.getHeaderIndex("AdjudicadoCosto1"),
                        headerGestor.getHeaderIndex("AdjudicadoCosto2"),
                        headerGestor.getHeaderIndex("AdjudicadoCosto3")
                );

                this.almacenarLicitacion(currentRow, mes, cliente, status, adjudicada, indexNumeroCompulsa, indexRiesgo, indexFecha, indexMotivo, indexMontoAdjudicado, indicesRiesgoCosto, indicesAdjudicadoCosto);
            }

            System.out.println("¡Importación completada con éxito!");

        } catch (Exception e) {

            System.err.println("Error al procesar el Excel: " + e.getMessage());

            e.printStackTrace();
        }

    }

    private Cliente almacenarCliente(Row row, Integer indexCliente) {

        String detalle = row.getCell(indexCliente).getStringCellValue();

        Cliente cliente = this.clienteRepository.findByDetalle(detalle);

        if (cliente == null) {

            cliente = this.clienteRepository.save(new Cliente(detalle));

        }

        return cliente;

    }

    private Mes almacenarMes(Row row, Integer indexMes) {

        String detalle = row.getCell(indexMes).getStringCellValue();

        Mes mes = this.entidadMesRepository.findByDetalle(detalle);

        if (mes == null) {

            mes = this.entidadMesRepository.save(new Mes(detalle));

        }

        return mes;

    }

    private void almacenarRiesgo() {

        Map<Long, String> datosExcel = new LinkedHashMap<>();

        datosExcel.put(1L, "RC COMPRENSIVA");

        datosExcel.put(2L, "RC ASCENSORES");

        datosExcel.put(3L, "RC CALDERAS");

        datosExcel.put(4L, "RC GUARDA/DEPOSITO");

        datosExcel.put(5L, "RC CARTELES");

        datosExcel.put(6L, "INCENDIO");

        datosExcel.put(7L, "TECNICO EQ. ELECTRONICOS");

        datosExcel.put(8L, "APC");

        datosExcel.put(9L, "ROBO Y RIESGOS SIMILARES");

        datosExcel.put(10L, "VALORES EN TRANSITO");

        datosExcel.put(11L, "VALORES EN CAJA");

        datosExcel.put(12L, "TR INSTRUMENTOS MUSICALES");

        datosExcel.put(13L, "TR OBRAS DE ARTE");

        datosExcel.put(14L, "DRONES");

        datosExcel.put(15L, "INTEGRAL");

        datosExcel.put(16L, "AERONAVEACION");

        datosExcel.put(17L, "CAUCION");

        datosExcel.put(18L, "TRO");

        datosExcel.put(19L, "TRANSPORTE");

        datosExcel.put(20L, "SEPELIO");

        datosExcel.put(21L, "VIDA");

        datosExcel.put(22L, "SALUD");

        datosExcel.put(23L, "FRANQUICIAS");

// Recorremos y guardamos
        datosExcel.forEach((id, detalle) -> {

            if (!entidadRiesgoRepository.existsById(id)) {

                entidadRiesgoRepository.save(new Riesgo(id, detalle));

            }

        });

    }

    private Status almacenarStatus(Row row, Integer indexStatus) {

        String detalle = row.getCell(indexStatus).getStringCellValue();

        Status status = this.entidadStatusRepository.findByDetalle(detalle);

        if (status == null) {

            status = this.entidadStatusRepository.save(new Status(detalle));

        }

        return status;

    }

    private EntidadAdjudicada almacenarAdjudicadoA(Row row, Integer indexAdjudicadoA) {

        String detalle = row.getCell(indexAdjudicadoA).getStringCellValue();

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

    private void almacenarLicitacion(Row row, Mes mes, Cliente cliente, Status status, EntidadAdjudicada adjudicada, Integer indexNumeroCompulsa, Integer indexRiesgo, Integer indexFecha, Integer indexMotivo, Integer indexMontoAdjudicado, List<Integer> indicesRiesgoCosto, List<Integer> indicesAdjudicadoCosto) {

        Licitacion licitacion;
        String numero_str = lectorCeldas.leerComoTexto(row, indexNumeroCompulsa);

        System.out.println("NUMERO DE COMPULSA:" + numero_str);

        if(this.entidadLicitacionRepository.findByNumeroCompulsa(numero_str).isPresent()) {

            licitacion = this.entidadLicitacionRepository.findByNumeroCompulsa(numero_str).orElse(null);

            if(licitacion == null) {
                System.out.println("Como entras a aca animal");
                return;
            }

        } else {
            licitacion = new Licitacion();
            licitacion.setNumeroCompulsa(numero_str);
            licitacion.setCliente(cliente);
        }

        String riesgos_compactados_str = lectorCeldas.leerComoTexto(row, indexRiesgo);

        System.out.println("Riesgos compactados:" + riesgos_compactados_str);

        List<String> riesgos_str = this.gestorRiesgos.obtenerTokens(riesgos_compactados_str);

        int indiceRiesgo = 0;

        for(String riesgo_str : riesgos_str){

            System.out.println("SINONIMO DE RIESGO ENCONTRADO: " + riesgo_str);

            if(riesgo_str.equals("SEGURO TECNICO")){
                System.out.println("SOY UN ESTORBO, AVANZO CON EL SIGUIENTE!");
                continue;
            }

            Riesgo riesgo = this.gestorRiesgos.resolverRiesgo(riesgo_str);

            if(riesgo != null) {

                System.out.println("RIESGO MATCHEADO: " + riesgo.getDetalle());

                LicitacionRiesgo licitacionRiesgo = new LicitacionRiesgo();
                licitacionRiesgo.setRiesgo(riesgo);
                licitacionRiesgo.setStatus(status);
                licitacionRiesgo.setAdjudicada(adjudicada);
                licitacionRiesgo.setFecha(lectorCeldas.leerFecha(row, indexFecha));
                licitacionRiesgo.setMes(mes);

                Cell motivoCelda = row.getCell(indexMotivo);

                if (motivoCelda == null || motivoCelda.getCellType() == CellType.BLANK) {
                    System.out.println("Saltando fila: El motivo de la compulsa está vacío.");
                } else {
                    String motivo_str = motivoCelda.getStringCellValue();
                    licitacionRiesgo.setMotivo(motivo_str);
                }

                licitacionRiesgo.setLicitacion(licitacion);

//monto adjudicado
                Cell celdaMontoAdjudicado = row.getCell(indexMontoAdjudicado);

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

// monto cotizado (RiesgoCosto1 -> primer riesgo, RiesgoCosto2 -> segundo riesgo)
                if (indiceRiesgo < indicesRiesgoCosto.size()) {
                    Integer indexRiesgoCosto = indicesRiesgoCosto.get(indiceRiesgo);

                    if (indexRiesgoCosto != null) {
                        Double montoCotizado = lectorCeldas.leerComoDouble(row.getCell(indexRiesgoCosto));
                        licitacionRiesgo.setMontoCotizado(montoCotizado);
                    }
                }

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

    @Transactional
    public void actualizarBDcon2doExcel(String rutaArchivo) {

        try (FileInputStream fis = new FileInputStream(rutaArchivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

// Obtenemos la primera hoja del Excel
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

// Saltamos la primera fila si tiene encabezados (Títulos)
            if (rows.hasNext()) rows.next();

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // FILTRO 1: ¿La fila es nula?
                if (currentRow == null) continue;

                // FILTRO 2: ¿La celda de la compulsa tiene algo?
                Cell cellCompulsa = currentRow.getCell(5);
                if (cellCompulsa == null || cellCompulsa.getCellType() == CellType.BLANK) {
                    continue;
                }

                // FILTRO 3: ¿El texto de la compulsa es válido?
                String num_compulsa = lectorCeldas.leerComoTexto(currentRow, 5).trim();
                if (num_compulsa.isEmpty() || num_compulsa.equals("0")) { // A veces lee ceros fantasma
                    continue;
                }

                System.out.println("Procesando fila: " + currentRow.getRowNum() + " - Compulsa: " + num_compulsa);

                // Si pasó los filtros, procesamos
                this.almacenarDatosRestantesLicitacion(currentRow);
            }

            System.out.println("¡Actualizacion completada con éxito!");

        } catch (Exception e) {

            System.err.println("Error al procesar el Excel: " + e.getMessage());

            e.printStackTrace();
        }

    }

    //              0      1      2     3             4              5       6       7        8           9           10      11       12       13         14         15        16         17             18           19            20       21
    // header = [Seccion, Mes, Fecha, CUIT, TIPO DE CONTRATACION, Numero, Cliente, Riesgo, Renglon, Monto cotizado, Status, Ganada, Perdida, Renglon 1, Renglon 2, desistida, Motivo, AdjudicadoA, MontoAdjudicado, renglon 1, renglon 2, renglon 3]
    @Transactional
    private void almacenarDatosRestantesLicitacion(Row currentRow) {

        String num_compulsa = lectorCeldas.leerComoTexto(currentRow, 5);

        System.out.println("numero de compulsa: " +  num_compulsa);

        Licitacion licitacion = this.entidadLicitacionRepository.findByNumeroCompulsa(num_compulsa).orElse(null);

        if(licitacion == null){
            throw MensajesError.licitacionNoEncontrada(num_compulsa);
        }

        int j = 0;

        List<LicitacionRiesgo> coincidenciasEncontradas = new ArrayList<>();

        for (LicitacionRiesgo licitacionRiesgo : licitacion.getRiesgosAsignados()) {

            System.out.println(j + " licitacionRiesgo a analizar");

            Date date_excel = lectorCeldas.leerFecha(currentRow, 2);

            if (!(licitacionRiesgo.getFecha().compareTo(date_excel) == 0)) {
                System.out.println("Fallo la fecha");
                j++;
                continue;
            }

            String motivoExcel = lectorCeldas.leerCeldaRecortada(currentRow, 16);

            String motivoDB = (licitacionRiesgo.getMotivo() == null) ? "" : licitacionRiesgo.getMotivo();

            if (!motivoDB.equals(motivoExcel)) {
                System.out.println("Fallo el motivo: DB[" + motivoDB + "] vs Excel[" + motivoExcel + "]");
                j++;
                continue;
            }

            String mes_str = lectorCeldas.leerCeldaRecortada(currentRow, 1);

            if (!licitacionRiesgo.getMes().getDetalle().equals(mes_str)) {
                System.out.println("Fallo el mes: DB[" + licitacionRiesgo.getMes().getDetalle() + "] vs Excel[" + mes_str + "]");
                j++;
                continue;
            }

            String status_str = lectorCeldas.leerComoTexto(currentRow, 10);

            if (!licitacionRiesgo.getStatus().getDetalle().equals(status_str)) {
                System.out.println("Fallo el status");
                j++;
                continue;
            }

            String adjudicada_str = lectorCeldas.leerComoTexto(currentRow, 17);

            if (!licitacionRiesgo.getAdjudicada().getDetalle().equals(adjudicada_str)) {
                System.out.println("Fallo la adjudicada: DB[" + licitacionRiesgo.getAdjudicada().getDetalle() + "] vs Excel[" + adjudicada_str + "]");
                j++;
                continue;
            }

            j++;
            coincidenciasEncontradas.add(licitacionRiesgo);
        }
        if (coincidenciasEncontradas.isEmpty()) {
            throw MensajesError.coincidenciaLicitacionRiesgoNoEncontrada(licitacion.getNumeroCompulsa());
        }


        String riesgosCompactados = currentRow.getCell(7).getStringCellValue();

        List<String> riesgos_str = this.gestorRiesgos.obtenerTokens(riesgosCompactados);

        int i = 0;

        for(String riesgo_str : riesgos_str){

            Riesgo riesgoAsociadoAlToken = this.gestorRiesgos.resolverRiesgo(riesgo_str);

            if(riesgoAsociadoAlToken == null){
                throw MensajesError.riesgoNoEncontrado();
            }

            LicitacionRiesgo licitacionRiesgo = coincidenciasEncontradas.stream().filter(l -> l.getRiesgo().equals(riesgoAsociadoAlToken)).findFirst().orElse(null);

            if(licitacionRiesgo == null){
                throw MensajesError.licitacionPorRiesgoNoEncontrada(riesgo_str);
            }

            if (i < 2) {
                Cell celdaMonto = currentRow.getCell(13 + i);
                Double valorCotizado = lectorCeldas.leerComoDouble(celdaMonto);

                if (valorCotizado != null) {
                    licitacionRiesgo.setMontoCotizado(valorCotizado);
                } else {
                    // Si la celda 13 o 14 falla, intenta el respaldo de la columna 9
                    licitacionRiesgo.setMontoCotizado(lectorCeldas.leerComoDouble(currentRow.getCell(9)));
                }
            }

// 2. MONTO ADJUDICADO (Para riesgos 1, 2 y 3 -> columnas 19, 20, 21)
            if (i < 3) {
                Cell celdaMontoAdj = currentRow.getCell(19 + i);
                Double valorAdj = lectorCeldas.leerComoDouble(celdaMontoAdj);

                if (valorAdj != null) {
                    licitacionRiesgo.setMontoAdjudicado(valorAdj);
                    System.out.println("Guardando Adjudicado Riesgo " + (i+1) + ": " + valorAdj);
                } else {
                    System.out.println("Riesgo " + (i+1) + " sin monto adjudicado (celda vacía o null)");
                }
            }

            i++;
        }

        this.entidadLicitacionRepository.save(licitacion);
    }

    private List<String> leerHeadersDelExcel(String ruta) {

        try (FileInputStream fis = new FileInputStream(ruta);
             Workbook workbook = new XSSFWorkbook(fis)) {

            return lectorCeldas.leerHeaders(workbook);

        } catch (Exception e) {
            throw MensajesError.errorLecturaHeaders(e.getMessage(), e);
        }
    }
}
