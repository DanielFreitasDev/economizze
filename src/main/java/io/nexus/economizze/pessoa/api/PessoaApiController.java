package io.nexus.economizze.pessoa.api;

import io.nexus.economizze.pessoa.dto.PessoaForm;
import io.nexus.economizze.pessoa.dto.PessoaResponse;
import io.nexus.economizze.pessoa.model.Pessoa;
import io.nexus.economizze.pessoa.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de pessoas.
 */
@RestController
@RequestMapping("/api/pessoas")
@RequiredArgsConstructor
@Tag(name = "Pessoas")
public class PessoaApiController {

    private final PessoaService pessoaService;

    /**
     * Lista pessoas cadastradas.
     */
    @Operation(summary = "Listar pessoas")
    @GetMapping
    public List<PessoaResponse> listar() {
        return pessoaService.listar(true).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Busca pessoa por id.
     */
    @Operation(summary = "Buscar pessoa por ID")
    @GetMapping("/{id}")
    public PessoaResponse buscarPorId(@PathVariable Long id) {
        return toResponse(pessoaService.buscarPorId(id));
    }

    /**
     * Cria pessoa.
     */
    @Operation(summary = "Criar pessoa")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PessoaResponse> criar(@Valid @RequestBody PessoaForm form) {
        Pessoa pessoa = pessoaService.criar(form);
        return ResponseEntity.created(URI.create("/api/pessoas/" + pessoa.getId())).body(toResponse(pessoa));
    }

    /**
     * Atualiza pessoa.
     */
    @Operation(summary = "Atualizar pessoa")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public PessoaResponse atualizar(@PathVariable Long id, @Valid @RequestBody PessoaForm form) {
        return toResponse(pessoaService.atualizar(id, form));
    }

    /**
     * Desativa pessoa.
     */
    @Operation(summary = "Desativar pessoa")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        pessoaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exclui pessoa.
     */
    @Operation(summary = "Excluir pessoa")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pessoaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converte entidade para DTO de resposta.
     */
    private PessoaResponse toResponse(Pessoa pessoa) {
        return new PessoaResponse(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getTelefoneCelular(),
                pessoa.getWhatsapp(),
                pessoa.getEmail(),
                pessoa.getAtivo()
        );
    }
}
