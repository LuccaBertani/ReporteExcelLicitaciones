package raiz.dominio;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsula la lectura y formateo de celdas de un Excel, aislando a los
 * consumidores de los detalles de la API de Apache POI.
 */
public class LectorCeldas {

    private final DataFormatter formatter;

    public LectorCeldas() {
        this.formatter = new DataFormatter();
    }

    /**
     * Devuelve el valor de la celda formateado como String, igual que
     * formatter.formatCellValue, manejando celdas nulas.
     */
    public String leerComoTexto(Row row, int index) {

        if (row == null) {
            return "";
        }

        Cell cell = row.getCell(index);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell);
    }

    /**
     * Igual que leerComoTexto pero recortando espacios, devolviendo "" si la
     * celda es nula o está en blanco.
     */
    public String leerCeldaRecortada(Row row, int index) {

        if (row == null) {
            return "";
        }

        Cell cell = row.getCell(index);

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Formatos de fecha en texto que aceptamos cuando la celda viene
     * tipeada como STRING en vez de NUMERIC (caso común cuando la fecha
     * fue pegada como texto o exportada desde otro sistema).
     */
    private static final DateTimeFormatter[] FORMATOS_FECHA_TEXTO = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    };

    /**
     * Lee el valor de tipo fecha de una celda, o null si la celda es nula,
     * está vacía, o no se pudo interpretar como fecha.
     *
     * Soporta celdas NUMERIC (fecha real de Excel) y celdas STRING cuyo
     * texto representa una fecha en alguno de los formatos habituales
     * (dd/MM/yyyy, etc.), ya que algunas filas llegan con la fecha
     * tipeada como texto en lugar de como valor numérico de fecha.
     */
    public java.sql.Date leerFecha(Row row, int index) {

        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(index);

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.STRING) {
            return parsearFechaDesdeTexto(cell.getStringCellValue());
        }

        try {
            // POI moderno permite obtener directamente LocalDate libre de zonas horarias
            LocalDateTime ldt = cell.getLocalDateTimeCellValue();
            return java.sql.Date.valueOf(ldt.toLocalDate());
        } catch (IllegalStateException e) {
            // Defensa adicional: si por algún motivo el tipo reportado no
            // coincide con lo que POI puede convertir a fecha, intentamos
            // como texto antes de rendirnos.
            return parsearFechaDesdeTexto(formatter.formatCellValue(cell));
        }
    }

    private java.sql.Date parsearFechaDesdeTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        String valor = texto.trim();

        for (DateTimeFormatter formato : FORMATOS_FECHA_TEXTO) {
            try {
                LocalDate fecha = LocalDate.parse(valor, formato);
                return java.sql.Date.valueOf(fecha);
            } catch (DateTimeParseException ignored) {
                // probamos el siguiente formato
            }
        }

        return null;
    }

    /**
     * Intenta extraer un valor numérico (Double) de una celda, manejando
     * celdas numéricas, fórmulas con resultado numérico, o strings que
     * representen números. Devuelve null si no es posible.
     */
    public Double leerComoDouble(Cell cell) {

        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case FORMULA:
                if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                    return cell.getNumericCellValue();
                }
                return null;
            case STRING:
                try {
                    // Formato USA: coma como separador de miles, punto como
                    // decimal (ej. "1,234.56"). Solo hace falta quitar las
                    // comas de miles; el punto decimal ya es válido para
                    // Double.parseDouble.
                    String val = cell.getStringCellValue().replace(",", "").trim();
                    return Double.parseDouble(val);
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Indica si la celda representa un valor "vacío" o inválido a efectos
     * de montos: nula, o un string que contenga '#' (p.ej. "#N/A").
     */
    public boolean esCeldaVaciaOInvalida(Cell cell) {

        if (cell == null) {
            return true;
        }

        return cell.getCellType() == CellType.STRING
                && cell.getStringCellValue().contains("#");
    }

    /**
     * Lee la fila de encabezados (fila 0) de la primera hoja del workbook,
     * devolviendo "" para celdas vacías.
     */
    public List<String> leerHeaders(Workbook workbook) {

        List<String> headers = new ArrayList<>();

        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.iterator().next();

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell celda = headerRow.getCell(i);
            if (celda != null) {
                headers.add(celda.getStringCellValue());
            } else {
                headers.add("");
            }
        }

        return headers;
    }
}
