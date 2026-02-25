package mx.florinda.eats.pedidos.dto;

import java.math.BigDecimal;

public record PagamentoDTO(Long id, String status, BigDecimal valor, Long pedidoId) {
}
