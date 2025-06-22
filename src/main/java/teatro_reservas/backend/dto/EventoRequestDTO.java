package teatro_reservas.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.entity.enums.TipoEvento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventoRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 150)
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000)
    private String descripcion;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fechaHora;

    @NotNull(message = "El tipo de evento es obligatorio")
    private TipoEvento tipoEvento;

    @NotNull(message = "La capacidad total es obligatoria")
    @Min(value = 1)
    private Integer capacidadTotal;

    // Configuración por tipo de entrada
    @NotEmpty(message = "Debe configurar al menos un tipo de entrada")
    private Map<TipoEntrada, ConfiguracionEntradaDTO> configuracionEntradas;
}
