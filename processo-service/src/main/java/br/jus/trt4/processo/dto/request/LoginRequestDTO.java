package br.jus.trt4.processo.dto.request;

import javax.validation.constraints.NotBlank;

public class LoginRequestDTO {

    @NotBlank(message = "username é obrigatório")
    private String username;

    @NotBlank(message = "password é obrigatório")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
