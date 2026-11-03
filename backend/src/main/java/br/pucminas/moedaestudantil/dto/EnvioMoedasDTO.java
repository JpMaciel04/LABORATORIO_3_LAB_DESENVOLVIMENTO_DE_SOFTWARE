package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados informados pelo professor para enviar moedas (RN05). */
public record EnvioMoedasDTO(
        @NotNull(message = "Selecione o aluno.")
        Long alunoId,

        @NotNull(message = "Informe a quantidade de moedas.")
        @Positive(message = "A quantidade deve ser um número inteiro positivo.")
        Integer valor,

        @NotBlank(message = "Informe o motivo do reconhecimento.")
        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres.")
        String motivo) {

    public EnvioMoedasDTO {
        motivo = Formatos.texto(motivo);
    }
}
