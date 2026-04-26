package com.novabank.service;

import com.novabank.dto.ClienteRequestDTO;
import com.novabank.dto.ClienteResponseDTO;
import com.novabank.exception.DuplicateResourceException;
import com.novabank.exception.ResourceNotFoundException;
import com.novabank.mapper.ClienteMapper;
import com.novabank.model.Cliente;
import com.novabank.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional
    public ClienteResponseDTO crearCliente(ClienteRequestDTO request) {
        ClienteRequestDTO normalizado = normalizar(request);

        validarDuplicados(normalizado);

        Cliente cliente = clienteMapper.toEntity(normalizado);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerCliente(Long id) {
        return clienteMapper.toResponse(buscarCliente(id));
    }

    Cliente buscarCliente(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id del cliente debe ser positivo");
        }

        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ningun cliente con id " + id));
    }

    private ClienteRequestDTO normalizar(ClienteRequestDTO request) {
        return new ClienteRequestDTO(
                request.nombre().trim(),
                request.apellidos().trim(),
                request.dni().trim().toUpperCase(Locale.ROOT),
                request.email().trim().toLowerCase(Locale.ROOT),
                request.telefono().trim()
        );
    }

    private void validarDuplicados(ClienteRequestDTO request) {
        if (clienteRepository.existsByDni(request.dni())) {
            throw new DuplicateResourceException("Ya existe un cliente con el DNI " + request.dni());
        }
        if (clienteRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe un cliente con el email " + request.email());
        }
        if (clienteRepository.existsByTelefono(request.telefono())) {
            throw new DuplicateResourceException("Ya existe un cliente con el telefono " + request.telefono());
        }
    }
}
