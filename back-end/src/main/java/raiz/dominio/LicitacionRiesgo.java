package raiz.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(exclude = "licitacion")
@Entity
@Table(name = "licitacion_riesgo")
public class LicitacionRiesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_licitacion")
    private Licitacion licitacion;

    @ManyToOne
    @JoinColumn(name = "id_riesgo")
    private Riesgo riesgo;

    @ManyToOne
    @JoinColumn(name = "id_status")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "id_tipoAdjudicacion")
    private TipoAdjudicacion tipoAdjudicacion;

    @Column(name = "montoAdjudicado")
    private Double montoAdjudicado;

    @Column(name = "montoCotizado")
    private Double montoCotizado;

    @ManyToOne
    @JoinColumn(name = "id_moneda")
    private Moneda moneda;

    @Column(name = "motivo", length = 1000)
    private String motivo;

    @Column(name = "estadoMotivo", length = 1000)
    private String estadoMotivo;

    @ManyToOne
    @JoinColumn(name = "id_adjudicada")
    private EntidadAdjudicada adjudicada;

    @Column(name = "fecha")
    private Date fecha;

    @ManyToOne
    @JoinColumn(name = "id_mes")
    private Mes mes;
}
