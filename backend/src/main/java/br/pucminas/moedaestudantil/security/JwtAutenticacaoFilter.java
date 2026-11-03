package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Autentica a requisição a partir do cabeçalho "Authorization: Bearer token". Sem token válido, a requisição
 * segue como anônima e as regras de SegurancaConfig decidem se ela pode prosseguir.
 */
public class JwtAutenticacaoFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAutenticacaoFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            jwtService.validarToken(cabecalho.substring(PREFIXO.length()))
                    .filter(usuario -> usuarioRepository.existsByIdAndAtivoTrue(usuario.id()))
                    .ifPresent(usuario -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.autoridades())));
        }
        filterChain.doFilter(request, response);
    }
}
