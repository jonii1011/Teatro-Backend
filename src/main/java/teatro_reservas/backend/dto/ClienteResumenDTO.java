package teatro_reservas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResumenDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Integer eventosAsistidos;
    private Integer pasesGratuitos;
}
