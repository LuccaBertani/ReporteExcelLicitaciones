package raiz.dominio.estadisticas;

public interface IRentabilidadMensual {

    Long getId_mes();
    String getMes();
    Double getCant_cotizada();
    Double getCant_ganada();
    String getPorcentaje_beneficio();
    String getCompulsas_ganadas();
    String getWinrate();

}
