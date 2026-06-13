package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detalle", nullable = false)
    private String detalle;

    public Status() {
    }

    public Status(String statusStr) {
        this.detalle = statusStr;
    }
}
