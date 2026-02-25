# Live: Integração Assíncrona com RabbitMQ

Este repositório contém um projeto usado em uma live sobre integração assíncrona com mensageria (RabbitMQ). São três microserviços independentes que ilustram comunicação síncrona (REST) e assíncrona (mensageria): **pedidos**, **pagamentos** e **notas-fiscais**.

## Sumário

- Visão geral
- Pré-requisitos
- Tecnologias utilizadas
- Descrição dos módulos
- Como executar (Docker Compose + IntelliJ)
- Postman: coleções e o que elas fazem

## Visão geral

O objetivo desta base é demonstrar um cenário integração entre microserviços usando padrões síncronos (chamadas REST) e assíncronos (RabbitMQ). Cada serviço tem seu próprio banco de dados MySQL e responsabilidades de domínio:

- `pedidos`: gestão de pedidos (criação, consulta). Porta: `8080`.
- `pagamentos`: processamento de pagamento associado a pedidos. Porta: `8081`.
- `notas-fiscais`: emissão/registro de notas fiscais com integração ao serviço de `pedidos`. Porta: `8082`.

## Pré-requisitos

- Docker & Docker Compose (para subir MySQL e RabbitMQ local)
- JDK 25
- Maven
- IntelliJ IDEA (ou outra IDE Java com suporte a Spring Boot)
- Postman (para importar e executar as collections fornecidas)

Observação: as URLs de banco de dados e credenciais usadas nas propriedades dos módulos apontam para `localhost:3306` com usuário `root` e senha `senha123` (ver `docker-compose.yml`).

## Tecnologias

- Spring Boot 4.0.3
- Spring Data JPA + Hibernate
- Flyway (migrações de banco)
- MySQL (imagem `mysql:9` usada no Docker Compose)
- RabbitMQ 
- Maven (build)

## Descrição dos módulos (domínio e tech)

- `pedidos`
  - Domínio: criação e consulta de pedidos, expõe API REST.
  - Tech: Spring Boot, JPA, Flyway, MySQL, Spring AMQP.
  - Main: `mx.florinda.eats.pedidos.PedidosApplication`.
  - DB: `florinda_pedidos`

- `pagamentos`
  - Domínio: criação/registro de pagamentos
  - Tech: Spring Boot, JPA, Flyway, MySQL, Spring AMQP.
  - Main: `mx.florinda.eats.pagamentos.PagamentosApplication`.
  - DB: `florinda_pagamentos`.

- `notas-fiscais`
  - Domínio: emissão e armazenamento de notas fiscais a partir de integrações com pedidos.
  - Tech: Spring Boot, consumo de APIs REST de `pedidos`, Spring AMQP.
  - Main: `mx.florinda.eats.notasfiscais.NotasFiscaisApplication`.

## Como executar

1) Iniciar o banco MySQL e RabbitMQ via Docker Compose (arquivo `docker-compose.yml` na raiz):

```bash
docker-compose up -d
```

Isso cria o serviço `db.florinda` usando `mysql:9`, mapeando a porta `3306` e definindo `MYSQL_ROOT_PASSWORD=senha123`.

Cria também um broker RabbitMQ na porta `5672` com Management UI na porta `15672`:

- URL: http://localhost:15672
- usuário/senha: `admin` / `senha123`

3) Iniciar as aplicações na IDE (IntelliJ):

- Abra a pasta do repositório como um projeto Maven.
- Aguarde o download das dependências.
- Para cada módulo (`pedidos`, `pagamentos`, `notas-fiscais`) localize a classe `*Application` (ex.: `PedidosApplication`) e execute como aplicação Spring Boot.

Exemplo de execução a partir do terminal (opcional):

```bash
# Na raiz de cada módulo
mvn clean spring-boot:run -DskipTests
```


4) Verifique as portas e endpoints:

- `pedidos` → http://localhost:8080
- `pagamentos` → http://localhost:8081
- `notas-fiscais` → http://localhost:8082

6) Executando múltiplas instâncias do mesmo serviço (para testes de escalabilidade/fair-dispatch):

- Na IDE duplique a Run Configuration e modifique `VM Options` / `Program Arguments` para alterar `server.port` e/ou `--app.simulacao.delay`.
  - Exemplo: adicionar `--app.simulacao.delay=15000` em uma das instâncias para simular lentidão.

7) Re-rodando mensagens / reset de status (exemplo de SQL útil durante a live):

```sql
UPDATE florinda_pagamentos.pagamento SET status = 'CRIADO';
```

Isso permite reenfileirar processamentos simulando novos pagamentos.

## Postman — collections

Existem collections Postman fornecidas para facilitar os testes e demonstrar os fluxos. Arquivos (na raiz de cada módulo):

- [notas-fiscais/florinda-eats-notas-fiscais.postman_collection.json](notas-fiscais/florinda-eats-notas-fiscais.postman_collection.json)
- [pagamentos/florinda-eats-notas-fiscais.postman_collection.json](pagamentos/florinda-eats-notas-fiscais.postman_collection.json)
- [pedidos/florinda-eats-pedidos.postman_collection.json](pedidos/florinda-eats-pedidos.postman_collection.json)

O que as collections fazem (resumo):

- `pedidos` collection:
  - Endpoints para criar pedidos, consultar pedidos, listar itens.
  - Fluxo típico: criar pedido → obter id → consultar status.

- `pagamentos` collection:
  - Endpoints para simular criação de pagamento e consulta de status do pagamento.
  - Usar para testar integrações entre pedido → processamento de pagamento.

