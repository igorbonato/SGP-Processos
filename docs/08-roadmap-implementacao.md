# 08 — Roadmap de Implementação (Passo 2)

Ordem em que o código será escrito, cada fase pequena o bastante para você revisar/aprovar antes de eu
seguir para a próxima. Nenhuma fase começa sem a fase anterior estar "verde".

| Fase | Entregável | Cobre do edital |
|---|---|---|
| 0 | POM pai (aggregator) com `<dependencyManagement>` — versões centralizadas de Spring Boot, Spring Cloud, MapStruct, JJWT | Maven |
| 1 | `eureka-server` funcional (sobe, mostra dashboard em `localhost:8761`) | Spring Cloud, Spring Eureka |
| 2 | `processo-service`: entidades `Processo`/`Parte`/`Movimentacao` (JPA + Hibernate) + migrations Flyway + repositories Spring Data | Jakarta EE 8, JPA 2.0, Hibernate |
| 3 | `processo-service`: DTOs + MapStruct mappers + services + controllers REST + `GlobalExceptionHandler` | Spring Boot, REST, MapStruct |
| 4 | `processo-service`: Swagger/OpenAPI configurado e navegável | Swagger/OpenAPI |
| 5 | `processo-service`: Spring Security + JWT (login, filtro, `@PreAuthorize`) | JWT |
| 6 | `api-gateway`: Zuul roteando para `processo-service` via Eureka + filtro de validação JWT | Spring Cloud, Zuul |
| 7 | Testes unitários (JUnit + Mockito) dos services/mappers + testes de integração dos controllers + JaCoCo configurado com gate mínimo | JUnit, Mockito, testes unitários/integração, cobertura |
| 8 | `logback-spring.xml` com encoder JSON + appender para Logstash; `processo-service` empacotado como WAR e testado em Tomcat local | Elastic Stack, Tomcat/JBoss/WildFly |
| 9 (opcional) | `docker-compose.yml` subindo Elasticsearch + Logstash + Kibana para validar o pipeline ponta a ponta | Elastic Stack (demonstração prática) |

## Regra de comentários do código (relembrando o combinado)

A partir da Fase 0, todo arquivo novo segue:

- Comentário no topo do pacote (`package-info.java` quando fizer sentido) explicando o papel do pacote.
- Toda annotation não-trivial comentada com "o que faz por baixo dos panos" + equivalente C#.
- Todo `pom.xml` comentado dependência por dependência, com o paralelo `.csproj`/NuGet.
- Onde houver ambiguidade spec-vs-implementação (JPA/Hibernate, Servlet/Tomcat), o comentário explicita qual é qual.
- Sempre que uma nuance com valor de prova aparecer durante a implementação/explicação do código de uma fase (uma pegadinha, uma decisão de versão, uma diferença de comportamento .NET-vs-Java), ela vira uma entrada nova em [`09-notas-para-prova.md`](09-notas-para-prova.md), na seção correspondente ao tópico do edital. É um arquivo vivo, não escrito uma vez só na Fase 0.

## Próximo passo

Se este plano estiver de acordo, confirme e eu começo pela **Fase 0** (POM pai) e **Fase 1** (`eureka-server`),
que juntas já dão um "hello world" executável do Spring Cloud antes de entrarmos na complexidade de JPA/JWT.
Se quiser reordenar, cortar ou aprofundar alguma fase (ex.: trocar Zuul por Spring Cloud Gateway também,
além do pedido no edital), me diga agora antes do código começar.
