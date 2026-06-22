package raiz.Repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import raiz.dominio.Licitacion;
import raiz.dominio.estadisticas.IEvolucionMensual;
import raiz.dominio.estadisticas.ITopRiesgos;
import raiz.dominio.estadisticas.ITotalLicitacionesUnicas;
import raiz.dominio.estadisticas.IWinrateGlobal;
import raiz.dominio.estadisticas.ITopClientesTasaExito;
import raiz.dominio.estadisticas.IFugasPorCompetidor;
import raiz.dominio.estadisticas.IPerdidasPorMotivo;
import raiz.dominio.estadisticas.IDesvioPrecioPorMotivo;
import raiz.dominio.estadisticas.IResumenMontosPorRiesgo;
import raiz.dominio.estadisticas.IRentabilidadMensual;
import raiz.dominio.estadisticas.IRentabilidadGlobal;
import raiz.dominio.estadisticas.IRentabilidadPorRiesgo;
import raiz.dominio.estadisticas.IEstadoLicitaciones;
import raiz.dominio.estadisticas.IMotivoGanada;
import raiz.dominio.estadisticas.ITotalAdjudicadoGanadas;
import raiz.dominio.estadisticas.IRankingRiesgosGanados;
import raiz.dominio.estadisticas.ISobreprecioPromedio;
import raiz.dominio.estadisticas.IRentabilidadResidualPerdidas;
import raiz.dominio.estadisticas.ITotalDesistidas;
import raiz.dominio.estadisticas.ITopMotivosDesistidas;
import raiz.dominio.estadisticas.IMontoAdjudicadoDesistido;
import raiz.dominio.estadisticas.IRenglonesDesistidos;

import java.util.List;

@Repository
public interface IEstadisticasRepository extends org.springframework.data.repository.Repository<Licitacion, Long>{

    @Query(value = "SELECT COUNT(DISTINCT numero_compulsa) AS cant_licitaciones \n" +
            "FROM licitacion", nativeQuery = true)
    ITotalLicitacionesUnicas getCantLicitaciones();

    @Query(value = "SELECT \n" +
            "    FORMAT(\n" +
            "        (COUNT(*) / (SELECT COUNT(*) FROM licitacion_riesgo WHERE id_status <> 2)) * 100, \n" +
            "        2, \n" +
            "        'de_DE'\n" +
            "    ) AS winrate \n" +
            "FROM licitacion_riesgo \n" +
            "WHERE id_status = 3", nativeQuery = true)
    IWinrateGlobal getWinrateGlobal();

    @Query(value = "SELECT \n" +
            "    COALESCE(m.detalle, 'Sin Mes Asignado') AS mes, \n" +
            "    COUNT(DISTINCT l.numero_compulsa) AS cantidad_licitaciones, \n" +
            "    FORMAT(SUM(l_r.monto_cotizado), 2, 'de_DE') AS total_cotizado, \n" +
            "    FORMAT(SUM(l_r.monto_adjudicado), 2, 'de_DE') AS total_adjudicado\n" +
            "FROM licitacion_riesgo AS l_r \n" +
            "LEFT JOIN Mes AS m ON m.id = l_r.id_mes\n" +
            "LEFT JOIN Licitacion AS l ON l.id = l_r.id_licitacion\n" +
            "GROUP BY m.detalle, l_r.id_mes\n" +
            "ORDER BY l_r.id_mes ASC", nativeQuery = true)
    List<IEvolucionMensual> getEvolucionMensual();

