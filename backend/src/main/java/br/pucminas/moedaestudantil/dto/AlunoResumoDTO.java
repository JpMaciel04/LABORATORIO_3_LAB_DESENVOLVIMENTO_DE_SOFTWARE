package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Aluno;

/** O que o professor vê para escolher a quem enviar moedas; os dados pessoais do aluno ficam de fora (US03). */
public record AlunoResumoDTO(Long id, String nome, String curso) {

    public static AlunoResumoDTO from(Aluno aluno) {
        return new AlunoResumoDTO(aluno.getId(), aluno.getNome(), aluno.getCurso());
    }
}
