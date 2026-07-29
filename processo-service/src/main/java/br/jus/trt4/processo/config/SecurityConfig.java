package br.jus.trt4.processo.config;

import br.jus.trt4.processo.security.JwtAccessDeniedHandler;
import br.jus.trt4.processo.security.JwtAuthEntryPoint;
import br.jus.trt4.processo.security.JwtAuthenticationFilter;
import br.jus.trt4.processo.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ---------------------------------------------------------------------------------------------
 * @EnableWebSecurity + WebSecurityConfigurerAdapter
 * ---------------------------------------------------------------------------------------------
 * Ativa e customiza a cadeia de filtros de segurança do Spring Security. Estendemos
 * WebSecurityConfigurerAdapter (a forma padrão em Spring Security 5.3.x, a versão trazida pelo
 * Spring Boot 2.3.12.RELEASE deste projeto — só foi deprecated a partir do Spring Security 5.7).
 *
 * @EnableGlobalMethodSecurity(prePostEnabled = true) — liga o suporte a {@code @PreAuthorize}/
 * {@code @PostAuthorize} em métodos de QUALQUER bean Spring (não só controllers). Sem esta
 * anotação, {@code @PreAuthorize("hasRole('ANALISTA')")} nos controllers (ver
 * ProcessoController/MovimentacaoController) seria só um comentário decorativo, ignorado.
 *
 * Paralelo .NET: o conjunto inteiro desta classe equivale ao que você configura em
 * `builder.Services.AddAuthentication().AddJwtBearer(...)` +
 * `builder.Services.AddAuthorization(...)` no Program.cs — só que concentrado em uma única
 * classe de configuração, em vez de chamadas soltas no bootstrap.
 * ---------------------------------------------------------------------------------------------
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtAuthEntryPoint jwtAuthEntryPoint,
                           JwtAccessDeniedHandler jwtAccessDeniedHandler,
                           JwtTokenProvider jwtTokenProvider) {
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Expõe o AuthenticationManager (já configurado com os usuários abaixo) como @Bean, para o
     * AuthController poder injetá-lo e chamar {@code authenticate(...)} manualmente no login —
     * por padrão o WebSecurityConfigurerAdapter mantém esse objeto "escondido", só usado
     * internamente pelo próprio filtro de login form-based que NÃO usamos aqui.
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * BCryptPasswordEncoder: hash de senha com salt embutido automaticamente e fator de custo
     * ajustável (mais lento de propósito, para dificultar força bruta). Nunca comparamos senha
     * em texto puro em lugar nenhum — nem a que o usuário digita, nem a que fica "armazenada"
     * (ver configure(AuthenticationManagerBuilder) abaixo).
     * Paralelo .NET: `PasswordHasher<TUser>` do ASP.NET Core Identity — mesmo papel, algoritmo
     * default diferente (PBKDF2 lá, BCrypt aqui).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // -------------------------------------------------------------------------------------------
    // Usuários EM MEMÓRIA — simplificação deliberada para este estudo. Um sistema real teria um
    // "Usuario" persistido (entidade JPA + repository + um UserDetailsService buscando no banco);
    // aqui o foco da Fase 5 é o mecanismo de autenticação/JWT em si, não gestão de usuários — essa
    // extensão (Usuario como agregado, hash de senha no banco) fica registrada como possível
    // próximo passo fora do roadmap principal.
    // -------------------------------------------------------------------------------------------
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .passwordEncoder(passwordEncoder())
                .withUser("analista").password(passwordEncoder().encode("senha123")).roles("ANALISTA")
                .and()
                .withUser("consulta").password(passwordEncoder().encode("senha123")).roles("CONSULTA");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // CSRF protege formulários HTML tradicionais (cookies + sessão); uma API REST
                // stateless autenticada por token não tem esse vetor de ataque — a proteção certa
                // aqui é o próprio JWT, não teria motivo pra pedir um segundo token de sessão.
                // Paralelo .NET: equivalente a nunca ligar o middleware de Antiforgery numa API
                // pura que só aceita Bearer token.
                .csrf().disable()

                // Substitui o handling padrão do Spring Security (que redirecionaria para uma
                // tela de login HTML) pelos nossos handlers JSON — ver javadoc de
                // JwtAuthEntryPoint sobre por que isso não é um @ExceptionHandler comum.
                .exceptionHandling()
                    .authenticationEntryPoint(jwtAuthEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
                    .and()

                // STATELESS: o Spring Security NUNCA cria ou lê um HttpSession para guardar
                // "quem está logado" — cada requisição se autentica sozinha, via o JWT que ela
                // mesma carrega no header. Sem isso, o Spring tentaria manter uma sessão de
                // servidor por padrão, o que não faz sentido combinado com JWT (e quebraria a
                // escalabilidade horizontal do microsserviço).
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()

                .authorizeRequests()
                    // Login precisa ser público — ninguém tem token ainda nesse momento.
                    .antMatchers("/auth/login").permitAll()
                    // Documentação (Fase 4) também fica pública — não faria sentido exigir login
                    // só para LER o contrato da API.
                    .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    // H2 Console (só existe no perfil dev, ver application-dev.yml) e Actuator
                    // (health check) também públicos, por conveniência de desenvolvimento/infra.
                    .antMatchers("/h2-console/**", "/actuator/**").permitAll()
                    // Todo o resto (tudo em /api/**) exige um JWT válido — o refinamento por
                    // ROLE específica fica a cargo do @PreAuthorize em cada endpoint.
                    .anyRequest().authenticated()
                    .and()

                // Necessário SÓ por causa do H2 Console: a tela dele é renderizada dentro de um
                // <frame>, e o Spring Security bloqueia frames de qualquer origem por padrão
                // (proteção contra clickjacking) — sem esta linha, o H2 Console fica em branco.
                .headers().frameOptions().disable();

        // Registra nosso filtro ANTES do UsernamePasswordAuthenticationFilter padrão do Spring
        // Security (o filtro do login form-based tradicional, que não usamos, mas que ainda
        // existe na cadeia por padrão) — garante que o JWT já seja processado e o
        // SecurityContext já esteja populado antes de qualquer outro filtro de autenticação
        // rodar.
        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class);
    }
}
