package com.novabank.repository;

import com.novabank.model.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @EntityGraph(attributePaths = "cuentas")
    @Query("select distinct c from Cliente c")
    List<Cliente> findAllWithCuentas();

    @EntityGraph(attributePaths = "cuentas")
    @Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> findByIdWithCuentas(Long id);

    Optional<Cliente> findByDni(String dni);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByTelefono(String telefono);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByTelefono(String telefono);
}
