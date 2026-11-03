package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Vantagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados de uma vantagem (RN09). A foto é enviada à parte, como arquivo da requisição multipart. */
public record VantagemRequestDTO(
        @NotBlank(message = "Informe o nome da vantagem.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe a descrição.")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String descricao,

        @NotNull(message = "Informe o custo em moedas.")
        @Positive(message = "O custo deve ser um número inteiro positivo.")
        Integer custoMoedas) {

    /** A empresa e a foto são definidas pelo service. */
    public Vantagem toEntity() {
        return new Vantagem(null, Formatos.texto(nome), Formatos.texto(descricao), null, custoMoedas);
    }
}
