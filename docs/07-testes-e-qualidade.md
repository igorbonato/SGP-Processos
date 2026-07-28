# 07 — Testes e Qualidade

## Pirâmide de testes deste projeto

```
        /\
       /IT\        poucos — sobem o contexto Spring inteiro, testam HTTP fim a fim (H2 em memória)
      /------\
     / Unit   \     muitos — service e mapper, tudo que é regra de negócio, com Mockito mockando dependências
    /----------\
```

## Testes unitários — `JUnit 5` + `Mockito`

Local: `src/test/java/.../unit/`.

Alvo principal: `service` (regra de negócio) e `mapper` (MapStruct — garantir que o mapeamento gerado bate
com o esperado). O `repository` **não** é mockado quando o teste É sobre o repository (nesse caso vira
teste de integração com H2, ver abaixo); é mockado quando o teste é sobre o `service` que o usa.

Estrutura padrão (AAA — Arrange, Act, Assert), igual ao que você já faz com xUnit/Moq:

```java
// Arrange
when(processoRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
// Act
ProcessoResponseDTO resultado = processoService.buscarPorId(1L);
// Assert
assertEquals("0001234-56.2024.5.04.0001", resultado.getNumero());
```

Equivalente direto: `Mock<IProcessoRepository>` do Moq ~ `@Mock ProcessoRepository` do Mockito;
`_mockRepo.Setup(...)` ~ `when(...).thenReturn(...)`.

## Testes de integração — `@SpringBootTest`

Local: `src/test/java/.../integration/`.

Sobem o `ApplicationContext` do Spring de verdade (ou uma fatia dele, via `@WebMvcTest`/`@DataJpaTest`
quando não precisa do contexto inteiro), com banco **H2 em memória** no lugar do PostgreSQL, e testam o
fluxo HTTP completo via `MockMvc` ou `TestRestTemplate`.

Equivalente direto: `WebApplicationFactory<TEntryPoint>` do ASP.NET Core + `HttpClient` de teste — mesma
ideia de "app real, banco descartável".

Convenção de nomenclatura que o Maven já reconhece por padrão:

| Sufixo | Tipo | Executado por |
|---|---|---|
| `*Test.java` | Unitário | `mvn test` (plugin Surefire) |
| `*IT.java` | Integração | `mvn verify` (plugin Failsafe) — roda **depois** do build, não durante `test` |

Essa separação existe porque testes de integração são mais lentos (sobem contexto Spring) — você não quer
que rodem em todo `mvn test` de desenvolvimento, só no `mvn verify` do pipeline de CI, por exemplo.

## Cobertura de código — JaCoCo

Plugin Maven (`jacoco-maven-plugin`) que instrumenta o bytecode durante os testes e gera relatório HTML em
`target/site/jacoco/index.html`. Equivalente ao **Coverlet** do .NET (que você provavelmente já usa com
`dotnet test --collect:"XPlat Code Coverage"`). Vamos configurar um gate mínimo de cobertura (ex.: 70% em
`service`) que falha o build se não atingido — prática comum em pipelines de CI corporativos e citada
explicitamente no edital ("cobertura de código").

## O que cada camada testa (e o que não testa)

| Camada | O que testar | O que NÃO testar aqui |
|---|---|---|
| `controller` | Serialização, status HTTP, validação de `@Valid` — via teste de integração | Regra de negócio (já testada no service) |
| `service` | Regras de negócio, exceções lançadas em cenários inválidos | Detalhe de SQL (mocka o repository) |
| `mapper` | Campos mapeados corretamente, especialmente os que exigem `@Mapping` customizado | Lógica de negócio (mapper não deveria ter nenhuma) |
| `repository` | Apenas se houver `@Query` customizada — via `@DataJpaTest` + H2 | Métodos derivados simples do Spring Data (já testado pelo próprio framework) |
