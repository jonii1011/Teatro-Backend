package teatro_reservas.backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import teatro_reservas.backend.entity.Cliente;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar por email
    Optional<Cliente> findByEmail(String email);

    // Verificar si existe email
    boolean existsByEmail(String email);

    // Clientes activos
    List<Cliente> findByActivoTrue();

    // Clientes frecuentes (5+ eventos)
    @Query("SELECT c FROM Cliente c WHERE c.eventosAsistidos >= 5")
    List<Cliente> findClientesFrecuentes();

    // Clientes con pases gratuitos disponibles
    @Query("SELECT c FROM Cliente c WHERE c.pasesGratuitos > 0")
    List<Cliente> findClientesConPasesGratuitos();

    // Buscar por nombre/apellido
    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Cliente> findByNombreOrApellidoContainingIgnoreCase(@Param("termino") String termino);

    // Top clientes por eventos asistidos
    @Query("SELECT c FROM Cliente c ORDER BY c.eventosAsistidos DESC")
    Page<Cliente> findTopClientesByEventosAsistidos(Pageable pageable);

    // Clientes registrados en un período
    @Query("SELECT c FROM Cliente c WHERE c.fechaRegistro BETWEEN :desde AND :hasta")
    List<Cliente> findByFechaRegistroBetween(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
