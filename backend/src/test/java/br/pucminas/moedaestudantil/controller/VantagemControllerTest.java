package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.DadosDeTeste;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.FotoRepository;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import br.pucminas.moedaestudantil.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Manutenção de vantagens pela empresa (UC05) e catálogo do aluno (UC08). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"h2", "teste"})
@Transactional
class VantagemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaParceiraRepository empresaParceiraRepository;
    @Autowired
    private VantagemRepository vantagemRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private InstituicaoEnsinoRepository instituicaoEnsinoRepository;
    @Autowired
    private FotoRepository fotoRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JwtService jwtService;

    private EmpresaParceira cantina;
    private RequestPostProcessor comoCantina;

    @BeforeEach
    void setUp() {
        cantina = empresaParceiraRepository.save(DadosDeTeste.empresa("contato@cantina.com", "11222333000181"));
        comoCantina = DadosDeTeste.autenticadoComo(cantina, jwtService);
    }

    @Test
    void empresaCadastraVantagemComFotoPublica() throws Exception {
        String resposta = mockMvc.perform(multipart("/api/vantagens")
                        .file(dados("Almoço", "Almoço no restaurante", 30))
                        .file(DadosDeTeste.foto(DadosDeTeste.BYTES_PNG))
                        .with(comoCantina))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Almoço"))
                .andExpect(jsonPath("$.custoMoedas").value(30))
                .andExpect(jsonPath("$.ativa").value(true))
                .andExpect(jsonPath("$.empresa").value("Cantina Central"))
                .andExpect(jsonPath("$.fotoUrl", startsWith("/api/fotos/")))
                .andExpect(jsonPath("$.fotoUrl", endsWith(".png")))
                .andReturn().getResponse().getContentAsString();

        // a tag <img> não envia o token, então a foto é acessível sem login
        String fotoUrl = objectMapper.readTree(resposta).get("fotoUrl").asText();
        mockMvc.perform(get(fotoUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(DadosDeTeste.BYTES_PNG));
    }

    @Test
    void fotoFicaGravadaNoBancoEATrocaRemoveAAnterior() throws Exception {
        String resposta = mockMvc.perform(multipart("/api/vantagens")
                        .file(dados("Almoço", "Almoço no restaurante", 30))
                        .file(DadosDeTeste.foto(DadosDeTeste.BYTES_PNG))
                        .with(comoCantina))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(resposta).get("id").asLong();
        String fotoAnterior = objectMapper.readTree(resposta).get("fotoUrl").asText();
        assertThat(fotoRepository.existsById(fotoAnterior.substring("/api/fotos/".length()))).isTrue();

        resposta = mockMvc.perform(multipart(HttpMethod.PUT, "/api/vantagens/{id}", id)
                        .file(dados("Almoço", "Almoço no restaurante", 30))
                        .file(DadosDeTeste.foto(DadosDeTeste.BYTES_JPG))
                        .with(comoCantina))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String fotoNova = objectMapper.readTree(resposta).get("fotoUrl").asText();
        // fora do teste, cada requisição tem a sua transação e não enxerga a foto já removida do banco
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get(fotoNova)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_JPEG));
        mockMvc.perform(get(fotoAnterior)).andExpect(status().isNotFound());
    }

    @Test
    void fotosDoPerfilDemoSaoServidasDeDentroDoJar() throws Exception {
        mockMvc.perform(get("/api/fotos/demo-almoco.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void fotoInexistenteOuComNomeInvalidoRetorna404() throws Exception {
        mockMvc.perform(get("/api/fotos/nao-existe.png")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/fotos/application.properties")).andExpect(status().isNotFound());
        // a barra codificada nem chega ao controller: o firewall do Spring Security recusa a URL
        mockMvc.perform(get("/api/fotos/..%2Fapplication.properties")).andExpect(status().isBadRequest());
    }

    @Test
    void fotoJpgTambemEAceita() throws Exception {
        mockMvc.perform(multipart("/api/vantagens")
                        .file(dados("Almoço", "Almoço no restaurante", 30))
                        .file(DadosDeTeste.foto(DadosDeTeste.BYTES_JPG))
                        .with(comoCantina))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fotoUrl", endsWith(".jpg")));
    }

    @Test
    void cadastrarSemFotoRetorna422() throws Exception {
        mockMvc.perform(multipart("/api/vantagens")
                        .file(dados("Almoço", "Almoço no restaurante", 30))
                        .with(comoCantina))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.foto").value("Envie a foto da vantagem em JPG ou PNG."));
    }

    @Test
    void arquivoQueNaoEImagemERecusadoMesmoComNomeEContentTypeDeImagem() throws Exception {
        byte[] html = "<script>alert('oi')</script>".getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(multipart("/api/vantagens")
                        .file(dados("Almoço", "Almoço no restaurante", 30))
                        .file(DadosDeTeste.foto(html))
                        .with(comoCantina))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.foto").value("A foto deve estar em formato JPG ou PNG."));
    }

    @Test
    void camposInvalidosRetornamErrosPorCampo() throws Exception {
        mockMvc.perform(multipart("/api/vantagens")
                        .file(dados("", "", 0))
                        .file(DadosDeTeste.foto(DadosDeTeste.BYTES_PNG))
                        .with(comoCantina))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value("Informe o nome da vantagem."))
                .andExpect(jsonPath("$.campos.descricao").value("Informe a descrição."))
                .andExpect(jsonPath("$.campos.custoMoedas").value("O custo deve ser um número inteiro positivo."));
    }

    @Test
    void custoFracionadoERecusadoEmVezDeTruncado() throws Exception {
        MockMultipartFile dados = new MockMultipartFile("dados", "", "application/json",
                "{\"nome\":\"Almoço\",\"descricao\":\"Almoço\",\"custoMoedas\":10.5}".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/vantagens").file(dados).file(DadosDeTeste.foto(DadosDeTeste.BYTES_PNG)).with(comoCantina))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alunoVeSoVantagensAtivasEMinhasVantagensMostraTodas() throws Exception {
        Vantagem ativa = vantagemRepository.save(new Vantagem(cantina, "Almoço", "Almoço", "/api/fotos/a.png", 30));
        Vantagem inativa = vantagemRepository.save(new Vantagem(cantina, "Café", "Café", "/api/fotos/b.png", 5));
        inativa.desativar();

        mockMvc.perform(get("/api/vantagens").with(DadosDeTeste.autenticadoComo(novoAluno(), jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(ativa.getId().intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(inativa.getId().intValue()))));

        mockMvc.perform(get("/api/vantagens/minhas").with(comoCantina))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void alterarSemNovaFotoMantemAFotoAtual() throws Exception {
        Vantagem vantagem = vantagemRepository.save(new Vantagem(cantina, "Almoço", "Almoço", "/api/fotos/a.png", 30));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/vantagens/{id}", vantagem.getId())
                        .file(dados("Almoço completo", "Com sobremesa", 40))
                        .with(comoCantina))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Almoço completo"))
                .andExpect(jsonPath("$.custoMoedas").value(40))
                .andExpect(jsonPath("$.fotoUrl").value("/api/fotos/a.png"));
    }

    @Test
    void alterarComNovaFotoSubstituiAUrl() throws Exception {
        Vantagem vantagem = vantagemRepository.save(new Vantagem(cantina, "Almoço", "Almoço", "/api/fotos/a.png", 30));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/vantagens/{id}", vantagem.getId())
                        .file(dados("Almoço", "Almoço", 30))
                        .file(DadosDeTeste.foto(DadosDeTeste.BYTES_JPG))
                        .with(comoCantina))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotoUrl", endsWith(".jpg")));
    }

    @Test
    void excluirDesativaAVantagemSemApagar() throws Exception {
        Vantagem vantagem = vantagemRepository.save(new Vantagem(cantina, "Almoço", "Almoço", "/api/fotos/a.png", 30));

        mockMvc.perform(delete("/api/vantagens/{id}", vantagem.getId()).with(comoCantina)).andExpect(status().isNoContent());

        assertThat(vantagemRepository.findById(vantagem.getId())).get()
                .satisfies(desativada -> assertThat(desativada.isAtiva()).isFalse());
    }

    @Test
    void empresaNaoAlteraVantagemDeOutraEmpresa() throws Exception {
        Vantagem vantagem = vantagemRepository.save(new Vantagem(cantina, "Almoço", "Almoço", "/api/fotos/a.png", 30));
        EmpresaParceira livraria = empresaParceiraRepository.save(DadosDeTeste.empresa("contato@livraria.com", "45723174000110"));
        RequestPostProcessor comoLivraria = DadosDeTeste.autenticadoComo(livraria, jwtService);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/vantagens/{id}", vantagem.getId())
                        .file(dados("Invadida", "Invadida", 1))
                        .with(comoLivraria))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/vantagens/{id}", vantagem.getId()).with(comoLivraria)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/vantagens/{id}", vantagem.getId()).with(comoLivraria)).andExpect(status().isNotFound());

        assertThat(vantagem.getNome()).isEqualTo("Almoço");
        assertThat(vantagem.isAtiva()).isTrue();
    }

    @Test
    void alunoNaoGerenciaVantagensEVisitanteNaoVeOCatalogo() throws Exception {
        Vantagem vantagem = vantagemRepository.save(new Vantagem(cantina, "Almoço", "Almoço", "/api/fotos/a.png", 30));
        RequestPostProcessor comoAluno = DadosDeTeste.autenticadoComo(novoAluno(), jwtService);

        mockMvc.perform(multipart("/api/vantagens")
                        .file(dados("Almoço", "Almoço", 30))
                        .file(DadosDeTeste.foto(DadosDeTeste.BYTES_PNG))
                        .with(comoAluno))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/vantagens/{id}", vantagem.getId()).with(comoAluno)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/vantagens")).andExpect(status().isUnauthorized());
    }

    private Aluno novoAluno() {
        return alunoRepository.save(DadosDeTeste.aluno("joana@email.com", "12345678909",
                instituicaoEnsinoRepository.findAllByOrderByNomeAsc().getFirst()));
    }

    private MockMultipartFile dados(String nome, String descricao, int custoMoedas) throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", nome);
        corpo.put("descricao", descricao);
        corpo.put("custoMoedas", custoMoedas);
        return new MockMultipartFile("dados", "", "application/json", objectMapper.writeValueAsBytes(corpo));
    }
}
