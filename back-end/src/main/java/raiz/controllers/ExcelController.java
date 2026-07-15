package raiz.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import raiz.componentes.InsertorDatos;
import raiz.dtos.output.ImportacionResultadoDtoOutput;
import raiz.dtos.output.VerificacionCargaResultadoDtoOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/excel")
public class ExcelController {

    private final InsertorDatos insertorDatos;

    public ExcelController(InsertorDatos insertorDatos) {
        this.insertorDatos = insertorDatos;
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importarExcel(@RequestParam("archivo") MultipartFile archivo) {

        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "mensaje", "El archivo está vacío."));
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "mensaje", "Solo se aceptan archivos .xlsx"));
        }

        File tempFile = null;
        try {
            // Guardamos el MultipartFile en un archivo temporal para que el InsertorDatos lo lea
            tempFile = Files.createTempFile("licitaciones_", ".xlsx").toFile();
            archivo.transferTo(tempFile);

            ImportacionResultadoDtoOutput resultado = insertorDatos.importarDesdeExcel(tempFile.getAbsolutePath());

            // Antes esto siempre respondía "ok" aunque el import hubiera fallado a mitad de
            // camino (la excepción se atrapaba en silencio adentro de importarDesdeExcel).
            // Ahora el resultado refleja fila por fila qué se cargó y qué no.
            Map<String, Object> body = new HashMap<>();
            body.put("status", resultado.isExitosa() ? "ok" : "parcial");
            body.put("archivo", nombreOriginal);
            body.put("filasProcesadas", resultado.getFilasProcesadas());
            body.put("filasOk", resultado.getFilasOk());
            body.put("filasConError", resultado.getFilasConError());
            body.put("errores", resultado.getErrores());
            body.put("mensaje", resultado.isExitosa()
                    ? "Importación completada correctamente (" + resultado.getFilasOk() + " filas)."
                    : resultado.getFilasConError() + " de " + resultado.getFilasProcesadas()
                            + " fila(s) no se pudieron cargar. Ver detalle de errores.");

            return ResponseEntity.ok(body);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "mensaje", "Error al procesar el archivo: " + e.getMessage()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Corre InsertorDatos.verificarCarga() sobre el Excel recibido y
     * devuelve el resultado como JSON para que el front lo muestre. Es una
     * auditoría de solo lectura: compara el Excel contra lo que ya está en
     * la base de datos, no inserta ni modifica nada.
     */
    @PostMapping("/verificar")
    public ResponseEntity<?> verificarCarga(@RequestParam("archivo") MultipartFile archivo) {

        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "mensaje", "El archivo está vacío."));
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "mensaje", "Solo se aceptan archivos .xlsx"));
        }

        File tempFile = null;
        try {
            tempFile = Files.createTempFile("verificacion_", ".xlsx").toFile();
            archivo.transferTo(tempFile);

            VerificacionCargaResultadoDtoOutput resultado = insertorDatos.verificarCarga(tempFile.getAbsolutePath());

            return ResponseEntity.ok(resultado);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "mensaje", "Error al procesar el archivo: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "mensaje", "Error al verificar la carga: " + e.getMessage()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
