package teatro_reservas.backend.dto;

import lombok.*;
import teatro_reservas.backend.entity.enums.EstadoReserva;
import teatro_reservas.backend.entity.enums.TipoEntrada;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReservaResponseDTO {
    private Long id;
    private String codigoReserva;
    private ClienteResumenDTO cliente;
    private EventoResumenDTO evento;
    private TipoEntrada tipoEntrada;
    private LocalDateTime fechaReserva;
    private EstadoReserva estado;
    private Boolean esPaseGratuito;
    private BigDecimal precioPagado;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;
    private Boolean puedeSerCancelada;
    private Boolean estaVigente;
}
