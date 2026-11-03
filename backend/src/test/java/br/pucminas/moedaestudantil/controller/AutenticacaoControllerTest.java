package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.DadosDeTeste;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import br.pucminas.moedaestudantil.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Login (UC01) e acesso às rotas protegidas (RN12). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"h2", "teste"})
@Transactional
class AutenticacaoControllerTest {

    private static final String ORIGEM_FRONTEND = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private EmpresaParceiraRepository empresaParceiraRepository;
    @Autowired
    private InstituicaoEnsinoRepository instituicaoEnsinoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Value("${app.jwt.secret}")
    private String segredo;

    @Test
    void professorDaCargaInicialEntraComASenhaInicial() throws Exception {
        String resposta = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana.souza@pucminas.br", "professor123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Ana Paula Souza"))
                .andExpect(jsonPath("$.perfil").value("PROFESSOR"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(resposta).get("token").asText();
        mockMvc.perform(get("/api/transacoes/extrato").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void alunoEEmpresaEntramComOPerfilCorreto() throws Exception {
        Aluno aluno = DadosDeTeste.aluno("joana@email.com", "12345678909", instituicaoEnsinoRepository.findAll().getFirst());
        aluno.setSenha(passwordEncoder.encode("segredo1"));
        alunoRepository.save(aluno);
        EmpresaParceira empresa = DadosDeTeste.empresa("contato@cantina.com", "11222333000181");
        empresa.setSenha(passwordEncoder.encode("segredo2"));
        empresaParceiraRepository.save(empresa);

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("JOANA@email.com ", "segredo1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ALUNO"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("contato@cantina.com", "segredo2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("EMPRESA"));
    }

    @Test
    void senhaErradaEEmailInexistenteRecebemAMesmaMensagem() throws Exception {
        String mensagem = "E-mail ou senha incorretos.";

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana.souza@pucminas.br", "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value(mensagem));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ninguem@email.com", "professor123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value(mensagem));
    }

    @Test
    void usuarioDesativadoNaoConsegueEntrar() throws Exception {
        Aluno aluno = DadosDeTeste.aluno("joana@email.com", "12345678909", instituicaoEnsinoRepository.findAll().getFirst());
        aluno.setSenha(passwordEncoder.encode("segredo1"));
        aluno.desativar();
        alunoRepository.save(aluno);

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("joana@email.com", "segredo1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSemCamposRetornaErrosPorCampo() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").value("Informe o e-mail."))
                .andExpect(jsonPath("$.campos.senha").value("Informe a senha."));
    }

    @Test
    void tokenInvalidoOuAdulteradoNaoAutentica() throws Exception {
        Aluno aluno = alunoRepository.save(DadosDeTeste.aluno("joana@email.com", "12345678909",
                instituicaoEnsinoRepository.findAll().getFirst()));
        String token = jwtService.gerarToken(aluno);
        String adulterado = token.substring(0, token.lastIndexOf('.') + 1) + "assinaturaFalsa";

        mockMvc.perform(get("/api/transacoes/extrato").header(HttpHeaders.AUTHORIZATION, "Bearer lixo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Faça login para acessar este recurso."));
        mockMvc.perform(get("/api/transacoes/extrato").header(HttpHeaders.AUTHORIZATION, "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoNaoAutentica() throws Exception {
        Aluno aluno = alunoRepository.save(DadosDeTeste.aluno("joana@email.com", "12345678909",
                instituicaoEnsinoRepository.findAll().getFirst()));
        String expirado = new JwtService(segredo, -1).gerarToken(aluno);

        assertThat(jwtService.validarToken(expirado)).isEmpty();
        mockMvc.perform(get("/api/transacoes/extrato").header(HttpHeaders.AUTHORIZATION, "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAntigoNaoImpedeRotasPublicas() throws Exception {
        mockMvc.perform(get("/api/instituicoes").header(HttpHeaders.AUTHORIZATION, "Bearer token-vencido"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login").header(HttpHeaders.AUTHORIZATION, "Bearer token-vencido")
                        .contentType(MediaType.APPLICATION_JSON).content(json("ana.souza@pucminas.br", "professor123")))
                .andExpect(status().isOk());
    }

    @Test
    void frontendPodeEnviarOTokenEntreOrigens() throws Exception {
        mockMvc.perform(options("/api/transacoes/extrato")
                        .header(HttpHeaders.ORIGIN, ORIGEM_FRONTEND)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_FRONTEND));
    }

    private String json(String email, String senha) throws Exception {
        return objectMapper.writeValueAsString(Map.of("email", email, "senha", senha));
    }
}
