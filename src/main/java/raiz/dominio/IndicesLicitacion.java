package raiz.dominio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndicesLicitacion {

    private Integer indexNumeroCompulsa;
    private Integer indexRiesgo;
    private Integer indexFecha;
    private Integer indexMotivo;
    private Integer indexMontoAdjudicado;
    private Integer indexMontoCotizado;
    private List<Integer> indicesRiesgoCosto;
    private List<Integer> indicesAdjudicadoCosto;

}
