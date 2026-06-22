package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = "riesgosAsignados")
@Entity
@Table(name = "Licitacion")
public class Licitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @OneToMany(mappedBy = "licitacion", cascade = CascadeType.ALL)
    private List<LicitacionRiesgo> riesgosAsignados;

    @Column(name = "numeroCompulsa")
    private String numeroCompulsa;

    public Licitacion() {
        this.riesgosAsignados = new ArrayList<>();
    }


}
