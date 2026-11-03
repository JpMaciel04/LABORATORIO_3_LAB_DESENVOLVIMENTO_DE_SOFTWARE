package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Vantagem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VantagemRepository extends JpaRepository<Vantagem, Long> {

    @EntityGraph(attributePaths = "empresa")
    List<Vantagem> findByAtivaTrueOrderByNomeAsc();

    @EntityGraph(attributePaths = "empresa")
    Optional<Vantagem> findByIdAndAtivaTrue(Long id);

    /** Inclui as vantagens desativadas: a empresa continua vendo tudo o que já ofereceu. */
    @EntityGraph(attributePaths = "empresa")
    List<Vantagem> findByEmpresaIdOrderByNomeAsc(Long empresaId);

    /** Restringe a busca às vantagens da empresa: as de outras empresas se comportam como inexistentes (US14). */
    @EntityGraph(attributePaths = "empresa")
    Optional<Vantagem> findByIdAndEmpresaId(Long id, Long empresaId);
}
