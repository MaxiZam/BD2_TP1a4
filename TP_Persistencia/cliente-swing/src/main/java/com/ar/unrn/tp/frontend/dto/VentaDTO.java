package com.ar.unrn.tp.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDTO {
    private Long id;
    private LocalDateTime fecha;
    private Long clienteId;
    private Long tarjetaId;
    private List<Long> productos;
    private Float montoTotal;
    private String numeroVenta;
}