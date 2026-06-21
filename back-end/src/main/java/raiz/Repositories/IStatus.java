package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import raiz.dominio.Status;

@Repository
public interface IStatus extends JpaRepository<Status,Long> {

    boolean existsByDetalle(String detalle);

    @Query("""
            SELECT s FROM Status s where s.detalle = :detalle
            """)
    Status findByDetalle(@Param("detalle") String stringCellValue);
}
