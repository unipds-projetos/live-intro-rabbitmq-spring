package mx.florinda.eats.notasfiscais.messaging;

import mx.florinda.eats.notasfiscais.config.AmqpConfig;
import mx.florinda.eats.notasfiscais.dto.PagamentoDTO;
import mx.florinda.eats.notasfiscais.service.NotaFiscalService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PagamentoConfirmadoListener {

  private final NotaFiscalService notaFiscalService;
  private final long delayInMilliseconds;

  public PagamentoConfirmadoListener(NotaFiscalService notaFiscalService, @Value("${app.simulacao.delay:0}") long delayInMilliseconds) {
    this.notaFiscalService = notaFiscalService;
    this.delayInMilliseconds = delayInMilliseconds;
  }

  @RabbitListener(queues = AmqpConfig.PAGAMENTO_CONFIRMADO_QUEUE)
  public void pagamentoConfirmado(PagamentoDTO pagamentoDTO) {
    System.out.println("[Notas Fiscais] Pagamento confirmado: " + pagamentoDTO);

    simularLentidao();

    String notaFiscal = notaFiscalService.geraNotaFiscal(pagamentoDTO.pedidoId(), pagamentoDTO.valor());
    System.out.println(notaFiscal);
  }

  private void simularLentidao() {
    if (delayInMilliseconds > 0) {
      try {
        System.out.println("[Notas Fiscais] ⏳ Simulando lentidão de " + delayInMilliseconds + "ms...");
        Thread.sleep(delayInMilliseconds);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.err.println("Thread interrompida durante o sleep");
      }
    }
  }

}
