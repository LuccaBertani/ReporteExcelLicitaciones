package raiz.dominio.estadisticas;

public interface ITopClientesTasaExito {

    String getRanking();
    String getCliente();
    Long getTotal_compulsas_participadas();
    String getTasa_exito_porcentaje();

}
