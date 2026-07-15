package raiz.dtos.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resultado de importarDesdeExcel: a diferencia del comportamiento anterior
 * (una excepción en cualquier fila abortaba todo el resto del archivo en
 * silencio y el controller igual respondía "éxito"), ahora cada fila se
 * procesa de forma aislada y este DTO reporta exactamente qué se cargó y qué
 * falló, para que el front pueda mostrarlo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportacionResultadoDtoOutput {

    private boolean exitosa;
    private int filasProcesadas;
    private int filasOk;
    private int filasConError;
    private List<String> errores;

}
