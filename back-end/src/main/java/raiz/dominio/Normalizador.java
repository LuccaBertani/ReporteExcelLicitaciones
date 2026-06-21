package raiz.dominio;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class Normalizador {

    public static String limpiarTexto(String textoOriginal) {

        if (textoOriginal == null) {
            return "";
        }

        // 1. Separar los acentos de las letras (Ej: 'á' se convierte en 'a' + '´')
        String textoNormalizado = Normalizer.normalize(textoOriginal, Normalizer.Form.NFD);

        // 2. Eliminar los diacríticos (los acentos sueltos) usando expresiones regulares
        Pattern patronAcentos = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String sinAcentos = patronAcentos.matcher(textoNormalizado).replaceAll("");

        // 3. Eliminar espacios, comas y otros signos de puntuación comunes
        // [\\s,.:;?¿!¡()-] busca espacios (\s), comas, puntos, dos puntos, etc.
        String textoLimpio = sinAcentos.replaceAll("[\\s,.:;?¿!¡()\\-_]", "");

        return textoLimpio.toUpperCase();
    }
}