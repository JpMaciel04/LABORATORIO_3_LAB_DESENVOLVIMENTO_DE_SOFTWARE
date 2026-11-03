package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Emite e valida os tokens JWT assinados com HMAC. */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String CLAIM_PERFIL = "perfil";

    private final SecretKey chave;
    private final Duration validade;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.chave = segredo.isBlank() ? gerarChaveAleatoria() : Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.validade = Duration.ofMinutes(expiracaoMinutos);
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade)))
                .signWith(chave)
                .compact();
    }

    /** Vazio quando o token é inválido, foi adulterado ou expirou. */
    public Optional<UsuarioAutenticado> validarToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new UsuarioAutenticado(Long.valueOf(claims.getSubject()),
                    Perfil.valueOf(claims.get(CLAIM_PERFIL, String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Nenhum segredo fica versionado: sem JWT_SECRET, os tokens valem apenas até a aplicação reiniciar. */
    private static SecretKey gerarChaveAleatoria() {
        log.warn("JWT_SECRET não definido: usando uma chave aleatória; os logins serão perdidos ao reiniciar a aplicação.");
        return Jwts.SIG.HS256.key().build();
    }
}
