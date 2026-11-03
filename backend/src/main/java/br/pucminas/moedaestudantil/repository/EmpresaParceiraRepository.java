package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.EmpresaParceira;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaParceiraRepository extends JpaRepository<EmpresaParceira, Long> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);

    List<EmpresaParceira> findByAtivoTrueOrderByNomeAsc();

    Optional<EmpresaParceira> findByIdAndAtivoTrue(Long id);
}
