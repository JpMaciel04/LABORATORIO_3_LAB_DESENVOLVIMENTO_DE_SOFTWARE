package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Crédito de 1.000 moedas por semestre aos professores (UC11; RN04). */
@SpringBootTest
@ActiveProfiles({"h2", "teste"})
@Transactional
class CreditoSemestralTest {

    @Autowired
    private MoedaService moedaService;
    @Autowired
    private CreditoSemestralScheduler scheduler;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private TransacaoRepository transacaoRepository;

    @Test
    void cadaProfessorAtivoRecebeMilMoedas() {
        int creditados = moedaService.creditarMoedasSemestrais("2031-1");

        assertThat(creditados).isEqualTo(4);
        assertThat(professorRepository.findAll()).allSatisfy(professor -> {
            assertThat(professor.getConta().getSaldo()).isEqualTo(1000);
            assertThat(transacaoRepository.existsCreditoSemestral(professor, "2031-1")).isTrue();
        });
    }

    @Test
    void creditoNaoSeRepeteNoMesmoSemestre() {
        moedaService.creditarMoedasSemestrais("2031-1");

        int creditados = moedaService.creditarMoedasSemestrais("2031-1");

        assertThat(creditados).isZero();
        assertThat(professorRepository.findAll()).allSatisfy(professor ->
                assertThat(professor.getConta().getSaldo()).isEqualTo(1000));
    }

    @Test
    void saldoNaoDistribuidoEAcumuladoNoSemestreSeguinte() {
        moedaService.creditarMoedasSemestrais("2031-1");
        Professor professor = professorRepository.findAll().getFirst();
        professor.getConta().debitar(300);

        moedaService.creditarMoedasSemestrais("2031-2");

        assertThat(professor.getConta().getSaldo()).isEqualTo(1700);
        assertThat(transacaoRepository.findByConta(professor.getConta())).hasSize(2);
    }

    @Test
    void professorDesativadoNaoRecebeCredito() {
        Professor professor = professorRepository.findAll().getFirst();
        professor.desativar();

        int creditados = moedaService.creditarMoedasSemestrais("2031-1");

        assertThat(creditados).isEqualTo(3);
        assertThat(professor.getConta().getSaldo()).isZero();
    }

    @Test
    void agendadorCreditaOSemestreAtual() {
        scheduler.executar();

        String semestreAtual = CreditoSemestral.semestreDe(LocalDate.now());
        List<Professor> professores = professorRepository.findAll();
        assertThat(professores).allSatisfy(professor ->
                assertThat(transacaoRepository.existsCreditoSemestral(professor, semestreAtual)).isTrue());
    }

    @Test
    void semestreSeguiOCalendario() {
        assertThat(CreditoSemestral.semestreDe(LocalDate.of(2026, 1, 1))).isEqualTo("2026-1");
        assertThat(CreditoSemestral.semestreDe(LocalDate.of(2026, 6, 30))).isEqualTo("2026-1");
        assertThat(CreditoSemestral.semestreDe(LocalDate.of(2026, 7, 1))).isEqualTo("2026-2");
        assertThat(CreditoSemestral.semestreDe(LocalDate.of(2026, 12, 31))).isEqualTo("2026-2");
    }
}
