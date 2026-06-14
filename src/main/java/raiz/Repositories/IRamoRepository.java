package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import raiz.dominio.Ramo;

@Repository
public interface IRamoRepository extends JpaRepository<Ramo, Long> {

    Ramo findByDetalle(String detalle);
}
