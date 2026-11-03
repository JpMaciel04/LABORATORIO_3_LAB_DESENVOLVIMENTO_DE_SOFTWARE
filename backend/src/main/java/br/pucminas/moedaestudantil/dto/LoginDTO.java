package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.NotBlank;

/** Credenciais informadas na tela de login (UC01). */
public record LoginDTO(
        @NotBlank(message = "Informe o e-mail.")
        String email,

        @NotBlank(message = "Informe a senha.")
        String senha) {

    /** O e-mail é armazenado em minúsculas, então o login não diferencia maiúsculas. */
    public LoginDTO {
        email = Formatos.email(email);
    }
}
