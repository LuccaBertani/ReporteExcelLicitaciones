package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import raiz.dominio.TipoAdjudicacion;

@Repository
public interface ITipoAdjudicacion extends JpaRepository<TipoAdjudicacion,Long> {

    boolean existsByDetalle(String detalle);

    @Query("""
            SELECT t FROM TipoAdjudicacion t where t.detalle = :detalle
            """)
    TipoAdjudicacion findByDetalle(@Param("detalle") String detalle);
}
