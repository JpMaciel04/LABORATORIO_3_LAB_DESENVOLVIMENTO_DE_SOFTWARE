package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Envio de moedas (UC06; RN05, RN06, RN07) e crédito semestral aos professores (UC11; RN04). */
@Service
public class MoedaService {

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final TransacaoRepository transacaoRepository;
    private final NotificacaoService notificacaoService;

    public MoedaService(ProfessorRepository professorRepository, AlunoRepository alunoRepository,
                        TransacaoRepository transacaoRepository, NotificacaoService notificacaoService) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.transacaoRepository = transacaoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public EnvioMoedas enviarMoedas(Long professorId, Long alunoId, int valor, String motivo) {
        Professor professor = professorRepository.findByIdAndAtivoTrue(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor " + professorId + " não encontrado."));
        Aluno aluno = alunoRepository.findByIdAndAtivoTrue(alunoId)
                .orElseThrow(() -> new RegraNegocioException("alunoId", "Selecione um aluno cadastrado."));
        if (!professor.getInstituicao().getId().equals(aluno.getInstituicao().getId())) {
            throw new RegraNegocioException("alunoId", "Só é possível enviar moedas a alunos da sua instituição.");
        }

        professor.getConta().debitar(valor);
        aluno.getConta().creditar(valor);
        EnvioMoedas envio = transacaoRepository.save(new EnvioMoedas(professor, aluno, valor, motivo));

        notificacaoService.notificarRecebimentoMoedas(envio, professor, aluno);
        return envio;
    }

    /**
     * Credita as moedas do semestre a quem ainda não as recebeu; o saldo anterior é mantido (acumulativo).
     * Pode ser executado mais de uma vez: quem já recebeu no semestre é ignorado.
     *
     * @return quantidade de professores creditados
     */
    @Transactional
    public int creditarMoedasSemestrais(String semestre) {
        int creditados = 0;
        for (Professor professor : professorRepository.findByAtivoTrue()) {
            if (!transacaoRepository.existsCreditoSemestral(professor, semestre)) {
                professor.getConta().creditar(CreditoSemestral.MOEDAS_POR_SEMESTRE);
                transacaoRepository.save(new CreditoSemestral(professor, semestre));
                creditados++;
            }
        }
        return creditados;
    }
}
