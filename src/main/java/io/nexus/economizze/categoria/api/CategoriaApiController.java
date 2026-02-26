package io.nexus.economizze.categoria.api;

import io.nexus.economizze.categoria.dto.CategoriaForm;
import io.nexus.economizze.categoria.dto.CategoriaResponse;
import io.nexus.economizze.categoria.model.Categoria;
import io.nexus.economizze.categoria.service.CategoriaService;
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
 * API REST de categorias.
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias")
public class CategoriaApiController {

    private final CategoriaService categoriaService;

    /**
     * Lista categorias.
     */
    @Operation(summary = "Listar categorias")
    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar(true).stream().map(this::toResponse).toList();
    }

    /**
     * Busca categoria por id.
     */
    @Operation(summary = "Buscar categoria por ID")
    @GetMapping("/{id}")
    public CategoriaResponse buscarPorId(@PathVariable Long id) {
        return toResponse(categoriaService.buscarPorId(id));
    }

    /**
     * Cria categoria.
     */
    @Operation(summary = "Criar categoria")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaForm form) {
        Categoria categoria = categoriaService.criar(form);
        return ResponseEntity.created(URI.create("/api/categorias/" + categoria.getId())).body(toResponse(categoria));
    }

    /**
     * Atualiza categoria.
     */
    @Operation(summary = "Atualizar categoria")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaForm form) {
        return toResponse(categoriaService.atualizar(id, form));
    }

    /**
     * Desativa categoria.
     */
    @Operation(summary = "Desativar categoria")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        categoriaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exclui categoria.
     */
    @Operation(summary = "Excluir categoria")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converte entidade para DTO.
     */
    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao(), categoria.getAtivo());
    }
}
