package raiz.dominio.estadisticas;

public interface IDesvioPrecioPorMotivo {

    String getRiesgo();
    String getMotivo();
    Long getLicitaciones_perdidas_por_motivo();
    Long getTotal_ganadas_riesgo();
    Long getTotal_activas_riesgo();
    String getMonto_cotizado_total_riesgo();
    String getMonto_adjudicado_total_riesgo();
    String getDesvio_total_riesgo_porcentaje();

}
