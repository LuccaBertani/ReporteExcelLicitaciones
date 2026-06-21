package raiz.dominio.estadisticas;

public interface IRentabilidadPorRiesgo {

    Long getId_riesgo();
    String getRiesgo();
    Double getCant_cotizada();
    Double getCant_ganada();
    String getPorcentaje_beneficio();

}
