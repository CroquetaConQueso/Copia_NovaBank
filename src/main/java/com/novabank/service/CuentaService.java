package com.novabank.service;

import com.novabank.dto.CuentaCreateRequestDTO;
import com.novabank.dto.CuentaResponseDTO;
import com.novabank.exception.ResourceNotFoundException;
import com.novabank.mapper.CuentaMapper;
import com.novabank.model.Cliente;
import com.novabank.model.Cuenta;
import com.novabank.repository.ClienteRepository;
import com.novabank.repository.CuentaRepository;
import com.novabank.service.strategy.GeneradorNumeroCuentaStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final GeneradorNumeroCuentaStrategy generadorNumeroCuentaStrategy;
    private final CuentaMapper cuentaMapper;

    public CuentaService(
            CuentaRepository cuentaRepository,
            ClienteRepository clienteRepository,
            GeneradorNumeroCuentaStrategy generadorNumeroCuentaStrategy,
            CuentaMapper cuentaMapper
    ) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.generadorNumeroCuentaStrategy = generadorNumeroCuentaStrategy;
        this.cuentaMapper = cuentaMapper;
    }

    @Transactional
    public CuentaResponseDTO crearCuenta(CuentaCreateRequestDTO request) {
        Long clienteId = validarId(request == null ? null : request.clienteId(), "El id del cliente debe ser positivo");

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ningun cliente con id " + clienteId));

        Cuenta cuenta = Cuenta.builder()
                .cliente(cliente)
                .numeroCuenta(generadorNumeroCuentaStrategy.generarNumeroCuenta())
                .saldo(BigDecimal.ZERO)
                .build();

        return cuentaMapper.toResponse(cuentaRepository.save(cuenta));
    }

    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerCuenta(Long id) {
        return cuentaMapper.toResponse(buscarCuenta(id));
    }

    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> listarCuentasPorCliente(Long clienteId) {
        clienteId = validarId(clienteId, "El id del cliente debe ser positivo");

        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("No existe ningun cliente con id " + clienteId);
        }

        return cuentaRepository.findByClienteId(clienteId)
                .stream()
                .map(cuentaMapper::toResponse)
                .toList();
    }

    Cuenta buscarCuenta(Long id) {
        Long cuentaId = validarId(id, "El id de la cuenta debe ser positivo");

        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ninguna cuenta con id " + cuentaId));
    }

    private Long validarId(Long id, String mensaje) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensaje);
        }

        return id;
    }
}
