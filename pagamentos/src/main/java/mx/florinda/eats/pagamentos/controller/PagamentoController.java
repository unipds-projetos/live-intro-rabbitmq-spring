package mx.florinda.eats.pagamentos.controller;

import jakarta.transaction.Transactional;
import mx.florinda.eats.pagamentos.config.AmqpConfig;
import mx.florinda.eats.pagamentos.dto.PagamentoDTO;
import mx.florinda.eats.pagamentos.integration.rabbit.PagamentoConfirmadoEvent;
import mx.florinda.eats.pagamentos.model.Pagamento;
import mx.florinda.eats.pagamentos.repository.PagamentoRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

  private final PagamentoRepository pagamentoRepository;
  private final AmqpTemplate amqpTemplate;

  public PagamentoController(PagamentoRepository pagamentoRepository, AmqpTemplate amqpTemplate) {
    this.pagamentoRepository = pagamentoRepository;
    this.amqpTemplate = amqpTemplate;
  }

  @GetMapping
  public List<PagamentoDTO> lista() {
    List<Pagamento> pagamentos = pagamentoRepository.findAll();
    return pagamentos.stream().map(PagamentoDTO::new).toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<PagamentoDTO> porId(@PathVariable("id") Long id) {
    return pagamentoRepository.findById(id)
        .map(pagamento -> ResponseEntity.ok(new PagamentoDTO(pagamento)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  @Transactional
  public  ResponseEntity<PagamentoDTO> confirma(@PathVariable("id") Long id) {
    return pagamentoRepository.findById(id)
        .map(pagamento -> {
          pagamento.confirma();

          var evento = new PagamentoConfirmadoEvent(pagamento.getId(), pagamento.getValor(), pagamento.getPedidoId());

          amqpTemplate.convertAndSend(AmqpConfig.PAGAMENTOS_EXCHANGE, "pagamentos.pagamento.confirmado", evento);

          return ResponseEntity.ok(new PagamentoDTO(pagamento));
        })
        .orElse(ResponseEntity.notFound().build());
  }

}
