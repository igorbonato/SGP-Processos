# 04 — De → Para: .NET Core / C# ↔ Java / Spring

Tabela de referência rápida. Volte aqui sempre que um conceito Java parecer "estranho" — provavelmente
você já fez o equivalente no .NET sem perceber que era o mesmo padrão com nome diferente.

## Web / API

| .NET Core | Java / Spring | Observação |
|---|---|---|
| ASP.NET Core MVC / Minimal APIs | Spring MVC (parte do Spring Boot) | `DispatcherServlet` do Spring ~ pipeline de middleware do Kestrel |
| `[ApiController]` | `@RestController` | `@RestController` = `@Controller` + `@ResponseBody` combinados |
| `[HttpGet("rota")]` | `@GetMapping("rota")` | idem para `@PostMapping`, `@PutMapping`, `@DeleteMapping` |
| `[FromBody]` | `@RequestBody` | |
| `[FromRoute]` / `{id}` | `@PathVariable` | |
| `[FromQuery]` | `@RequestParam` | |
| Model binding + `[Required]`/DataAnnotations | Bean Validation (`@Valid` + `@NotNull`, `@Size` — Jakarta Validation) | Jakarta Validation também é uma **spec**, implementada por Hibernate Validator |
| Middleware (`app.Use...`) | `Filter` (Servlet spec) / `HandlerInterceptor` (Spring) | |
| Swashbuckle | springdoc-openapi (ou SpringFox, mais antigo) | Anotação principal: `@Operation`, `@Schema` |

## Injeção de Dependência

| .NET Core | Java / Spring | Observação |
|---|---|---|
| `IServiceCollection` / `builder.Services.AddScoped<T>()` | Container IoC do Spring (`ApplicationContext`) | |
| `AddSingleton` | `@Component`/`@Service`/`@Repository` (escopo `singleton` é o **default** do Spring) | Cuidado: default do Spring é singleton; default do .NET é transient (dependendo do que você registra) |
| `AddScoped` | escopo `request` (menos comum; precisa `@RequestScope`) | |
| `AddTransient` | escopo `prototype` (`@Scope("prototype")`) | |
| Injeção via construtor (recomendado) | Injeção via construtor com `@Autowired` (ou nem precisa, se só houver 1 construtor) | Injeção de campo (`@Autowired` direto no atributo) existe mas é considerada anti-pattern, igual em C# |

## Persistência

| .NET Core | Java / Spring | Observação |
|---|---|---|
| Entity Framework Core (`DbContext`, `DbSet<T>`) | JPA (spec) + Hibernate (impl) + Spring Data JPA (abstração de repositório) | .NET não tem essa separação em 3 camadas — é só a Microsoft |
| Classe POCO + `[Table]`/`[Key]`/Fluent API | `@Entity`, `@Table`, `@Id`, `@GeneratedValue` | |
| Relacionamento `.HasMany()` | `@OneToMany`, `@ManyToOne`, `@JoinColumn` | |
| EF Core Migrations (`dotnet ef migrations add`) | Flyway ou Liquibase (versionamento de schema via SQL/XML) | `hibernate.hbm2ddl.auto` existe mas é considerado **só para dev/teste**, nunca produção |
| LINQ to Entities | JPQL / Criteria API / Query Methods (`findByStatusAndDataBetween(...)`) | Spring Data JPA deriva a query pelo **nome do método** |
| AutoMapper (reflection em runtime) | MapStruct (geração de código em compile-time) | Mais parecido com Source Generators do .NET do que com AutoMapper |

## Configuração e Build

| .NET Core | Java / Spring | Observação |
|---|---|---|
| `appsettings.json` / `appsettings.{Env}.json` | `application.yml` / `application-{profile}.yml` | |
| `ASPNETCORE_ENVIRONMENT` | Spring Profiles (`spring.profiles.active`) | |
| `.csproj` + NuGet | `pom.xml` + Maven (ou `build.gradle`) | Maven `<dependency>` ~ `<PackageReference>`; repositório central = Maven Central ~ nuget.org |
| `Directory.Build.props` (config compartilhada) | POM pai com `<dependencyManagement>` (projeto multi-module) | |
| `dotnet build` / `dotnet publish` | `mvn package` / `mvn install` | |

## Segurança

| .NET Core | Java / Spring | Observação |
|---|---|---|
| `AddAuthentication().AddJwtBearer()` | Spring Security + `SecurityFilterChain` + biblioteca JJWT/Nimbus | |
| `[Authorize(Roles = "Admin")]` | `@PreAuthorize("hasRole('ADMIN')")` | |
| Middleware customizado de token | `OncePerRequestFilter` (`JwtAuthenticationFilter`) | |

## Gateway / Discovery / Microsserviços

| .NET Core | Java / Spring | Observação |
|---|---|---|
| Ocelot / YARP | Zuul (Spring Cloud Netflix) — ou Spring Cloud Gateway, o sucessor mais moderno | Edital pede Zuul especificamente, por isso usamos ele aqui |
| Steeltoe + Consul/Eureka client | Netflix Eureka (Spring Cloud Netflix) | Eureka é nativo do ecossistema Spring Cloud, sem precisar de lib de terceiro |

## Testes

| .NET Core | Java / Spring | Observação |
|---|---|---|
| xUnit / NUnit | JUnit 5 | |
| Moq | Mockito | `Mockito.mock(X.class)` ~ `Mock<X>()` |
| `WebApplicationFactory<T>` (teste de integração) | `@SpringBootTest` + `MockMvc`/`TestRestTemplate` | |
| Coverlet | JaCoCo | plugin Maven `jacoco-maven-plugin`, gera relatório HTML |

## Hosting / Deploy

| .NET Core | Java / Spring | Observação |
|---|---|---|
| Kestrel self-hosted (+ IIS/Nginx como reverse proxy opcional) | **JAR** com Tomcat embutido (Spring Boot default) — modelo mais parecido com Kestrel | |
| — (sem equivalente direto) | **WAR** deployado em servidor externo (Tomcat, JBoss, WildFly) | Modelo "app server tradicional": você entrega o artefato, o servidor já está rodando e hospeda várias apps. Sem paralelo comum no .NET moderno — é mais próximo do IIS hospedando múltiplos App Pools |
| Serilog + `Serilog.Sinks.Elasticsearch` | Logback + `logstash-logback-encoder` → Logstash → Elasticsearch | |

## Observação sobre "arquitetura em camadas" vs a Clean Architecture que você já usa

A estrutura deste projeto (`controller/service/repository/domain`) é a **arquitetura idiomática do
ecossistema Spring/Java corporativo** e é o que um edital/avaliador espera ver. Ela **não** é Clean
Architecture/Onion com módulos Maven separados (`domain`, `application`, `infrastructure`, `api`) como
você provavelmente estruturou seus projetos .NET. Dá para evoluir para esse modelo (multi-module por
camada, portas/adaptadores, domínio sem dependência de framework) — é só mais verboso para um projeto de
estudo. Se quiser, podemos fazer isso como uma variação depois que o modelo "clássico" estiver rodando.
