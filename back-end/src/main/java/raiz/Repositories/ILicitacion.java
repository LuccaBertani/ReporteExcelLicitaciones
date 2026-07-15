package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import raiz.dominio.Licitacion;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ILicitacion extends JpaRepository<Licitacion,Long> {

    boolean existsByNumeroCompulsa(String stringCellValue);

    Optional<Licitacion> findByNumeroCompulsa(String numeroCompulsa);

    List<Licitacion> findAllByNumeroCompulsa(String numExcel);

    @Query("""
            SELECT l FROM Licitacion l where l.numeroCompulsa = :numero_str AND l.anio = :anio
            """)
    Optional<Licitacion> findByNumeroCompulsaAndAnio(@Param("numero_str") String numeroStr, @Param("anio") String anio);
/*
    @Query("SELECT COUNT(l) > 0 FROM Licitacion l WHERE " +
            "l.numeroCompulsa = :numero AND " +
            "l.motivo = :motivo AND " +
            // COALESCE trata el NULL de la DB como 0.0 para la comparación
            "COALESCE(l.montoAdjudicado, 0.0) = :monto AND " +
            "l.fecha = :fecha AND " +
            "l.mes.detalle = :mes AND " +
            "l.cliente.detalle = :cliente AND " +
            "l.riesgo.detalle = :riesgo AND " +
            "l.status.detalle = :status AND " +
            "l.adjudicada.detalle = :adjudicada")
    boolean existeRegistroIdentico(
            @Param("numero") String numero,
            @Param("motivo") String motivo,
            @Param("monto") Double monto,
            @Param("fecha") Date fecha,
            @Param("mes") String mes,
            @Param("cliente") String cliente,
            @Param("riesgo") String riesgo,
            @Param("status") String status,
            @Param("adjudicada") String adjudicada
    );
    */
}
