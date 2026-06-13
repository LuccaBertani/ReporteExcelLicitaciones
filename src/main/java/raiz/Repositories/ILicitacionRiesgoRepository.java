package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import raiz.dominio.Licitacion;
import raiz.dominio.LicitacionRiesgo;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILicitacionRiesgoRepository extends JpaRepository<LicitacionRiesgo, Integer> {

    @Query("SELECT l FROM LicitacionRiesgo l WHERE l.licitacion.id = :id_licitacion")
    List<LicitacionRiesgo> findByLicitacion(@Param("id_licitacion") Long id_licitacion);
}
