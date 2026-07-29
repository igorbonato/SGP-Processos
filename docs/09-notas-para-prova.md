# 09 — Notas para Prova (cheat sheet do concurso)

> Diferente dos docs `01`-`08` (que explicam a arquitetura e o "porquê" das decisões de design),
> este arquivo é uma **folha de revisão rápida**, no formato pergunta/resposta, organizada pelas
> mesmas seções do edital (`conteudo_programatico.md`, raiz do repo). Ele é **cumulativo**: cresce
> a cada fase implementada, sempre que uma nuance com valor de prova aparecer no código — ver
> a convenção em [`08-roadmap-implementacao.md`](08-roadmap-implementacao.md).

---

## 3. APIs e Web Services

### OpenAPI, Swagger e springdoc — qual é a especificação e qual é a ferramenta?

**OpenAPI Specification** é o padrão (hoje mantido pela OpenAPI Initiative/Linux Foundation) que
descreve um contrato REST em JSON/YAML — de que endpoints existem a que schema cada campo tem.
"Swagger" é a marca das ferramentas construídas em torno dessa spec (ela, aliás, se chamava
"Swagger Specification" antes de ser doada e renomeada). **springdoc-openapi** é a biblioteca
concreta escolhida aqui para GERAR essa especificação em tempo de execução, lendo os
`@RestController` via reflection — poderíamos trocar por "springfox" (a biblioteca mais antiga
que fazia o mesmo papel, hoje descontinuada) sem mudar uma linha dos controllers.

> **Pegadinha:** perguntas de edital que dizem só "Swagger" geralmente querem dizer "documentação
> de API compatível com OpenAPI", não uma biblioteca específica — é a mesma armadilha de nome de
> `05-especificacao-vs-implementacao.md` (JPA vs Hibernate), aplicada a APIs. Paralelo .NET:
> Swashbuckle.AspNetCore é o "springdoc" do ecossistema .NET — mesma spec (OpenAPI é agnóstica de
> linguagem), biblioteca geradora diferente.
>
> _Fonte: [`OpenApiConfig.java`](../processo-service/src/main/java/br/jus/trt4/processo/config/OpenApiConfig.java), Fase 4._

### springdoc-openapi 1.x vs 2.x — por que importa qual eu uso?

A série **1.x** do springdoc gera documentação para projetos Spring Boot 2.x (namespace
`javax.*`); a série **2.x** exige Spring Boot 3 (`jakarta.*`). Mesma lógica de trem de versões já
vista com Spring Cloud/Zuul (`04`/`05`) — misturar a série errada com a versão de Boot do projeto
quebra a auto-configuração silenciosamente ou nem resolve as dependências.
>
> _Fonte: `pom.xml` raiz (propriedade `springdoc.version`), Fase 4._

### Onde tratar uma falha de autenticação/autorização: `@ExceptionHandler` ou outra coisa?

**Outra coisa.** `AuthenticationException` (token ausente/inválido, credenciais erradas no login)
e `AccessDeniedException` (`@PreAuthorize` barrando um usuário sem a role certa) são interceptadas
pelo `ExceptionTranslationFilter` do Spring Security **antes** de qualquer `@ExceptionHandler` — a
exceção nunca chega ao `@RestControllerAdvice`, mesmo quando lançada de dentro de um método de
controller normal (como o `authenticationManager.authenticate(...)` do login). O ponto de
configuração certo é um `AuthenticationEntryPoint` (401) e um `AccessDeniedHandler` (403),
registrados via `.exceptionHandling()` no `SecurityConfig`.

