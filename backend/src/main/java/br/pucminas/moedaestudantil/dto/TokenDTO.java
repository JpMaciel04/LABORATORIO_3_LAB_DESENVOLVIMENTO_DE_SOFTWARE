package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.model.Usuario;
import br.pucminas.moedaestudantil.security.Sessao;

/** Resposta do login: o token a ser enviado nas próximas requisições e os dados exibidos pelo front-end. */
public record TokenDTO(String token, Long id, String nome, String email, Perfil perfil) {

    public static TokenDTO from(Sessao sessao) {
        Usuario usuario = sessao.usuario();
        return new TokenDTO(sessao.token(), usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}
