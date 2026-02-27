package mx.florinda.eats.notasfiscais.repository;

import mx.florinda.eats.notasfiscais.dto.PedidoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class PedidoClient {

  private final String pedidoBaseUrl;

  public PedidoClient(@Value("${pedido.api.base-url}")  String pedidoBaseUrl) {
    this.pedidoBaseUrl = pedidoBaseUrl;
  }

  public Optional<PedidoDTO> getById(Long pedidoId) {
    ResponseEntity<PedidoDTO> responseEntity = RestClient.create()
        .get()
        .uri(pedidoBaseUrl + "/pedidos/{id}", pedidoId)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .toEntity(PedidoDTO.class);

    if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
      return Optional.empty();
    }

    return Optional.ofNullable(responseEntity.getBody());
  }


}
