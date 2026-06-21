package raiz;

import raiz.dominio.ImprimidorFilas;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import raiz.componentes.EstadisticasService;
import raiz.componentes.InsertorDatos;
import raiz.dominio.estadisticas.IEvolucionMensual;
import raiz.dominio.estadisticas.ITopRiesgos;
import raiz.dominio.estadisticas.ITotalLicitacionesUnicas;
import raiz.dominio.estadisticas.IWinrateGlobal;
import raiz.dominio.estadisticas.ITopClientesTasaExito;
import raiz.dominio.estadisticas.IFugasPorCompetidor;
import raiz.dominio.estadisticas.IPerdidasPorMotivo;
import raiz.dominio.estadisticas.IDesvioPrecioPorMotivo;
import raiz.dominio.estadisticas.IResumenMontosPorRiesgo;
import raiz.dominio.estadisticas.IRentabilidadMensual;
import raiz.dominio.estadisticas.IRentabilidadGlobal;
import raiz.dominio.estadisticas.IRentabilidadPorRiesgo;
import raiz.dominio.estadisticas.IEstadoLicitaciones;
import raiz.dominio.estadisticas.IMotivoGanada;
import raiz.dominio.estadisticas.ITotalAdjudicadoGanadas;
import raiz.dominio.estadisticas.IRankingRiesgosGanados;
import raiz.dominio.estadisticas.ISobreprecioPromedio;
import raiz.dominio.estadisticas.IRentabilidadResidualPerdidas;
import raiz.dominio.estadisticas.ITotalDesistidas;
import raiz.dominio.estadisticas.ITopMotivosDesistidas;
import raiz.dominio.estadisticas.IMontoAdjudicadoDesistido;
import raiz.dominio.estadisticas.IRenglonesDesistidos;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Application implements CommandLineRunner {

    private final InsertorDatos insertor;
    private final EstadisticasService estadisticasService;

    public Application(InsertorDatos insertor, EstadisticasService estadisticasService) {
        this.insertor = insertor;
        this.estadisticasService = estadisticasService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n1_Cargar excel \n2_Obtener estadisticas \n3_Finalizar programa \n");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1:
                    System.out.println("Ingrese el path del archivo excel:");
                    String rutaArchivo = sc.nextLine();

                    insertor.importarDesdeExcel(rutaArchivo);
                    break;

                case 2:
                    System.out.println("#### Estadísticas: ");

                    ITotalLicitacionesUnicas cantLicitaciones = this.estadisticasService.cantLicitaciones().getBody();

                    System.out.println("-- ============================================================================\n" +
                            "-- 1. TOTAL DE LICITACIONES ÚNICAS\n" +
                            "-- ============================================================================");

                    System.out.println("Total Licitaciones Únicas: " + cantLicitaciones.getCant_licitaciones());

                    System.out.println("-- ============================================================================\n" +
                            "-- 2. WINRATE GENERAL GLOBAL DE LAS QUE SE PRESENTÓ LA CAJA (PORCENTUAL)\n" +
                            "-- ============================================================================");

                    IWinrateGlobal winrate = this.estadisticasService.getWinrateGlobal().getBody();

                    System.out.println("Winrate: " +  winrate.getWinrate());

                    System.out.println("-- ============================================================================\n" +
                            "-- 4. EVOLUCIÓN MENSUAL DE MONTOS COTIZADOS Y ADJUDICADOS\n" +
                            "-- ============================================================================");

                    List<IEvolucionMensual> evolucionMensual =  this.estadisticasService.getEvolucionMensual().getBody();

                    evolucionMensual.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- 5. RANKING TOP 5: RIESGOS MÁS COTIZADOS VS MÁS ADJUDICADOS\n" +
                            "-- ============================================================================");

                    List<ITopRiesgos> topRiesgos = this.estadisticasService.getTopRiesgos().getBody();

                    topRiesgos.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- 7. SHARE DE CLIENTES Y SU TASA DE ÉXITO (TOP 5)\n" +
                            "-- ============================================================================");

                    List<ITopClientesTasaExito> topClientes = this.estadisticasService.getTopClientesTasaExito().getBody();

                    topClientes.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- 8. ANÁLISIS COMPETITIVO: FUGAS POR COMPETIDOR\n" +
                            "-- ============================================================================");

                    List<IFugasPorCompetidor> fugasPorCompetidor = this.estadisticasService.getFugasPorCompetidor().getBody();

                    fugasPorCompetidor.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- 9. VOLUMEN ECONÓMICO DE PÉRDIDAS POR MOTIVO\n" +
                            "-- ============================================================================");

                    List<IPerdidasPorMotivo> perdidasPorMotivo = this.estadisticasService.getPerdidasPorMotivo().getBody();

                    perdidasPorMotivo.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- 10. CORRELACIÓN: DESVÍO DE PRECIO VS MOTIVO DE RECHAZO (POR RENGLÓN)\n" +
                            "-- ============================================================================");

                    List<IDesvioPrecioPorMotivo> desvioPrecioPorMotivo = this.estadisticasService.getDesvioPrecioPorMotivo().getBody();

                    desvioPrecioPorMotivo.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- 10.1 RESUMEN DE MONTOS COTIZADOS Y ADJUDICADOS POR RIESGO\n" +
                            "-- ============================================================================");

                    List<IResumenMontosPorRiesgo> resumenMontosPorRiesgo = this.estadisticasService.getResumenMontosPorRiesgo().getBody();

                    resumenMontosPorRiesgo.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- 12. RENTABILIDAD FINANCIERA MENSUAL (PORCENTAJE DE BENEFICIO)\n" +
                            "-- ============================================================================");

                    List<IRentabilidadMensual> rentabilidadMensual = this.estadisticasService.getRentabilidadMensual().getBody();

                    rentabilidadMensual.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- 12. RENTABILIDAD FINANCIERA GLOBAL (PORCENTAJE DE BENEFICIO)\n" +
                            "-- ============================================================================");

                    IRentabilidadGlobal rentabilidadGlobal = this.estadisticasService.getRentabilidadGlobal().getBody();

                    ImprimidorFilas.imprimirFilaGenerica(rentabilidadGlobal);

                    System.out.println("-- ============================================================================\n" +
                            "-- 12. RENTABILIDAD FINANCIERA POR RIESGO (PORCENTAJE DE BENEFICIO)\n" +
                            "-- ============================================================================");

                    List<IRentabilidadPorRiesgo> rentabilidadPorRiesgo = this.estadisticasService.getRentabilidadPorRiesgo().getBody();

                    rentabilidadPorRiesgo.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- A. CONTEO DE COMPULSAS/LICITACIONES ÚNICAS POR ESTADO HISTÓRICO\n" +
                            "-- ============================================================================");

                    List<IEstadoLicitaciones> estadoLicitaciones = this.estadisticasService.getEstadoLicitaciones().getBody();

                    estadoLicitaciones.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- B. ANÁLISIS DE GANADAS: APERTURA POR MOTIVO ESPECÍFICO\n" +
                            "-- ============================================================================");

                    List<IMotivoGanada> motivoGanada = this.estadisticasService.getMotivoGanada().getBody();

                    motivoGanada.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- C. TOTAL ABSOLUTO ADJUDICADO EN PROCESOS EXITOSOS\n" +
                            "-- ============================================================================");

                    ITotalAdjudicadoGanadas totalAdjudicadoGanadas = this.estadisticasService.getTotalAdjudicadoGanadas().getBody();

                    ImprimidorFilas.imprimirFilaGenerica(totalAdjudicadoGanadas);

                    System.out.println("-- ============================================================================\n" +
                            "-- D. RANKING DE RENGLONES (RIESGOS) CON MAYOR CANTIDAD DE ÉXITOS\n" +
                            "-- ============================================================================");

                    List<IRankingRiesgosGanados> rankingRiesgosGanados = this.estadisticasService.getRankingRiesgosGanados().getBody();

                    rankingRiesgosGanados.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- E. RELACIÓN DE PÉRDIDAS: NUESTRO PRECIO VS EL COMPETIDOR\n" +
                            "-- ============================================================================");

                    ISobreprecioPromedio sobreprecioPromedio = this.estadisticasService.getSobreprecioPromedio().getBody();

                    ImprimidorFilas.imprimirFilaGenerica(sobreprecioPromedio);

                    System.out.println("-- ============================================================================\n" +
                            "-- F. RENTABILIDAD RESIDUAL DE RIESGOS EN PÉRDIDAS\n" +
                            "-- ============================================================================");

                    List<IRentabilidadResidualPerdidas> rentabilidadResidualPerdidas = this.estadisticasService.getRentabilidadResidualPerdidas().getBody();

                    rentabilidadResidualPerdidas.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    System.out.println("-- ============================================================================\n" +
                            "-- G. RADIOGRAFÍA COMPLETA DE DESISTIDAS\n" +
                            "-- ============================================================================");

                    ITotalDesistidas totalDesistidas = this.estadisticasService.getTotalDesistidas().getBody();

                    ImprimidorFilas.imprimirFilaGenerica(totalDesistidas);

                    List<ITopMotivosDesistidas> topMotivosDesistidas = this.estadisticasService.getTopMotivosDesistidas().getBody();

                    topMotivosDesistidas.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    IMontoAdjudicadoDesistido montoAdjudicadoDesistido = this.estadisticasService.getMontoAdjudicadoDesistido().getBody();

                    ImprimidorFilas.imprimirFilaGenerica(montoAdjudicadoDesistido);

                    List<IRenglonesDesistidos> renglonesDesistidos = this.estadisticasService.getRenglonesDesistidos().getBody();

                    renglonesDesistidos.forEach(ImprimidorFilas::imprimirFilaGenerica);

                    break;
                case 3:
                    System.out.println("Finalizando programa...");
                    break;

                default:
                    System.out.println("Respuesta inválida, elija un número dentro del rango esperado.");
                    break;
            }

        } while (opcion != 3);

        sc.close();
    }
}