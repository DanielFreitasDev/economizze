package io.nexus.economizze.usuario.service;

import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.util.TextoUtil;
import io.nexus.economizze.usuario.dto.AlterarSenhaForm;
import io.nexus.economizze.usuario.dto.SetupAdminForm;
import io.nexus.economizze.usuario.dto.UsuarioForm;
import io.nexus.economizze.usuario.model.PerfilUsuario;
import io.nexus.economizze.usuario.model.Usuario;
import io.nexus.economizze.usuario.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para gestao de usuarios e credenciais.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Verifica se existe ao menos um usuario cadastrado no sistema.
     */
    @Transactional(readOnly = true)
    public boolean existeUsuarios() {
        return usuarioRepository.count() > 0;
    }

    /**
     * Lista usuarios ordenados por nome para exibicao administrativa.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllByOrderByNomeCompletoAsc();
    }

    /**
     * Busca usuario por id com validacao de existencia.
     */
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));
    }

    /**
     * Cria o primeiro administrador no setup inicial.
     */
    @Transactional
    public Usuario criarPrimeiroAdmin(SetupAdminForm form) {
        if (existeUsuarios()) {
            throw new RegraDeNegocioException("O primeiro administrador ja foi cadastrado");
        }

        if (!form.getSenha().equals(form.getConfirmarSenha())) {
            throw new RegraDeNegocioException("As senhas informadas nao conferem");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(TextoUtil.paraMaiusculo(form.getNomeCompleto()));
        usuario.setUsername(TextoUtil.paraMaiusculo(form.getUsername()));
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        usuario.setPerfil(PerfilUsuario.ADMIN);

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Primeiro administrador criado com sucesso: {}", salvo.getUsername());
        return salvo;
    }

    /**
     * Cria um novo usuario administrativo com perfil selecionado.
     */
    @Transactional
    public Usuario criar(UsuarioForm form) {
        validarDuplicidadeUsername(form.getUsername(), null);

        if (form.getSenha() == null || form.getSenha().isBlank()) {
            throw new RegraDeNegocioException("Senha e obrigatoria para novo usuario");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(TextoUtil.paraMaiusculo(form.getNomeCompleto()));
        usuario.setUsername(TextoUtil.paraMaiusculo(form.getUsername()));
        usuario.setPerfil(form.getPerfil());
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuario criado: {} com perfil {}", salvo.getUsername(), salvo.getPerfil());
        return salvo;
    }

    /**
     * Atualiza dados cadastrais de usuario sem trocar senha.
     */
    @Transactional
    public Usuario atualizar(Long id, UsuarioForm form) {
        Usuario usuario = buscarPorId(id);
        validarDuplicidadeUsername(form.getUsername(), id);

        usuario.setNomeCompleto(TextoUtil.paraMaiusculo(form.getNomeCompleto()));
        usuario.setUsername(TextoUtil.paraMaiusculo(form.getUsername()));
        usuario.setPerfil(form.getPerfil());

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuario atualizado: {}", salvo.getUsername());
        return salvo;
    }

    /**
     * Atualiza a senha de um usuario especifico.
     */
    @Transactional
    public void alterarSenha(Long id, AlterarSenhaForm form) {
        if (!form.getNovaSenha().equals(form.getConfirmarSenha())) {
            throw new RegraDeNegocioException("As senhas informadas nao conferem");
        }

        Usuario usuario = buscarPorId(id);
        usuario.setSenha(passwordEncoder.encode(form.getNovaSenha()));
        usuarioRepository.save(usuario);
        log.warn("Senha alterada para o usuario {}", usuario.getUsername());
    }

    /**
     * Desativa usuario para bloquear autenticacao sem remover historico.
     */
    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(Boolean.FALSE);
        usuarioRepository.save(usuario);
        log.warn("Usuario desativado: {}", usuario.getUsername());
    }

    /**
     * Remove usuario definitivamente quando permitido.
     */
    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
        log.warn("Usuario excluido: {}", usuario.getUsername());
    }

    /**
     * Busca usuario por login para exibir no contexto autenticado.
     */
    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario logado nao encontrado"));
    }

    /**
     * Valida duplicidade de username em cenarios de criacao e edicao.
     */
    private void validarDuplicidadeUsername(String username, Long idIgnorado) {
        String usernamePadronizado = TextoUtil.paraMaiusculo(username);

        boolean jaExiste = usuarioRepository.findByUsernameIgnoreCase(usernamePadronizado)
                .filter(usuario -> idIgnorado == null || !usuario.getId().equals(idIgnorado))
                .isPresent();

        if (jaExiste) {
            throw new RegraDeNegocioException("Ja existe usuario com este login");
        }
    }
}
