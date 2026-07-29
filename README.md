# SGP-Processos — Projeto Prático de Estudo Java Corporativo

> Domínio escolhido de propósito: **Sistema de Gestão de Processos Judiciais** (Processo, Parte, Movimentação).
> Motivo: cobre quase toda a seção "Frameworks Java" / "APIs e Web Services" do edital do TRT 4ª Região
> (arquivo `conteudo_programatico.md` na raiz), e o domínio é próximo o suficiente de algo real para você
> pensar em agregados/invariantes como já faz em DDD no .NET.

Este projeto é um laboratório de arquitetura Java corporativo para quem já domina C#/.NET Core + DDD e
quer atingir profundidade técnica equivalente no ecossistema Java, no nível cobrado em editais de
concursos de TI (Spring Boot, Spring Cloud, Jakarta EE, JPA/Hibernate, JWT, testes, observabilidade).

## Como ler esta documentação (Passo 1 — Planejamento)

Leia os arquivos em `docs/` **nesta ordem**:

1. [`01-arquitetura-geral.md`](docs/01-arquitetura-geral.md) — visão macro, diagrama, módulos, por que microsserviços aqui.
2. [`02-estrutura-pastas.md`](docs/02-estrutura-pastas.md) — árvore de pacotes completa, módulo a módulo.
3. [`03-camadas-e-responsabilidades.md`](docs/03-camadas-e-responsabilidades.md) — o que cada camada resolve e por quê.
4. [`04-mapeamento-dotnet-java.md`](docs/04-mapeamento-dotnet-java.md) — **De → Para** .NET Core ↔ Java/Spring.
5. [`05-especificacao-vs-implementacao.md`](docs/05-especificacao-vs-implementacao.md) — Jakarta EE (spec) vs Spring/Hibernate (impl), a maior pegadinha conceitual para quem vem do .NET.
6. [`06-seguranca-e-observabilidade.md`](docs/06-seguranca-e-observabilidade.md) — fluxo JWT e logging estruturado para ELK.
7. [`07-testes-e-qualidade.md`](docs/07-testes-e-qualidade.md) — estratégia de testes, JUnit/Mockito, cobertura (JaCoCo).
8. [`08-roadmap-implementacao.md`](docs/08-roadmap-implementacao.md) — ordem em que o código será escrito no Passo 2, por fases aprováveis.
9. [`09-notas-para-prova.md`](docs/09-notas-para-prova.md) — cheat sheet cumulativa de nuances com valor de prova (concurso), atualizada a cada fase.

## Regra de ouro do Passo 2 (código)

Quando você aprovar este plano, todo código gerado vai seguir esta regra sem exceção:
todo pacote, toda annotation e toda decisão não-óbvia do ecossistema Java vem comentada,
com o equivalente C#/.NET ao lado (ex.: `@Autowired` → injeção nativa do `IServiceCollection`,
`@Entity`/`@Table` → `[Table]`/Fluent API do EF Core, MapStruct → source generators, não o AutoMapper por reflection).

## Stack coberta

| Categoria | Tecnologias |
|---|---|
| Linguagem/Framework | Java 17, Spring Boot 3, Spring Cloud (Eureka, Zuul), Jakarta EE 8, JPA 2.0, Hibernate |
| Mapeamento | MapStruct |
| API | REST, Swagger/OpenAPI, JWT |
| Testes | JUnit 5, Mockito, testes unitários e de integração, JaCoCo |
| Deploy | WAR compatível com Tomcat / JBoss / WildFly (além de JAR embarcado) |
| Observabilidade | Logback + logstash-logback-encoder → Logstash → Elasticsearch → Kibana |

**Status:** aguardando sua aprovação do plano para iniciar o Passo 2 (código).
