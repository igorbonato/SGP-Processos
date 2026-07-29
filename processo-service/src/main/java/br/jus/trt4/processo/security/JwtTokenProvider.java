package br.jus.trt4.processo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Emite e valida os JWT deste serviço. Concentra TUDO que envolve o token em si — geração,
 * parsing, validação de assinatura/expiração — para o resto da aplicação (filtro, controller de
 * login) nunca precisar saber como um JWT é montado por dentro.
 */
@Component
public class JwtTokenProvider {

    // -------------------------------------------------------------------------------------
    // @Value injeta um valor de application.yml (jwt.secret / jwt.expiration-ms) direto no
    // campo — sem precisar de um objeto de configuração dedicado, para um valor tão pontual.
    // Paralelo .NET: equivalente a `IConfiguration["Jwt:Secret"]` injetado, ou a um
    // IOptions<JwtSettings> para um grupo maior de propriedades relacionadas.
    // -------------------------------------------------------------------------------------
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key key;

    // @PostConstruct — método que roda UMA vez, logo depois do Spring terminar de injetar os
    // campos deste bean (mas antes de qualquer outro bean poder usá-lo). Convertemos a string
    // "secret" (texto simples do application.yml) para um objeto Key assinável apenas aqui,
    // porque @Value ainda não tem o valor disponível no momento em que o construtor roda.
    // Paralelo .NET: comparável a um método chamado no `ConfigureServices`/`OnModelCreating`
    // logo após resolver as opções — não há uma anotação de ciclo de vida idêntica, o mais
    // próximo é implementar `IHostedService.StartAsync` ou similar para inicialização tardia.
    @PostConstruct
    public void init() {
        // Keys.hmacShaKeyFor exige uma chave de pelo menos 256 bits (32 bytes) para o algoritmo
        // HS256 usado abaixo — por isso o valor de "jwt.secret" no application.yml precisa ter
        // esse tamanho mínimo; um segredo curto faz esta linha lançar WeakKeyException no boot.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gera um JWT assinado a partir de uma Authentication já validada pelo AuthenticationManager
     * (ver AuthController) — o "subject" do token é o username, e as roles vão num claim
     * customizado "roles" (texto separado por vírgula, o formato mais simples possível).
     */
    public String gerarToken(Authentication authentication) {
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("roles", roles)
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** true se o token tem assinatura válida E não expirou. Qualquer outro problema (token
     *  corrompido, algoritmo diferente, formato inválido) também cai no catch e vira false —
     *  o chamador (JwtAuthenticationFilter) não distingue os motivos, só age em cima do booleano. */
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String obterUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public List<GrantedAuthority> obterAuthorities(String token) {
        String roles = parseClaims(token).get("roles", String.class);
        return Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
