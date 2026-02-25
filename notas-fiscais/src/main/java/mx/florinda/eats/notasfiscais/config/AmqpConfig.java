package mx.florinda.eats.notasfiscais.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {

  public static final String PAGAMENTO_CONFIRMADO_QUEUE = "notas-fiscais.pagamento-confirmado";


  @Bean
  public Queue filaPagamentoConfirmado() {
    return new Queue(PAGAMENTO_CONFIRMADO_QUEUE);
  }


  @Bean
  public TopicExchange pagamentosExchange() {
    return new TopicExchange("pagamentos");
  }


  @Bean
  public Binding bindingPagamentoConfirmado(Queue filaPedidoRealizado, TopicExchange pedidosExchange) {
    return BindingBuilder
        .bind(filaPedidoRealizado)
        .to(pedidosExchange)
        .with("pagamentos.pagamento.confirmado");
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

}