    @Query(value = "WITH RankingsComoCTE AS (\n" +
            "    SELECT \n" +
            "        e_a.detalle AS compania,\n" +
            "        r.detalle AS riesgo,\n" +
            "        SUM(l_r.monto_cotizado) AS monto_cotizado,\n" +
            "        SUM(l_r.monto_adjudicado) AS monto_adjudicado,\n" +
            "        \n" +
            "        -- Generamos los rankings numéricos basados en los totales pesificados\n" +
            "        ROW_NUMBER() OVER (ORDER BY SUM(l_r.monto_cotizado) DESC) AS rank_cotizado,\n" +
            "        ROW_NUMBER() OVER (ORDER BY SUM(l_r.monto_adjudicado) DESC) AS rank_adjudicado\n" +
            "    FROM licitacion_riesgo AS l_r\n" +
            "    INNER JOIN riesgo AS r ON r.id = l_r.id_riesgo\n" +
            "    INNER JOIN licitacion AS l ON l.id = l_r.id_licitacion\n" +
            "    INNER JOIN entidad_adjudicada AS e_a ON e_a.id = l_r.id_adjudicada\n" +
            "    GROUP BY e_a.detalle, r.detalle\n" +
            "),\n" +
            "TopCotizado AS (\n" +
            "    SELECT \n" +
            "        rank_cotizado AS ranking,\n" +
            "        compania,\n" +
            "        riesgo,\n" +
            "        FORMAT(monto_cotizado, 2, 'de_DE') AS monto_cotizado,\n" +
            "        FORMAT(monto_adjudicado, 2, 'de_DE') AS monto_adjudicado,\n" +
            "        'Cotizado' AS tipo\n" +
            "    FROM RankingsComoCTE\n" +
            "    WHERE rank_cotizado <= 5\n" +
            "),\n" +
            "TopAdjudicado AS (\n" +
            "    SELECT \n" +
            "        rank_adjudicado AS ranking,\n" +
            "        compania,\n" +
            "        riesgo,\n" +
            "        FORMAT(monto_cotizado, 2, 'de_DE') AS monto_cotizado,\n" +
            "        FORMAT(monto_adjudicado, 2, 'de_DE') AS monto_adjudicado,\n" +
            "        'Adjudicado' AS tipo\n" +
            "    FROM RankingsComoCTE\n" +
            "    WHERE rank_adjudicado <= 5\n" +
            ")\n" +
            "SELECT ranking, compania, riesgo, monto_cotizado, monto_adjudicado, tipo \n" +
            "FROM TopCotizado\n" +
            "UNION ALL\n" +
            "SELECT ranking, compania, riesgo, monto_cotizado, monto_adjudicado, tipo \n" +
            "FROM TopAdjudicado\n" +
            "ORDER BY tipo DESC, ranking ASC", nativeQuery = true)
    List<ITopRiesgos> getTopRiesgos();

    // ============================================================================
    // 7. SHARE DE CLIENTES Y SU TASA DE ÉXITO (TOP 5)
    // ============================================================================
    @Query(value = "WITH TotalPorCliente AS (\n" +
            "    SELECT \n" +
            "        l.id_cliente,\n" +
            "        COUNT(CASE WHEN s.detalle <> 'Desistida' THEN l_r.id END) AS total_compulsas_participadas,\n" +
            "        COUNT(CASE WHEN s.detalle = 'Ganada' THEN l_r.id END) AS ganadas\n" +
            "    FROM licitacion_riesgo AS l_r\n" +
            "    INNER JOIN licitacion AS l ON l.id = l_r.id_licitacion\n" +
            "    INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "    GROUP BY l.id_cliente\n" +
            "),\n" +
            "RankingClientes AS (\n" +
            "    SELECT \n" +
            "        cl.detalle AS cliente,\n" +
            "        t.total_compulsas_participadas,\n" +
            "        FORMAT((t.ganadas * 100.0) / NULLIF(t.total_compulsas_participadas, 0), 2, 'de_DE') AS tasa_exito_porcentaje,\n" +
            "        ROW_NUMBER() OVER (ORDER BY t.total_compulsas_participadas DESC) AS ranking\n" +
            "    FROM TotalPorCliente AS t\n" +
            "    INNER JOIN cliente AS cl ON cl.id = t.id_cliente\n" +
            ")\n" +
            "SELECT ranking, cliente, total_compulsas_participadas, tasa_exito_porcentaje\n" +
            "FROM RankingClientes\n" +
            "WHERE ranking <= 5", nativeQuery = true)
    List<ITopClientesTasaExito> getTopClientesTasaExito();

