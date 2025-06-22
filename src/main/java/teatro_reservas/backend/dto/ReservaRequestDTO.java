package teatro_reservas.backend.dto;


import jakarta.validation.constraints.*;
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
public class ReservaRequestDTO {
    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El evento es obligatorio")
    private Long eventoId;

    @NotNull(message = "El tipo de entrada es obligatorio")
    private TipoEntrada tipoEntrada;

    private Boolean usarPaseGratuito = false;
}
