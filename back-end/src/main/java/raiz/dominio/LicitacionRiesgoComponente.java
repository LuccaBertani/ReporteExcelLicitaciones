package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Un aporte individual (una fila de un Excel) que contribuyó al monto total
 * de un LicitacionRiesgo. Existe para poder distinguir "esta fila ya fue
 * sumada antes" de "esta fila es un aporte nuevo": sin este registro, al
 * recargar el mismo Excel (completo o parcial/delta) no hay forma de saber
 * si un monto que coincide en riesgo+fecha+status+motivo pero no en el total
 * ya fue contabilizado o no, y se corre el riesgo de sumar dos veces.
 *
 * El total de LicitacionRiesgo (montoCotizado/montoAdjudicado) se sigue
 * guardando en esa tabla como antes -para no romper las queries nativas del
 * dashboard que leen esas columnas directamente- pero ahora es la suma
 * calculada de sus componentes, no un valor que se va incrementando a mano.
 */
@Data
@EqualsAndHashCode(exclude = "licitacionRiesgo")
@Entity
@Table(name = "licitacion_riesgo_componente")
public class LicitacionRiesgoComponente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_licitacion_riesgo", nullable = false)
    private LicitacionRiesgo licitacionRiesgo;

    @Column(name = "montoCotizado")
    private Double montoCotizado;

    @Column(name = "montoAdjudicado")
    private Double montoAdjudicado;

    public LicitacionRiesgoComponente() {
    }

    public LicitacionRiesgoComponente(LicitacionRiesgo licitacionRiesgo, Double montoCotizado, Double montoAdjudicado) {
        this.licitacionRiesgo = licitacionRiesgo;
        this.montoCotizado = montoCotizado;
        this.montoAdjudicado = montoAdjudicado;
    }
}
