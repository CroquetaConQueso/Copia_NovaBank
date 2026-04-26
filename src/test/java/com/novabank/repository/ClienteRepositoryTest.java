package com.novabank.repository;

import com.novabank.model.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void guardaYBuscaClientePorDniEmailYTelefono() {
        Cliente cliente = Cliente.builder()
                .nombre("Ana")
                .apellidos("Garcia Lopez")
                .dni("12345678A")
                .email("ana.garcia@example.com")
                .telefono("600111222")
                .build();

        Cliente guardado = clienteRepository.save(cliente);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getFechaCreacion()).isNotNull();
        assertThat(clienteRepository.findByDni("12345678A")).contains(guardado);
        assertThat(clienteRepository.findByEmail("ana.garcia@example.com")).contains(guardado);
        assertThat(clienteRepository.findByTelefono("600111222")).contains(guardado);
    }
}
