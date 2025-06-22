package teatro_reservas.backend.dto;

import lombok.*;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.entity.enums.TipoEvento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaHora;
    private TipoEvento tipoEvento;
    private Integer capacidadTotal;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private Set<TipoEntrada> tiposEntrada;
    private Map<TipoEntrada, BigDecimal> precios;
    private Map<TipoEntrada, Integer> capacidades;
    private Map<TipoEntrada, Long> disponibilidadPorTipo;
    private Boolean estaVigente;
    private Long totalReservasActivas;
}
