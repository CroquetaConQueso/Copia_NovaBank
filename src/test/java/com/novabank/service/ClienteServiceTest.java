package com.novabank.service;

import com.novabank.dto.ClienteRequestDTO;
import com.novabank.dto.ClienteResponseDTO;
import com.novabank.exception.DuplicateResourceException;
import com.novabank.mapper.ClienteMapper;
import com.novabank.model.Cliente;
import com.novabank.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Spy
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void crearClienteNormalizaDatosYGuardaCliente() {
        ClienteRequestDTO request = new ClienteRequestDTO(
                " Ana ",
                " Garcia ",
                "12345678a",
                "ANA@example.COM",
                "600111222"
        );

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            cliente.setFechaCreacion(LocalDateTime.now());
            return cliente;
        });

        ClienteResponseDTO response = clienteService.crearCliente(request);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());

        Cliente guardado = captor.getValue();
        assertThat(guardado.getNombre()).isEqualTo("Ana");
        assertThat(guardado.getApellidos()).isEqualTo("Garcia");
        assertThat(guardado.getDni()).isEqualTo("12345678A");
        assertThat(guardado.getEmail()).isEqualTo("ana@example.com");
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.numeroCuentas()).isZero();
    }

    @Test
    void crearClienteLanzaErrorSiDniYaExiste() {
        ClienteRequestDTO request = new ClienteRequestDTO(
                "Ana",
                "Garcia",
                "12345678A",
                "ana@example.com",
                "600111222"
        );
        when(clienteRepository.existsByDni("12345678A")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crearCliente(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("DNI");
    }
}
