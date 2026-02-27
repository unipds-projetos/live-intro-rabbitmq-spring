package mx.florinda.eats.pagamentos.integration.rabbit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoConfirmadoEvent(Long pagamentoId, BigDecimal valor, Long pedidoId,
                                       LocalDateTime dataHora, UUID eventId) {

  public PagamentoConfirmadoEvent(Long pagamentoId, BigDecimal valor, Long pedidoId) {
    this(pagamentoId, valor, pedidoId, LocalDateTime.now(), UUID.randomUUID());
  }

}
