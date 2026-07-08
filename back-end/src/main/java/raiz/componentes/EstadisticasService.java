package raiz.componentes;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import raiz.Repositories.IEstadisticasRepository;
import raiz.dominio.estadisticas.*;

import java.util.List;

@Service
public class EstadisticasService {

    private final IEstadisticasRepository estadisticasRepository;

    public EstadisticasService(IEstadisticasRepository estadisticasRepository) {
        this.estadisticasRepository = estadisticasRepository;
    }

    public ResponseEntity<ITotalLicitacionesUnicas> cantLicitaciones(){
        return ResponseEntity.ok(this.estadisticasRepository.getCantLicitaciones());
    }

    public ResponseEntity<IWinrateGlobal> getWinrateGlobal(){
        return ResponseEntity.ok(this.estadisticasRepository.getWinrateGlobal());
    }

    public ResponseEntity<List<IEvolucionMensual>> getEvolucionMensual(){
        return ResponseEntity.ok(this.estadisticasRepository.getEvolucionMensual());
    }

    public ResponseEntity<List<ITopRiesgos>> getTopRiesgos(){
        return ResponseEntity.ok(this.estadisticasRepository.getTopRiesgos());
    }

    public ResponseEntity<List<ITopClientesTasaExito>> getTopClientesTasaExito(){
        return ResponseEntity.ok(this.estadisticasRepository.getTopClientesTasaExito());
    }

    public ResponseEntity<List<IFugasPorCompetidor>> getFugasPorCompetidor(){
        return ResponseEntity.ok(this.estadisticasRepository.getFugasPorCompetidor());
    }

    public ResponseEntity<List<IPerdidasPorMotivo>> getPerdidasPorMotivo(){
        return ResponseEntity.ok(this.estadisticasRepository.getPerdidasPorMotivo());
    }

    public ResponseEntity<List<IDesvioPrecioPorMotivo>> getDesvioPrecioPorMotivo(){
        return ResponseEntity.ok(this.estadisticasRepository.getDesvioPrecioPorMotivo());
    }

    public ResponseEntity<List<IResumenMontosPorRiesgo>> getResumenMontosPorRiesgo(){
        return ResponseEntity.ok(this.estadisticasRepository.getResumenMontosPorRiesgo());
    }

    public ResponseEntity<List<IRentabilidadMensual>> getRentabilidadMensual(){
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadMensual());
    }

    public ResponseEntity<List<IRentabilidadMensual>> getRentabilidadMensual(List<String> motivos){
        if (motivos == null || motivos.isEmpty()) {
            return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadMensual());
        }
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadMensualPorMotivos(motivos));
    }

    public ResponseEntity<IRentabilidadGlobal> getRentabilidadGlobal(){
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadGlobal());
    }

    public ResponseEntity<List<IRentabilidadPorRiesgo>> getRentabilidadPorRiesgo(){
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadPorRiesgo());
    }

    public ResponseEntity<List<IEstadoLicitaciones>> getEstadoLicitaciones(){
        return ResponseEntity.ok(this.estadisticasRepository.getEstadoLicitaciones());
    }

    public ResponseEntity<List<IMotivoGanada>> getMotivoGanada(){
        return ResponseEntity.ok(this.estadisticasRepository.getMotivoGanada());
    }

    public ResponseEntity<ITotalAdjudicadoGanadas> getTotalAdjudicadoGanadas(){
        return ResponseEntity.ok(this.estadisticasRepository.getTotalAdjudicadoGanadas());
    }

    public ResponseEntity<List<IRankingRiesgosGanados>> getRankingRiesgosGanados(){
        return ResponseEntity.ok(this.estadisticasRepository.getRankingRiesgosGanados());
    }

    public ResponseEntity<ISobreprecioPromedio> getSobreprecioPromedio(){
        return ResponseEntity.ok(this.estadisticasRepository.getSobreprecioPromedio());
    }

    public ResponseEntity<List<IRentabilidadResidualPerdidas>> getRentabilidadResidualPerdidas(){
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadResidualPerdidas());
    }

    public ResponseEntity<ITotalDesistidas> getTotalDesistidas(){
        return ResponseEntity.ok(this.estadisticasRepository.getTotalDesistidas());
    }

    public ResponseEntity<List<ITopMotivosDesistidas>> getTopMotivosDesistidas(){
        return ResponseEntity.ok(this.estadisticasRepository.getTopMotivosDesistidas());
    }

    public ResponseEntity<IMontoAdjudicadoDesistido> getMontoAdjudicadoDesistido(){
        return ResponseEntity.ok(this.estadisticasRepository.getMontoAdjudicadoDesistido());
    }

    public ResponseEntity<List<IRenglonesDesistidos>> getRenglonesDesistidos(){
        return ResponseEntity.ok(this.estadisticasRepository.getRenglonesDesistidos());
    }

    public ResponseEntity<List<String>> getMotivosDisponibles(){
        return ResponseEntity.ok(this.estadisticasRepository.getMotivosDisponibles());
    }

    public ResponseEntity<List<ICantidadLicitacionesPorMes>> getCantidadLicitacionesPorMes(List<String> motivos){
        if (motivos == null || motivos.isEmpty()) {
            return ResponseEntity.ok(this.estadisticasRepository.getCantidadLicitacionesPorMes());
        }
        return ResponseEntity.ok(this.estadisticasRepository.getCantidadLicitacionesPorMesPorMotivos(motivos));
    }
}