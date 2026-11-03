package br.pucminas.moedaestudantil.dto;

import java.util.Map;

/**
 * Corpo padrão das respostas de erro da API.
 *
 * @param campos mensagens por campo do formulário (ex.: {"cpf": "CPF já cadastrado."}); vazio quando o erro é geral
 */
public record ErroDTO(int status, String mensagem, Map<String, String> campos) {
}
