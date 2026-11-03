package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Aluno;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    /** Carrega instituição e conta na mesma consulta (JOIN), evitando o problema N+1. */
    @EntityGraph(attributePaths = {"instituicao", "conta"})
    List<Aluno> findByInstituicaoIdAndAtivoTrueOrderByNomeAsc(Long instituicaoId);

    @EntityGraph(attributePaths = {"instituicao", "conta"})
    Optional<Aluno> findByIdAndAtivoTrue(Long id);

    /** Inclui alunos desativados: o nome continua aparecendo no histórico dos extratos (RN08). */
    @Query("select a.conta.id as contaId, a.nome as nome from Aluno a where a.conta.id in :contaIds")
    List<TitularConta> findTitulares(Collection<Long> contaIds);
}
