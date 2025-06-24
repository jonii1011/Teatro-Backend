package teatro_reservas.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import teatro_reservas.backend.entity.enums.EstadoReserva;
import teatro_reservas.backend.entity.enums.TipoEntrada;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    @JsonBackReference("cliente-reservas")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @NotNull(message = "El evento es obligatorio")
    @JsonBackReference("evento-reservas")
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrada", nullable = false)
    @NotNull(message = "El tipo de entrada es obligatorio")
    private TipoEntrada tipoEntrada;

    @Column(name = "fecha_reserva", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "El estado es obligatorio")
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    @Column(name = "es_pase_gratuito", nullable = false)
    @NotNull
    private Boolean esPaseGratuito = false;

    @Column(name = "precio_pagado", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @NotNull(message = "El precio pagado es obligatorio")
    private BigDecimal precioPagado = BigDecimal.ZERO;

    @Column(name = "fecha_confirmacion", nullable = false)
    @NotNull(message = "La fecha de confirmación es obligatoria")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo_cancelacion", length = 500)
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivoCancelacion;

    @Column(name = "codigo_reserva", unique = true, length = 20, nullable = false, updatable = false)
    @NotBlank(message = "El código de reserva es obligatorio")
    private String codigoReserva;

    @PrePersist
    private void generarCodigoReserva() {
        if (codigoReserva == null) {
            codigoReserva = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (fechaConfirmacion == null) {
            fechaConfirmacion = LocalDateTime.now();
        }
    }

    public void cancelar(String motivo) {
        this.estado = EstadoReserva.CANCELADA;
        this.fechaCancelacion = LocalDateTime.now();
        this.motivoCancelacion = motivo;
    }

    public boolean puedeSerCancelada() {
        return estado == EstadoReserva.CONFIRMADA;
    }

    public boolean estaVigente() {
        return estado == EstadoReserva.CONFIRMADA && evento.estaVigente();
    }

    public String getResumenReserva() {
        return String.format("%s - %s - %s",
                codigoReserva,
                evento.getNombre(),
                tipoEntrada.name());
    }
}

