package br.jus.trt4.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Ponto de entrada do servidor de Service Discovery (Netflix Eureka).
 *
 * Paralelo .NET: esta classe com o método main() é o equivalente ao "Program.cs" de um projeto
 * ASP.NET Core minimal hosting model — é literalmente onde o processo Java começa a executar
 * (a JVM procura por um método `public static void main(String[] args)` para arrancar, igual o
 * runtime do .NET procura pelo Main() ou pelo top-level statements do Program.cs).
 */
// ---------------------------------------------------------------------------------------------
// @SpringBootApplication
// ---------------------------------------------------------------------------------------------
// Não é UMA annotation, é um "combo" (meta-annotation) de três outras, cada uma com um papel:
//
//   1) @SpringBootConfiguration (variação de @Configuration) — diz "esta classe pode declarar
//      @Bean" e é o "ponto de configuração raiz" da aplicação.
//   2) @EnableAutoConfiguration — a MÁGICA do Spring Boot: ele escaneia o classpath (o que você
//      tem no pom.xml) e liga automaticamente comportamentos baseado no que ENCONTRA. Achou o
//      starter do Eureka Server no classpath? Ele prepara toda a infraestrutura de dashboard e
//      registro de instâncias sozinho, sem você escrever XML nem código de bootstrap.
//      Paralelo .NET: não existe equivalente direto — no ASP.NET Core você registra CADA coisa
//      explicitamente em `builder.Services.AddX()`. O Spring Boot inverte isso: convenção sobre
//      configuração, baseada em "o que está no seu classpath", não em chamadas explícitas.
//   3) @ComponentScan — varre o pacote ATUAL (br.jus.trt4.eureka) e todos os sub-pacotes, achando
//      qualquer classe anotada com @Component/@Service/@Repository/@Controller e registrando
//      como bean gerenciado pelo container. Paralelo .NET: parecido com scanning automático de
//      assembly que alguns containers de DI de terceiros (Scrutor, Autofac) oferecem — mas o
//      `IServiceCollection` nativo do ASP.NET Core NÃO faz isso sozinho, você registra um a um.
// ---------------------------------------------------------------------------------------------
@SpringBootApplication
// ---------------------------------------------------------------------------------------------
// @EnableEurekaServer
// ---------------------------------------------------------------------------------------------
// Esta é a annotation que efetivamente transforma este processo Spring Boot comum em um
// SERVIDOR Eureka (e não apenas em um CLIENTE que se registra em outro Eureka — essa seria
// @EnableEurekaClient/@EnableDiscoveryClient, usada pelo api-gateway e pelo processo-service
// nas próximas fases). Ela liga os endpoints internos do Eureka: o dashboard HTML em "/" e a
// API REST que os clientes usam para registrar/consultar instâncias (por baixo dos panos, em
// "/eureka/**").
//
// Não existe um paralelo direto no ecossistema .NET porque o .NET não tem uma solução de
// service discovery "nativa da Microsoft" com o mesmo papel — o mais próximo seria rodar um
// HashiCorp Consul (agnóstico de linguagem) ou usar o Steeltoe (biblioteca de terceiros que,
// aliás, foi criada justamente para dar a apps .NET a capacidade de falar com um Eureka já
// existente em um ambiente híbrido Java+.NET).
// ---------------------------------------------------------------------------------------------
@EnableEurekaServer
public class EurekaServerApplication {

    /**
     * SpringApplication.run(...) sobe todo o container Spring (o "ApplicationContext"): lê as
     * configurações (application.yml), instancia os beans, sobe o servidor HTTP embutido
     * (Tomcat, por padrão) e bloqueia a thread principal mantendo o processo vivo — igual o
     * `app.Run()` no final de um Program.cs do ASP.NET Core faz com o Kestrel.
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
