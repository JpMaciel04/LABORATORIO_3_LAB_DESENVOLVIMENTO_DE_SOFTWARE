package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record EnderecoDTO(
        @NotBlank(message = "Informe o logradouro.")
        @Size(max = 150, message = "O logradouro deve ter no máximo 150 caracteres.")
        String logradouro,

        @NotBlank(message = "Informe o número.")
        @Size(max = 10, message = "O número deve ter no máximo 10 caracteres.")
        String numero,

        @Size(max = 60, message = "O complemento deve ter no máximo 60 caracteres.")
        String complemento,

        @NotBlank(message = "Informe o bairro.")
        @Size(max = 80, message = "O bairro deve ter no máximo 80 caracteres.")
        String bairro,

        @NotBlank(message = "Informe a cidade.")
        @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres.")
        String cidade,

        @NotBlank(message = "Informe o estado.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "Informe a sigla do estado com 2 letras (ex.: MG).")
        String estado,

        @NotBlank(message = "Informe o CEP.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Informe um CEP válido (ex.: 30535-901).")
        String cep) {

    public Endereco toEntity() {
        return new Endereco(Formatos.texto(logradouro), Formatos.texto(numero), Formatos.opcional(complemento),
                Formatos.texto(bairro), Formatos.texto(cidade), estado.toUpperCase(Locale.ROOT),
                Formatos.apenasDigitos(cep));
    }

    public static EnderecoDTO from(Endereco endereco) {
        return new EnderecoDTO(endereco.getLogradouro(), endereco.getNumero(), endereco.getComplemento(),
                endereco.getBairro(), endereco.getCidade(), endereco.getEstado(), endereco.getCep());
    }
}
