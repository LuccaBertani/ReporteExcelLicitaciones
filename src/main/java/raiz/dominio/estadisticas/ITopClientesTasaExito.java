package raiz.dominio.estadisticas;

public interface ITopClientesTasaExito {

    String getRanking();
    String getCliente();
    Long getTotal_licitaciones();
    String getTasa_exito_porcentaje();

}
