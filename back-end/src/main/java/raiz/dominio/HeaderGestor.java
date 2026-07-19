package raiz.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HeaderGestor {

    private final Integer[] headers;
    private final List<String> headersTemplate;
    // Headers reales del Excel, normalizados, en su posición de columna original.
    // Se guardan aparte del match contra headersTemplate para poder buscar
    // dinámicamente columnas numeradas (CotizadoCostoN, AdjudicadoCostoN, ...) que
    // no están en la plantilla fija, sin límite de N.
    private final List<String> headersOriginales;

    public HeaderGestor(List<String> headers, List<String> headersTemplate) {

        System.out.println("Headers: " + headers);
        System.out.println("Headers template: "  + headersTemplate);

        this.headers = new Integer[headersTemplate.size()];
        this.headersTemplate = new ArrayList<>();

        for (String template : headersTemplate) {
            this.headersTemplate.add(Normalizador.limpiarTexto(template));
        }

        this.headersOriginales = new ArrayList<>();
        for (String header : headers) {
            this.headersOriginales.add(Normalizador.limpiarTexto(header));
        }

        this.inicializarHeaders(headers);
    }

    private void inicializarHeaders(List<String> headers) {

        int i = 0;

        for(String header : headers) {

            int j = 0;

            for(String headerTemplate : this.headersTemplate) {

                String headerNormalizado = Normalizador.limpiarTexto(header);

                if(headerNormalizado.equals(headerTemplate)) {
                    this.headers[j] = i;
                    break;
                }

                j++;
            }

            i++;
        }

    }

    public Integer getHeaderIndex(String header) {

        String headerNormalizado = Normalizador.limpiarTexto(header);

        for (int i = 0; i < this.headersTemplate.size(); i++) {

            if (headerNormalizado.equals(this.headersTemplate.get(i))) {
                return this.headers[i];
            }
        }

        return null;
    }

    /**
     * Busca en el Excel real (no en headersTemplate) todas las columnas que
     * matcheen "prefijoN" -por ejemplo CotizadoCosto1, CotizadoCosto2,
     * CotizadoCosto3...- sin límite fijo de N. Devuelve una lista donde la
     * posición i corresponde al riesgo en esa misma posición (prefijo + (i+1)).
     * Si un N intermedio no existe en este Excel puntual, esa posición queda
     * en null para no romper el alineamiento con el riesgo correspondiente.
     * El tamaño de la lista es el N más alto encontrado; si no se encuentra
     * ninguna columna con ese prefijo, devuelve una lista vacía.
     */
    public List<Integer> getIndicesPorPrefijoNumerado(String prefijo) {

        String prefijoNormalizado = Normalizador.limpiarTexto(prefijo);
        Map<Integer, Integer> encontrados = new TreeMap<>();

        for (int i = 0; i < this.headersOriginales.size(); i++) {

            String header = this.headersOriginales.get(i);

            if (header.startsWith(prefijoNormalizado)) {

                String sufijo = header.substring(prefijoNormalizado.length());

                if (sufijo.matches("\\d+")) {
                    encontrados.put(Integer.parseInt(sufijo), i);
                }
            }
        }

        List<Integer> resultado = new ArrayList<>();

        if (encontrados.isEmpty()) {
            return resultado;
        }

        int maxN = Collections.max(encontrados.keySet());

        for (int n = 1; n <= maxN; n++) {
            resultado.add(encontrados.get(n)); // null si ese N puntual no existe en este Excel
        }

        return resultado;
    }

}
