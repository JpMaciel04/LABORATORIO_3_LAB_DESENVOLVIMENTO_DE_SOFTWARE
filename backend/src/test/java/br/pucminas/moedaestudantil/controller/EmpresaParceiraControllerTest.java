package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.DadosDeTeste;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import br.pucminas.moedaestudantil.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
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
class EmpresaParceiraControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaParceiraRepository empresaParceiraRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private InstituicaoEnsinoRepository instituicaoEnsinoRepository;
    @Autowired
    private TransacaoRepository transacaoRepository;
    @Autowired
    private VantagemRepository vantagemRepository;
    @Autowired
    private JwtService jwtService;

    @Test
    void crudCompletoDeEmpresaParceira() throws Exception {
        String resposta = mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cantina Central", "11.222.333/0001-81", "contato@cantina.com", "segredo1")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.cnpj").value("11222333000181"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(resposta).get("id").asLong();
        RequestPostProcessor empresa = comoEmpresa(id);

        mockMvc.perform(get("/api/empresas/{id}", id).with(empresa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cantina Central"));

        mockMvc.perform(put("/api/empresas/{id}", id).with(empresa).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cantina Central Ltda", "11222333000181", "contato@cantina.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cantina Central Ltda"));

        mockMvc.perform(get("/api/empresas").with(empresa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Cantina Central Ltda")));

        mockMvc.perform(delete("/api/empresas/{id}", id).with(empresa)).andExpect(status().isNoContent());
        // a exclusão encerra o acesso: o token emitido antes dela deixa de valer
        mockMvc.perform(get("/api/empresas/{id}", id).with(empresa)).andExpect(status().isUnauthorized());
        assertThat(empresaParceiraRepository.existsById(id)).isFalse();
    }

    @Test
    void excluirEmpresaComResgatesFazExclusaoLogicaEDesativaVantagens() throws Exception {
        String resposta = mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cantina Central", "11222333000181", "contato@cantina.com", "segredo1")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(resposta).get("id").asLong();

        EmpresaParceira empresa = empresaParceiraRepository.findById(id).orElseThrow();
        RequestPostProcessor autenticada = DadosDeTeste.autenticadoComo(empresa, jwtService);
        Vantagem vantagem = new Vantagem(empresa, "Almoço", "Almoço no restaurante universitário", "/fotos/almoco.png", 30);
        empresa.getVantagens().add(vantagem);
        vantagemRepository.save(vantagem);
        Aluno aluno = novoAluno();
        aluno.getConta().creditar(30);
        aluno.getConta().debitar(30);
        alunoRepository.save(aluno);
        transacaoRepository.save(new ResgateVantagem(aluno, vantagem, "CUPOM-0001"));

        mockMvc.perform(delete("/api/empresas/{id}", id).with(autenticada)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/empresas/{id}", id).with(autenticada)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/empresas").with(DadosDeTeste.autenticadoComo(aluno, jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));
        EmpresaParceira excluida = empresaParceiraRepository.findById(id).orElseThrow();
        assertThat(excluida.isAtivo()).isFalse();
        assertThat(excluida.getVantagens()).allSatisfy(v -> assertThat(v.isAtiva()).isFalse());
        assertThat(transacaoRepository.findByConta(aluno.getConta())).hasSize(1);
    }

    @Test
    void cadastrarComCnpjInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cantina", "11.111.111/1111-11", "contato@cantina.com", "segredo1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cnpj").value("Informe um CNPJ válido."));
    }

    @Test
    void cadastrarComCnpjDuplicadoRetorna422() throws Exception {
        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cantina", "11222333000181", "contato@cantina.com", "segredo1")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Outra", "11222333000181", "outra@empresa.com", "segredo1")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.cnpj").exists());
    }

    @Test
    void cadastrarSemSenhaRetorna400() throws Exception {
        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cantina", "11222333000181", "contato@cantina.com", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").value("Informe a senha."));
    }

    @Test
    void semTokenNaoAcessaEmpresas() throws Exception {
        EmpresaParceira empresa = empresaParceiraRepository.save(DadosDeTeste.empresa("contato@cantina.com", "11222333000181"));

        mockMvc.perform(get("/api/empresas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/empresas/{id}", empresa.getId())).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/empresas/{id}", empresa.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void empresaNaoAcessaDadosDeOutraEmpresa() throws Exception {
        EmpresaParceira cantina = empresaParceiraRepository.save(DadosDeTeste.empresa("contato@cantina.com", "11222333000181"));
        EmpresaParceira livraria = empresaParceiraRepository.save(DadosDeTeste.empresa("contato@livraria.com", "45723174000110"));
        RequestPostProcessor comoLivraria = DadosDeTeste.autenticadoComo(livraria, jwtService);

        mockMvc.perform(get("/api/empresas/{id}", cantina.getId()).with(comoLivraria)).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/empresas/{id}", cantina.getId()).with(comoLivraria).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Invasora", "11222333000181", "contato@cantina.com", null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/empresas/{id}", cantina.getId()).with(comoLivraria)).andExpect(status().isForbidden());

        assertThat(empresaParceiraRepository.findById(cantina.getId()).orElseThrow().getNome()).isEqualTo("Cantina Central");
    }

    @Test
    void alunoListaEmpresasMasNaoAlteraOsDados() throws Exception {
        EmpresaParceira empresa = empresaParceiraRepository.save(DadosDeTeste.empresa("contato@cantina.com", "11222333000181"));
        RequestPostProcessor comoAluno = DadosDeTeste.autenticadoComo(alunoRepository.save(novoAluno()), jwtService);

        mockMvc.perform(get("/api/empresas").with(comoAluno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(empresa.getId().intValue())));
        mockMvc.perform(delete("/api/empresas/{id}", empresa.getId()).with(comoAluno)).andExpect(status().isForbidden());
    }

    private RequestPostProcessor comoEmpresa(long id) {
        return DadosDeTeste.autenticadoComo(empresaParceiraRepository.findById(id).orElseThrow(), jwtService);
    }

    private Aluno novoAluno() {
        return DadosDeTeste.aluno("joana@email.com", "12345678909", instituicaoEnsinoRepository.findAllByOrderByNomeAsc().getFirst());
    }

    private String json(String nome, String cnpj, String email, String senha) throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", nome);
        corpo.put("cnpj", cnpj);
        corpo.put("email", email);
        corpo.put("senha", senha);
        return objectMapper.writeValueAsString(corpo);
    }
}
