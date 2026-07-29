# 09 — Notas para Prova (cheat sheet do concurso)

> Diferente dos docs `01`-`08` (que explicam a arquitetura e o "porquê" das decisões de design),
> este arquivo é uma **folha de revisão rápida**, no formato pergunta/resposta, organizada pelas
> mesmas seções do edital (`conteudo_programatico.md`, raiz do repo). Ele é **cumulativo**: cresce
> a cada fase implementada, sempre que uma nuance com valor de prova aparecer no código — ver
> a convenção em [`08-roadmap-implementacao.md`](08-roadmap-implementacao.md).

---

## 2. Frameworks Java

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
