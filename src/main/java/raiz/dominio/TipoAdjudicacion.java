package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "TipoAdjudicacion")
public class TipoAdjudicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detalle", nullable = false)
    private String detalle;

    public TipoAdjudicacion() {
    }

    public TipoAdjudicacion(String dejadaSinEfecto) {
        this.detalle = dejadaSinEfecto;
    }
}
