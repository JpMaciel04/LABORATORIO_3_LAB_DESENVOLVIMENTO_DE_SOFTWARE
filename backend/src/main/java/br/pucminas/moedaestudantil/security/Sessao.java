package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.model.Usuario;

/** Resultado de um login bem-sucedido: o token emitido e o usuário a que ele pertence. */
public record Sessao(String token, Usuario usuario) {
}
