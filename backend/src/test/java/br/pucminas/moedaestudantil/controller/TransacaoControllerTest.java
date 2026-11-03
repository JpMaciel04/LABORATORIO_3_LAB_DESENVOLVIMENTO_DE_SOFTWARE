package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.DadosDeTeste;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import br.pucminas.moedaestudantil.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Envio de moedas (UC06), resgate de vantagem (UC09) e extrato (UC07). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"h2", "teste"})
@Transactional
class TransacaoControllerTest {

    private static final String FORMATO_CUPOM = "[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private EmpresaParceiraRepository empresaParceiraRepository;
    @Autowired
    private VantagemRepository vantagemRepository;
    @Autowired
    private TransacaoRepository transacaoRepository;
    @Autowired
    private JwtService jwtService;

    private Professor professor;
    private Aluno aluno;
    private EmpresaParceira empresa;
    private Vantagem vantagem;

    @BeforeEach
    void setUp() {
        professor = professorDe("ana.souza@pucminas.br");
        professor.getConta().creditar(100);
        aluno = alunoRepository.save(DadosDeTeste.aluno("joana@email.com", "12345678909", professor.getInstituicao()));
        empresa = empresaParceiraRepository.save(DadosDeTeste.empresa("contato@cantina.com", "11222333000181"));
        vantagem = vantagemRepository.save(new Vantagem(empresa, "Almoço", "Almoço no restaurante", "/api/fotos/a.png", 30));
    }

    @Test
    void envioTransfereMoedasDoProfessorParaOAluno() throws Exception {
        enviar(comoProfessor(), aluno.getId(), 40, "Participação em aula")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENVIO_MOEDAS"))
                .andExpect(jsonPath("$.valor").value(40))
                .andExpect(jsonPath("$.motivo").value("Participação em aula"));

        assertThat(professor.getConta().getSaldo()).isEqualTo(60);
        assertThat(aluno.getConta().getSaldo()).isEqualTo(40);
    }

    @Test
    void envioAcimaDoSaldoERecusadoENaoAlteraOsSaldos() throws Exception {
        enviar(comoProfessor(), aluno.getId(), 101, "Participação em aula")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Saldo insuficiente: disponível 100, necessário 101."));

        assertThat(professor.getConta().getSaldo()).isEqualTo(100);
        assertThat(aluno.getConta().getSaldo()).isZero();
    }

