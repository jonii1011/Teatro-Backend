package teatro_reservas.backend.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import teatro_reservas.backend.dto.ClienteRequestDTO;
import teatro_reservas.backend.dto.ClienteResponseDTO;
import teatro_reservas.backend.dto.ClienteResumenDTO;
import teatro_reservas.backend.entity.Cliente;
import teatro_reservas.backend.exception.BusinessException;
import teatro_reservas.backend.exception.ResourceNotFoundException;
import teatro_reservas.backend.repository.ClienteRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
        configurarModelMapper();
    }

    private void configurarModelMapper() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(Cliente.class, ClienteResponseDTO.class)
                .addMapping(src -> src.esClienteFrecuente(), ClienteResponseDTO::setEsClienteFrecuente)
                .addMapping(src -> src.getReservasActivas(), ClienteResponseDTO::setReservasActivas);
    }

    @Override
    public ClienteResponseDTO crearCliente(ClienteRequestDTO clienteDTO) {
        if (clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            throw new BusinessException("Ya existe un cliente con este email: " + clienteDTO.getEmail());
        }

        Cliente cliente = modelMapper.map(clienteDTO, Cliente.class);
        Cliente clienteGuardado = clienteRepository.save(cliente);
        return modelMapper.map(clienteGuardado, ClienteResponseDTO.class);
    }

    @Override
    @Transactional
    public ClienteResponseDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }

    @Override
    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        if (!cliente.getEmail().equals(clienteDTO.getEmail()) &&
                clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            throw new BusinessException("Ya existe un cliente con este email: " + clienteDTO.getEmail());
        }

        // Actualizar campos
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setApellido(clienteDTO.getApellido());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefono(clienteDTO.getTelefono());

        Cliente clienteActualizado = clienteRepository.save(cliente);
        return modelMapper.map(clienteActualizado, ClienteResponseDTO.class);
    }

    @Override
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    // Consultas
    @Override
    @Transactional
    public List<ClienteResponseDTO> obtenerTodosLosClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        return mapToClienteResponseDTOList(clientes);
    }

    @Override
    @Transactional
    public List<ClienteResumenDTO> obtenerClientesActivos() {
        List<Cliente> clientes = clienteRepository.findByActivoTrue();
        return mapToClienteResumenDTOList(clientes);
    }

    @Override
    @Transactional
    public Optional<ClienteResponseDTO> obtenerClientePorEmail(String email) {
        Optional<Cliente> cliente = clienteRepository.findByEmail(email);
        return cliente.map(c -> modelMapper.map(c, ClienteResponseDTO.class));
    }

    @Override
    @Transactional
    public List<ClienteResumenDTO> buscarClientesPorNombre(String termino) {
        List<Cliente> clientes = clienteRepository.findByNombreOrApellidoContainingIgnoreCase(termino);
        return mapToClienteResumenDTOList(clientes);
    }

    @Override
    @Transactional
    public List<ClienteResumenDTO> obtenerClientesFrecuentes() {
        List<Cliente> clientes = clienteRepository.findClientesFrecuentes();
        return mapToClienteResumenDTOList(clientes);
    }

    @Override
    @Transactional
    public List<ClienteResumenDTO> obtenerClientesConPasesGratuitos() {
        List<Cliente> clientes = clienteRepository.findClientesConPasesGratuitos();
        return mapToClienteResumenDTOList(clientes);
    }

    @Override
    @Transactional
    public boolean puedeUsarPaseGratuito(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));
        return cliente.tienePasesGratuitos();
    }

    @Override
    public void usarPaseGratuito(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        if (!cliente.tienePasesGratuitos()) {
            throw new BusinessException("El cliente no tiene pases gratuitos disponibles");
        }

        cliente.usarPaseGratuito();
        clienteRepository.save(cliente);
    }

    @Override
    public Page<ClienteResponseDTO> obtenerTopClientesPorAsistencia(int page, int size) {
        Pageable pageable = (Pageable) PageRequest.of(page, size);
        Page<Cliente> clientes = clienteRepository.findTopClientesByEventosAsistidos(pageable);
        return clientes.map(cliente -> modelMapper.map(cliente, ClienteResponseDTO.class));
    }

    @Override
    @Transactional
    public List<ClienteResponseDTO> obtenerClientesRegistradosEnPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        List<Cliente> clientes = clienteRepository.findByFechaRegistroBetween(desde, hasta);
        return mapToClienteResponseDTOList(clientes);
    }

    // Validaciones
    @Override
    @Transactional
    public boolean existeClientePorEmail(String email) {
        return clienteRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void validarClienteActivo(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        if (!cliente.getActivo()) {
            throw new BusinessException("El cliente está inactivo y no puede realizar operaciones");
        }
    }

    private List<ClienteResponseDTO> mapToClienteResponseDTOList(List<Cliente> clientes) {
        return clientes.stream()
                .map(cliente -> modelMapper.map(cliente, ClienteResponseDTO.class))
                .collect(Collectors.toList());
    }

    private List<ClienteResumenDTO> mapToClienteResumenDTOList(List<Cliente> clientes) {
        return clientes.stream()
                .map(cliente -> modelMapper.map(cliente, ClienteResumenDTO.class))
                .collect(Collectors.toList());
    }
}
