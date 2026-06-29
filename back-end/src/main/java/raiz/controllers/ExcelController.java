package raiz.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import raiz.componentes.InsertorDatos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/excel")
@CrossOrigin(origins = "*")
public class ExcelController {

    private final InsertorDatos insertorDatos;

    public ExcelController(InsertorDatos insertorDatos) {
        this.insertorDatos = insertorDatos;
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, String>> importarExcel(@RequestParam("archivo") MultipartFile archivo) {

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

            insertorDatos.importarDesdeExcel(tempFile.getAbsolutePath());

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "mensaje", "Importación completada correctamente.",
                    "archivo", nombreOriginal
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "mensaje", "Error al procesar el archivo: " + e.getMessage()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
