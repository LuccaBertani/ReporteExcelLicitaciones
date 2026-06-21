package raiz.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import raiz.componentes.EstadisticasService;
import raiz.dominio.estadisticas.*;

import java.util.List;

@RestController
@RequestMapping("/estadisticas")
@CrossOrigin(origins = "*") // Permite que tu frontend acceda sin problemas de CORS
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    // Inyección por constructor (la más segura y recomendada)
    public EstadisticasController(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @GetMapping("/total-licitaciones")
    public ResponseEntity<ITotalLicitacionesUnicas> totalLicitaciones() {
        return this.estadisticasService.cantLicitaciones();
    }

    @GetMapping("/winrate-global")
    public ResponseEntity<IWinrateGlobal> getWinrateGlobal() {
        return this.estadisticasService.getWinrateGlobal();
    }

    @GetMapping("/evolucion-mensual")
    public ResponseEntity<List<IEvolucionMensual>> getEvolucionMensual() {
        return this.estadisticasService.getEvolucionMensual();
    }

    @GetMapping("/top-riesgos")
    public ResponseEntity<List<ITopRiesgos>> getTopRiesgos() {
        return this.estadisticasService.getTopRiesgos();
    }

    @GetMapping("/top-clientes-tasa-exito")
    public ResponseEntity<List<ITopClientesTasaExito>> getTopClientesTasaExito() {
        return this.estadisticasService.getTopClientesTasaExito();
    }

    @GetMapping("/fugas-competidor")
    public ResponseEntity<List<IFugasPorCompetidor>> getFugasPorCompetidor() {
        return this.estadisticasService.getFugasPorCompetidor();
    }

    @GetMapping("/perdidas-motivo")
    public ResponseEntity<List<IPerdidasPorMotivo>> getPerdidasPorMotivo() {
        return this.estadisticasService.getPerdidasPorMotivo();
    }

    @GetMapping("/desvio-precio-motivo")
    public ResponseEntity<List<IDesvioPrecioPorMotivo>> getDesvioPrecioPorMotivo() {
        return this.estadisticasService.getDesvioPrecioPorMotivo();
    }

    @GetMapping("/resumen-montos-riesgo")
    public ResponseEntity<List<IResumenMontosPorRiesgo>> getResumenMontosPorRiesgo() {
        return this.estadisticasService.getResumenMontosPorRiesgo();
    }

    @GetMapping("/rentabilidad-mensual")
    public ResponseEntity<List<IRentabilidadMensual>> getRentabilidadMensual() {
        return this.estadisticasService.getRentabilidadMensual();
    }

    @GetMapping("/rentabilidad-global")
    public ResponseEntity<IRentabilidadGlobal> getRentabilidadGlobal() {
        return this.estadisticasService.getRentabilidadGlobal();
    }

    @GetMapping("/rentabilidad-riesgo")
    public ResponseEntity<List<IRentabilidadPorRiesgo>> getRentabilidadPorRiesgo() {
        return this.estadisticasService.getRentabilidadPorRiesgo();
    }

    @GetMapping("/estado-licitaciones")
    public ResponseEntity<List<IEstadoLicitaciones>> getEstadoLicitaciones() {
        return this.estadisticasService.getEstadoLicitaciones();
    }

    @GetMapping("/motivo-ganada")
    public ResponseEntity<List<IMotivoGanada>> getMotivoGanada() {
        return this.estadisticasService.getMotivoGanada();
    }

    @GetMapping("/total-adjudicado-ganadas")
    public ResponseEntity<ITotalAdjudicadoGanadas> getTotalAdjudicadoGanadas() {
        return this.estadisticasService.getTotalAdjudicadoGanadas();
    }

    @GetMapping("/ranking-riesgos-ganados")
    public ResponseEntity<List<IRankingRiesgosGanados>> getRankingRiesgosGanados() {
        return this.estadisticasService.getRankingRiesgosGanados();
    }

    @GetMapping("/sobreprecio-promedio")
    public ResponseEntity<ISobreprecioPromedio> getSobreprecioPromedio() {
        return this.estadisticasService.getSobreprecioPromedio();
    }

    @GetMapping("/rentabilidad-residual-perdidas")
    public ResponseEntity<List<IRentabilidadResidualPerdidas>> getRentabilidadResidualPerdidas() {
        return this.estadisticasService.getRentabilidadResidualPerdidas();
    }

    @GetMapping("/total-desistidas")
    public ResponseEntity<ITotalDesistidas> getTotalDesistidas() {
        return this.estadisticasService.getTotalDesistidas();
    }

    @GetMapping("/top-motivos-desistidas")
    public ResponseEntity<List<ITopMotivosDesistidas>> getTopMotivosDesistidas() {
        return this.estadisticasService.getTopMotivosDesistidas();
    }

    @GetMapping("/monto-adjudicado-desistido")
    public ResponseEntity<IMontoAdjudicadoDesistido> getMontoAdjudicadoDesistido() {
        return this.estadisticasService.getMontoAdjudicadoDesistido();
    }

    @GetMapping("/renglones-desistidos")
    public ResponseEntity<List<IRenglonesDesistidos>> getRenglonesDesistidos() {
        return this.estadisticasService.getRenglonesDesistidos();
    }
}