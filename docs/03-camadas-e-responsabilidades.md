# 03 — Camadas e Responsabilidades

Cada camada abaixo existe para resolver **um** problema específico. Regra prática: uma requisição sempre
atravessa nesta ordem, nunca pula etapa (ex.: controller nunca fala direto com repository).

```
Controller → Service → Repository → Banco
    ↑            ↑
   DTO        Domain (Entity)
    ↑            ↑
  Mapper ────────┘
```

## 1. `controller` — Camada de apresentação (REST)

Responsabilidade: expor endpoints HTTP, validar formato de entrada (`@Valid`), traduzir `service` em
`ResponseEntity` com status HTTP correto. **Não contém regra de negócio.**

Equivalente .NET: `[ApiController]` + `ActionResult<T>`. Mesma responsabilidade, mesmo limite: se você
está validando regra de negócio dentro do controller no .NET, já sabe que é code smell — vale a mesma regra aqui.

## 2. `dto` — Data Transfer Objects

Responsabilidade: definir o contrato público da API, independente do modelo de persistência. Separado em
`request` (o que o cliente envia) e `response` (o que devolvemos) porque raramente o shape de entrada e
saída é idêntico (ex.: request não tem `id`, response tem `id` + campos calculados).

Equivalente .NET: seus `ViewModels`/`Contracts` em Clean Architecture — mesmo racional.

## 3. `mapper` — MapStruct

Responsabilidade: converter `Entity ↔ DTO` sem escrever getters/setters manualmente. Diferente do
AutoMapper (reflection em runtime), o MapStruct **gera uma classe Java real em compile-time** — você pode
abrir `target/generated-sources` e ler o mapeamento gerado. Detalhes em
[`05-especificacao-vs-implementacao.md`](05-especificacao-vs-implementacao.md).

## 4. `service` — Regra de negócio (camada de aplicação)

Responsabilidade: orquestrar casos de uso — validar invariantes (`RegraDeNegocioException` se violar),
coordenar múltiplos repositories, decidir transação (`@Transactional`). É onde vive a lógica que, no seu
DDD em .NET, ficaria em Application Services ou métodos do próprio agregado.

Nota de propósito: interface (`ProcessoService`) separada da implementação (`ProcessoServiceImpl`) é
convenção herdada do Java EE clássico (facilita mock em teste e troca de implementação). Em Spring moderno
com Mockito isso é **opcional** — dá para mockar classes concretas direto — mas o edital/mercado ainda
espera que você reconheça e saiba justificar esse padrão.

## 5. `repository` — Persistência (Spring Data JPA)

Responsabilidade: abstrair acesso a dados. Você declara uma *interface* estendendo `JpaRepository<Processo, Long>`
e o Spring gera a implementação em runtime via proxy dinâmico — nem MapStruct nem você escrevem essa classe.

Equivalente .NET: mistura de `DbSet<T>` do EF Core com o *Repository Pattern* que muita gente implementa
manualmente em cima do EF Core — aqui o repositório genérico já vem pronto do framework.

## 6. `domain` — Entidades JPA

Responsabilidade: modelo de dados anotado com Jakarta Persistence (`@Entity`, `@Id`, `@OneToMany`...).
É a fronteira onde JPA (especificação) encontra Hibernate (implementação) — ver
[`05-especificacao-vs-implementacao.md`](05-especificacao-vs-implementacao.md) para o porquê disso importar.

## 7. `security` — JWT

Responsabilidade: emitir token (`AuthController` + `JwtTokenProvider`), interceptar toda requisição
protegida (`JwtAuthenticationFilter`, análogo a um `Servlet Filter`) e popular o `SecurityContext` do
Spring Security. Detalhado em [`06-seguranca-e-observabilidade.md`](06-seguranca-e-observabilidade.md).

## 8. `exception` + `GlobalExceptionHandler`

Responsabilidade: centralizar tradução de exceção → resposta HTTP (`@ControllerAdvice` + `@ExceptionHandler`),
para nenhum controller precisar de `try/catch` repetido.

Equivalente .NET: middleware de exceção global (`app.UseExceptionHandler` ou um middleware customizado).

## 9. `config` — Configuração declarativa (`@Configuration`)

Responsabilidade: registrar beans que não são `@Component` de negócio (Swagger, `SecurityFilterChain`,
`PasswordEncoder`). Equivalente ao que você registra manualmente em `Program.cs`/`Startup.cs`
(`builder.Services.AddX()`), só que aqui via classes anotadas em vez de um único arquivo de bootstrap.
