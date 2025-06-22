package teatro_reservas.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import teatro_reservas.backend.entity.enums.EstadoReserva;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.entity.enums.TipoEvento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    private String nombre;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha del evento debe ser futura")
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    @NotNull(message = "El tipo de evento es obligatorio")
    private TipoEvento tipoEvento;

    @Column(name = "capacidad_total", nullable = false)
    @NotNull(message = "La capacidad total es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Max(value = 999999, message = "Capacidad máxima excedida")
    private Integer capacidadTotal;

    @Column(name = "precio_base", nullable = true, precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @DecimalMax(value = "99999999.99", message = "Precio demasiado alto")
    private BigDecimal precioBase;

    @Column(nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @ElementCollection
    @CollectionTable(name = "evento_precios", joinColumns = @JoinColumn(name = "evento_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "precio")
    private Map<TipoEntrada, BigDecimal> precios = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "evento_capacidades", joinColumns = @JoinColumn(name = "evento_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "capacidad")
    private Map<TipoEntrada, Integer> capacidades = new HashMap<>();

    // Métodos útiles
    public boolean tieneDisponibilidad(TipoEntrada tipoEntrada) {
        Integer capacidadTipo = capacidades.get(tipoEntrada);
        if (capacidadTipo == null) return false;

        long reservasConfirmadas = reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .filter(r -> r.getTipoEntrada() == tipoEntrada)
                .count();

        return reservasConfirmadas < capacidadTipo;
    }

    public long getCapacidadDisponible(TipoEntrada tipoEntrada) {
        Integer capacidadTipo = capacidades.get(tipoEntrada);
        if (capacidadTipo == null) return 0;

        long reservasConfirmadas = reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .filter(r -> r.getTipoEntrada() == tipoEntrada)
                .count();

        return capacidadTipo - reservasConfirmadas;
    }

    public BigDecimal getPrecio(TipoEntrada tipoEntrada) {
        return precios.get(tipoEntrada);
    }

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("evento-reservas")
    private List<Reserva> reservas = new ArrayList<>();


    public boolean estaVigente() {
        return activo && fechaHora.isAfter(LocalDateTime.now());
    }

    public long getReservasActivas() {
        return reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .count();
    }
}
