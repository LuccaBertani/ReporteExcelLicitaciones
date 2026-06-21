package raiz.dominio;

/**
 * Centraliza la construcción de mensajes de error y el lanzamiento de las
 * excepciones de validación usadas durante la importación/verificación de
 * licitaciones.
 */
public class MensajesError {

    /**
     * Reporta (por stderr) y lanza una excepción cuando un valor leído del
     * Excel no coincide con el valor almacenado en base de datos para una
     * licitación dada.
     */
    public static void errorComparacion(String numCompulsa, String valorExcel, String valorDB) {

        System.err.println("Comparacion fallida entre: Excel: " + valorExcel + "DB: " + valorDB);

        throw new RuntimeException("La licitacion num" + numCompulsa + " no coincide en mes con el excel");
    }

    public static RuntimeException licitacionFaltante(String numCompulsa) {
        return new RuntimeException("[FALTANTE] Fila " + numCompulsa + " no existe en DB.");
    }

    public static RuntimeException sinLicitacionesRiesgo(String numCompulsa) {
        return new RuntimeException("La licitacion num " + numCompulsa + " no tiene licitacionesRiesgo en la db");
    }

    public static RuntimeException verificacionFallida(Long idLicitacion) {
        return new RuntimeException("Fallo el verificado de licitacionRiesgo respecto al excel de la licitacion de numero: " + idLicitacion);
    }

    public static RuntimeException licitacionNoEncontrada(String numCompulsa) {
        return new RuntimeException("Licitacion no encontrada de numero: " + numCompulsa);
    }

    public static RuntimeException coincidenciaLicitacionRiesgoNoEncontrada(String numCompulsa) {
        return new RuntimeException("Fallo el buscado de licitacionRiesgo respecto al excel de la licitacion de numero: " + numCompulsa);
    }

    public static RuntimeException riesgoNoEncontrado() {
        return new RuntimeException("Fallo el riesgo encontrado");
    }

    public static RuntimeException licitacionPorRiesgoNoEncontrada(String riesgoStr) {
        return new RuntimeException("Licitacion no encontrada de numero: " + riesgoStr);
    }

    public static RuntimeException errorLecturaHeaders(String mensaje, Throwable causa) {
        return new RuntimeException("Error crítico al leer los headers del Excel: " + mensaje, causa);
    }
}
