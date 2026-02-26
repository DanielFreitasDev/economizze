package io.nexus.economizze.usuario.service;

import io.nexus.economizze.usuario.model.Usuario;
import io.nexus.economizze.usuario.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service que integra usuarios persistidos com o mecanismo de autenticacao do Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UsuarioAutenticacaoService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carrega usuario por login para autenticar e autorizar acessos.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getSenha())
                .disabled(!Boolean.TRUE.equals(usuario.getAtivo()))
                .authorities(authorities)
                .build();
    }
}
