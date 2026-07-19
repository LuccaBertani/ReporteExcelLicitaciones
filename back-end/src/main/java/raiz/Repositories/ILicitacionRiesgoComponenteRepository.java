package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import raiz.dominio.LicitacionRiesgoComponente;

@Repository
public interface ILicitacionRiesgoComponenteRepository extends JpaRepository<LicitacionRiesgoComponente, Long> {
}
