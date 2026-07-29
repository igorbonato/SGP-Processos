package br.jus.trt4.processo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ---------------------------------------------------------------------------------------------
 * OpenAPI (especificação) vs Swagger (ferramentas) vs springdoc (implementação usada aqui)
 * ---------------------------------------------------------------------------------------------
 * Mesma distinção spec-vs-implementação de docs/05 (JPA/Hibernate), aplicada a documentação de
 * API: "OpenAPI Specification" é o PADRÃO (hoje mantido pela OpenAPI Initiative/Linux Foundation,
 * ex-"Swagger Specification") que descreve, em JSON/YAML, o formato de um contrato REST — quais
 * endpoints existem, quais parâmetros aceitam, quais schemas de request/response. "Swagger" virou
 * a marca das FERRAMENTAS em torno dessa spec (Swagger UI, Swagger Editor, Swagger Codegen).
 * "springdoc-openapi" é a biblioteca CONCRETA que escolhemos para GERAR essa especificação a
 * partir do nosso código Spring MVC em tempo de execução — poderíamos trocar por "springfox"
 * (biblioteca mais antiga, hoje descontinuada, que fazia o mesmo papel) sem mudar uma linha dos
 * nossos controllers, só a dependência no pom.xml.
 *
 * Paralelo .NET: OpenAPI Specification é a mesma para os dois ecossistemas (é agnóstica de
 * linguagem); Swashbuckle é o "springdoc" do mundo .NET — a biblioteca concreta que lê os
 * controllers ASP.NET Core via reflection e gera o mesmo tipo de JSON.
 * ---------------------------------------------------------------------------------------------
 */
// @Configuration — mesma anotação explicada em docs/03: esta classe pode declarar @Bean.
@Configuration
public class OpenApiConfig {

    // -----------------------------------------------------------------------------------------
    // Este @Bean substitui/complementa o OpenAPI gerado automaticamente pelo springdoc — sem
    // esta classe, o springdoc já geraria uma documentação funcional sozinho (auto-configuração,
    // ver docs/05), só que com um título genérico. Aqui customizamos os metadados descritivos.
    // "OpenAPI"/"Info"/"Contact"/"License" são classes do modelo Java da ESPECIFICAÇÃO (pacote
    // io.swagger.v3.oas.models, biblioteca "swagger-models", trazida transitivamente pelo
    // springdoc-openapi-ui) — não são anotações Spring, são um modelo de objetos Java que espelha
    // 1:1 a estrutura do JSON da spec OpenAPI 3.
    // -----------------------------------------------------------------------------------------
    @Bean
    public OpenAPI processoServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGP-Processos — processo-service")
                        .description("API REST de Gestão de Processos Judiciais — projeto de "
                                + "estudo para o edital do TRT 4ª Região (ver conteudo_programatico.md).")
                        .version("v1")
                        .contact(new Contact().name("Igor Bonato"))
                        .license(new License().name("Uso interno / estudo")));
    }
}
