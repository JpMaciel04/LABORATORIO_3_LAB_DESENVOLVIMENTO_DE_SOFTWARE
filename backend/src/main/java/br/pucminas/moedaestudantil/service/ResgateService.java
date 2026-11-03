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

import java.security.SecureRandom;

/** Troca de moedas por uma vantagem (UC09, UC12; RN10, RN11). */
@Service
public class ResgateService {

    /** Sem os caracteres que se confundem na conferência presencial: 0/O e 1/I. */
    private static final String ALFABETO_CUPOM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int GRUPOS_CUPOM = 3;
    private static final int CARACTERES_POR_GRUPO = 4;

    private final AlunoRepository alunoRepository;
    private final VantagemRepository vantagemRepository;
    private final TransacaoRepository transacaoRepository;
    private final NotificacaoService notificacaoService;
    private final SecureRandom aleatorio = new SecureRandom();

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

        aluno.getConta().debitar(vantagem.getCustoMoedas());
        ResgateVantagem resgate = transacaoRepository.save(new ResgateVantagem(aluno, vantagem, gerarCodigoCupom()));

        notificacaoService.enviarCupomAluno(resgate, aluno);
        notificacaoService.notificarEmpresaParceira(resgate, aluno);
        return resgate;
    }

    /** Código no formato XXXX-XXXX-XXXX. A coluna codigo_cupom é UNIQUE: o banco garante a unicidade (RN11). */
    private String gerarCodigoCupom() {
        StringBuilder codigo = new StringBuilder();
        for (int grupo = 0; grupo < GRUPOS_CUPOM; grupo++) {
            if (grupo > 0) {
                codigo.append('-');
            }
            for (int i = 0; i < CARACTERES_POR_GRUPO; i++) {
                codigo.append(ALFABETO_CUPOM.charAt(aleatorio.nextInt(ALFABETO_CUPOM.length())));
            }
        }
        return codigo.toString();
    }
}
