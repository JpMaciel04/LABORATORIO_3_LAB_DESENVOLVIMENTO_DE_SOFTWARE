package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.DadosDeTeste;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * E-mails do sistema (UC10; RN07, RN11; US04). As transações precisam ser confirmadas de verdade, pois a entrega
 * só acontece depois do commit; por isso esta classe usa um banco próprio, sem afetar os demais testes.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:teste_email;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@ActiveProfiles({"h2", "teste"})
class NotificacaoEmailTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();
    private static final int ESPERA_MS = 3000;

    @MockitoBean
    private JavaMailSender mailSender;
    @Autowired
    private MoedaService moedaService;
    @Autowired
    private ResgateService resgateService;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private EmpresaParceiraRepository empresaParceiraRepository;
    @Autowired
    private VantagemRepository vantagemRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private Professor professor;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        moedaService.creditarMoedasSemestrais("2031-1");
        professor = professorRepository.findAll().stream()
                .filter(p -> p.getEmail().equals("ana.souza@pucminas.br")).findFirst().orElseThrow();
        aluno = novoAluno();
    }

    @Test
    void alunoEAvisadoComProfessorQuantidadeEMotivo() {
        moedaService.enviarMoedas(professor.getId(), aluno.getId(), 25, "Participação em aula");

        ArgumentCaptor<SimpleMailMessage> email = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(ESPERA_MS)).send(email.capture());
        assertThat(email.getValue().getTo()).containsExactly(aluno.getEmail());
        assertThat(email.getValue().getSubject()).contains("25");
        assertThat(email.getValue().getText())
                .contains("Ana Paula Souza", "25 moedas", "Participação em aula");
    }

    @Test
    void resgateEnviaOMesmoCodigoAoAlunoEAEmpresa() {
        EmpresaParceira empresa = novaEmpresa();
        Vantagem vantagem = vantagemRepository.save(new Vantagem(empresa, "Almoço", "Almoço", "/api/fotos/a.png", 10));
        moedaService.enviarMoedas(professor.getId(), aluno.getId(), 10, "Participação em aula");
        verify(mailSender, timeout(ESPERA_MS)).send(any(SimpleMailMessage.class));

        ResgateVantagem resgate = resgateService.resgatarVantagem(aluno.getId(), vantagem.getId());

        ArgumentCaptor<SimpleMailMessage> emails = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(ESPERA_MS).times(3)).send(emails.capture());
        List<SimpleMailMessage> doResgate = emails.getAllValues().stream()
                .filter(email -> email.getText().contains(resgate.getCodigoCupom())).toList();
        assertThat(doResgate).hasSize(2);
        assertThat(doResgate).extracting(email -> email.getTo()[0])
                .containsExactlyInAnyOrder(aluno.getEmail(), empresa.getEmail());
        assertThat(doResgate).filteredOn(email -> email.getTo()[0].equals(aluno.getEmail())).singleElement()
                .satisfies(cupom -> assertThat(cupom.getText()).contains("Almoço", empresa.getNome(), "10 moedas"));
        assertThat(doResgate).filteredOn(email -> email.getTo()[0].equals(empresa.getEmail())).singleElement()
                .satisfies(aviso -> assertThat(aviso.getText()).contains("Almoço", aluno.getNome()));
    }

    @Test
    void falhaNoEnvioDoEmailNaoDesfazATransacao() {
        doThrow(new MailSendException("Servidor SMTP fora do ar")).when(mailSender).send(any(SimpleMailMessage.class));

        moedaService.enviarMoedas(professor.getId(), aluno.getId(), 15, "Participação em aula");

        verify(mailSender, timeout(ESPERA_MS)).send(any(SimpleMailMessage.class));
        assertThat(alunoRepository.findById(aluno.getId()).orElseThrow().getConta().getSaldo()).isEqualTo(15);
    }

    @Test
    void operacaoRecusadaNaoEnviaEmail() {
        assertThatThrownBy(() -> moedaService.enviarMoedas(professor.getId(), aluno.getId(), 1_000_000, "Motivo"))
                .isInstanceOf(RegraNegocioException.class);

        verify(mailSender, after(300).never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void transacaoDesfeitaNaoEnviaEmail() {
        transactionTemplate.executeWithoutResult(status -> {
            moedaService.enviarMoedas(professor.getId(), aluno.getId(), 5, "Participação em aula");
            status.setRollbackOnly();
        });

        verify(mailSender, after(300).never()).send(any(SimpleMailMessage.class));
        assertThat(alunoRepository.findById(aluno.getId()).orElseThrow().getConta().getSaldo()).isZero();
    }

    private Aluno novoAluno() {
        int numero = SEQUENCIA.incrementAndGet();
        return alunoRepository.save(DadosDeTeste.aluno("aluno" + numero + "@email.com",
                String.format("%011d", numero), professor.getInstituicao()));
    }

    private EmpresaParceira novaEmpresa() {
        int numero = SEQUENCIA.incrementAndGet();
        return empresaParceiraRepository.save(DadosDeTeste.empresa("empresa" + numero + "@email.com",
                String.format("%014d", numero)));
    }
}
