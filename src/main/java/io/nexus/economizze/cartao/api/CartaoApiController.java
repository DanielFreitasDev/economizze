package io.nexus.economizze.cartao.api;

import io.nexus.economizze.cartao.dto.CartaoForm;
import io.nexus.economizze.cartao.dto.CartaoResponse;
import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.cartao.service.CartaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
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
 * API REST de cartoes.
 */
@RestController
@RequestMapping("/api/cartoes")
@RequiredArgsConstructor
@Tag(name = "Cartoes")
public class CartaoApiController {

    private final CartaoService cartaoService;

    /**
     * Lista cartoes.
     */
    @Operation(summary = "Listar cartoes")
    @GetMapping
    public List<CartaoResponse> listar() {
        return cartaoService.listar(true).stream().map(this::toResponse).toList();
    }

    /**
     * Busca cartao por id.
     */
    @Operation(summary = "Buscar cartao por ID")
    @GetMapping("/{id}")
    public CartaoResponse buscarPorId(@PathVariable Long id) {
        return toResponse(cartaoService.buscarPorId(id));
    }

    /**
     * Lista cinco competencias disponiveis do cartao.
     */
    @Operation(summary = "Listar 5 competencias de fatura")
    @GetMapping("/{id}/competencias")
    public List<YearMonth> listarCompetencias(@PathVariable Long id) {
        return cartaoService.listarCincoCompetencias(id, LocalDate.now());
    }

    /**
     * Cria cartao.
     */
    @Operation(summary = "Criar cartao")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CartaoResponse> criar(@Valid @RequestBody CartaoForm form) {
        Cartao cartao = cartaoService.criar(form);
        return ResponseEntity.created(URI.create("/api/cartoes/" + cartao.getId())).body(toResponse(cartao));
    }

    /**
     * Atualiza cartao.
     */
    @Operation(summary = "Atualizar cartao")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CartaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody CartaoForm form) {
        return toResponse(cartaoService.atualizar(id, form));
    }

    /**
     * Desativa cartao.
     */
    @Operation(summary = "Desativar cartao")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        cartaoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exclui cartao.
     */
    @Operation(summary = "Excluir cartao")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        cartaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converte entidade para DTO.
     */
    private CartaoResponse toResponse(Cartao cartao) {
        return new CartaoResponse(
                cartao.getId(),
                cartao.getNumeroCartao(),
                cartao.getBandeira(),
                cartao.getBanco(),
                cartao.getDiaFechamento(),
                cartao.getDiaVencimento(),
                cartao.getAtivo()
        );
    }
}
