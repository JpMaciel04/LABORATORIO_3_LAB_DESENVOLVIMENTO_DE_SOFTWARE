package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Conta;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    /** Transações em que a conta é origem ou destino, da mais recente para a mais antiga (RN08). */
    @Query("""
            select t from Transacao t
            where t.contaOrigem = :conta or t.contaDestino = :conta
            order by t.dataHora desc, t.id desc
            """)
    List<Transacao> findByConta(Conta conta);

    @Query("""
            select case when count(t) > 0 then true else false end
            from Transacao t
            where t.contaOrigem = :conta or t.contaDestino = :conta
            """)
    boolean existsByConta(Conta conta);

    @Query("""
            select case when count(r) > 0 then true else false end
            from ResgateVantagem r
            where r.vantagem.empresa = :empresa
            """)
    boolean existsResgateByEmpresa(EmpresaParceira empresa);

    /** Impede crédito semestral em duplicidade para o mesmo professor (RN04). */
    @Query("""
            select case when count(c) > 0 then true else false end
            from CreditoSemestral c, Professor p
            where p = :professor and c.contaDestino = p.conta and c.semestre = :semestre
            """)
    boolean existsCreditoSemestral(Professor professor, String semestre);
}
