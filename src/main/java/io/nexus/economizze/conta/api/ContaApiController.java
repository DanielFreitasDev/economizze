package io.nexus.economizze.conta.api;

import io.nexus.economizze.conta.dto.ContaForm;
import io.nexus.economizze.conta.dto.ContaResponse;
import io.nexus.economizze.conta.model.Conta;
import io.nexus.economizze.conta.service.ContaService;
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
 * API REST de contas.
 */
@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
@Tag(name = "Contas")
public class ContaApiController {

    private final ContaService contaService;

    /**
     * Lista contas.
     */
    @Operation(summary = "Listar contas")
    @GetMapping
    public List<ContaResponse> listar() {
        return contaService.listar(true).stream().map(this::toResponse).toList();
    }

    /**
     * Busca conta por id.
     */
    @Operation(summary = "Buscar conta por ID")
    @GetMapping("/{id}")
    public ContaResponse buscarPorId(@PathVariable Long id) {
        return toResponse(contaService.buscarPorId(id));
    }

    /**
     * Cria conta.
     */
    @Operation(summary = "Criar conta")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaForm form) {
        Conta conta = contaService.criar(form);
        return ResponseEntity.created(URI.create("/api/contas/" + conta.getId())).body(toResponse(conta));
    }

    /**
     * Atualiza conta.
     */
    @Operation(summary = "Atualizar conta")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ContaResponse atualizar(@PathVariable Long id, @Valid @RequestBody ContaForm form) {
        return toResponse(contaService.atualizar(id, form));
    }

    /**
     * Desativa conta.
     */
    @Operation(summary = "Desativar conta")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        contaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exclui conta.
     */
    @Operation(summary = "Excluir conta")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        contaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converte entidade para DTO.
     */
    private ContaResponse toResponse(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getNome(),
                conta.getBanco(),
                conta.getTipoConta(),
                conta.getSaldoInicial(),
                conta.getAtivo()
        );
    }
}
