package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.model.Perfil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/** Identidade extraída do token JWT e disponível nos controllers via @AuthenticationPrincipal. */
public record UsuarioAutenticado(Long id, Perfil perfil) {

    public List<GrantedAuthority> autoridades() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }
}
