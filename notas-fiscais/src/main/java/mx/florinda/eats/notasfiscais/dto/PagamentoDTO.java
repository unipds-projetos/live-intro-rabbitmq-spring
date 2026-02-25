package mx.florinda.eats.notasfiscais.dto;

import java.math.BigDecimal;

public record PagamentoDTO(Long id, String status, BigDecimal valor, Long pedidoId) {
}

