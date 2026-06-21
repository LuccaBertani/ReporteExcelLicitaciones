package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Riesgo")
public class Riesgo {

    @Id
    private Long id;

    @Column(name = "detalle", nullable = false)
    private String detalle;

    @ManyToOne
    @JoinColumn(name = "id_ramo")
    private Ramo ramo;

    public Riesgo() {
    }

    public Riesgo(Long id, String riesgoStr) {
        this.id = id;
        this.detalle = riesgoStr;
    }
}
