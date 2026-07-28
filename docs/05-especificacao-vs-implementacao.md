# 05 — Especificação vs Implementação (a maior pegadinha para quem vem do .NET)

## O problema que não existe no .NET

No .NET, a Microsoft define **e** implementa a pilha inteira: ASP.NET Core, Entity Framework Core, a
runtime. É um único fornecedor, então "spec" e "implementação" são a mesma coisa na prática.

No mundo Java, historicamente, existe uma organização (hoje a **Eclipse Foundation**, via *Jakarta EE* —
antes era a Oracle, via *Java EE*) que publica apenas **contratos** (interfaces, anotações, regras de
comportamento). Múltiplos fornecedores competem para implementar esses contratos. Isso existe por design:
portabilidade entre servidores/fornecedores é um requisito histórico do Java corporativo — e é exatamente
por isso que o edital pergunta sobre Tomcat **e** JBoss **e** WildFly como opções intercambiáveis.

## Os três exemplos que aparecem neste projeto

### 1. JPA (especificação) vs Hibernate (implementação)

- **JPA** (`jakarta.persistence.*`) — apenas define as anotações (`@Entity`, `@Id`, `@OneToMany`) e as
  interfaces (`EntityManager`). Não faz nada sozinha.
- **Hibernate** — implementa JPA de fato: gera o SQL, gerencia o cache de sessão, controla o dirty
  checking. Poderia ser trocado por **EclipseLink** ou **OpenJPA** sem mudar uma linha das entidades
  anotadas com JPA puro (na teoria — na prática sempre há detalhes específicos do Hibernate usados).
- **Spring Data JPA** — uma **terceira** camada, da Spring (não é spec, é conveniência do Spring), que
  gera a implementação dos seus `Repository` em runtime por cima do `EntityManager` do JPA/Hibernate.

Cadeia completa: `@Entity Processo` (JPA spec) → Hibernate traduz para SQL → `ProcessoRepository extends
JpaRepository` (Spring Data, conveniência) → você nunca escreve a implementação do repositório.

### 2. Servlet (especificação) vs Tomcat/Jetty/Undertow (implementação) vs Spring MVC (framework em cima)

- **Servlet spec** — define `HttpServletRequest`, `HttpServletResponse`, o ciclo de vida de uma aplicação
  web Java.
- **Tomcat, Jetty, Undertow** — *servlet containers*, cada um implementa a spec ao seu jeito (performance,
  configuração diferentes).
- **Spring MVC** — não substitui o servlet container, é construído **em cima** dele: o `DispatcherServlet`
  do Spring é, ele mesmo, apenas *um* Servlet (respeita a spec) que decide como rotear para seus
  `@RestController`.
- **JBoss / WildFly** — vão além de só implementar Servlet: implementam a especificação **Jakarta EE
  inteira** (Servlet + CDI + JMS + JTA + Bean Validation...). Rodar nosso `processo-service` como WAR
  neles funciona porque o WAR só depende da parte Servlet — não usamos CDI/EJB deste projeto.

Por isso o `processo-service` é empacotado como **WAR**: é o artefato padrão que qualquer servlet
container/app server sabe hospedar, independente de fornecedor.

### 3. Bean Validation (especificação) vs Hibernate Validator (implementação)

`@NotNull`, `@Size`, `@Email` vêm da spec `jakarta.validation.*`. Quem executa a validação de fato,
quando o Spring dispara `@Valid`, é o **Hibernate Validator** (biblioteca separada do Hibernate ORM, apesar
do nome parecido — armadilha clássica de entrevista).

## Regra prática para reconhecer spec vs impl no dia a dia

Se o pacote/import começa com `javax.*` (neste projeto) ou `jakarta.*` (em projetos mais novos), é
especificação — um contrato que qualquer fornecedor pode implementar. Se começa com `org.hibernate.*`,
`org.springframework.*`, `com.querydsl.*` etc., é implementação/framework concreto de um fornecedor específico.

> **Atenção com uma pegadinha de nomenclatura:** "Jakarta EE 8" (o que o edital pede) **ainda usa o
> namespace `javax.*`**. A Eclipse Foundation assumiu a especificação da Oracle em 2017/2018 e a rebatizou
> de "Jakarta EE", mas por motivos de marca registrada (a Oracle não cedeu os direitos sobre o prefixo de
> pacote `javax`), o namespace só foi de fato renomeado para `jakarta.*` a partir do **Jakarta EE 9**
> (final de 2020) — que é a versão que o **Spring Boot 3** adota. Ou seja: `javax.persistence.Entity` e
> `jakarta.persistence.Entity` são **a mesma anotação, versões diferentes da spec**, não coisas
> diferentes. Como este projeto usa Spring Boot 2.3.x (ver decisão de versão no `pom.xml` raiz), tudo
> aqui é `javax.*`.

| Prefixo do pacote (neste projeto — Jakarta EE 8 / Spring Boot 2.x) | Equivalente em Jakarta EE 9+ / Spring Boot 3 | Natureza |
|---|---|---|
| `javax.persistence.*` | `jakarta.persistence.*` | Spec (JPA) |
| `org.hibernate.*` | `org.hibernate.*` (mesmo pacote) | Implementação (Hibernate) |
| `javax.validation.*` | `jakarta.validation.*` | Spec (Bean Validation) |
| `org.hibernate.validator.*` | `org.hibernate.validator.*` | Implementação (Hibernate Validator) |
| `javax.servlet.*` | `jakarta.servlet.*` | Spec (Servlet) |
| `org.apache.catalina.*` | `org.apache.catalina.*` | Implementação (Tomcat) |
| `org.springframework.*` | `org.springframework.*` | Framework Spring (não é uma spec Jakarta EE — é uma alternativa/complemento) |

## Onde isso "dói" na prática (e por que o edital cobra)

Trocar de servidor de aplicação (Tomcat → WildFly) ou trocar de provedor JPA (Hibernate → EclipseLink) em
teoria não deveria exigir reescrever código de negócio — só configuração. Isso é o argumento histórico de
venda do Java EE/Jakarta EE para bancos e órgãos públicos (o seu contexto de concurso): você não fica
"preso" a um único fornecedor, ao contrário do .NET onde trocar de "implementação do EF Core" não é nem
uma opção que existe.
