package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.DadosDeTeste;
import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.service.MoedaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Perfil "nuvem" (Lab04S02) sobre um PostgreSQL 17 real, a mesma versão do Render, iniciado pelo próprio teste.
 * Subir o contexto já confere que as migrações de db/postgresql rodam e que o Hibernate valida as entidades contra
 * o esquema criado; os testes percorrem pela API os fluxos que gravam no banco, com os dados do perfil demo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"teste", "nuvem", "demo"})
@DirtiesContext
class PerfilNuvemPostgresTest {

    private static EmbeddedPostgres postgres;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private FotoRepository fotoRepository;
    @Autowired
    private MoedaService moedaService;

    /** As mesmas variáveis que o render.yaml preenche a partir do banco criado no Blueprint. */
    @DynamicPropertySource
    static void bancoPostgres(DynamicPropertyRegistry registro) throws IOException {
        if (postgres == null) {
            postgres = EmbeddedPostgres.builder().start();
        }
        registro.add("DB_HOST", () -> "localhost");
        registro.add("DB_PORT", postgres::getPort);
        registro.add("DB_NAME", () -> "postgres");
        registro.add("DB_USERNAME", () -> "postgres");
        registro.add("DB_PASSWORD", () -> "");
    }

    @Test
    void migracoesDoPostgresCriamOEsquemaEACargaInicial() {
        assertThat(jdbcTemplate.queryForObject("SELECT version()", String.class)).startsWith("PostgreSQL 17");
        assertThat(jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success AND version IS NOT NULL", String.class))
                .containsExactlyInAnyOrder("1", "2", "3", "4");
        assertThat(contar("instituicao_ensino")).isEqualTo(3);
        assertThat(contar("professor")).isEqualTo(4);
        assertThat(contar("aluno")).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vantagem WHERE foto_url LIKE '/api/fotos/demo-%'", Integer.class)).isEqualTo(5);
    }

    @Test
    void restricoesDoEsquemaValemNoPostgres() {
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE conta SET saldo = -1"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE vantagem SET custo_moedas = 0"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void professorEnviaMoedasEAlunoResgataVantagem() throws Exception {
        moedaService.creditarMoedasSemestrais(CreditoSemestral.semestreDe(LocalDate.now()));
        String professora = login("ana.souza@pucminas.br", "professor123");
        String aluno = login("pedro.santos@aluno.pucminas.br", "demo123");

        long pedroId = buscarId(json(get("/api/alunos"), professora), "nome", "Pedro Santos");
        int saldoInicial = json(get("/api/transacoes/extrato"), aluno).get("saldoAtual").asInt();

        mockMvc.perform(post("/api/transacoes/envios").header("Authorization", professora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "alunoId", pedroId, "valor", 40, "motivo", "Apresentou a implantação na nuvem."))))
                .andExpect(status().isCreated());

        long cafeId = buscarId(json(get("/api/vantagens"), aluno), "nome", "Combo café da manhã");
        JsonNode resgate = objectMapper.readTree(mockMvc.perform(post("/api/transacoes/resgates/{id}", cafeId)
                        .header("Authorization", aluno))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        assertThat(resgate.get("codigoCupom").asText()).isNotBlank();

        JsonNode extrato = json(get("/api/transacoes/extrato"), aluno);
        assertThat(extrato.get("saldoAtual").asInt()).isEqualTo(saldoInicial + 40 - 15);
        assertThat(extrato.get("transacoes").get(0).get("tipo").asText()).isEqualTo("RESGATE_VANTAGEM");
        assertThat(extrato.get("transacoes").get(1).get("motivo").asText()).isEqualTo("Apresentou a implantação na nuvem.");
    }

    @Test
    void fotoEnviadaPelaEmpresaFicaGravadaNoBanco() throws Exception {
        String cantina = login("contato@cantinacentral.com.br", "demo123");
        MockMultipartFile dados = new MockMultipartFile("dados", "", "application/json", objectMapper.writeValueAsBytes(
                Map.of("nome", "Suco natural", "descricao", "Suco de laranja de 500 ml.", "custoMoedas", 10)));

        JsonNode vantagem = objectMapper.readTree(mockMvc.perform(multipart("/api/vantagens")
                        .file(dados).file(DadosDeTeste.foto(DadosDeTeste.BYTES_PNG)).header("Authorization", cantina))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        String fotoPng = vantagem.get("fotoUrl").asText();
        assertThat(fotoRepository.existsById(fotoPng.substring(fotoPng.lastIndexOf('/') + 1))).isTrue();
        mockMvc.perform(get(fotoPng))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(DadosDeTeste.BYTES_PNG));

        String fotoJpg = objectMapper.readTree(mockMvc.perform(multipart(HttpMethod.PUT, "/api/vantagens/{id}",
                                vantagem.get("id").asLong())
                        .file(dados).file(DadosDeTeste.foto(DadosDeTeste.BYTES_JPG)).header("Authorization", cantina))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("fotoUrl").asText();
        mockMvc.perform(get(fotoJpg)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_JPEG));
        mockMvc.perform(get(fotoPng)).andExpect(status().isNotFound());

        // as fotos do perfil demo vêm de dentro do JAR, pois não há como colocá-las no banco por SQL
        mockMvc.perform(get("/api/fotos/demo-almoco.png")).andExpect(status().isOk());
    }

    /** @return o valor do cabeçalho Authorization */
    private String login(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "senha", senha))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(resposta).get("token").asText();
    }

    private JsonNode json(MockHttpServletRequestBuilder requisicao, String autorizacao) throws Exception {
        ResultActions resultado = mockMvc.perform(requisicao.header("Authorization", autorizacao))
                .andExpect(status().isOk());
        return objectMapper.readTree(resultado.andReturn().getResponse().getContentAsString());
    }

    private static long buscarId(JsonNode lista, String campo, String valor) {
        for (JsonNode item : lista) {
            if (valor.equals(item.get(campo).asText())) {
                return item.get("id").asLong();
            }
        }
        throw new AssertionError(valor + " não encontrado em " + lista);
    }

    private int contar(String tabela) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tabela, Integer.class);
    }
}
