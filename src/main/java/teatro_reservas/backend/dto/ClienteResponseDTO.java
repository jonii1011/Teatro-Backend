package teatro_reservas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private LocalDateTime fechaRegistro;
    private Integer eventosAsistidos;
    private Integer pasesGratuitos;
    private Boolean activo;
    private Boolean esClienteFrecuente;
    private Long reservasActivas;
}
