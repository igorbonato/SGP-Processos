package br.jus.trt4.processo.controller;

import br.jus.trt4.processo.dto.request.LoginRequestDTO;
import br.jus.trt4.processo.dto.response.LoginResponseDTO;
import br.jus.trt4.processo.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "Autenticação", description = "Emissão de JWT")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "Autentica usuário/senha e devolve um JWT",
            description = "Usuários de estudo disponíveis: analista/senha123 (ROLE_ANALISTA) "
                    + "e consulta/senha123 (ROLE_CONSULTA) — ver SecurityConfig.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        // -------------------------------------------------------------------------------------
        // authenticationManager.authenticate(...) é o MESMO objeto que o Spring Security usaria
        // internamente num login form-based tradicional — aqui o chamamos manualmente, passando
        // um UsernamePasswordAuthenticationToken "não autenticado" (username+password crus). Por
        // baixo dos panos, ele consulta o AuthenticationManagerBuilder configurado no
        // SecurityConfig (os usuários em memória + o PasswordEncoder) e:
        //   - se a senha bater (após o BCrypt comparar hash), devolve uma Authentication "cheia"
        //     (autenticada, com as GrantedAuthority/roles do usuário);
        //   - se não bater, lança BadCredentialsException — que, sendo uma AuthenticationException,
        //     é capturada pelo ExceptionTranslationFilter e vira 401 via JwtAuthEntryPoint (ver o
        //     javadoc completo lá), NUNCA por um @ExceptionHandler deste controller.
        // -------------------------------------------------------------------------------------
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        String token = jwtTokenProvider.gerarToken(authentication);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
