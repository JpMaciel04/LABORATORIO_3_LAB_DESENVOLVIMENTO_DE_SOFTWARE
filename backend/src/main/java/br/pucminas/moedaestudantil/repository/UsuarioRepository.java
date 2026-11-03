package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Usuários desativados não conseguem mais entrar no sistema (US03). */
    Optional<Usuario> findByEmailAndAtivoTrue(String email);

    /** Consultado a cada requisição: um token continua válido só enquanto o usuário existir e estiver ativo. */
    boolean existsByIdAndAtivoTrue(Long id);

    /** O e-mail é o login de todos os perfis, por isso a unicidade é verificada em toda a tabela usuario. */
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
