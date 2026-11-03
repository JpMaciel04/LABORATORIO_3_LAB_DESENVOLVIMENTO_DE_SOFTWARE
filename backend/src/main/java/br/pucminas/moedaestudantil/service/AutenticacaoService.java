package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.CredenciaisInvalidasException;
import br.pucminas.moedaestudantil.model.Usuario;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import br.pucminas.moedaestudantil.security.JwtService;
import br.pucminas.moedaestudantil.security.Sessao;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Login (UC01): valida e-mail e senha e emite o token JWT (RN12). */
@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashFicticio;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFicticio = passwordEncoder.encode("usuario-inexistente");
    }

    /** E-mails desconhecidos também passam pelo BCrypt, para que o tempo de resposta não revele quais existem. */
    @Transactional(readOnly = true)
    public Sessao autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmailAndAtivoTrue(email).orElse(null);
        boolean senhaConfere = passwordEncoder.matches(senha, usuario == null ? hashFicticio : usuario.getSenha());
        if (usuario == null || !senhaConfere) {
            throw new CredenciaisInvalidasException();
        }
        return new Sessao(jwtService.gerarToken(usuario), usuario);
    }
}
