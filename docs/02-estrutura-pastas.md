# 02 — Estrutura de Pastas (Pacotes)

## Árvore do repositório (multi-module Maven)

```
sgp-processos/
├── pom.xml                              # POM pai (aggregator) — ~ Directory.Build.props / central package management
├── eureka-server/
│   ├── pom.xml
│   └── src/main/
│       ├── java/br/jus/trt4/eureka/
│       │   └── EurekaServerApplication.java
│       └── resources/
│           └── application.yml
├── api-gateway/
│   ├── pom.xml
│   └── src/main/
│       ├── java/br/jus/trt4/gateway/
│       │   ├── ApiGatewayApplication.java
│       │   ├── config/
│       │   │   └── ZuulRoutesConfig.java
│       │   └── filter/
│       │       └── JwtValidationZuulFilter.java   # cross-cutting: valida token antes de rotear
│       └── resources/
│           └── application.yml
└── processo-service/
    ├── pom.xml                          # packaging = war
    └── src/
        ├── main/
        │   ├── java/br/jus/trt4/processo/
        │   │   ├── ProcessoServiceApplication.java   # extends SpringBootServletInitializer (permite WAR)
        │   │   ├── config/               # @Configuration: Swagger, Security, CORS, MapStruct central config
        │   │   │   ├── OpenApiConfig.java
        │   │   │   └── SecurityConfig.java
        │   │   ├── security/             # tudo relacionado a JWT
        │   │   │   ├── JwtTokenProvider.java
        │   │   │   ├── JwtAuthenticationFilter.java
        │   │   │   └── JwtAuthEntryPoint.java
        │   │   ├── controller/           # camada REST — ~ Controllers do ASP.NET Core
        │   │   │   ├── ProcessoController.java
        │   │   │   ├── MovimentacaoController.java
        │   │   │   └── AuthController.java
        │   │   ├── dto/                  # objetos de transporte — nunca expor entidade JPA direto
        │   │   │   ├── request/
        │   │   │   │   ├── ProcessoRequestDTO.java
        │   │   │   │   └── MovimentacaoRequestDTO.java
        │   │   │   └── response/
        │   │   │       ├── ProcessoResponseDTO.java
        │   │   │       └── MovimentacaoResponseDTO.java
        │   │   ├── mapper/                # interfaces MapStruct — geradas em compile-time
        │   │   │   ├── ProcessoMapper.java
        │   │   │   └── MovimentacaoMapper.java
        │   │   ├── service/               # regra de negócio — ~ Application/Domain Services no seu DDD
        │   │   │   ├── ProcessoService.java          # interface
        │   │   │   ├── impl/ProcessoServiceImpl.java
        │   │   │   ├── MovimentacaoService.java
        │   │   │   └── impl/MovimentacaoServiceImpl.java
        │   │   ├── repository/            # Spring Data JPA — ~ DbSet<T> / repositório do EF Core
        │   │   │   ├── ProcessoRepository.java
        │   │   │   ├── ParteRepository.java
        │   │   │   └── MovimentacaoRepository.java
        │   │   ├── domain/                # entidades JPA (Jakarta EE) — o "modelo rico"
        │   │   │   ├── Processo.java              # agregado raiz
        │   │   │   ├── Parte.java
        │   │   │   ├── Movimentacao.java
        │   │   │   └── StatusProcesso.java         # enum
        │   │   └── exception/             # exceções de domínio + handler central
        │   │       ├── ProcessoNaoEncontradoException.java
        │   │       ├── RegraDeNegocioException.java
        │   │       └── GlobalExceptionHandler.java  # @ControllerAdvice — ~ middleware de exceção do ASP.NET
        │   └── resources/
        │       ├── application.yml
        │       ├── application-dev.yml     # ~ appsettings.Development.json
        │       ├── application-prod.yml
        │       ├── logback-spring.xml      # config de log estruturado p/ ELK
        │       └── db/migration/           # scripts Flyway (versionamento de schema)
        │           └── V1__criar_tabelas_processo.sql
        └── test/
            └── java/br/jus/trt4/processo/
                ├── unit/                  # testes unitários — mocka repository/mapper com Mockito
                │   ├── service/ProcessoServiceTest.java
                │   └── mapper/ProcessoMapperTest.java
                └── integration/           # sobe contexto Spring real + H2, testa fluxo HTTP completo
                    └── ProcessoControllerIT.java
```

## Por que separar `dto/`, `mapper/`, `domain/` e nunca deixar a entidade JPA "vazar" para o controller?

Isso é o equivalente Java do que você já faz no .NET quando não retorna a entidade EF Core direto no
`ActionResult` — evita over-posting, evita lazy-loading estourar em serialização, e desacopla o contrato
HTTP da modelagem de persistência. MapStruct existe exatamente para essa fronteira, gerando o código de
conversão em tempo de compilação (zero reflection em runtime, diferente do AutoMapper).

## Nomes de pacote: por que `br.jus.trt4...`?

Convenção Java de pacotes é domínio invertido (`br.jus.trt4` em vez de `com.empresa`), sem equivalente
direto em C# — o `namespace` do .NET não tem essa exigência de exclusividade global. Aqui é só para soar
autêntico ao contexto do edital; em projeto real seria o domínio da instituição.
