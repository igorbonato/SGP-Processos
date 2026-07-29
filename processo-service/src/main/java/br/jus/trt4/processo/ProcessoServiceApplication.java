package br.jus.trt4.processo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Ponto de entrada do processo-service — mas com duas responsabilidades extras em relação ao
 * EurekaServerApplication (eureka-server): precisa saber arrancar de DUAS formas diferentes,
 * porque este módulo é empacotado como WAR (ver processo-service/pom.xml); e, desde a Fase 6,
 * também se registra no Eureka (@EnableDiscoveryClient), para o api-gateway conseguir rotear
 * até aqui por "serviceId", não por um host:porta fixo.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProcessoServiceApplication extends SpringBootServletInitializer {

    /**
     * Cenário 1 — execução standalone: "mvn spring-boot:run" ou "java -jar processo-service.war".
     * Este main() é usado exatamente como no eureka-server: sobe o Tomcat embutido (que, mesmo
     * marcado como "provided" no pom.xml, o plugin do Spring Boot ainda disponibiliza para este
     * cenário) e roda como um processo independente.
     */
    public static void main(String[] args) {
        SpringApplication.run(ProcessoServiceApplication.class, args);
    }

    // ---------------------------------------------------------------------------------------
    // Cenário 2 — deploy em servidor externo: Tomcat, JBoss ou WildFly já rodando, recebendo o
    // processo-service.war dentro da pasta de deploy (ex.: webapps/ do Tomcat).
    //
    // Neste cenário, NINGUÉM chama o nosso main() — quem manda é o próprio servidor externo, via
    // um mecanismo padronizado da especificação Servlet 3+ chamado "ServletContainerInitializer"
    // (SPI - Service Provider Interface): ao subir, o servidor varre o WAR procurando por
    // arquivos de registro em META-INF/services e por classes que implementem a interface
    // "WebApplicationInitializer". O Spring Boot já entrega essa peça pronta via a classe
    // SpringBootServletInitializer que estamos estendendo — é ela quem responde ao "chamado" do
    // servidor externo, no lugar do nosso main().
    //
    // Por isso o método abaixo é obrigatório sempre que packaging=war: ele diz ao Spring "quando
    // for um servidor externo chamando você (e não o main()), use ESTA classe como fonte de
    // configuração" — o mesmo papel que "sources(ProcessoServiceApplication.class)" já cumpre
    // implicitamente dentro do SpringApplication.run() do cenário 1.
    //
    // Paralelo .NET: não existe um SPI equivalente no ASP.NET Core porque o modelo de hosting é
    // sempre "self-hosted" (Kestrel) — quem inicia o processo é sempre o seu próprio Program.cs
    // (ou o `dotnet` CLI chamando ele), nunca um processo de servidor externo pré-existente indo
    // buscar sua aplicação dentro de um pacote genérico.
    // ---------------------------------------------------------------------------------------
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ProcessoServiceApplication.class);
    }
}
