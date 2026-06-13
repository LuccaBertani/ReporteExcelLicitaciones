package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import raiz.dominio.EntidadAdjudicada;
import raiz.dominio.Status;

@Repository
public interface IEntidadAdjudicada extends JpaRepository<EntidadAdjudicada,Long>{

    boolean existsByDetalle(String detalle);

    @Query("""
            SELECT e FROM EntidadAdjudicada e where e.detalle = :detalle
            """)
    EntidadAdjudicada findByDetalle(@Param("detalle") String stringCellValue);
}
