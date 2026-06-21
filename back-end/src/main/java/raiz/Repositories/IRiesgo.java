package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import raiz.dominio.Riesgo;

import java.util.Optional;

@Repository
public interface IRiesgo extends JpaRepository<Riesgo, Long> {

    boolean existsByDetalle(String detalle);

    @Query("""
            SELECT r FROM Riesgo r where r.detalle = :detalle
            """)
    Optional<Riesgo> findByDetalle(@Param("detalle") String stringCellValue);
}
