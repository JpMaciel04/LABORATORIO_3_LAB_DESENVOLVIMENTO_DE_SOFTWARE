package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Aluno;

/** Dados do aluno devolvidos pela API. A senha nunca é exposta. */
public record AlunoResponseDTO(
        Long id,
        String nome,
        String email,
        String cpf,
        String rg,
        String curso,
        InstituicaoEnsinoDTO instituicao,
        EnderecoDTO endereco,
        int saldo) {

    public static AlunoResponseDTO from(Aluno aluno) {
        return new AlunoResponseDTO(aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getCpf(),
                aluno.getRg(), aluno.getCurso(), InstituicaoEnsinoDTO.from(aluno.getInstituicao()),
                EnderecoDTO.from(aluno.getEndereco()), aluno.getConta().getSaldo());
    }
}
