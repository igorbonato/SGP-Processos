# SGP-Processos — Projeto Prático de Estudo Java Corporativo

> Domínio escolhido de propósito: **Sistema de Gestão de Processos Judiciais** (Processo, Parte, Movimentação).
> Motivo: cobre quase toda a seção "Frameworks Java" / "APIs e Web Services" do edital do TRT 4ª Região
> (arquivo `conteudo_programatico.md` na raiz), e o domínio é próximo o suficiente de algo real para você
> pensar em agregados/invariantes como já faz em DDD no .NET.

Laboratório de arquitetura Java corporativo para quem já domina C#/.NET Core + DDD e quer atingir
profundidade técnica equivalente no ecossistema Java, no nível cobrado em editais de concursos de
TI (Spring Boot, Spring Cloud, Jakarta EE, JPA/Hibernate, JWT, testes, observabilidade).

## Status

**Roadmap principal completo (Fases 0-8)** — três módulos rodando, testados de ponta a ponta
diversas vezes ao longo da construção (não só revisão de código): `eureka-server` (Service
Discovery), `processo-service` (o serviço de domínio, com toda a stack: JPA/Hibernate, MapStruct,
Spring Security + JWT, Swagger/OpenAPI, testes JUnit/Mockito/JaCoCo, logging pronto para ELK,
testado tanto embutido quanto como WAR num Tomcat externo de verdade) e `api-gateway` (Zuul
roteando via Eureka + validação de JWT). A **Fase 9** (subir o stack ELK via Docker para ver o
pipeline de log funcionando de ponta a ponta) é opcional — os arquivos já estão prontos
(`docker-compose.yml`), fica para quando você quiser rodar com calma.

## Como rodar

### Pré-requisitos

- **JDK 8** e **Maven 3.9.16**, instalados em `C:\Users\Igor\tools\` e no PATH do usuário (abra um
  terminal **novo** se acabou de instalar — terminais já abertos não pegam a variável).
- **Docker** (só se for testar a Fase 9/ELK).

### Build

```
mvn clean install
```
na raiz do repositório, compila e testa os 4 módulos (roda `mvn verify` para rodar também os
testes de integração de `processo-service`).

### Subir os 3 serviços (nesta ordem — cada um espera o anterior estar de pé)

| Ordem | Módulo | Comando (dentro da pasta do módulo) | Porta | O que checar |
|---|---|---|---|---|
| 1 | `eureka-server` | `mvn spring-boot:run` | 8761 | Dashboard em http://localhost:8761 |
| 2 | `processo-service` | `mvn spring-boot:run` | 8081 | http://localhost:8081/actuator/health |
| 3 | `api-gateway` | `mvn spring-boot:run` | 8080 | http://localhost:8080/actuator/health — **porta de entrada única**, o cliente final só deveria falar com esta |

Dê uns 20-30s depois do `api-gateway` subir antes de testar rotas via ele — é o tempo que o
cliente Eureka leva para atualizar o catálogo de instâncias (ver docs/09, seção sobre isso não
existir ainda — é comportamento normal do Ribbon/Eureka, não bug).

### Usuários de teste (em memória, ver `SecurityConfig`)

| username | password | role | pode |
|---|---|---|---|
| `analista` | `senha123` | `ROLE_ANALISTA` | criar/arquivar processo, adicionar movimentação |
| `consulta` | `senha123` | `ROLE_CONSULTA` | só leitura (GET) |

```
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"username":"analista","password":"senha123"}'
```

### Endpoints úteis

- **Swagger UI**: http://localhost:8081/swagger-ui.html (direto) ou http://localhost:8080/swagger-ui.html (via gateway)
- **Eureka dashboard**: http://localhost:8761
- **H2 Console** (perfil dev): http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:sgp_processos`, usuário `sa`, senha em branco)

### ELK (Fase 9, opcional)

```
docker-compose up -d
```
Depois, abra o Kibana em http://localhost:5601 e crie um index pattern `sgp-processos-*` — os
logs do `processo-service` (rodando fora do Docker, normalmente) já chegam lá sozinhos via o
`logback-spring.xml`. Ver comentários em `docker-compose.yml` e `elk/logstash/pipeline/logstash.conf`.

### Troubleshooting