    @Test
    void motivoEObrigatorio() throws Exception {
        enviar(comoProfessor(), aluno.getId(), 10, "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.motivo").value("Informe o motivo do reconhecimento."));
    }

    @Test
    void quantidadeDeveSerUmInteiroPositivo() throws Exception {
        enviar(comoProfessor(), aluno.getId(), 0, "Motivo")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").value("A quantidade deve ser um número inteiro positivo."));
        enviar(comoProfessor(), aluno.getId(), -5, "Motivo").andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/transacoes/envios").with(comoProfessor()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":" + aluno.getId() + ",\"valor\":10.5,\"motivo\":\"Motivo\"}"))
                .andExpect(status().isBadRequest());
        assertThat(aluno.getConta().getSaldo()).isZero();
    }

    @Test
    void naoEnviaParaAlunoDeOutraInstituicao() throws Exception {
        Professor outraInstituicao = professorDe("beatriz.rocha@ufmg.br");
        outraInstituicao.getConta().creditar(100);

        enviar(DadosDeTeste.autenticadoComo(outraInstituicao, jwtService), aluno.getId(), 10, "Motivo")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.alunoId").value("Só é possível enviar moedas a alunos da sua instituição."));
    }

    @Test
    void naoEnviaParaAlunoInexistenteOuDesativado() throws Exception {
        enviar(comoProfessor(), 99999L, 10, "Motivo")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.alunoId").exists());

        aluno.desativar();
        enviar(comoProfessor(), aluno.getId(), 10, "Motivo")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.alunoId").exists());
    }

    @Test
    void somenteProfessorEnviaMoedas() throws Exception {
        enviar(comoAluno(), aluno.getId(), 10, "Motivo").andExpect(status().isForbidden());
        enviar(DadosDeTeste.autenticadoComo(empresa, jwtService), aluno.getId(), 10, "Motivo").andExpect(status().isForbidden());
        mockMvc.perform(post("/api/transacoes/envios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resgateDebitaOSaldoEGeraCodigoDeCupom() throws Exception {
        aluno.getConta().creditar(50);

        mockMvc.perform(post("/api/transacoes/resgates/{id}", vantagem.getId()).with(comoAluno()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("RESGATE_VANTAGEM"))
                .andExpect(jsonPath("$.valor").value(30))
                .andExpect(jsonPath("$.vantagem").value("Almoço"))
                .andExpect(jsonPath("$.empresa").value("Cantina Central"))
                .andExpect(jsonPath("$.codigoCupom", matchesPattern(FORMATO_CUPOM)));

        assertThat(aluno.getConta().getSaldo()).isEqualTo(20);
    }

    @Test
    void cadaResgateTemUmCodigoDiferente() throws Exception {
        aluno.getConta().creditar(60);

        String primeiro = resgatarERetornarCupom();
        String segundo = resgatarERetornarCupom();

        assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    void resgateSemSaldoSuficienteERecusado() throws Exception {
        aluno.getConta().creditar(29);

        mockMvc.perform(post("/api/transacoes/resgates/{id}", vantagem.getId()).with(comoAluno()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Saldo insuficiente: disponível 29, necessário 30."));

        assertThat(aluno.getConta().getSaldo()).isEqualTo(29);
        assertThat(transacaoRepository.findByConta(aluno.getConta())).isEmpty();
    }

    @Test
    void vantagemDesativadaNaoPodeSerResgatada() throws Exception {
        aluno.getConta().creditar(50);
        vantagem.desativar();

        mockMvc.perform(post("/api/transacoes/resgates/{id}", vantagem.getId()).with(comoAluno()))
                .andExpect(status().isNotFound());

        assertThat(aluno.getConta().getSaldo()).isEqualTo(50);
    }

    @Test
    void somenteAlunoResgata() throws Exception {
        mockMvc.perform(post("/api/transacoes/resgates/{id}", vantagem.getId()).with(comoProfessor()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/transacoes/resgates/{id}", vantagem.getId()).with(DadosDeTeste.autenticadoComo(empresa, jwtService)))
                .andExpect(status().isForbidden());
    }

    @Test
    void extratoDoAlunoMostraRecebimentosEResgatesDoMaisRecenteParaOMaisAntigo() throws Exception {
        enviar(comoProfessor(), aluno.getId(), 50, "Participação em aula").andExpect(status().isCreated());
        String cupom = resgatarERetornarCupom();

        mockMvc.perform(get("/api/transacoes/extrato").with(comoAluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAtual").value(20))
                .andExpect(jsonPath("$.transacoes", hasSize(2)))
                .andExpect(jsonPath("$.transacoes[0].tipo").value("RESGATE_VANTAGEM"))
                .andExpect(jsonPath("$.transacoes[0].entrada").value(false))
                .andExpect(jsonPath("$.transacoes[0].valor").value(30))
                .andExpect(jsonPath("$.transacoes[0].vantagem").value("Almoço"))
                .andExpect(jsonPath("$.transacoes[0].empresa").value("Cantina Central"))
                .andExpect(jsonPath("$.transacoes[0].codigoCupom").value(cupom))
                .andExpect(jsonPath("$.transacoes[0].dataHora").isNotEmpty())
                .andExpect(jsonPath("$.transacoes[1].tipo").value("ENVIO_MOEDAS"))
                .andExpect(jsonPath("$.transacoes[1].entrada").value(true))
                .andExpect(jsonPath("$.transacoes[1].professor").value("Ana Paula Souza"))
                .andExpect(jsonPath("$.transacoes[1].motivo").value("Participação em aula"));
    }

    @Test
    void extratoDoProfessorMostraCreditosEEnvios() throws Exception {
        professor.getConta().creditar(CreditoSemestral.MOEDAS_POR_SEMESTRE);
        transacaoRepository.save(new CreditoSemestral(professor, "2026-2"));
        enviar(comoProfessor(), aluno.getId(), 40, "Participação em aula").andExpect(status().isCreated());

        mockMvc.perform(get("/api/transacoes/extrato").with(comoProfessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAtual").value(1060))
                .andExpect(jsonPath("$.transacoes", hasSize(2)))
                .andExpect(jsonPath("$.transacoes[0].tipo").value("ENVIO_MOEDAS"))
                .andExpect(jsonPath("$.transacoes[0].entrada").value(false))
                .andExpect(jsonPath("$.transacoes[0].aluno").value("Joana Silva"))
                .andExpect(jsonPath("$.transacoes[0].motivo").value("Participação em aula"))
                .andExpect(jsonPath("$.transacoes[1].tipo").value("CREDITO_SEMESTRAL"))
                .andExpect(jsonPath("$.transacoes[1].entrada").value(true))
                .andExpect(jsonPath("$.transacoes[1].semestre").value("2026-2"))
                .andExpect(jsonPath("$.transacoes[1].valor").value(1000));
    }

    @Test
    void cadaUsuarioVeApenasOProprioExtrato() throws Exception {
        Aluno outro = alunoRepository.save(DadosDeTeste.aluno("outro@email.com", "98765432100", professor.getInstituicao()));
        enviar(comoProfessor(), aluno.getId(), 40, "Participação em aula").andExpect(status().isCreated());

        mockMvc.perform(get("/api/transacoes/extrato").with(DadosDeTeste.autenticadoComo(outro, jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAtual").value(0))
                .andExpect(jsonPath("$.transacoes", hasSize(0)));
    }

    @Test
    void empresaNaoTemExtrato() throws Exception {
        mockMvc.perform(get("/api/transacoes/extrato").with(DadosDeTeste.autenticadoComo(empresa, jwtService)))
                .andExpect(status().isForbidden());
    }

    private String resgatarERetornarCupom() throws Exception {
        String resposta = mockMvc.perform(post("/api/transacoes/resgates/{id}", vantagem.getId()).with(comoAluno()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("codigoCupom").asText();
    }

    private ResultActions enviar(RequestPostProcessor usuario, Long alunoId, int valor, String motivo) throws Exception {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("alunoId", alunoId);
        corpo.put("valor", valor);
        corpo.put("motivo", motivo);
        return mockMvc.perform(post("/api/transacoes/envios").with(usuario).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(corpo)));
    }

    private RequestPostProcessor comoProfessor() {
        return DadosDeTeste.autenticadoComo(professor, jwtService);
    }

    private RequestPostProcessor comoAluno() {
        return DadosDeTeste.autenticadoComo(aluno, jwtService);
    }

    private Professor professorDe(String email) {
        return professorRepository.findAll().stream().filter(p -> p.getEmail().equals(email)).findFirst().orElseThrow();
    }
}
