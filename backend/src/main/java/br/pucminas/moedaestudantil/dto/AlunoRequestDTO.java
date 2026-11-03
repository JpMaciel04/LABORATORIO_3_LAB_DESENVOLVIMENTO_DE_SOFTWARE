package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Aluno;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

/** Dados enviados pelo front-end para cadastrar ou alterar um aluno (RN01). */
public record AlunoRequestDTO(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres.")
        String email,

        @NotBlank(groups = ValidacaoCadastro.class, message = "Informe a senha.")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
        String senha,

        @NotBlank(message = "Informe o CPF.")
        @CPF(message = "Informe um CPF válido.")
        String cpf,

        @NotBlank(message = "Informe o RG.")
        @Size(max = 20, message = "O RG deve ter no máximo 20 caracteres.")
        String rg,

        @NotBlank(message = "Informe o curso.")
        @Size(max = 100, message = "O curso deve ter no máximo 100 caracteres.")
        String curso,

        @NotNull(message = "Selecione a instituição de ensino.")
        Long instituicaoId,

        @NotNull(message = "Informe o endereço.")
        @Valid
        EnderecoDTO endereco) {

    /** A instituição é resolvida pelo service a partir de {@link #instituicaoId()} (RN02). */
    public Aluno toEntity() {
        Aluno aluno = new Aluno();
        aluno.setNome(Formatos.texto(nome));
        aluno.setEmail(Formatos.email(email));
        aluno.setSenha(senha);
        aluno.setCpf(Formatos.apenasDigitos(cpf));
        aluno.setRg(Formatos.texto(rg));
        aluno.setCurso(Formatos.texto(curso));
        aluno.setEndereco(endereco.toEntity());
        return aluno;
    }
}
