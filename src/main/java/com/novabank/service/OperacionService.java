package com.novabank.service;

import com.novabank.dto.MovimientoResponseDTO;
import com.novabank.dto.OperacionRequestDTO;
import com.novabank.dto.TransferenciaRequestDTO;
import com.novabank.exception.InsufficientBalanceException;
import com.novabank.exception.ResourceNotFoundException;
import com.novabank.mapper.MovimientoMapper;
import com.novabank.model.Cuenta;
import com.novabank.model.Movimiento;
import com.novabank.repository.CuentaRepository;
import com.novabank.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class OperacionService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final MovimientoFactory movimientoFactory;
    private final MovimientoMapper movimientoMapper;

    public OperacionService(
            CuentaRepository cuentaRepository,
            MovimientoRepository movimientoRepository,
            MovimientoFactory movimientoFactory,
            MovimientoMapper movimientoMapper
    ) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.movimientoFactory = movimientoFactory;
        this.movimientoMapper = movimientoMapper;
    }

    @Transactional
    public MovimientoResponseDTO depositar(OperacionRequestDTO request) {
        Cuenta cuenta = buscarPorNumero(request.numeroCuenta());

        cuenta.setSaldo(cuenta.getSaldo().add(request.cantidad()));
        Movimiento movimiento = movimientoRepository.save(
                movimientoFactory.crearDeposito(cuenta, request.cantidad())
        );

        return movimientoMapper.toResponse(movimiento);
    }

    @Transactional
    public MovimientoResponseDTO retirar(OperacionRequestDTO request) {
        Cuenta cuenta = buscarPorNumero(request.numeroCuenta());
        validarSaldoSuficiente(cuenta, request.cantidad());

        cuenta.setSaldo(cuenta.getSaldo().subtract(request.cantidad()));
        Movimiento movimiento = movimientoRepository.save(
                movimientoFactory.crearRetiro(cuenta, request.cantidad())
        );

        return movimientoMapper.toResponse(movimiento);
    }

    @Transactional
    public List<MovimientoResponseDTO> transferir(TransferenciaRequestDTO request) {
        String origen = normalizarNumeroCuenta(request.numeroCuentaOrigen());
        String destino = normalizarNumeroCuenta(request.numeroCuentaDestino());

        if (origen.equals(destino)) {
            throw new IllegalArgumentException("La cuenta origen y destino deben ser diferentes");
        }

        Cuenta cuentaOrigen = buscarPorNumero(origen);
        Cuenta cuentaDestino = buscarPorNumero(destino);
        BigDecimal cantidad = request.cantidad();

        validarSaldoSuficiente(cuentaOrigen, cantidad);

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(cantidad));
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(cantidad));

        Movimiento saliente = movimientoRepository.save(
                movimientoFactory.crearTransferenciaSaliente(cuentaOrigen, cantidad)
        );
        Movimiento entrante = movimientoRepository.save(
                movimientoFactory.crearTransferenciaEntrante(cuentaDestino, cantidad)
        );

        return List.of(saliente, entrante)
                .stream()
                .map(movimientoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarMovimientos(Long cuentaId) {
        if (!cuentaRepository.existsById(cuentaId)) {
            throw new ResourceNotFoundException("No existe ninguna cuenta con id " + cuentaId);
        }

        return movimientoRepository.findByCuentaIdOrderByFechaDesc(cuentaId)
                .stream()
                .map(movimientoMapper::toResponse)
                .toList();
    }

    private Cuenta buscarPorNumero(String numeroCuenta) {
        String normalizado = normalizarNumeroCuenta(numeroCuenta);

        return cuentaRepository.findByNumeroCuenta(normalizado)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ninguna cuenta con numero " + normalizado));
    }

    private String normalizarNumeroCuenta(String numeroCuenta) {
        return numeroCuenta.trim().toUpperCase(Locale.ROOT);
    }

    private void validarSaldoSuficiente(Cuenta cuenta, BigDecimal cantidad) {
        if (cuenta.getSaldo().compareTo(cantidad) < 0) {
            throw new InsufficientBalanceException("SALDO_INSUFICIENTE");
        }
    }
}
