package raiz.dominio;

import java.util.ArrayList;
import java.util.List;

public class HeaderGestor {

    private final Integer[] headers;
    private final List<String> headersTemplate;

    public HeaderGestor(List<String> headers, List<String> headersTemplate) {

        this.headers = new Integer[headersTemplate.size()];
        this.headersTemplate = new ArrayList<>();

        for (String template : headersTemplate) {
            this.headersTemplate.add(Normalizador.limpiarTexto(template));
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


}
