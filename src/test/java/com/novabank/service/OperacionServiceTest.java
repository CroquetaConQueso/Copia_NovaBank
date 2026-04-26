package com.novabank.service;

import com.novabank.dto.MovimientoResponseDTO;
import com.novabank.dto.OperacionRequestDTO;
import com.novabank.dto.TransferenciaRequestDTO;
import com.novabank.exception.InsufficientBalanceException;
import com.novabank.mapper.MovimientoMapper;
import com.novabank.model.Cuenta;
import com.novabank.model.Movimiento;
import com.novabank.model.TipoMovimiento;
import com.novabank.repository.CuentaRepository;
import com.novabank.repository.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperacionServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Spy
    private MovimientoFactory movimientoFactory;

    @Spy
    private MovimientoMapper movimientoMapper;

    @InjectMocks
    private OperacionService operacionService;

    @Test
    void depositarAumentaSaldoYRegistraMovimiento() {
        Cuenta cuenta = cuenta(1L, "ES00000000000000000001", "100.00");
        when(cuentaRepository.findByNumeroCuenta("ES00000000000000000001")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
            Movimiento movimiento = invocation.getArgument(0);
            movimiento.setId(20L);
            return movimiento;
        });

        MovimientoResponseDTO response = operacionService.depositar(
                new OperacionRequestDTO("ES00000000000000000001", new BigDecimal("50.00"))
        );

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("150.00");
        assertThat(response.tipo()).isEqualTo(TipoMovimiento.DEPOSITO);
        assertThat(response.cantidad()).isEqualByComparingTo("50.00");
        verify(movimientoRepository).save(any(Movimiento.class));
    }

    @Test
    void retirarLanzaErrorSiSaldoInsuficiente() {
        Cuenta cuenta = cuenta(1L, "ES00000000000000000001", "25.00");
        when(cuentaRepository.findByNumeroCuenta("ES00000000000000000001")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> operacionService.retirar(
                new OperacionRequestDTO("ES00000000000000000001", new BigDecimal("50.00"))
        ))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("SALDO_INSUFICIENTE");

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("25.00");
        verify(movimientoRepository, never()).save(any(Movimiento.class));
    }

    @Test
    void transferirActualizaAmbasCuentasYRegistraDosMovimientos() {
        Cuenta origen = cuenta(1L, "ES00000000000000000001", "200.00");
        Cuenta destino = cuenta(2L, "ES00000000000000000002", "10.00");
        AtomicLong ids = new AtomicLong(30L);

        when(cuentaRepository.findByNumeroCuenta("ES00000000000000000001")).thenReturn(Optional.of(origen));
        when(cuentaRepository.findByNumeroCuenta("ES00000000000000000002")).thenReturn(Optional.of(destino));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
            Movimiento movimiento = invocation.getArgument(0);
            movimiento.setId(ids.getAndIncrement());
            return movimiento;
        });

        List<MovimientoResponseDTO> movimientos = operacionService.transferir(
                new TransferenciaRequestDTO(
                        "ES00000000000000000001",
                        "ES00000000000000000002",
                        new BigDecimal("75.00")
                )
        );

        assertThat(origen.getSaldo()).isEqualByComparingTo("125.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("85.00");
        assertThat(movimientos).hasSize(2);
        assertThat(movimientos.get(0).tipo()).isEqualTo(TipoMovimiento.TRANSFERENCIA_SALIENTE);
        assertThat(movimientos.get(1).tipo()).isEqualTo(TipoMovimiento.TRANSFERENCIA_ENTRANTE);
    }

    private Cuenta cuenta(Long id, String numeroCuenta, String saldo) {
        return Cuenta.builder()
                .id(id)
                .numeroCuenta(numeroCuenta)
                .saldo(new BigDecimal(saldo))
                .build();
    }
}
