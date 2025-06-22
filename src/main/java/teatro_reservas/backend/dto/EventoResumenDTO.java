package teatro_reservas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import teatro_reservas.backend.entity.enums.TipoEvento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoResumenDTO {
    private Long id;
    private String nombre;
    private LocalDateTime fechaHora;
    private TipoEvento tipoEvento;
    private Integer capacidadTotal;
    private Long capacidadDisponible;
    private Boolean estaVigente;
    private BigDecimal precioDesde;
}
