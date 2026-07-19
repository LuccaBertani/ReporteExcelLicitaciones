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
        // [\\s,.:;?¿!¡()-_/'’] busca espacios (\s), comas, puntos, dos puntos, etc.
        // Se agregan '/' y los dos tipos de apóstrofe (recto ' y tipográfico ’, este
        // último es el que deja el autocorrector de Word/Excel) para que headers como
        // "Adjudicado/Costo1" o nombres con apóstrofe normalicen igual que sin ellos.
        // Se usa el escape unicode ’ en vez del carácter literal para evitar
        // problemas si el archivo se compila con un encoding distinto a UTF-8.
        String textoLimpio = sinAcentos.replaceAll("[\\s,.:;?¿!¡()\\-_/'\\u2019]", "");

        return textoLimpio.toUpperCase();
    }
}