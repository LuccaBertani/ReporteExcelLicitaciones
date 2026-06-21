package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Mes")
public class Mes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detalle", nullable = false)
    private String detalle;

    public Mes() {
    }

    public Mes(String mesStr) {
        this.detalle = mesStr;
    }
}
