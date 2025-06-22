package teatro_reservas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import teatro_reservas.backend.entity.enums.EstadoReserva;
import teatro_reservas.backend.entity.enums.TipoEntrada;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResumenDTO {
    private Long id;
    private String codigoReserva;
    private String nombreEvento;
    private LocalDateTime fechaEvento;
    private TipoEntrada tipoEntrada;
    private EstadoReserva estado;
    private BigDecimal precioPagado;
    private Boolean esPaseGratuito;
}
