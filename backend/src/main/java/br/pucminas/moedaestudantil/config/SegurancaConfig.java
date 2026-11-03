package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.dto.ErroDTO;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import br.pucminas.moedaestudantil.security.JwtAutenticacaoFilter;
import br.pucminas.moedaestudantil.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Regras de acesso da API (RN12). Só o login, os auto-cadastros e o que eles precisam são públicos.
 * Os perfis são verificados aqui; a posse do registro ("só os meus dados"), nos controllers com @PreAuthorize.
 */
@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    @Bean
    public SecurityFilterChain filtrosDeSeguranca(HttpSecurity http, JwtService jwtService,
                                                  UsuarioRepository usuarioRepository, ObjectMapper objectMapper) throws Exception {
        return http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                // A API não usa cookies de sessão: o token vai no cabeçalho, o que dispensa a proteção contra CSRF.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint((request, response, excecao) -> responder(response, objectMapper,
                                HttpStatus.UNAUTHORIZED, "Faça login para acessar este recurso."))
                        .accessDeniedHandler((request, response, excecao) -> responder(response, objectMapper,
                                HttpStatus.FORBIDDEN, "Você não tem permissão para realizar esta operação.")))
                .authorizeHttpRequests(regras -> regras
                        // As fotos são públicas porque a tag <img> não envia o token.
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/alunos", "/api/empresas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/instituicoes", "/api/fotos/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/alunos").hasRole("PROFESSOR")
                        .requestMatchers("/api/alunos/**").hasRole("ALUNO")

                        .requestMatchers(HttpMethod.GET, "/api/empresas").authenticated()
                        .requestMatchers("/api/empresas/**").hasRole("EMPRESA")

                        .requestMatchers(HttpMethod.GET, "/api/vantagens").authenticated()
                        .requestMatchers("/api/vantagens/**").hasRole("EMPRESA")

                        .requestMatchers(HttpMethod.POST, "/api/transacoes/envios").hasRole("PROFESSOR")
                        .requestMatchers(HttpMethod.POST, "/api/transacoes/resgates/**").hasRole("ALUNO")
                        .requestMatchers(HttpMethod.GET, "/api/transacoes/extrato").hasAnyRole("ALUNO", "PROFESSOR")

                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAutenticacaoFilter(jwtService, usuarioRepository), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** Os erros dos filtros acontecem antes do GlobalExceptionHandler, por isso o ErroDTO é escrito aqui. */
    private static void responder(HttpServletResponse response, ObjectMapper objectMapper, HttpStatus status, String mensagem)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), new ErroDTO(status.value(), mensagem, Map.of()));
    }
}
