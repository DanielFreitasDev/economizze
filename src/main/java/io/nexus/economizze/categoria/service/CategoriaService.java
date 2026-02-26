package io.nexus.economizze.categoria.service;

import io.nexus.economizze.categoria.dto.CategoriaForm;
import io.nexus.economizze.categoria.model.Categoria;
import io.nexus.economizze.categoria.repository.CategoriaRepository;
import io.nexus.economizze.cobranca.repository.CobrancaPessoaRepository;
import io.nexus.economizze.lancamento.repository.LancamentoRepository;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.exception.RecursoVinculadoException;
import io.nexus.economizze.shared.util.TextoUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio de categorias.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CobrancaPessoaRepository cobrancaPessoaRepository;

    /**
     * Lista categorias com opcao de incluir inativas.
     */
    @Transactional(readOnly = true)
    public List<Categoria> listar(boolean incluirInativas) {
        return incluirInativas ? categoriaRepository.findAllByOrderByNomeAsc() : categoriaRepository.findAllByAtivoTrueOrderByNomeAsc();
    }

    /**
     * Busca categoria por id validando existencia.
     */
    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada"));
    }

    /**
     * Cria nova categoria.
     */
    @Transactional
    public Categoria criar(CategoriaForm form) {
        String nomePadronizado = TextoUtil.paraMaiusculo(form.getNome());
        if (categoriaRepository.existsByNomeIgnoreCase(nomePadronizado)) {
            throw new RegraDeNegocioException("Ja existe categoria com este nome");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nomePadronizado);
        categoria.setDescricao(TextoUtil.paraMaiusculo(form.getDescricao()));

        Categoria salva = categoriaRepository.save(categoria);
        log.info("Categoria criada: {}", salva.getNome());
        return salva;
    }

    /**
     * Atualiza categoria existente.
     */
    @Transactional
    public Categoria atualizar(Long id, CategoriaForm form) {
        Categoria categoria = buscarPorId(id);

        String nomePadronizado = TextoUtil.paraMaiusculo(form.getNome());
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nomePadronizado, id)) {
            throw new RegraDeNegocioException("Ja existe categoria com este nome");
        }

        categoria.setNome(nomePadronizado);
        categoria.setDescricao(TextoUtil.paraMaiusculo(form.getDescricao()));

        Categoria salva = categoriaRepository.save(categoria);
        log.info("Categoria atualizada: {}", salva.getNome());
        return salva;
    }

    /**
     * Desativa categoria para impedir novo uso mantendo historico.
     */
    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscarPorId(id);
        categoria.setAtivo(Boolean.FALSE);
        categoriaRepository.save(categoria);
        log.warn("Categoria desativada: {}", categoria.getNome());
    }

    /**
     * Exclui categoria sem relacionamento.
     */
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);
        if (lancamentoRepository.existsByCategoriaId(id) || cobrancaPessoaRepository.existsByCategoriaId(id)) {
            throw new RecursoVinculadoException("Categoria vinculada a lancamentos/cobrancas nao pode ser excluida");
        }
        categoriaRepository.delete(categoria);
        log.warn("Categoria excluida: {}", categoria.getNome());
    }
}
