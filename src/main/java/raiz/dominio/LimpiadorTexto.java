package raiz.dominio;

import java.text.Normalizer;

/**
 * Utilidades de limpieza de texto orientadas a comparaciones (a diferencia
 * de Normalizador, que limpia texto para usarlo como clave de matching de
 * headers, esta clase solo remueve tildes y normaliza a mayúsculas).
 */
public class LimpiadorTexto {

    /**
     * Quita los acentos/tildes de un texto y lo convierte a mayúsculas,
     * recortando espacios. Útil para comparar strings que pueden diferir
     * solo en acentuación.
     */
    public static String limpiarTildes(String texto) {

        if (texto == null) {
            return "";
        }

        String textoNormalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);

        return textoNormalizado.replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                .trim()
                .toUpperCase();
    }
}
