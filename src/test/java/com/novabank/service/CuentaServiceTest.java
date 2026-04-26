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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private GeneradorNumeroCuentaStrategy generadorNumeroCuentaStrategy;

    @Spy
    private CuentaMapper cuentaMapper;

    @InjectMocks
    private CuentaService cuentaService;

    @Test
    void crearCuentaAsociaClienteYSaldoInicialCero() {
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Ana")
                .build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(generadorNumeroCuentaStrategy.generarNumeroCuenta()).thenReturn("ES00000000000000000001");
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> {
            Cuenta cuenta = invocation.getArgument(0);
            cuenta.setId(10L);
            cuenta.setFechaCreacion(LocalDateTime.now());
            return cuenta;
        });

        CuentaResponseDTO response = cuentaService.crearCuenta(new CuentaCreateRequestDTO(1L));

        ArgumentCaptor<Cuenta> captor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).save(captor.capture());

        Cuenta guardada = captor.getValue();
        assertThat(guardada.getCliente()).isSameAs(cliente);
        assertThat(guardada.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(guardada.getNumeroCuenta()).isEqualTo("ES00000000000000000001");
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.clienteId()).isEqualTo(1L);
    }

    @Test
    void crearCuentaLanzaErrorSiClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.crearCuenta(new CuentaCreateRequestDTO(99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("cliente");
    }
}
