package raiz.dominio.estadisticas;

public interface IDesvioPrecioPorMotivo {

    String getRiesgo();
    Long getLicitaciones_perdidas_riesgo();
    Long getTotal_ganadas_riesgo();
    Long getTotal_activas_riesgo();
    String getMonto_cotizado_total_riesgo();
    String getMonto_adjudicado_total_riesgo();
    String getDesvio_total_riesgo_porcentaje();

}