- `notas-fiscais` collection:
  - Endpoints para gerar/consultar notas fiscais associadas a pedidos.
  - Demonstra integração com `pedidos` (consulta de dados do pedido para emissão).

Fluxos de teste com Postman (exemplos):

- Criar um pedido via `pedidos` → criar um pagamento via `pagamentos` (endpoint de confirmar pagamento) → verificar se o pagamento produz a mensagem `pagamentos.pagamento.confirmado` na exchange `pagamentos` e se os consumidores (`pedidos` e `notas-fiscais`) processaram a mensagem.
- Use o Management UI para inspecionar mensagens nas filas, executar `Get Messages` e testar ACK/NACK.

Importação: no Postman clique em _Import_ → selecione o arquivo `.json` correspondente.

## O que foi implementado durante a live

Principais pontos implementados:

- Serviço RabbitMQ no `docker-compose.yml` (imagem `rabbitmq:4.2-management`) com usuário `admin` / senha `senha123` e Management UI em `http://localhost:15672`.
- Producer em `pagamentos` usando `AmqpTemplate` e envio para a exchange `pagamentos` com routing keys do tipo `pagamentos.<agregado>.<evento>`.
- Configuração de `MessageConverter` com `JacksonJsonMessageConverter` para serializar/deserializar payloads JSON entre produtores e consumidores.
- Declaração programática de `TopicExchange`, `Queue` e `Binding` em `AmqpConfig` nos módulos `pagamentos`, `pedidos` e `notas-fiscais`.
- Consumidores (`@RabbitListener`) implementados em `pedidos` e `notas-fiscais` que escutam as filas:
  - `pedidos.pagamento-confirmado`
  - `notas-fiscais.pagamento-confirmado`
- DTO compartilhado `PagamentoDTO` (record) usado como payload entre os serviços.
- Controle de comportamento do listener:
  - `spring.rabbitmq.listener.simple.prefetch` configurado com `1` para demonstrar fair-dispatch.
  - `app.simulacao.delay` property usada para simular lentidão em uma instância de consumidor (útil para demonstração de distribuição de trabalho).
- O módulo `pedidos` demonstra o uso de Dead Letter Exchange (DLX) e Dead Letter Queue (DLQ). O padrão ajuda a: isolar mensagens com problema, permitir reprocessamento manual/automatizado e evitar perda silenciosa de dados. Configuração de DLX e DLQ feita em `AmqpConfig` e tratamento de erros lançando `AmqpRejectAndDontRequeueException` no `PagamentoConfirmadoListener` de `pedidos`.

Estas mudanças permitem demonstrar os conceitos da live: exchanges, routing keys, bindings, consumer groups, fair dispatch e DLX/DLQ.

## Desafios

### 🥉 Protegendo as Notas Fiscais
Durante a live, a infraestrutura de Dead Letter Exchange (DLX) e Dead Letter Queue (DLQ) foi configurada apenas no microserviço de Pedidos.

Vá além e treine com os desafios a seguir. Mande uma PR e uma mensagem no Discord para que seu código seja revisado pelos nossos instrutores.

**A Tarefa**

Você deve replicar essa mesma infra de Dead Letters no microserviço de Notas Fiscais.

Simule uma exceção no PagamentoConfirmadoListener de Notas Fiscais (por exemplo, lançar um erro se o valor do pagamento for superior a R$ 100,00) e garantir que a mensagem rejeitada vá parar em uma nova fila chamada `notas-fiscais.pagamento-confirmado.dlq`

### 🥈 Implementando um retry

Durante a live, foi comentado conceitualmente que falhas intermitentes (como uma queda rápida de rede) podem ser tratadas com retentativas (retry), inclusive usando _Exponential Backoff_ para não sobrecarregar o servidor.

**A Tarefa**

Implementar esse mecanismo no Spring Boot sem escrever novos blocos try/catch. Você deve pesquisar como configurar o Retry do Spring AMQP diretamente no `application.properties` do consumidor.

Configurar para que o Spring tente reprocessar a mensagem até 3 vezes, com um intervalo inicial de 2 segundos que vai dobrando a cada falha (2s, 4s, 8s). Somente após esgotar essas 3 tentativas a mensagem deve ser finalmente enviada para a DLX.

### 🥇 Migrando para Spring Cloud Stream

Durante a live, foi mencionado que ferramentas como o Spring Integration e o Spring Cloud Stream abstraem ainda mais o uso da mensageria, sendo o Spring AMQP a melhor escolha inicial para focar e fixar os conceitos do RabbitMQ. No entanto, o Spring Cloud Stream é amplamente adotado no mercado por oferecer um modelo de programação funcional, onde você se preocupa apenas com a regra de negócio e deixa a criação e o roteamento das *Exchanges* e *Queues* a cargo da configuração do *binder*.

**A Tarefa**

Refatorar o projeto para utilizar o Spring Cloud Stream na comunicação entre os microsserviços. 

Você deverá adicionar a dependência do `spring-cloud-stream-binder-rabbit`, remover o uso direto do `AmqpTemplate` no produtor (Pagamentos) e substituir as anotações `@RabbitListener` nos consumidores (Pedidos e Notas Fiscais). Em seu lugar, implemente a emissão e o consumo de mensagens utilizando as interfaces funcionais do Java (`Supplier` e `Consumer`). Toda a configuração de infraestrutura — como definições de destinos, grupos de consumidores (que representam as filas) e até as regras de DLQ e Retry — deverá ser migrada e configurada exclusivamente via `application.properties`.

---
Projeto destinado a fins educacionais para a live sobre integração assíncrona com mensageria.
