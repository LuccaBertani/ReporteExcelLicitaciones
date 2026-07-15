package raiz.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import raiz.services.EstadisticasService;
import raiz.dominio.estadisticas.*;

import java.util.List;

@RestController
@RequestMapping("/estadisticas")
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    // Inyección por constructor (la más segura y recomendada)
    public EstadisticasController(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @GetMapping("/total-licitaciones")
    public ResponseEntity<ITotalLicitacionesUnicas> totalLicitaciones(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.cantLicitaciones(fechaDesde, fechaHasta);
    }

    @GetMapping("/winrate-global")
    public ResponseEntity<IWinrateGlobal> getWinrateGlobal(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getWinrateGlobal(fechaDesde, fechaHasta);
    }

    @GetMapping("/evolucion-mensual")
    public ResponseEntity<List<IEvolucionMensual>> getEvolucionMensual(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getEvolucionMensual(fechaDesde, fechaHasta);
    }

    @GetMapping("/top-riesgos")
    public ResponseEntity<List<ITopRiesgos>> getTopRiesgos(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getTopRiesgos(fechaDesde, fechaHasta);
    }

    @GetMapping("/top-clientes-tasa-exito")
    public ResponseEntity<List<ITopClientesTasaExito>> getTopClientesTasaExito(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getTopClientesTasaExito(fechaDesde, fechaHasta);
    }

    @GetMapping("/fugas-competidor")
    public ResponseEntity<List<IFugasPorCompetidor>> getFugasPorCompetidor(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getFugasPorCompetidor(fechaDesde, fechaHasta);
    }

    @GetMapping("/perdidas-motivo")
    public ResponseEntity<List<IPerdidasPorMotivo>> getPerdidasPorMotivo(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getPerdidasPorMotivo(fechaDesde, fechaHasta);
    }

    @GetMapping("/desvio-precio-motivo")
    public ResponseEntity<List<IDesvioPrecioPorMotivo>> getDesvioPrecioPorMotivo(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getDesvioPrecioPorMotivo(fechaDesde, fechaHasta);
    }

    @GetMapping("/resumen-montos-riesgo")
    public ResponseEntity<List<IResumenMontosPorRiesgo>> getResumenMontosPorRiesgo(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getResumenMontosPorRiesgo(fechaDesde, fechaHasta);
    }

    @GetMapping("/rentabilidad-mensual")
    public ResponseEntity<List<IRentabilidadMensual>> getRentabilidadMensual(
            @RequestParam(name = "motivos", required = false) List<String> motivos,
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getRentabilidadMensual(motivos, fechaDesde, fechaHasta);
    }

    @GetMapping("/rentabilidad-global")
    public ResponseEntity<IRentabilidadGlobal> getRentabilidadGlobal(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getRentabilidadGlobal(fechaDesde, fechaHasta);
    }

    @GetMapping("/rentabilidad-riesgo")
    public ResponseEntity<List<IRentabilidadPorRiesgo>> getRentabilidadPorRiesgo(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getRentabilidadPorRiesgo(fechaDesde, fechaHasta);
    }

    @GetMapping("/estado-licitaciones")
    public ResponseEntity<List<IEstadoLicitaciones>> getEstadoLicitaciones(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getEstadoLicitaciones(fechaDesde, fechaHasta);
    }

    @GetMapping("/motivo-ganada")
    public ResponseEntity<List<IMotivoGanada>> getMotivoGanada(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getMotivoGanada(fechaDesde, fechaHasta);
    }

    @GetMapping("/total-adjudicado-ganadas")
    public ResponseEntity<ITotalAdjudicadoGanadas> getTotalAdjudicadoGanadas(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getTotalAdjudicadoGanadas(fechaDesde, fechaHasta);
    }

    @GetMapping("/ranking-riesgos-ganados")
    public ResponseEntity<List<IRankingRiesgosGanados>> getRankingRiesgosGanados(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getRankingRiesgosGanados(fechaDesde, fechaHasta);
    }

    @GetMapping("/sobreprecio-promedio")
    public ResponseEntity<ISobreprecioPromedio> getSobreprecioPromedio(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getSobreprecioPromedio(fechaDesde, fechaHasta);
    }

    @GetMapping("/rentabilidad-residual-perdidas")
    public ResponseEntity<List<IRentabilidadResidualPerdidas>> getRentabilidadResidualPerdidas(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getRentabilidadResidualPerdidas(fechaDesde, fechaHasta);
    }

    @GetMapping("/total-desistidas")
    public ResponseEntity<ITotalDesistidas> getTotalDesistidas(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getTotalDesistidas(fechaDesde, fechaHasta);
    }

    @GetMapping("/top-motivos-desistidas")
    public ResponseEntity<List<ITopMotivosDesistidas>> getTopMotivosDesistidas(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getTopMotivosDesistidas(fechaDesde, fechaHasta);
    }

    @GetMapping("/monto-adjudicado-desistido")
    public ResponseEntity<IMontoAdjudicadoDesistido> getMontoAdjudicadoDesistido(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getMontoAdjudicadoDesistido(fechaDesde, fechaHasta);
    }

    @GetMapping("/renglones-desistidos")
    public ResponseEntity<List<IRenglonesDesistidos>> getRenglonesDesistidos(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getRenglonesDesistidos(fechaDesde, fechaHasta);
    }

    @GetMapping("/motivos-disponibles")
    public ResponseEntity<List<String>> getMotivosDisponibles(
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getMotivosDisponibles(fechaDesde, fechaHasta);
    }

    @GetMapping("/cantidad-licitaciones-mensual")
    public ResponseEntity<List<ICantidadLicitacionesPorMes>> getCantidadLicitacionesPorMes(
            @RequestParam(name = "motivos", required = false) List<String> motivos,
            @RequestParam(name = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) String fechaHasta) {
        return this.estadisticasService.getCantidadLicitacionesPorMes(motivos, fechaDesde, fechaHasta);
    }
}
