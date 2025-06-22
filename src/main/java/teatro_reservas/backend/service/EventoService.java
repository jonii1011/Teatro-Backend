package teatro_reservas.backend.service;

import teatro_reservas.backend.dto.EventoFiltroDTO;
import teatro_reservas.backend.dto.EventoRequestDTO;
import teatro_reservas.backend.dto.EventoResponseDTO;
import teatro_reservas.backend.dto.EventoResumenDTO;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.entity.enums.TipoEvento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventoService {

    EventoResponseDTO crearEvento(EventoRequestDTO eventoDTO);
    EventoResponseDTO obtenerEventoPorId(Long id);
    EventoResponseDTO actualizarEvento(Long id, EventoRequestDTO eventoDTO);
    void eliminarEvento(Long id); // soft delete

    List<EventoResponseDTO> obtenerTodosLosEventos();
    List<EventoResumenDTO> obtenerEventosVigentes();
    List<EventoResumenDTO> obtenerEventosPorTipo(TipoEvento tipoEvento);
    List<EventoResumenDTO> buscarEventosPorNombre(String nombre);

    // Disponibilidad
    List<EventoResumenDTO> obtenerEventosConDisponibilidad();
    boolean tieneDisponibilidad(Long eventoId, TipoEntrada tipoEntrada);
    long obtenerCapacidadDisponible(Long eventoId, TipoEntrada tipoEntrada);
    Map<TipoEntrada, Long> obtenerDisponibilidadPorTipo(Long eventoId);

    // Filtros y búsquedas
    List<EventoResumenDTO> obtenerEventosProximos(int dias);
    List<EventoResumenDTO> obtenerEventosEnRango(LocalDateTime desde, LocalDateTime hasta);

    // Precios
    BigDecimal obtenerPrecioEntrada(Long eventoId, TipoEntrada tipoEntrada);
    Map<TipoEntrada, BigDecimal> obtenerTodosLosPrecios(Long eventoId);

    // Validaciones
    void validarEventoVigente(Long eventoId);
    void validarCompatibilidadTipoEntrada(Long eventoId, TipoEntrada tipoEntrada);
    void validarDisponibilidad(Long eventoId, TipoEntrada tipoEntrada);

    List<EventoResumenDTO> obtenerEventosMasPopulares(LocalDateTime desde, LocalDateTime hasta);
}
