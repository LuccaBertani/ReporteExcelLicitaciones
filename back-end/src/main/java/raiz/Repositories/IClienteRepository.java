package raiz.Repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import raiz.dominio.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDetalle(String detalle);

    @Query("""
            SELECT c FROM Cliente c where c.detalle = :detalle
            """)
    Cliente findByDetalle(@Param("detalle") String stringCellValue);
}
