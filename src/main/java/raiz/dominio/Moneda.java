package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "moneda")
public class Moneda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String detalle;

    public Moneda() {
    }

    public Moneda(String detalle) {
        this.detalle = detalle;
    }
}
