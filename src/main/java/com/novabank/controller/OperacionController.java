package com.novabank.controller;

import com.novabank.dto.MovimientoResponseDTO;
import com.novabank.dto.OperacionRequestDTO;
import com.novabank.dto.TransferenciaRequestDTO;
import com.novabank.service.OperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operaciones")
@Tag(name = "Operaciones", description = "Depositos, retiros y transferencias")
public class OperacionController {

    private final OperacionService operacionService;

    public OperacionController(OperacionService operacionService) {
        this.operacionService = operacionService;
    }

    @PostMapping("/deposito")
    @Operation(summary = "Registrar deposito")
    public ResponseEntity<MovimientoResponseDTO> depositar(@Valid @RequestBody OperacionRequestDTO request) {
        return ResponseEntity.ok(operacionService.depositar(request));
    }

    @PostMapping("/retiro")
    @Operation(summary = "Registrar retiro")
    public ResponseEntity<MovimientoResponseDTO> retirar(@Valid @RequestBody OperacionRequestDTO request) {
        return ResponseEntity.ok(operacionService.retirar(request));
    }

    @PostMapping("/transferencia")
    @Operation(summary = "Registrar transferencia")
    public ResponseEntity<List<MovimientoResponseDTO>> transferir(
            @Valid @RequestBody TransferenciaRequestDTO request
    ) {
        return ResponseEntity.ok(operacionService.transferir(request));
    }
}
