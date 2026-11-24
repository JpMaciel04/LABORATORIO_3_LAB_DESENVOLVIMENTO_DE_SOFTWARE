package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Troca de moedas por uma vantagem (UC09, UC12; RN10, RN11). */
@Service
public class ResgateService {

    private final AlunoRepository alunoRepository;
    private final VantagemRepository vantagemRepository;
    private final TransacaoRepository transacaoRepository;
    private final NotificacaoService notificacaoService;

    public ResgateService(AlunoRepository alunoRepository, VantagemRepository vantagemRepository,
                          TransacaoRepository transacaoRepository, NotificacaoService notificacaoService) {
        this.alunoRepository = alunoRepository;
        this.vantagemRepository = vantagemRepository;
        this.transacaoRepository = transacaoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public ResgateVantagem resgatarVantagem(Long alunoId, Long vantagemId) {
        Aluno aluno = alunoRepository.findByIdAndAtivoTrue(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno " + alunoId + " não encontrado."));
        Vantagem vantagem = vantagemRepository.findByIdAndAtivaTrue(vantagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vantagem " + vantagemId + " não está disponível."));

        ResgateVantagem resgate = transacaoRepository.save(aluno.resgatar(vantagem));

        notificacaoService.enviarCupomAluno(resgate, aluno);
        notificacaoService.notificarEmpresaParceira(resgate, aluno);
        return resgate;
    }
}
