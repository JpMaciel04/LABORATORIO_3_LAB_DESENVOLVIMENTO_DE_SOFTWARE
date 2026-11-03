package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.DadosDeTeste;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"h2", "teste"})
@Transactional
class AlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private InstituicaoEnsinoRepository instituicaoEnsinoRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private EmpresaParceiraRepository empresaParceiraRepository;
    @Autowired
    private TransacaoRepository transacaoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private Long instituicaoId;

    @BeforeEach
    void setUp() {
        instituicaoId = instituicaoEnsinoRepository.findAllByOrderByNomeAsc().getFirst().getId();
    }

    @Test
    void cadastrarCriaAlunoComSaldoZeroESenhaCriptografada() throws Exception {
        mockMvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Joana Silva", "Joana@Email.com", "segredo1", "123.456.789-09", instituicaoId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("joana@email.com"))
                .andExpect(jsonPath("$.cpf").value("12345678909"))
                .andExpect(jsonPath("$.endereco.cep").value("30535901"))
                .andExpect(jsonPath("$.instituicao.id").value(instituicaoId))
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.senha").doesNotExist());

        Aluno salvo = alunoRepository.findAll().stream()
                .filter(a -> a.getCpf().equals("12345678909")).findFirst().orElseThrow();
        assertThat(salvo.getSenha()).isNotEqualTo("segredo1");
        assertThat(passwordEncoder.matches("segredo1", salvo.getSenha())).isTrue();
    }

    @Test
    void cadastrarComCamposInvalidosRetornaErrosPorCampo() throws Exception {
        mockMvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "email-invalido", null, "111.111.111-11", instituicaoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").value("Informe a senha."))
                .andExpect(jsonPath("$.campos.cpf").value("Informe um CPF válido."));
    }

    @Test
    void campoVazioMostraMensagemDeObrigatorioEmVezDeFormato() throws Exception {
        mockMvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Joana Silva", "joana@email.com", "segredo1", "", instituicaoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").value("Informe o CPF."));
    }

    @Test
    void cadastrarComCpfDuplicadoRetorna422() throws Exception {
        cadastrar("Joana Silva", "joana@email.com", "12345678909");

        mockMvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Outra Pessoa", "outra@email.com", "segredo1", "123.456.789-09", instituicaoId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.cpf").value("Já existe um aluno cadastrado com este CPF."));
    }

    @Test
    void cadastrarComEmailDeOutroUsuarioRetorna422() throws Exception {
        // e-mail de um professor da carga inicial: o login é único entre todos os perfis
        mockMvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Joana Silva", "ana.souza@pucminas.br", "segredo1", "12345678909", instituicaoId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.email").value("Este e-mail já está em uso."));
    }

    @Test
    void cadastrarComInstituicaoInexistenteRetorna422() throws Exception {
        mockMvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Joana Silva", "joana@email.com", "segredo1", "12345678909", 9999L)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.instituicaoId").exists());
    }

    @Test
    void alunoConsultaOsProprioDados() throws Exception {
        long id = cadastrar("Joana Silva", "joana@email.com", "12345678909");

        mockMvc.perform(get("/api/alunos/{id}", id).with(comoAluno(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joana Silva"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void atualizarSemSenhaMantemSenhaAtual() throws Exception {
        long id = cadastrar("Joana Silva", "joana@email.com", "12345678909");
        String hashAnterior = alunoRepository.findById(id).orElseThrow().getSenha();

        mockMvc.perform(put("/api/alunos/{id}", id).with(comoAluno(id)).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Joana Souza Silva", "joana@email.com", null, "12345678909", instituicaoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joana Souza Silva"));

        assertThat(alunoRepository.findById(id).orElseThrow().getSenha()).isEqualTo(hashAnterior);
    }

    @Test
    void atualizarComCpfDeOutroAlunoRetorna422() throws Exception {
        cadastrar("Joana Silva", "joana@email.com", "12345678909");
        long id = cadastrar("Pedro Santos", "pedro@email.com", "98765432100");

        mockMvc.perform(put("/api/alunos/{id}", id).with(comoAluno(id)).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Pedro Santos", "pedro@email.com", null, "12345678909", instituicaoId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.cpf").exists());
    }

    @Test
    void excluirAlunoSemMovimentacoesRemoveDoBancoEPermiteRecadastro() throws Exception {
        long id = cadastrar("Joana Silva", "joana@email.com", "12345678909");
        RequestPostProcessor joana = comoAluno(id);

        mockMvc.perform(delete("/api/alunos/{id}", id).with(joana)).andExpect(status().isNoContent());

        assertThat(alunoRepository.existsById(id)).isFalse();
        // o token emitido antes da exclusão deixa de valer: a exclusão encerra o acesso (US03)
        mockMvc.perform(get("/api/alunos/{id}", id).with(joana)).andExpect(status().isUnauthorized());

        cadastrar("Joana Silva", "joana@email.com", "12345678909");
    }

    @Test
    void excluirAlunoComMovimentacoesFazExclusaoLogica() throws Exception {
        long id = cadastrar("Joana Silva", "joana@email.com", "12345678909");
        RequestPostProcessor joana = comoAluno(id);
        Aluno aluno = alunoRepository.findById(id).orElseThrow();
        Professor professor = professorRepository.findAll().getFirst();
        professor.getConta().creditar(100);
        professor.getConta().debitar(10);
        aluno.getConta().creditar(10);
        transacaoRepository.save(new EnvioMoedas(professor, aluno, 10, "Participação em aula"));

        mockMvc.perform(delete("/api/alunos/{id}", id).with(joana)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/alunos/{id}", id).with(joana)).andExpect(status().isUnauthorized());
        Aluno excluido = alunoRepository.findById(id).orElseThrow();
        assertThat(excluido.isAtivo()).isFalse();
        assertThat(transacaoRepository.findByConta(excluido.getConta())).hasSize(1);
    }

    @Test
    void semTokenNaoAcessaDadosDeAluno() throws Exception {
        long id = cadastrar("Joana Silva", "joana@email.com", "12345678909");

        mockMvc.perform(get("/api/alunos/{id}", id))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(delete("/api/alunos/{id}", id)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/alunos")).andExpect(status().isUnauthorized());
    }

    @Test
    void alunoNaoAcessaDadosDeOutroAluno() throws Exception {
        long joana = cadastrar("Joana Silva", "joana@email.com", "12345678909");
        long pedro = cadastrar("Pedro Santos", "pedro@email.com", "98765432100");

        mockMvc.perform(get("/api/alunos/{id}", joana).with(comoAluno(pedro)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para realizar esta operação."));
        mockMvc.perform(put("/api/alunos/{id}", joana).with(comoAluno(pedro)).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Invasor", "joana@email.com", null, "12345678909", instituicaoId)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/alunos/{id}", joana).with(comoAluno(pedro))).andExpect(status().isForbidden());

        Aluno aluno = alunoRepository.findById(joana).orElseThrow();
        assertThat(aluno.getNome()).isEqualTo("Joana Silva");
    }

    @Test
    void outrosPerfisNaoAcessamDadosDeAluno() throws Exception {
        long id = cadastrar("Joana Silva", "joana@email.com", "12345678909");
        Professor professor = professorRepository.findAll().getFirst();
        var empresa = empresaParceiraRepository.save(DadosDeTeste.empresa("contato@cantina.com", "11222333000181"));

        mockMvc.perform(get("/api/alunos/{id}", id).with(DadosDeTeste.autenticadoComo(professor, jwtService)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/alunos/{id}", id).with(DadosDeTeste.autenticadoComo(empresa, jwtService)))
                .andExpect(status().isForbidden());
    }

    @Test
    void professorListaAlunosDaSuaInstituicaoSemDadosPessoais() throws Exception {
        Professor professor = professorRepository.findAll().getFirst();
        InstituicaoEnsino instituicaoDoProfessor = professor.getInstituicao();
        InstituicaoEnsino outraInstituicao = instituicaoEnsinoRepository.findAll().stream()
                .filter(i -> !i.getId().equals(instituicaoDoProfessor.getId())).findFirst().orElseThrow();
        Aluno colega = alunoRepository.save(DadosDeTeste.aluno("colega@email.com", "12345678909", instituicaoDoProfessor));
        alunoRepository.save(DadosDeTeste.aluno("outro@email.com", "98765432100", outraInstituicao));
        Aluno excluido = alunoRepository.save(DadosDeTeste.aluno("excluido@email.com", "52998224725", instituicaoDoProfessor));
        excluido.desativar();

        mockMvc.perform(get("/api/alunos").with(DadosDeTeste.autenticadoComo(professor, jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(colega.getId()))
                .andExpect(jsonPath("$[0].curso").value("Engenharia de Software"))
                .andExpect(jsonPath("$[0].cpf").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].endereco").doesNotExist())
                .andExpect(jsonPath("$[*].id", not(hasItem(excluido.getId().intValue()))));
    }

    @Test
    void alunoNaoListaOutrosAlunos() throws Exception {
        long id = cadastrar("Joana Silva", "joana@email.com", "12345678909");

        mockMvc.perform(get("/api/alunos").with(comoAluno(id))).andExpect(status().isForbidden());
    }

    private RequestPostProcessor comoAluno(long id) {
        return DadosDeTeste.autenticadoComo(alunoRepository.findById(id).orElseThrow(), jwtService);
    }

    private long cadastrar(String nome, String email, String cpf) throws Exception {
        String resposta = mockMvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(nome, email, "segredo1", cpf, instituicaoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode corpo = objectMapper.readTree(resposta);
        return corpo.get("id").asLong();
    }

    private String json(String nome, String email, String senha, String cpf, Long instituicaoId) throws Exception {
        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("logradouro", "Rua Dom José Gaspar");
        endereco.put("numero", "500");
        endereco.put("complemento", "");
        endereco.put("bairro", "Coração Eucarístico");
        endereco.put("cidade", "Belo Horizonte");
        endereco.put("estado", "mg");
        endereco.put("cep", "30535-901");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", nome);
        corpo.put("email", email);
        corpo.put("senha", senha);
        corpo.put("cpf", cpf);
        corpo.put("rg", "MG-12.345.678");
        corpo.put("curso", "Engenharia de Software");
        corpo.put("instituicaoId", instituicaoId);
        corpo.put("endereco", endereco);
        return objectMapper.writeValueAsString(corpo);
    }
}
