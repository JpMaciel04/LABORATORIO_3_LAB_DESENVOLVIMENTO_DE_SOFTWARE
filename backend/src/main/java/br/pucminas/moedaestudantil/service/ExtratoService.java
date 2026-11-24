package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.model.Conta;
import br.pucminas.moedaestudantil.model.Extrato;
import br.pucminas.moedaestudantil.model.Transacao;
import br.pucminas.moedaestudantil.model.Usuario;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.TitularConta;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Monta o extrato do usuário autenticado (UC07; RN08). */
@Service
public class ExtratoService {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final TransacaoRepository transacaoRepository;

    public ExtratoService(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository,
                          ProfessorRepository professorRepository, TransacaoRepository transacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public Extrato consultarExtrato(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + usuarioId + " não encontrado."));
        Conta conta = usuario.getConta();
        List<Transacao> transacoes = transacaoRepository.findByConta(conta);
        return new Extrato(conta, transacoes, buscarTitulares(conta, transacoes));
    }

    /** Busca os nomes de uma só vez, em vez de uma consulta por transação (problema N+1). */
    private Map<Long, String> buscarTitulares(Conta conta, List<Transacao> transacoes) {
        Set<Long> contaIds = new HashSet<>();
        contaIds.add(conta.getId());
        for (Transacao transacao : transacoes) {
            contaIds.addAll(transacao.getIdsContasEnvolvidas());
        }

        Map<Long, String> titulares = new HashMap<>();
        for (TitularConta titular : alunoRepository.findTitulares(contaIds)) {
            titulares.put(titular.getContaId(), titular.getNome());
        }
        for (TitularConta titular : professorRepository.findTitulares(contaIds)) {
            titulares.put(titular.getContaId(), titular.getNome());
        }
        return titulares;
    }
}
