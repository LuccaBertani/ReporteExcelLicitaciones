package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import raiz.dominio.Mes;

@Repository
public interface IMes extends JpaRepository<Mes, Long> {

    boolean existsByDetalle(String detalle);

    @Query("""
            SELECT m FROM Mes m where m.detalle = :detalle
            """)
    Mes findByDetalle(@Param("detalle") String detalle);
}
