package mx.florinda.eats.pedidos.messaging;

import jakarta.transaction.Transactional;
import mx.florinda.eats.pedidos.config.AmqpConfig;
import mx.florinda.eats.pedidos.dto.PagamentoDTO;
import mx.florinda.eats.pedidos.model.Pedido;
import mx.florinda.eats.pedidos.model.StatusPedido;
import mx.florinda.eats.pedidos.repository.PedidoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoConfirmadoListener {

  private final PedidoRepository pedidoRepository;

  public PagamentoConfirmadoListener(PedidoRepository pedidoRepository) {
    this.pedidoRepository = pedidoRepository;
  }

  @Transactional
  @RabbitListener(queues = AmqpConfig.PAGAMENTO_CONFIRMADO_QUEUE)
  public void pagamentoConfirmado(PagamentoDTO pagamentoDTO) {
    System.out.println("[Pedidos] Pagamento confirmado: " + pagamentoDTO);

    Pedido pedido = pedidoRepository.findById(pagamentoDTO.pedidoId()).orElseThrow();
    pedido.setStatus(StatusPedido.PAGO);
  }

}
