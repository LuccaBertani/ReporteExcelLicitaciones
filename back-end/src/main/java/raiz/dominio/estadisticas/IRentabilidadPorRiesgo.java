package raiz.dominio.estadisticas;

public interface IRentabilidadPorRiesgo {

    Long getId_riesgo();
    String getRiesgo();
    String getCant_cotizada();
    String getCant_ganada();
    String getPorcentaje_beneficio();
    String getCompulsas_ganadas();
    String getCompulsas_totales();
    String getWinrate();

}
