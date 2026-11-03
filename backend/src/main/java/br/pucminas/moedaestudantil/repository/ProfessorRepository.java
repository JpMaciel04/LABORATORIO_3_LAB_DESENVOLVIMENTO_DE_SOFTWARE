package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Professor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    @EntityGraph(attributePaths = {"instituicao", "conta"})
    Optional<Professor> findByIdAndAtivoTrue(Long id);

    @EntityGraph(attributePaths = {"instituicao", "conta"})
    List<Professor> findByAtivoTrue();

    @Query("select p.conta.id as contaId, p.nome as nome from Professor p where p.conta.id in :contaIds")
    List<TitularConta> findTitulares(Collection<Long> contaIds);
}
