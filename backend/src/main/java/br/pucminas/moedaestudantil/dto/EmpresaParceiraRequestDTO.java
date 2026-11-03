package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.EmpresaParceira;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

/** Dados enviados pelo front-end para cadastrar ou alterar uma empresa parceira. */
public record EmpresaParceiraRequestDTO(
        @NotBlank(message = "Informe o nome da empresa.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe o CNPJ.")
        @CNPJ(message = "Informe um CNPJ válido.")
        String cnpj,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres.")
        String email,

        @NotBlank(groups = ValidacaoCadastro.class, message = "Informe a senha.")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
        String senha) {

    public EmpresaParceira toEntity() {
        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setNome(Formatos.texto(nome));
        empresa.setCnpj(Formatos.apenasDigitos(cnpj));
        empresa.setEmail(Formatos.email(email));
        empresa.setSenha(senha);
        return empresa;
    }
}
