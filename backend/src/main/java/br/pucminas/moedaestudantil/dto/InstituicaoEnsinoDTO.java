package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;

public record InstituicaoEnsinoDTO(Long id, String nome) {

    public static InstituicaoEnsinoDTO from(InstituicaoEnsino instituicao) {
        return new InstituicaoEnsinoDTO(instituicao.getId(), instituicao.getNome());
    }
}
