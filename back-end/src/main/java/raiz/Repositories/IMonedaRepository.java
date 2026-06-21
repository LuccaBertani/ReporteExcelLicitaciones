package raiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import raiz.dominio.Moneda;
import raiz.dominio.Status;

@Repository
public interface IMonedaRepository extends JpaRepository<Moneda, Integer> {

    Moneda findByDetalle(String detalle);
}