> **Testado de verdade, não só lido**: chamei `/auth/login` com senha errada e o **mesmo**
> `JwtAuthEntryPoint` que trata "token ausente num endpoint protegido" respondeu — confirmando que
> os dois cenários (login errado E token ausente) passam pelo mesmo mecanismo. Por isso a
> mensagem de erro do `JwtAuthEntryPoint` é deliberadamente genérica ("credenciais ou token
> inválidos"), não específica de um dos dois casos.
>
> _Fonte: [`JwtAuthEntryPoint.java`](../processo-service/src/main/java/br/jus/trt4/processo/security/JwtAuthEntryPoint.java) / [`JwtAccessDeniedHandler.java`](../processo-service/src/main/java/br/jus/trt4/processo/security/JwtAccessDeniedHandler.java), Fase 5._

### 401 vs 403 — qual a diferença exata?

**401 Unauthorized**: "eu não sei quem você é" (token ausente, inválido, expirado, ou credenciais
erradas no login) — na prática, "**não autenticado**". **403 Forbidden**: "eu sei exatamente quem
você é, mas você não tem permissão para isso" — "**autenticado, porém não autorizado**". Testado
neste projeto: usuário `consulta` (sem `ROLE_ANALISTA`) chamando `POST /api/processos` recebe 403,
não 401 — o token dele é válido, só falta a role.
>
> _Fonte: `ProcessoController.criar` (`@PreAuthorize("hasRole('ANALISTA')")`), Fase 5._

### `hasRole('ANALISTA')` — por que não escrever `hasRole('ROLE_ANALISTA')`?

O Spring Security adiciona o prefixo `ROLE_` **automaticamente** ao configurar
`.roles("ANALISTA")` (ver `SecurityConfig`) e ao AVALIAR `hasRole(...)` — o prefixo existe só
internamente, na authority de verdade (`ROLE_ANALISTA`); escrevê-lo nos dois lugares duplicaria o
prefixo (`ROLE_ROLE_ANALISTA`) e a checagem falharia sempre. `hasAuthority(...)`, em contraste,
compara o texto exato, sem adicionar prefixo nenhum — é a opção certa quando a permissão não é uma
"role" no sentido tradicional (ex.: uma permissão granular tipo `"processos:arquivar"`).

### Por que uma API com JWT desliga CSRF, mas um app com login por formulário/cookie não pode?

Proteção CSRF existe para o cenário onde o **browser envia credenciais automaticamente** (cookie
de sessão) em toda requisição, inclusive as forjadas por um site malicioso. Uma API stateless
autenticada por JWT no header `Authorization` não tem esse vetor — o token só vai junto se o
código cliente explicitamente colocar ele lá, o que um site atacante não consegue forçar o
browser a fazer. Por isso `sessionCreationPolicy(STATELESS)` + `csrf().disable()` andam juntos.

---

## 2. Frameworks Java

### Zuul apaga o header `Authorization` ao encaminhar a requisição — por quê?

Por padrão, o Zuul remove uma lista de headers "sensíveis" (`Cookie`, `Set-Cookie`, `Authorization`)
antes de repassar a requisição ao serviço de destino — pensado originalmente para não vazar
cookies de sessão entre domínios diferentes atrás do mesmo gateway. Isso quebra qualquer API
downstream que também exija esse header (como o `processo-service`, com seu próprio
`SecurityConfig`). Correção: `zuul.sensitiveHeaders: ''` (string vazia) no `application.yml` do
gateway — desliga a lista de remoção.

> **Testado de verdade**: sem essa linha, uma requisição autenticada passava na validação do
> `JwtValidationZuulFilter` (gateway) e MESMO ASSIM voltava 401 do `processo-service` — porque o
> token nunca chegava lá. É provavelmente A pegadinha mais comum de quem configura Zuul + JWT pela
> primeira vez.
>
> _Fonte: [`api-gateway/application.yml`](../api-gateway/src/main/resources/application.yml), Fase 6._

### Zuul (legado) engole o corpo de um POST/PUT — como corrigir?

Sintoma real encontrado: `POST` através do gateway retornava 504/500/erro genérico, com o log do
gateway mostrando `"Content-length different from byte array length! cl=357, array=0"` — o Zuul
declarava um Content-Length para o serviço de destino mas não conseguia de fato ler/reenviar os
bytes do corpo original, e o `processo-service` ficava esperando bytes que nunca chegavam
(`ClientAbortException: EOFException`). Causa: o cliente HTTP padrão que o Ribbon usa para fazer a
chamada de fato (`RestClient`, legado) tem um bug antigo e conhecido nesse cenário.

> **Correção testada e confirmada**: forçar o Apache HttpClient em vez do `RestClient` legado —
> `ribbon.httpclient.enabled: true` + `ribbon.restclient.enabled: false`. Depois disso, POST com
> corpo (inclusive com acentuação UTF-8) passou a funcionar perfeitamente através do gateway.
> Este é um sintoma amplamente documentado do Zuul 1 (a versão "clássica", em manutenção) — é
> parte do motivo pelo qual o Spring recomenda o Spring Cloud Gateway (não-bloqueante, WebFlux)
> como sucessor. O edital pede Zuul especificamente (ver docs/09, seção sobre a decisão de
> versão), então vale saber que esse tipo de limitação existe e como contorná-la, não só que o
> substituto moderno existe.
>
> _Fonte: [`api-gateway/application.yml`](../api-gateway/src/main/resources/application.yml), Fase 6._

### Jakarta EE 8 usa o namespace `javax.*` ou `jakarta.*`?

**`javax.*`.** O rename de pacote para `jakarta.*` só aconteceu a partir do **Jakarta EE 9** (fim de
2020). A Eclipse Foundation assumiu a especificação da Oracle em 2017/2018 e rebatizou o projeto de
"Java EE" para "Jakarta EE", mas a Oracle não cedeu os direitos sobre o prefixo de pacote `javax` —
então, até a versão 8, o nome do projeto já era "Jakarta EE" mas o código continuava `javax.*`.

> **Pegadinha:** `javax.persistence.Entity` (Jakarta EE 8) e `jakarta.persistence.Entity`
> (Jakarta EE 9+) são a **mesma anotação**, em versões diferentes da especificação — não são coisas
> diferentes, e não há paralelo com nada do .NET (a Microsoft nunca precisou renomear um namespace
> inteiro por disputa de marca).
>
> _Fonte: [`05-especificacao-vs-implementacao.md`](05-especificacao-vs-implementacao.md), corrigido na Fase 0._

### Zuul funciona com Spring Boot 3?

**Não.** O suporte ao Netflix Zuul foi removido do Spring Cloud a partir do release train
`2020.0 (Ilford)`, que já exige Spring Boot 2.4+ rumo ao 3.x (namespace `jakarta.*`). O último trem
que ainda inclui Zuul é o **Hoxton**, compatível com **Spring Boot 2.3.x/2.4.x**.

> **Pegadinha clássica de entrevista/prova:** perguntar "qual API Gateway usar com Spring Boot 3" e
> responder "Zuul" está errado — o sucessor recomendado pelo próprio Spring é o **Spring Cloud
> Gateway**. Nós usamos Zuul propositalmente porque o **edital pede especificamente Zuul**, o que só
> é tecnicamente viável fixando a geração Boot 2.3.x / Spring Cloud Hoxton.SR12 — que, como bônus,
> já fala `javax.*` (Jakarta EE 8), fechando com o resto do edital.
>
> _Fonte: `pom.xml` raiz (comentário na tag `<parent>`), decisão da Fase 0._

---

## 2. Frameworks Java (JPA/Hibernate)

### Por que `@Enumerated(EnumType.STRING)` em vez de deixar o padrão?

O padrão da especificação JPA, se você **omitir** `@Enumerated` ou usar `EnumType.ORDINAL`, é
gravar o **número da posição** do enum (0, 1, 2...), não o nome. `EnumType.STRING` grava o nome
("ATIVO", "ARQUIVADO") por extenso.

> **Pegadinha:** com `ORDINAL`, se alguém reordenar ou inserir uma constante no meio do enum no
> futuro, todos os dados **já gravados** passam a significar outra coisa — silenciosamente, sem
> erro de compilação ou de runtime. `STRING` é imune a isso. Detalhe extra: o **default do EF
> Core é o oposto do bom senso aqui também** — ele grava `int` por padrão, e você converte para
> string manualmente com `.HasConversion<string>()`. Os dois frameworks defaultam para a opção
> "arriscada"; a diferença é só qual delas.
>
> _Fonte: [`domain/Processo.java`](../processo-service/src/main/java/br/jus/trt4/processo/domain/Processo.java), Fase 2._

### Como implementar `equals`/`hashCode` em uma entidade JPA?

**Nunca** use o campo `id` técnico direto, e nunca inclua coleções (`@OneToMany`) no cálculo. Use
uma **chave de negócio** estável, presente desde a criação do objeto (ex.: `numero` do processo,
`documento` da parte).

> **Pegadinha:** se você usar `id` e comparar duas entidades **antes** de qualquer uma ser
> persistida (ambas com `id = null`), elas parecem "iguais" dentro de um `HashSet`/`HashMap`.
> Incluir uma coleção lazy no cálculo pode disparar uma consulta ao banco só para computar um
> hash — e coleções mutáveis quebram o contrato de hash estável exigido por essas estruturas.
>
> _Fonte: [`domain/Processo.java`](../processo-service/src/main/java/br/jus/trt4/processo/domain/Processo.java), Fase 2._

### `open-in-view` — o que é, e por que o Spring Boot liga isso por padrão?

É o padrão "Open Session/EntityManager In View": a sessão do Hibernate fica aberta durante toda a
requisição HTTP, não só durante o método `@Transactional` do service, permitindo acessar
relacionamentos `LAZY` até na hora de serializar a resposta.

> **Pegadinha/anti-padrão:** isso esconde o problema de N+1 queries em vez de resolvê-lo — você só
> descobre em produção, sob carga, que o controller "sem querer" disparou uma query extra ao
> tocar numa coleção lazy fora do service. Este projeto desliga (`open-in-view: false`) para
> qualquer acesso lazy indevido estourar `LazyInitializationException` **imediatamente**, em
> desenvolvimento. Sem paralelo direto no EF Core, que não faz lazy loading automático por padrão.
>
> _Fonte: [`application.yml`](../processo-service/src/main/resources/application.yml), Fase 2._

### MapStruct: por que o DTO de resposta precisa de construtor sem argumentos + setters?

O MapStruct **existe** desde a versão 1.3 com suporte a "mapeamento por construtor" (para classes
sem construtor padrão), mas isso não é incondicional — na prática, com `mapstruct 1.3.1.Final`
(a versão fixada neste projeto), tentar usar um DTO só-com-construtor-de-todos-os-campos
resultou em erro de compilação: `"does not have an accessible parameterless constructor"`. O
MapStruct caiu para a estratégia PADRÃO dele (instanciar via construtor vazio, popular via
setters), sem usar o construtor alternativo.

> **Pegadinha/lição real:** comportamento de annotation processor (MapStruct, Lombok, etc.) é
> sensível à versão exata da biblioteca — a forma confiável de saber "isso funciona" é
> **compilar**, não confiar na documentação geral do recurso. A estratégia universalmente segura
> (funciona em qualquer versão) é DTO como JavaBean clássico: construtor sem argumentos + setters.
> Sem paralelo direto no AutoMapper, que sempre usa reflection em runtime e por isso nunca tem
> essa dependência de "o que o compilador consegue enxergar".
>
> _Fonte: [`ParteResponseDTO.java`](../processo-service/src/main/java/br/jus/trt4/processo/dto/response/ParteResponseDTO.java), Fase 3 — corrigido depois de um `mvn compile` real falhar._

### Como o MapStruct mapeia uma lista aninhada (ex.: `Processo.partes`) sem eu escrever o loop?

Declarando `@Mapper(uses = ParteMapper.class)` no `ProcessoMapper` — o MapStruct vê que o campo
`partes` é `List<Parte>` na entidade e `List<ParteResponseDTO>` no DTO, procura (nos mappers
listados em `uses`) um método `Parte -> ParteResponseDTO`, acha o do `ParteMapper`, e GERA
sozinho o loop que aplica esse método item a item — inclusive injetando o `ParteMapper` via
`@Autowired` dentro do `ProcessoMapperImpl` gerado.

> _Fonte: [`ProcessoMapperImpl.java`](../processo-service/target/generated-sources/annotations/br/jus/trt4/processo/mapper/ProcessoMapperImpl.java) (gerado), Fase 3._

### Por que `processoRepository.save(processo)` não é chamado depois de `processo.arquivar()`?

Dentro de uma transação (`@Transactional`), uma entidade carregada via `findById()` fica
"managed" (gerenciada) pelo Hibernate — qualquer mudança de estado nela é detectada por **dirty
checking** e vira `UPDATE` automaticamente no commit, sem precisar chamar `save()` de novo.

> **Pegadinha:** isso é DIFERENTE do EF Core, onde você **ainda precisa** chamar
> `SaveChanges()`/`SaveChangesAsync()` explicitamente mesmo com o Change Tracker já ciente da
> mudança — o Hibernate faz esse "SaveChanges automático" no commit da transação, o EF Core não.
>
> _Fonte: [`ProcessoServiceImpl.java`](../processo-service/src/main/java/br/jus/trt4/processo/service/impl/ProcessoServiceImpl.java), Fase 3._

### `repository.save(entity)` sempre atualiza os campos gerados (`id`) na variável original?

**Não, nem sempre** — e isso derrubou um teste real deste projeto. O Spring Data decide, dentro de
`save()`, entre `persist()` (entidade nova, sem id) e `merge()` (entidade já tem id — foi
carregada via `findById`, por exemplo). Para `persist()`, a MESMA instância Java que você passou
recebe o id gerado de volta (mutação in-place). Para `merge()`, a **especificação JPA diz
explicitamente** que a chamada pode devolver uma instância diferente — e é NELA, não no objeto
original, que os campos gerados de entidades recém-cascadeadas aparecem.

> **Pegadinha (bug real encontrado testando este projeto):** ao adicionar uma `Movimentacao` a um
> `Processo` já existente (`processoRepository.save(processo)` → é um `merge()`, pois o processo
> já tem id) e depois mapear a variável local `movimentacao` (a mesma instância criada com `new`
> antes do save) para o DTO de resposta, o `id` veio `null` — mesmo a linha já estando gravada
> corretamente no banco. A correção é sempre usar o **retorno** de `save()`/`merge()` para
> qualquer leitura pós-persistência. Note a assimetria: `ProcessoServiceImpl.criar()` nunca teve
> esse problema, porque ali o `Processo` é **novo** (sem id) — `save()` vira `persist()`, que
> atualiza a instância original em memória sem exigir usar o retorno.
>
> _Fonte: [`MovimentacaoServiceImpl.java`](../processo-service/src/main/java/br/jus/trt4/processo/service/impl/MovimentacaoServiceImpl.java), Fase 3 — corrigido depois de um teste HTTP real expor `id: null` na resposta._

### HTTP 400 vs 422 — qual a diferença e quando usar cada um?

400 (Bad Request): a requisição está malformada ou falhou uma validação de FORMATO (`@NotBlank`,
`@Size` — Bean Validation). 422 (Unprocessable Entity): o JSON é válido e teria o formato certo,
mas viola uma REGRA DE NEGÓCIO (ex.: adicionar movimentação em processo arquivado).

> _Fonte: [`GlobalExceptionHandler.java`](../processo-service/src/main/java/br/jus/trt4/processo/exception/GlobalExceptionHandler.java), Fase 3._

---

## 5. Servidores de Aplicação

### Como o mesmo artefato roda embutido (JAR) e em Tomcat/JBoss/WildFly externo (WAR)?

Duas peças: (1) `spring-boot-starter-tomcat` declarado com `<scope>provided</scope>` no `pom.xml`
— disponível para compilar/rodar localmente, mas não empacotado dentro do WAR, para não conflitar
com o Tomcat/JBoss/WildFly que o servidor de destino já tem rodando; (2) a classe principal
estendendo `SpringBootServletInitializer`, que implementa o mecanismo padrão da especificação
Servlet 3+ (`ServletContainerInitializer`, via SPI/`META-INF/services`) — é assim que um servidor
externo "acha" e inicia a aplicação Spring dentro do WAR, sem nunca chamar o `main()`.

> **Pegadinha:** o `main()` só é usado no cenário standalone (`java -jar`/`mvn spring-boot:run`).
> Em deploy num servidor externo, quem chama a aplicação é o SPI do servidor — por isso o método
> `configure(SpringApplicationBuilder)` é obrigatório sempre que `packaging=war`. Sem paralelo no
> ASP.NET Core: lá o hosting é sempre self-hosted (Kestrel iniciado pelo próprio processo), nunca
> um servidor externo pré-existente "descobrindo" a aplicação dentro de um pacote genérico.
>
> _Fonte: [`ProcessoServiceApplication.java`](../processo-service/src/main/java/br/jus/trt4/processo/ProcessoServiceApplication.java), Fase 2._

---

## 9. Banco de Dados

### Flyway vs EF Core Migrations — qual a diferença de filosofia?

Flyway: você escreve **SQL puro**, versionado por nome de arquivo (`V1__descricao.sql`,
`V2__...`), e cada arquivo roda exatamente uma vez, na ordem, controlado por uma tabela própria
(`flyway_schema_history`). EF Core Migrations: você muda as **classes** do modelo e o `dotnet ef
migrations add` **gera** o código da migration a partir do diff.

> **Pegadinha:** Flyway é "SQL-first" (schema pensado direto em SQL); EF Core é "code-first"
> (schema derivado das classes). Um arquivo Flyway já aplicado **nunca** deve ser editado — o
> Flyway guarda um checksum e falha a validação se detectar alteração; a correção sempre vem em
> um novo arquivo (`V2`), nunca editando o `V1` retroativamente.
>
> _Fonte: [`V1__criar_tabelas_processo.sql`](../processo-service/src/main/resources/db/migration/V1__criar_tabelas_processo.sql), Fase 2._

### Por que `GENERATED BY DEFAULT AS IDENTITY` em vez de `SERIAL`/`AUTO_INCREMENT`?

`GENERATED BY DEFAULT AS IDENTITY` é sintaxe do **padrão SQL:2003**, entendida tanto pelo H2 (dev)
quanto pelo PostgreSQL 10+ (prod) sem alterar o script. `SERIAL` é proprietário do PostgreSQL;
`AUTO_INCREMENT` é proprietário do MySQL — nenhum dos dois funciona nos dois bancos.

> _Fonte: [`V1__criar_tabelas_processo.sql`](../processo-service/src/main/resources/db/migration/V1__criar_tabelas_processo.sql), Fase 2._

---

## Linguagem Java vs C# (fundamentos)

### Java tem "checked exceptions". O que é isso, e por que importa na hora de criar uma exceção?

Uma exceção que estende `Exception` diretamente (checked) obriga, por regra do **compilador**,
quem chama o método a capturá-la ou redeclarar com `throws`. Uma exceção que estende
`RuntimeException` (unchecked) não tem essa obrigação — e é assim que **toda** exceção em C# se
comporta (o C# não tem o conceito de checked exception).

> **Pegadinha:** ao criar uma exceção de domínio própria em Java, você **escolhe** entre as duas —
> uma decisão de design que simplesmente não existe em C#. Regra de negócio violada normalmente
> deve ser unchecked (`RuntimeException`), para não poluir toda a cadeia de chamadas com
> `throws`/`try-catch` redundante até chegar numa camada central de tratamento.
>
> _Fonte: [`RegraDeNegocioException.java`](../processo-service/src/main/java/br/jus/trt4/processo/exception/RegraDeNegocioException.java), Fase 2._

---

## Como usar este arquivo

- Cada fase futura do [roadmap](08-roadmap-implementacao.md) pode adicionar novas seções/entradas
  aqui, sempre no formato: pergunta objetiva → resposta direta → bloco de citação com a pegadinha
  ou o motivo de confundir quem vem do .NET → fonte (doc ou fase onde foi decidido/explicado).
- As seções seguem os **títulos exatos** do `conteudo_programatico.md` para facilitar o cruzamento
  na hora de revisar para a prova.
