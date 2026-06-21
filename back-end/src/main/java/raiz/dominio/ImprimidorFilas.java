package raiz.dominio;

import java.lang.reflect.Method;

public class ImprimidorFilas {

    public static void imprimirFilaGenerica(Object fila) {
        if (fila == null) {
            System.out.println("Fila vacía (null)");
            return;
        }

        // Obtenemos todas las interfaces que implementa el objeto Proxy de Spring
        Class<?>[] interfaces = fila.getClass().getInterfaces();
        if (interfaces.length == 0) return;

        // Tomamos la interfaz principal (tu DTO)
        Class<?> interfazDto = interfaces[0];
        StringBuilder resultado = new StringBuilder();

        // Recorremos todos los métodos declarados en tu interfaz
        for (Method metodo : interfazDto.getDeclaredMethods()) {
            // Nos aseguramos de procesar solo los métodos que empiecen con "get"
            if (metodo.getName().startsWith("get") && metodo.getParameterCount() == 0) {
                try {
                    // 1. Limpiamos el nombre del método para dejarlo como NombreColumna
                    // Ej: "getCantidad_licitaciones" -> "Cantidad_licitaciones"
                    String nombreColumna = metodo.getName().substring(3);

                    // 2. Invocamos dinámicamente el método sobre la fila para obtener el valor real
                    Object valor = metodo.invoke(fila);

                    // 3. Lo acumulamos en un formato limpio
                    resultado.append(nombreColumna).append(": ").append(valor).append(" | ");
                } catch (Exception e) {
                    // Si algún método falla, lo ignoramos y continuamos con el siguiente
                    resultado.append(metodo.getName()).append(": [Error al leer] | ");
                }
            }
        }

        // Imprimimos la línea en consola (quitando el último separador ' | ')
        if (resultado.length() > 0) {
            System.out.println(resultado.substring(0, resultado.length() - 3));
        }
    }

}
