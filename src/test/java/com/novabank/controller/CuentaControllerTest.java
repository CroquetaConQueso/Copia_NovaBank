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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
                        "ES91210000000000000001",
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
                .andExpect(jsonPath("$.numeroCuenta").value("ES91210000000000000001"));
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
        when(operacionService.listarMovimientos(eq(10L), isNull(), isNull())).thenReturn(List.of(
                new MovimientoResponseDTO(
                        20L,
                        10L,
                        "ES91210000000000000001",
                        TipoMovimiento.DEPOSITO,
                        new BigDecimal("100.00"),
                        LocalDateTime.now()
                )
        ));

        mockMvc.perform(get("/api/cuentas/10/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$[0].numeroCuenta").value("ES91210000000000000001"));
    }

    @Test
    void listarMovimientosPorRangoDevuelveMovimientosFiltrados() throws Exception {
        LocalDate inicio = LocalDate.of(2026, 4, 1);
        LocalDate fin = LocalDate.of(2026, 4, 26);

        when(operacionService.listarMovimientos(10L, inicio, fin)).thenReturn(List.of(
                new MovimientoResponseDTO(
                        20L,
                        10L,
                        "ES91210000000000000001",
                        TipoMovimiento.DEPOSITO,
                        new BigDecimal("100.00"),
                        LocalDateTime.of(2026, 4, 20, 10, 0)
                )
        ));

        mockMvc.perform(get("/api/cuentas/10/movimientos")
                        .param("fechaInicio", "2026-04-01")
                        .param("fechaFin", "2026-04-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("DEPOSITO"));
    }

    @Test
    void listarMovimientosConRangoIncompletoDevuelve400() throws Exception {
        when(operacionService.listarMovimientos(eq(10L), eq(LocalDate.of(2026, 4, 1)), isNull()))
                .thenThrow(new IllegalArgumentException("Debe informar fechaInicio y fechaFin para filtrar por rango"));

        mockMvc.perform(get("/api/cuentas/10/movimientos")
                        .param("fechaInicio", "2026-04-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
