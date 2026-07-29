package br.jus.trt4.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Ponto de entrada do api-gateway. Diferente do processo-service (Fase 2), este módulo é
 * empacotado como JAR simples (sem WAR) — não faz parte do exercício de deploy em servidor
 * externo, então usa só o modelo padrão do Spring Boot (Tomcat embutido).
 */
@SpringBootApplication
// -------------------------------------------------------------------------------------------
// @EnableZuulProxy — a annotation que liga TODA a infraestrutura de roteamento Zuul: registra os
// filtros padrão (roteamento, tratamento de erro, etc.) e ativa a leitura das rotas declaradas em
// application.yml ("zuul.routes.*"). Sem ela, mesmo com o starter do Zuul no classpath, este
// processo seria só uma aplicação Spring Boot comum, sem rotear nada.
//
// (Existe uma variante mais simples, @EnableZuulServer, que liga só o mecanismo de filtros sem
// nenhuma configuração de proxy pré-pronta — @EnableZuulProxy é a escolha certa quando o objetivo
// é justamente "ser um API Gateway na frente de outros serviços", que é o nosso caso.)
//
// Paralelo .NET: comparável a `builder.Services.AddReverseProxy()` + `MapReverseProxy()` do YARP,
// ou à configuração de rotas do Ocelot em `ocelot.json` + `app.UseOcelot()` — mas aqui a
// integração com o Eureka (resolver "processo-service" para um host:porta real) já vem pronta
// pelo simples fato do Eureka Client estar no classpath, sem configuração extra de service
// discovery como o YARP/Ocelot exigiriam com um provider de terceiro.
// -------------------------------------------------------------------------------------------
@EnableZuulProxy
// @EnableDiscoveryClient — explicita que este processo é um CLIENTE de service discovery (se
// registra no Eureka e consulta o catálogo). Tecnicamente já seria ativado automaticamente pela
// auto-configuração ao detectar o starter do Eureka Client no classpath — deixamos explícito
// aqui por clareza didática, no mesmo espírito do @EnableEurekaServer explícito do eureka-server.
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