Se você usa a extensão **"Language Support for Java" (redhat.java)** do VS Code neste projeto:
já enfrentamos um problema real onde ela recompilava o projeto em background com um JDK
incompatível, corrompendo o `target/classes` do Maven no meio de builds. Se `mvn compile`/`test`
começar a falhar com `"Unsupported class file major version"` do nada, verifique se
`java.autoBuild.enabled` está desligado nas configurações do VS Code (`Ctrl+,`).

## Como estudar este projeto

1. **[`docs/09-notas-para-prova.md`](docs/09-notas-para-prova.md) primeiro, e sempre.** É a folha
   de revisão rápida (pergunta/resposta), organizada pelas seções do edital, cumulativa — cresceu
   a cada fase com nuances reais (a maioria descoberta testando, não só lendo documentação).
2. Depois, os docs `01`-`08` (ver índice completo abaixo) para entender o "porquê" por trás de
   cada decisão de arquitetura.
3. Por fim, o código-fonte em si — cada pacote, annotation e decisão não-óbvia está comentada
   inline, sempre com o paralelo C#/.NET ao lado.

## Estrutura do repositório

```
sgp-processos/
├── eureka-server/        # Service Discovery (Fase 1)
├── processo-service/      # Serviço de domínio — a maior parte do código (Fases 2-5, 7-8)
├── api-gateway/           # Zuul + validação JWT (Fase 6)
├── docs/                  # Documentação de arquitetura + cheat sheet de prova
├── elk/logstash/pipeline/ # Config do Logstash (Fase 9, opcional)
└── docker-compose.yml     # Stack ELK (Fase 9, opcional)
```

## Documentação de arquitetura (`docs/`)

1. [`01-arquitetura-geral.md`](docs/01-arquitetura-geral.md) — visão macro, diagrama, módulos, por que microsserviços aqui.
2. [`02-estrutura-pastas.md`](docs/02-estrutura-pastas.md) — árvore de pacotes completa, módulo a módulo.
3. [`03-camadas-e-responsabilidades.md`](docs/03-camadas-e-responsabilidades.md) — o que cada camada resolve e por quê.
4. [`04-mapeamento-dotnet-java.md`](docs/04-mapeamento-dotnet-java.md) — **De → Para** .NET Core ↔ Java/Spring.
5. [`05-especificacao-vs-implementacao.md`](docs/05-especificacao-vs-implementacao.md) — Jakarta EE (spec) vs Spring/Hibernate (impl), a maior pegadinha conceitual para quem vem do .NET.
6. [`06-seguranca-e-observabilidade.md`](docs/06-seguranca-e-observabilidade.md) — fluxo JWT e logging estruturado para ELK.
7. [`07-testes-e-qualidade.md`](docs/07-testes-e-qualidade.md) — estratégia de testes, JUnit/Mockito, cobertura (JaCoCo).
8. [`08-roadmap-implementacao.md`](docs/08-roadmap-implementacao.md) — a ordem em que o código foi de fato escrito, fase a fase.
9. [`09-notas-para-prova.md`](docs/09-notas-para-prova.md) — cheat sheet cumulativa de nuances com valor de prova (concurso).

## Convenção de comentários do código

Todo o código segue uma regra rígida: todo pacote, toda annotation e toda decisão não-óbvia do
ecossistema Java vem comentada, com o equivalente C#/.NET ao lado (ex.: `@Autowired` → injeção
nativa do `IServiceCollection`, `@Entity`/`@Table` → `[Table]`/Fluent API do EF Core, MapStruct →
source generators, não o AutoMapper por reflection).

## Stack coberta

| Categoria | Tecnologias |
|---|---|
| Linguagem/Framework | Java 8, Spring Boot 2.3.12.RELEASE, Spring Cloud Hoxton.SR12 (Eureka, Zuul), Jakarta EE 8 (`javax.*`), JPA 2.0, Hibernate |
| Mapeamento | MapStruct |
| API | REST, Swagger/OpenAPI (springdoc), JWT (JJWT) |
| Testes | JUnit 5, Mockito, testes unitários e de integração (MockMvc), cobertura (JaCoCo, gate 70%) |
| Deploy | WAR compatível com Tomcat / JBoss / WildFly (testado em Tomcat 9 real) + JAR embarcado |
| Observabilidade | Logback + logstash-logback-encoder → Logstash → Elasticsearch → Kibana (Fase 9 opcional) |

> Versões mais antigas que o "estado da arte" atual (Spring Boot 2.x, não 3.x) são uma decisão
> deliberada, não desatualização — ver `docs/05` e `docs/09` para o porquê (compatibilidade com
> Zuul, pedido explicitamente pelo edital).
