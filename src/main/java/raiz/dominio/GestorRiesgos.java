package raiz.dominio;

import raiz.Repositories.IRiesgo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encapsula la carga y consulta de sinónimos de Riesgo, así como la
 * tokenización de celdas que contienen múltiples riesgos separados por
 * '/', ';' o ','.
 */
public class GestorRiesgos {

    private final Map<String, List<String>> sinonimos;
    private final IRiesgo entidadRiesgoRepository;

    public GestorRiesgos(IRiesgo entidadRiesgoRepository) {
        this.entidadRiesgoRepository = entidadRiesgoRepository;
        this.sinonimos = cargarSinonimos();
    }

    /**
     * Separa un texto de riesgos compactados (p.ej. "RC / INCENDIO") en
     * tokens individuales, recortados y sin elementos vacíos.
     */
    public List<String> obtenerTokens(String texto) {

        if (texto == null || texto.isEmpty()) {
            return List.of();
        }

        String[] tokens = texto.split("\\s*[/;,]+\\s*");

        return Arrays.stream(tokens)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Busca el Riesgo oficial correspondiente a un token, ya sea porque el
     * token es un sinónimo conocido o porque coincide directamente con el
     * detalle de un Riesgo en base de datos.
     *
     * @return el Riesgo encontrado, o null si no hay coincidencia en DB.
     */
    public Riesgo resolverRiesgo(String token) {

        String nombreOficial = this.resolverNombreOficial(token);

        if (nombreOficial != null) {
            return this.entidadRiesgoRepository.findByDetalle(nombreOficial).orElse(null);
        }

        return this.entidadRiesgoRepository.findByDetalle(token).orElse(null);
    }

    /**
     * Traduce un token (sinónimo o nombre oficial) al nombre oficial del
     * Riesgo, sin depender de que ese Riesgo ya exista en base de datos.
     * La comparación se hace normalizando tildes, espacios y mayúsculas
     * en ambos lados.
     *
     * @return el nombre oficial, o null si el token no coincide con ningún
     * sinónimo conocido.
     */
    public String resolverNombreOficial(String token) {

        String tokenNormalizado = Normalizador.limpiarTexto(token);

        return this.sinonimos.entrySet()
                .stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(sinonimo -> Normalizador.limpiarTexto(sinonimo).equals(tokenNormalizado)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private Map<String, List<String>> cargarSinonimos() {

        Map<String, List<String>> sinonimos = new HashMap<>();

        sinonimos.put("RC COMPRENSIVA", Arrays.asList("RCC", "RC", "RC Comprensiva", "RC Canes"));
        sinonimos.put("RC ASCENSORES", Arrays.asList("RC Ascendores", "RC Ascensores"));
        sinonimos.put("RC CALDERAS", Arrays.asList("RC Calderas", "Calderas"));
        sinonimos.put("RC GUARDA/DEPOSITO", Arrays.asList("RC Guarda/Deposito", "RC Guarda y Deposito", "Guarda y Deposito"));
        sinonimos.put("RC CARTELES", Arrays.asList("RC Carteles", "Carteles"));
        sinonimos.put("INCENDIO", List.of("Incendio"));
        sinonimos.put("TECNICO EQ. ELECTRONICOS", Arrays.asList("ST EE", "ST", "ST TR", "TR Equipos Electrónicos"));
        sinonimos.put("APC", Arrays.asList("ACCIDENTES PERSONALES", "APC"));
        sinonimos.put("ROBO Y RIESGOS SIMILARES", Arrays.asList("Robo", "Robo Drones"));
        sinonimos.put("VALORES EN TRANSITO", Arrays.asList("ROBO DE VALORES", "Valores en Transito"));
        sinonimos.put("VALORES EN CAJA", Arrays.asList("Valores en Caja", "Valores en Caja y Cofre"));
        sinonimos.put("TR INSTRUMENTOS MUSICALES", Arrays.asList("TR Instrumentos Musicales", "TRIM"));
        sinonimos.put("TR OBRAS DE ARTE", Arrays.asList("TR Obras de Arte", "TROA"));
        sinonimos.put("DRONES", Arrays.asList("RC Drones", "RC Vant"));
        sinonimos.put("INTEGRAL", Arrays.asList("INTEGRAL", "ICO"));
        sinonimos.put("AERONAVEACION", Arrays.asList("Aeronavegación", "Aeronavegacion"));
        sinonimos.put("CAUCION", List.of("Caucion"));
        sinonimos.put("TRO", List.of("TRO"));
        sinonimos.put("TRANSPORTE", List.of("Transporte"));
        sinonimos.put("SEPELIO", List.of("Sepelio"));
        sinonimos.put("VIDA", List.of("Vida"));
        sinonimos.put("SALUD", List.of("Salud"));
        sinonimos.put("FRANQUICIAS", List.of("Franquicias"));

        return sinonimos;
    }
}
