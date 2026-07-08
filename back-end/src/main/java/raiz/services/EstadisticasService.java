package raiz.services;

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

    public ResponseEntity<ITotalLicitacionesUnicas> cantLicitaciones(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getCantLicitaciones(fechaDesde, fechaHasta));
    }

    public ResponseEntity<IWinrateGlobal> getWinrateGlobal(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getWinrateGlobal(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IEvolucionMensual>> getEvolucionMensual(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getEvolucionMensual(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<ITopRiesgos>> getTopRiesgos(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getTopRiesgos(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<ITopClientesTasaExito>> getTopClientesTasaExito(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getTopClientesTasaExito(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IFugasPorCompetidor>> getFugasPorCompetidor(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getFugasPorCompetidor(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IPerdidasPorMotivo>> getPerdidasPorMotivo(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getPerdidasPorMotivo(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IDesvioPrecioPorMotivo>> getDesvioPrecioPorMotivo(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getDesvioPrecioPorMotivo(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IResumenMontosPorRiesgo>> getResumenMontosPorRiesgo(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getResumenMontosPorRiesgo(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IRentabilidadMensual>> getRentabilidadMensual(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadMensual(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IRentabilidadMensual>> getRentabilidadMensual(List<String> motivos, String fechaDesde, String fechaHasta){
        if (motivos == null || motivos.isEmpty()) {
            return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadMensual(fechaDesde, fechaHasta));
        }
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadMensualPorMotivos(motivos, fechaDesde, fechaHasta));
    }

    public ResponseEntity<IRentabilidadGlobal> getRentabilidadGlobal(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadGlobal(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IRentabilidadPorRiesgo>> getRentabilidadPorRiesgo(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadPorRiesgo(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IEstadoLicitaciones>> getEstadoLicitaciones(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getEstadoLicitaciones(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IMotivoGanada>> getMotivoGanada(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getMotivoGanada(fechaDesde, fechaHasta));
    }

    public ResponseEntity<ITotalAdjudicadoGanadas> getTotalAdjudicadoGanadas(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getTotalAdjudicadoGanadas(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IRankingRiesgosGanados>> getRankingRiesgosGanados(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getRankingRiesgosGanados(fechaDesde, fechaHasta));
    }

    public ResponseEntity<ISobreprecioPromedio> getSobreprecioPromedio(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getSobreprecioPromedio(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IRentabilidadResidualPerdidas>> getRentabilidadResidualPerdidas(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getRentabilidadResidualPerdidas(fechaDesde, fechaHasta));
    }

    public ResponseEntity<ITotalDesistidas> getTotalDesistidas(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getTotalDesistidas(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<ITopMotivosDesistidas>> getTopMotivosDesistidas(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getTopMotivosDesistidas(fechaDesde, fechaHasta));
    }

    public ResponseEntity<IMontoAdjudicadoDesistido> getMontoAdjudicadoDesistido(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getMontoAdjudicadoDesistido(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<IRenglonesDesistidos>> getRenglonesDesistidos(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getRenglonesDesistidos(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<String>> getMotivosDisponibles(String fechaDesde, String fechaHasta){
        return ResponseEntity.ok(this.estadisticasRepository.getMotivosDisponibles(fechaDesde, fechaHasta));
    }

    public ResponseEntity<List<ICantidadLicitacionesPorMes>> getCantidadLicitacionesPorMes(List<String> motivos, String fechaDesde, String fechaHasta){
        if (motivos == null || motivos.isEmpty()) {
            return ResponseEntity.ok(this.estadisticasRepository.getCantidadLicitacionesPorMes(fechaDesde, fechaHasta));
        }
        return ResponseEntity.ok(this.estadisticasRepository.getCantidadLicitacionesPorMesPorMotivos(motivos, fechaDesde, fechaHasta));
    }
}
