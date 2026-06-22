package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "EntidadAdjudicada")
public class EntidadAdjudicada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detalle", nullable = false, length = 500)
    private String detalle;

    public EntidadAdjudicada() {
    }

    public EntidadAdjudicada(String adjudicadoAStr) {
        this.detalle = adjudicadoAStr;
    }
}
