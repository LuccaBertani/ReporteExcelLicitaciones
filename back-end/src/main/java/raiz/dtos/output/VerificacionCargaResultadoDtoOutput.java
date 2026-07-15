package raiz.dtos.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionCargaResultadoDtoOutput {

    private boolean exitosa;
    private int renglonesOk;
    private int renglonesFaltantes;
    private int renglonesMalMonto;
    private int renglonesMalCotizado;
    private List<String> incidencias;

}
