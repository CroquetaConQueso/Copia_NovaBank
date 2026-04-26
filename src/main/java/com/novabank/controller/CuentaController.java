package com.novabank.controller;

import com.novabank.dto.CuentaCreateRequestDTO;
import com.novabank.dto.CuentaResponseDTO;
import com.novabank.dto.MovimientoResponseDTO;
import com.novabank.service.CuentaService;
import com.novabank.service.OperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Cuentas", description = "Gestion de cuentas y consulta de movimientos")
public class CuentaController {

    private final CuentaService cuentaService;
    private final OperacionService operacionService;

    public CuentaController(CuentaService cuentaService, OperacionService operacionService) {
        this.cuentaService = cuentaService;
        this.operacionService = operacionService;
    }

    @PostMapping("/cuentas")
    @Operation(summary = "Crear cuenta")
    public ResponseEntity<CuentaResponseDTO> crearCuenta(@Valid @RequestBody CuentaCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cuentaService.crearCuenta(request));
    }

    @GetMapping("/cuentas/{id}")
    @Operation(summary = "Obtener cuenta por id")
    public ResponseEntity<CuentaResponseDTO> obtenerCuenta(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.obtenerCuenta(id));
    }

    @GetMapping("/clientes/{clienteId}/cuentas")
    @Operation(summary = "Listar cuentas de un cliente")
    public ResponseEntity<List<CuentaResponseDTO>> listarCuentasPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(cuentaService.listarCuentasPorCliente(clienteId));
    }

    @GetMapping("/cuentas/{id}/movimientos")
    @Operation(summary = "Listar movimientos de una cuenta")
    public ResponseEntity<List<MovimientoResponseDTO>> listarMovimientos(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.listarMovimientos(id));
    }
}