    // ============================================================================
    // 8. ANÁLISIS COMPETITIVO: FUGAS POR COMPETIDOR
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    ea.detalle AS competidor,\n" +
            "    COUNT(*) AS compulsas_ganadas,\n" +
            "    FORMAT(SUM(l_r.monto_adjudicado), 2, 'de_DE') AS total_monto_ganado,\n" +
            "    FORMAT(\n" +
            "        SUM(l_r.monto_adjudicado) * 100.0 / SUM(SUM(l_r.monto_adjudicado)) OVER(), \n" +
            "        2, \n" +
            "        'de_DE'\n" +
            "    ) AS porcentaje_tenencia_volumen\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN entidad_adjudicada AS ea ON ea.id = l_r.id_adjudicada \n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "GROUP BY ea.detalle\n" +
            "HAVING SUM(l_r.monto_adjudicado) > 0\n" +
            "ORDER BY compulsas_ganadas DESC", nativeQuery = true)
    List<IFugasPorCompetidor> getFugasPorCompetidor();

    // ============================================================================
    // 9. VOLUMEN ECONÓMICO DE PÉRDIDAS POR MOTIVO
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    l_r.motivo,\n" +
            "    COUNT(*) AS cantidad_casos,\n" +
            "    FORMAT(SUM(l_r.monto_adjudicado), 2, 'de_DE') AS monto_adjudicado_perdido\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Perdida' AND l_r.motivo IS NOT NULL\n" +
            "GROUP BY l_r.motivo\n" +
            "ORDER BY cantidad_casos DESC", nativeQuery = true)
    List<IPerdidasPorMotivo> getPerdidasPorMotivo();

    // ============================================================================
    // 10. CORRELACIÓN: DESVÍO DE PRECIO VS MOTIVO DE RECHAZO (POR RENGLÓN)
    // ============================================================================
    @Query(value = "WITH MetricasPorRiesgo AS (\n" +
            "    SELECT \n" +
            "        l_r.id_riesgo,\n" +
            "        COUNT(DISTINCT l_r.id_licitacion) AS licitaciones_totales,\n" +
            "        COUNT(DISTINCT CASE WHEN s.detalle = 'Ganada' THEN l_r.id_licitacion END) AS licitaciones_ganadas,\n" +
            "        SUM(l_r.monto_cotizado) AS monto_cotizado_total,\n" +
            "        SUM(l_r.monto_adjudicado) AS monto_adjudicado_total\n" +
            "    FROM licitacion_riesgo AS l_r\n" +
            "    INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "    WHERE s.detalle <> 'Desistida'\n" +
            "    GROUP BY l_r.id_riesgo\n" +
            ")\n" +
            "SELECT \n" +
            "    r.detalle AS riesgo,\n" +
            "    l_r.motivo,\n" +
            "    COUNT(DISTINCT l_r.id_licitacion) AS licitaciones_perdidas_por_motivo,\n" +
            "    m_r.licitaciones_ganadas AS total_ganadas_riesgo,\n" +
            "    m_r.licitaciones_totales AS total_activas_riesgo,\n" +
            "    FORMAT(m_r.monto_cotizado_total, 2, 'de_DE') AS monto_cotizado_total_riesgo,\n" +
            "    FORMAT(m_r.monto_adjudicado_total, 2, 'de_DE') AS monto_adjudicado_total_riesgo,\n" +
            "    FORMAT(\n" +
            "        ((m_r.monto_cotizado_total - m_r.monto_adjudicado_total) / NULLIF(m_r.monto_adjudicado_total, 0)) * 100, \n" +
            "        2, \n" +
            "        'de_DE'\n" +
            "    ) AS desvio_total_riesgo_porcentaje\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "INNER JOIN riesgo AS r ON r.id = l_r.id_riesgo\n" +
            "INNER JOIN MetricasPorRiesgo AS m_r ON m_r.id_riesgo = l_r.id_riesgo\n" +
            "WHERE s.detalle = 'Perdida' \n" +
            "  AND l_r.motivo IS NOT NULL\n" +
            "GROUP BY \n" +
            "    r.id, \n" +
            "    r.detalle, \n" +
            "    l_r.motivo, \n" +
            "    m_r.licitaciones_totales, \n" +
            "    m_r.licitaciones_ganadas,\n" +
            "    m_r.monto_cotizado_total,\n" +
            "    m_r.monto_adjudicado_total\n" +
            "ORDER BY \n" +
            "    r.detalle ASC, \n" +
            "    COUNT(DISTINCT l_r.id_licitacion) DESC", nativeQuery = true)
    List<IDesvioPrecioPorMotivo> getDesvioPrecioPorMotivo();

    // ============================================================================
    // 10.1 RESUMEN DE MONTOS COTIZADOS Y ADJUDICADOS POR RIESGO (EXCLUYENDO DESISTIDAS)
    // ============================================================================
    @Query(value = "SELECT r.detalle AS riesgo, FORMAT(SUM(l_r.monto_cotizado), 2, 'de_DE') AS monto_cotizado, FORMAT(SUM(l_r.monto_adjudicado), 2, 'de_DE') AS monto_adjudicado \n" +
            "FROM licitacion_riesgo l_r \n" +
            "INNER JOIN riesgo AS r ON r.id = l_r.id_riesgo\n" +
            "WHERE l_r.id_status <> 2\n" +
            "GROUP BY r.detalle\n" +
            "ORDER BY SUM(l_r.monto_cotizado) DESC, SUM(l_r.monto_adjudicado) DESC", nativeQuery = true)
    List<IResumenMontosPorRiesgo> getResumenMontosPorRiesgo();

    // ============================================================================
    // 12. RENTABILIDAD FINANCIERA MENSUAL (APERTURA POR MES)
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    id_mes, mes, cant_cotizada, cant_ganada, \n" +
            "    FORMAT((cant_ganada * 100.0) / NULLIF(cant_cotizada, 0), 2, 'de_DE') AS porcentaje_beneficio,\n" +
            "    FORMAT(licitaciones_ganadas, 0, 'de_DE') AS compulsas_ganadas,\n" +
            "    FORMAT((licitaciones_ganadas * 100.0) / NULLIF(licitaciones_totales, 0), 2, 'de_DE') AS winrate\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        l_r.id_mes, COALESCE(m.detalle, 'Sin Mes Asignado') AS mes,\n" +
            "        SUM(l_r.monto_cotizado) AS cant_cotizada,\n" +
            "        SUM(CASE WHEN l_r.id_status = 3 THEN l_r.monto_adjudicado ELSE 0 END) AS cant_ganada,\n" +
            "        COUNT(DISTINCT l.numero_compulsa) AS licitaciones_totales,\n" +
            "        SUM(CASE WHEN l_r.id_status = 3 THEN 1 ELSE 0 END) AS licitaciones_ganadas\n" +
            "    FROM licitacion_riesgo AS l_r\n" +
            "    LEFT JOIN mes AS m ON m.id = l_r.id_mes\n" +
            "    INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "    INNER JOIN licitacion AS l ON l.id = l_r.id_licitacion\n" +
            "    WHERE s.detalle <> 'Desistida'\n" +
            "    GROUP BY l_r.id_mes, m.detalle\n" +
            ") AS subconsulta ORDER BY id_mes ASC", nativeQuery = true)
    List<IRentabilidadMensual> getRentabilidadMensual();

    // ============================================================================
    // 12. RENTABILIDAD FINANCIERA GLOBAL (PORCENTAJE DE BENEFICIO)
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    SUM(l_r.monto_cotizado) AS cant_cotizada,\n" +
            "    SUM(CASE WHEN l_r.id_status = 3 THEN l_r.monto_adjudicado ELSE 0 END) AS cant_ganada,\n" +
            "    FORMAT((SUM(CASE WHEN l_r.id_status = 3 THEN l_r.monto_adjudicado ELSE 0 END) * 100.0) / NULLIF(SUM(l_r.monto_cotizado), 0), 2, 'de_DE') AS porcentaje_beneficio\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle <> 'Desistida'", nativeQuery = true)
    IRentabilidadGlobal getRentabilidadGlobal();

    // ============================================================================
    // 12. RENTABILIDAD FINANCIERA POR RIESGO (PORCENTAJE DE BENEFICIO)
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    id_riesgo, \n" +
            "    riesgo, \n" +
            "    FORMAT(cant_cotizada, 2, 'de_DE') AS cant_cotizada, \n" +
            "    FORMAT(cant_ganada, 2, 'de_DE') AS cant_ganada, \n" +
            "    FORMAT((cant_ganada * 100.0) / NULLIF(cant_cotizada, 0), 2, 'de_DE') AS porcentaje_beneficio,\n" +
            "    FORMAT(licitaciones_ganadas, 0, 'de_DE') AS licitaciones_ganadas,\n" +
            "    FORMAT(licitaciones_totales, 0, 'de_DE') AS licitaciones_totales,\n" +
            "    FORMAT((licitaciones_ganadas * 100.0) / NULLIF(licitaciones_totales, 0), 2, 'de_DE') AS winrate\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        l_r.id_riesgo, \n" +
            "        COALESCE(r.detalle, 'Sin Riesgo Asignado') AS riesgo,\n" +
            "        SUM(l_r.monto_cotizado) AS cant_cotizada,\n" +
            "        SUM(CASE WHEN l_r.id_status = 3 THEN l_r.monto_adjudicado ELSE 0 END) AS cant_ganada,\n" +
            "        -- Métricas de conteo de licitaciones\n" +
            "        COUNT(l_r.id) AS licitaciones_totales,\n" +
            "        SUM(CASE WHEN l_r.id_status = 3 THEN 1 ELSE 0 END) AS licitaciones_ganadas\n" +
            "    FROM licitacion_riesgo AS l_r\n" +
            "    LEFT JOIN riesgo AS r ON r.id = l_r.id_riesgo\n" +
            "    INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "    -- Filtro estricto para ignorar las desistidas (por texto y por ID)\n" +
            "    WHERE s.detalle <> 'Desistida'\n" +
            "    GROUP BY l_r.id_riesgo, r.detalle\n" +
            ") AS subconsulta \n" +
            "ORDER BY riesgo ASC", nativeQuery = true)
    List<IRentabilidadPorRiesgo> getRentabilidadPorRiesgo();

    // ============================================================================
    // A. CONTEO DE COMPULSAS/LICITACIONES ÚNICAS POR ESTADO HISTÓRICO
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    s.detalle AS estado_licitacion,\n" +
            "    COUNT(*) AS cantidad_compulsas\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "GROUP BY s.detalle", nativeQuery = true)
    List<IEstadoLicitaciones> getEstadoLicitaciones();

    // ============================================================================
    // B. ANÁLISIS DE GANADAS: APERTURA POR MOTIVO ESPECÍFICO
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    COALESCE(l_r.motivo, 'Sin Motivo Especificado') AS motivo_ganada,\n" +
            "    COUNT(*) AS cantidad_compulsas\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Ganada'\n" +
            "GROUP BY l_r.motivo", nativeQuery = true)
    List<IMotivoGanada> getMotivoGanada();

    // ============================================================================
    // C. TOTAL ABSOLUTO ADJUDICADO EN PROCESOS EXITOSOS
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    FORMAT(SUM(l_r.monto_adjudicado), 2, 'de_DE') AS total_adjudicado_ganadas\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Ganada'", nativeQuery = true)
    ITotalAdjudicadoGanadas getTotalAdjudicadoGanadas();

    // ============================================================================
    // D. RANKING DE RENGLONES (RIESGOS) CON MAYOR CANTIDAD DE ÉXITOS
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    r.detalle AS riesgo_renglon,\n" +
            "    COUNT(*) AS cantidad_renglones_ganados\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN riesgo AS r ON r.id = l_r.id_riesgo\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Ganada'\n" +
            "GROUP BY r.detalle\n" +
            "ORDER BY cantidad_renglones_ganados DESC", nativeQuery = true)
    List<IRankingRiesgosGanados> getRankingRiesgosGanados();

    // ============================================================================
    // E. RELACIÓN DE PÉRDIDAS: RELACIÓN PORCENTUAL DE NUESTRO PRECIO VS EL COMPETIDOR
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    FORMAT(\n" +
            "        AVG(((l_r.monto_cotizado - l_r.monto_adjudicado) / l_r.monto_adjudicado) * 100), \n" +
            "        2, \n" +
            "        'de_DE'\n" +
            "    ) AS sobreprecio_promedio_porcentaje\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Perdida' AND l_r.monto_adjudicado > 0", nativeQuery = true)
    ISobreprecioPromedio getSobreprecioPromedio();

    // ============================================================================
    // F. RENTABILIDAD RESIDUAL DE RIESGOS EN PÉRDIDAS: DÓNDE SE PERDIÓ MÁS VOLUMEN COMERCIAL
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    r.detalle AS riesgo_renglon,\n" +
            "    COUNT(*) AS compulsas_perdidas,\n" +
            "    FORMAT(SUM(l_r.monto_cotizado), 2, 'de_DE') AS total_monto_cotizado_perdido\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN riesgo AS r ON r.id = l_r.id_riesgo\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Perdida'\n" +
            "GROUP BY r.detalle\n" +
            "ORDER BY SUM(l_r.monto_cotizado) DESC", nativeQuery = true)
    List<IRentabilidadResidualPerdidas> getRentabilidadResidualPerdidas();

    // ============================================================================
    // G.1 RADIOGRAFÍA DE DESISTIDAS: CANTIDAD TOTAL DE PROCESOS DESISTIDOS
    // ============================================================================
    @Query(value = "SELECT COUNT(*) AS total_compulsas_desistidas \n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status WHERE s.detalle = 'Desistida'", nativeQuery = true)
    ITotalDesistidas getTotalDesistidas();

    // ============================================================================
    // G.2 RADIOGRAFÍA DE DESISTIDAS: TOP MOTIVOS DE DESISTIMIENTO
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    COALESCE(l_r.motivo, 'Sin Motivo Especificado') AS motivo_desistida,\n" +
            "    COALESCE(l_r.estado_motivo, 'Sin detalle especificado') AS detalle_motivo,\n" +
            "    COUNT(*) AS cantidad_casos\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Desistida'\n" +
            "GROUP BY l_r.motivo, l_r.estado_motivo\n" +
            "ORDER BY cantidad_casos DESC", nativeQuery = true)
    List<ITopMotivosDesistidas> getTopMotivosDesistidas();

    // ============================================================================
    // G.3 RADIOGRAFÍA DE DESISTIDAS: MASA ADJUDICADA TEÓRICA DESISTIDA
    // ============================================================================
    @Query(value = "SELECT FORMAT(SUM(l_r.monto_adjudicado), 2, 'de_DE') AS cantidad_adjudicada_total_desistida\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Desistida'", nativeQuery = true)
    IMontoAdjudicadoDesistido getMontoAdjudicadoDesistido();

    // ============================================================================
    // G.4 RADIOGRAFÍA DE DESISTIDAS: RENGLONES (RIESGOS) MÁS DESISTIDOS HISTÓRICAMENTE
    // ============================================================================
    @Query(value = "SELECT \n" +
            "    r.detalle AS riesgo_renglon,\n" +
            "    COUNT(*) AS cantidad_desistidos\n" +
            "FROM licitacion_riesgo AS l_r\n" +
            "INNER JOIN riesgo AS r ON r.id = l_r.id_riesgo\n" +
            "INNER JOIN status AS s ON s.id = l_r.id_status\n" +
            "WHERE s.detalle = 'Desistida'\n" +
            "GROUP BY r.detalle\n" +
            "ORDER BY cantidad_desistidos DESC", nativeQuery = true)
    List<IRenglonesDesistidos> getRenglonesDesistidos();

}
