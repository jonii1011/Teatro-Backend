package teatro_reservas.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import teatro_reservas.backend.entity.enums.EstadoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[A-Za-zÁáÉéÍíÓóÚúÑñ\\s]+$", message = "El nombre solo puede contener letras")
    private String nombre;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[A-Za-zÁáÉéÍíÓóÚúÑñ\\s]+$", message = "El apellido solo puede contener letras")
    private String apellido;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email es inválido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Column(unique = true, length = 8)
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener 7 u 8 dígitos")
    private String dni;

    @Column(length = 20)
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @Column(name = "fecha_nacimiento")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaRegistro;

    @Column(name = "eventos_asistidos", nullable = false)
    @Min(value = 0, message = "Los eventos asistidos no pueden ser negativos")
    @Max(value = 99999, message = "Número de eventos demasiado alto")
    private Integer eventosAsistidos = 0;

    @Column(name = "pases_gratuitos", nullable = false)
    @Min(value = 0, message = "Los pases gratuitos no pueden ser negativos")
    @Max(value = 999, message = "Número de pases demasiado alto")
    private Integer pasesGratuitos = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("cliente-reservas")
    private List<Reserva> reservas = new ArrayList<>();

    // Métodos de ciclo de vida JPA
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (eventosAsistidos == null) {
            eventosAsistidos = 0;
        }
        if (pasesGratuitos == null) {
            pasesGratuitos = 0;
        }
        if (activo == null) {
            activo = true;
        }
    }

    public boolean esClienteFrecuente() {
        return eventosAsistidos >= 5;
    }

    public boolean tienePasesGratuitos() {
        return pasesGratuitos > 0;
    }

    // Cálculo basado en relaciones ya cargadas
    public long getReservasActivas() {
        if (reservas == null) return 0;
        return reservas.stream()
                .filter(reserva -> reserva.getEstado() == EstadoReserva.CONFIRMADA)
                .count();
    }

    public void procesarAsistenciaEvento() {
        this.eventosAsistidos++;
        // Cada 5 eventos, otorgar un pase gratuito
        if (this.eventosAsistidos % 5 == 0) {
            this.pasesGratuitos++;
        }
    }

    public void usarPaseGratuito() {
        if (pasesGratuitos > 0) {
            pasesGratuitos--;
        }
    }

}
