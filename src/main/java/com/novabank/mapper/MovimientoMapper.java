package com.novabank.mapper;

import com.novabank.dto.MovimientoResponseDTO;
import com.novabank.model.Cuenta;
import com.novabank.model.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper {

    public MovimientoResponseDTO toResponse(Movimiento movimiento) {
        Cuenta cuenta = movimiento.getCuenta();

        return new MovimientoResponseDTO(
                movimiento.getId(),
                cuenta == null ? null : cuenta.getId(),
                cuenta == null ? null : cuenta.getNumeroCuenta(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getFecha()
        );
    }
}
