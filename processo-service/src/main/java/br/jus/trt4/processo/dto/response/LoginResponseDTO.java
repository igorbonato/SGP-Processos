package br.jus.trt4.processo.dto.response;

/** Construído manualmente no AuthController (nunca via MapStruct — não existe entidade "Login"
 *  para mapear a partir dela), por isso pode ser imutável sem os problemas de mapeamento por
 *  construtor discutidos em ParteResponseDTO. */
public class LoginResponseDTO {

    private final String token;
    private final String tipo;

    public LoginResponseDTO(String token) {
        this.token = token;
        this.tipo = "Bearer";
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }
}
