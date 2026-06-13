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

    private final Map<Riesgo, List<String>> sinonimos;
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
     * @return el Riesgo encontrado, o null si no hay coincidencia.
     */
    public Riesgo resolverRiesgo(String token) {

        Riesgo riesgo = this.sinonimos.entrySet()
                .stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(sinonimo -> sinonimo.equalsIgnoreCase(token)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (riesgo == null) {
            riesgo = this.entidadRiesgoRepository.findByDetalle(token).orElse(null);
        }

        return riesgo;
    }

    private Map<Riesgo, List<String>> cargarSinonimos() {

        Map<Riesgo, List<String>> sinonimos = new HashMap<>();

        agregarSinonimo(sinonimos, "RC COMPRENSIVA", "RCC", "RC", "RC Comprensiva", "RC Canes");
        agregarSinonimo(sinonimos, "RC ASCENSORES", "RC Ascendores", "RC Ascensores");
        agregarSinonimo(sinonimos, "TECNICO EQ. ELECTRONICOS", "ST EE", "ST", "ST TR", "TR Equipos Electrónicos");
        agregarSinonimo(sinonimos, "APC", "ACCIDENTES PERSONALES");
        agregarSinonimo(sinonimos, "ROBO Y RIESGOS SIMILARES", "Robo", "Robo Drones");
        agregarSinonimo(sinonimos, "VALORES EN TRANSITO", "ROBO DE VALORES");
        agregarSinonimo(sinonimos, "TR INSTRUMENTOS MUSICALES", "TR Instrumentos Musicales", "TRIM");
        agregarSinonimo(sinonimos, "TR OBRAS DE ARTE", "TR Obras de Arte", "TROA");
        agregarSinonimo(sinonimos, "DRONES", "RC Drones", "RC Vant");
        agregarSinonimo(sinonimos, "INTEGRAL", "INTEGRAL", "ICO");
        agregarSinonimo(sinonimos, "AERONAVEACION", "Aeronavegación");
        agregarSinonimo(sinonimos, "TRO", "TRO");

        return sinonimos;
    }

    private void agregarSinonimo(Map<Riesgo, List<String>> map, String nombreOficial, String... variantes) {

        this.entidadRiesgoRepository.findByDetalle(nombreOficial).ifPresent(riesgo -> {
            map.put(riesgo, Arrays.asList(variantes));
        });
    }
}
