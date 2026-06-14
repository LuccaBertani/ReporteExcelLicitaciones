package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Ramo")
public class Ramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detalle")
    private String detalle;

    @OneToMany(mappedBy = "ramo", cascade = CascadeType.ALL)
    private List<Riesgo> riesgosAsociados;

    public Ramo(String detalle) {
        this.detalle = detalle;
        this.riesgosAsociados = new ArrayList<>();
    }

    public Ramo(){

    }

}
