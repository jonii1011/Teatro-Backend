package teatro_reservas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.entity.enums.TipoEvento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoFiltroDTO {
    private TipoEvento tipoEvento;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private Boolean soloDisponibles;
    private TipoEntrada tipoEntrada;
    private BigDecimal precioMaximo;
}
