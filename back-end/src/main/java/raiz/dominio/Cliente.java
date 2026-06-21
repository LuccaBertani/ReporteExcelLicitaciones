package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detalle", nullable = false)
    private String detalle;

    public Cliente(String clienteStr) {
        this.detalle = clienteStr;
    }

    public Cliente() {
    }
}
