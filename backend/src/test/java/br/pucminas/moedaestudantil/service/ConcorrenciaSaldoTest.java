package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.DadosDeTeste;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O saldo não pode ser gasto duas vezes por operações simultâneas (bloqueio otimista da Conta).
 * Usa um banco próprio porque as threads precisam confirmar suas transações de verdade.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:teste_concorrencia;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@ActiveProfiles({"h2", "teste"})
class ConcorrenciaSaldoTest {

    @Autowired
    private MoedaService moedaService;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private AlunoRepository alunoRepository;

    @Test
    void gravarContaLidaAntesDeOutraAlteracaoERecusado() {
        Aluno aluno = alunoRepository.save(DadosDeTeste.aluno("leitura@email.com", "11111111111",
                professorRepository.findAll().getFirst().getInstituicao()));
        Aluno leituraA = alunoRepository.findById(aluno.getId()).orElseThrow();
        Aluno leituraB = alunoRepository.findById(aluno.getId()).orElseThrow();

        leituraA.getConta().creditar(10);
        alunoRepository.save(leituraA);

        leituraB.getConta().creditar(10);
        assertThatThrownBy(() -> alunoRepository.save(leituraB))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(alunoRepository.findById(aluno.getId()).orElseThrow().getConta().getSaldo()).isEqualTo(10);
    }

    @Test
    void enviosSimultaneosNaoGastamOMesmoSaldoDuasVezes() throws Exception {
        moedaService.creditarMoedasSemestrais("2031-1");
        Professor professor = professorRepository.findAll().stream()
                .filter(p -> p.getEmail().equals("carlos.lima@pucminas.br")).findFirst().orElseThrow();
        Aluno aluno = alunoRepository.save(DadosDeTeste.aluno("concorrente@email.com", "22222222222", professor.getInstituicao()));

        int operacoes = 4;
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        try (ExecutorService threads = Executors.newFixedThreadPool(operacoes)) {
            for (int i = 0; i < operacoes; i++) {
                resultados.add(threads.submit(() -> {
                    largada.await();
                    return enviarTudo(professor, aluno);
                }));
            }
            largada.countDown();
        }

        long sucessos = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get()) {
                sucessos++;
            }
        }
        assertThat(sucessos).isEqualTo(1);
        assertThat(professorRepository.findById(professor.getId()).orElseThrow().getConta().getSaldo()).isZero();
        assertThat(alunoRepository.findById(aluno.getId()).orElseThrow().getConta().getSaldo()).isEqualTo(1000);
    }

    /** O perdedor recebe "saldo insuficiente" ou um conflito de concorrência, conforme o instante da leitura. */
    private boolean enviarTudo(Professor professor, Aluno aluno) {
        try {
            moedaService.enviarMoedas(professor.getId(), aluno.getId(), 1000, "Concorrência");
            return true;
        } catch (ConcurrencyFailureException | RegraNegocioException e) {
            return false;
        }
    }
}
