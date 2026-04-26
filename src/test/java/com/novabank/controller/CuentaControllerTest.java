package com.novabank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novabank.dto.CuentaCreateRequestDTO;
import com.novabank.dto.CuentaResponseDTO;
import com.novabank.dto.MovimientoResponseDTO;
import com.novabank.model.TipoMovimiento;
import com.novabank.service.CuentaService;
import com.novabank.service.OperacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuentaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CuentaService cuentaService;

    @MockBean
    private OperacionService operacionService;

    @Test
    void crearCuentaDevuelveCreated() throws Exception {
        when(cuentaService.crearCuenta(any(CuentaCreateRequestDTO.class))).thenReturn(
                new CuentaResponseDTO(
                        10L,
                        "ES00000000000000000001",
                        1L,
                        BigDecimal.ZERO,
                        LocalDateTime.now()
                )
        );

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CuentaCreateRequestDTO(1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.numeroCuenta").value("ES00000000000000000001"));
    }

    @Test
    void crearCuentaConClienteIdInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CuentaCreateRequestDTO(0L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.clienteId").exists());
    }

    @Test
    void listarMovimientosDevuelveMovimientosCuenta() throws Exception {
        when(operacionService.listarMovimientos(10L)).thenReturn(List.of(
                new MovimientoResponseDTO(
                        20L,
                        10L,
                        "ES00000000000000000001",
                        TipoMovimiento.DEPOSITO,
                        new BigDecimal("100.00"),
                        LocalDateTime.now()
                )
        ));

        mockMvc.perform(get("/api/cuentas/10/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$[0].numeroCuenta").value("ES00000000000000000001"));
    }
}
