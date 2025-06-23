package teatro_reservas.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import teatro_reservas.backend.entity.Evento;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.entity.enums.TipoEvento;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Eventos vigentes (activos y fecha futura)
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND e.fechaHora > :ahora")
    List<Evento> findEventosVigentes(@Param("ahora") LocalDateTime ahora);

    // Eventos por tipo
    List<Evento> findByTipoEventoAndActivoTrue(TipoEvento tipoEvento);

    // Eventos en un rango de fechas
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND " +
            "e.fechaHora BETWEEN :desde AND :hasta ORDER BY e.fechaHora")
    List<Evento> findEventosByFechaBetween(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Eventos con disponibilidad
    @Query("SELECT DISTINCT e FROM Evento e " +
            "LEFT JOIN e.reservas r ON r.estado = 'CONFIRMADA' " +
            "WHERE e.activo = true AND e.fechaHora > :ahora " +
            "GROUP BY e.id " +
            "HAVING COUNT(r) < e.capacidadTotal")
    List<Evento> findEventosConDisponibilidad(@Param("ahora") LocalDateTime ahora);

    // Buscar por nombre
    @Query("SELECT e FROM Evento e WHERE " +
            "LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND e.activo = true")
    List<Evento> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    // Eventos próximos (siguientes 7 días)
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND " +
            "e.fechaHora BETWEEN :ahora AND :limite ORDER BY e.fechaHora")
    List<Evento> findEventosProximos(
            @Param("ahora") LocalDateTime ahora,
            @Param("limite") LocalDateTime limite);

    // Para estadísticas - eventos más populares
    @Query("SELECT e, COUNT(r) as reservas FROM Evento e " +
            "LEFT JOIN e.reservas r ON r.estado = 'CONFIRMADA' " +
            "WHERE e.fechaHora BETWEEN :desde AND :hasta " +
            "GROUP BY e.id ORDER BY reservas DESC")
    List<Object[]> findEventosMasPopulares(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}