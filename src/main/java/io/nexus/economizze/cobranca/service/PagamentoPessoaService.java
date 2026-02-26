package io.nexus.economizze.cobranca.service;

import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.cartao.service.CartaoService;
import io.nexus.economizze.cobranca.dto.PagamentoPessoaForm;
import io.nexus.economizze.cobranca.model.PagamentoPessoa;
import io.nexus.economizze.cobranca.model.TipoMovimentoPessoa;
import io.nexus.economizze.cobranca.repository.PagamentoPessoaRepository;
import io.nexus.economizze.pessoa.model.Pessoa;
import io.nexus.economizze.pessoa.service.PessoaService;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.util.NumeroUtil;
import io.nexus.economizze.shared.util.TextoUtil;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para pagamentos, ajustes e encargos de pessoas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoPessoaService {

    private final PagamentoPessoaRepository pagamentoPessoaRepository;
    private final PessoaService pessoaService;
    private final CartaoService cartaoService;

    /**
     * Lista movimentos com opcao de incluir inativos.
     */
    @Transactional(readOnly = true)
    public List<PagamentoPessoa> listar(boolean incluirInativos) {
        return incluirInativos ? pagamentoPessoaRepository.findAllByOrderByDataPagamentoDescDescricaoAsc()
                : pagamentoPessoaRepository.findAllByAtivoTrueOrderByDataPagamentoDescDescricaoAsc();
    }

    /**
     * Busca movimento por id.
     */
    @Transactional(readOnly = true)
    public PagamentoPessoa buscarPorId(Long id) {
        return pagamentoPessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimento de pagamento nao encontrado"));
    }

    /**
     * Cria movimento financeiro para pessoa/cartao.
     */
    @Transactional
    public PagamentoPessoa criar(PagamentoPessoaForm form) {
        PagamentoPessoa movimento = new PagamentoPessoa();
        preencherCampos(movimento, form);

        PagamentoPessoa salvo = pagamentoPessoaRepository.save(movimento);
        log.info("Movimento criado para pessoa {} tipo {}", salvo.getPessoa().getNome(), salvo.getTipo());
        return salvo;
    }

    /**
     * Atualiza movimento financeiro existente.
     */
    @Transactional
    public PagamentoPessoa atualizar(Long id, PagamentoPessoaForm form) {
        PagamentoPessoa movimento = buscarPorId(id);
        preencherCampos(movimento, form);

        PagamentoPessoa salvo = pagamentoPessoaRepository.save(movimento);
        log.info("Movimento atualizado para pessoa {} tipo {}", salvo.getPessoa().getNome(), salvo.getTipo());
        return salvo;
    }

    /**
     * Desativa movimento sem remover historico.
     */
    @Transactional
    public void desativar(Long id) {
        PagamentoPessoa movimento = buscarPorId(id);
        movimento.setAtivo(Boolean.FALSE);
        pagamentoPessoaRepository.save(movimento);
        log.warn("Movimento desativado: {}", movimento.getDescricao());
    }

    /**
     * Exclui movimento definitivamente.
     */
    @Transactional
    public void excluir(Long id) {
        PagamentoPessoa movimento = buscarPorId(id);
        pagamentoPessoaRepository.delete(movimento);
        log.warn("Movimento excluido: {}", movimento.getDescricao());
    }

    /**
     * Lista movimentos de uma pessoa ate a competencia informada.
     */
    @Transactional(readOnly = true)
    public List<PagamentoPessoa> listarMovimentosAteCompetenciaPorPessoa(Long pessoaId, YearMonth competenciaLimite) {
        return pagamentoPessoaRepository.findAllByAtivoTrueAndPessoaIdOrderByDataPagamentoAsc(pessoaId)
                .stream()
                .filter(movimento -> !YearMonth.from(movimento.getDataPagamento()).isAfter(competenciaLimite))
                .toList();
    }

    /**
     * Informa se o tipo de movimento reduz saldo devedor.
     */
    public boolean isTipoCredito(TipoMovimentoPessoa tipo) {
        return tipo == TipoMovimentoPessoa.PAGAMENTO || tipo == TipoMovimentoPessoa.AJUSTE_CREDITO;
    }

    /**
     * Informa se o tipo de movimento aumenta saldo devedor.
     */
    public boolean isTipoDebito(TipoMovimentoPessoa tipo) {
        return tipo == TipoMovimentoPessoa.ENCARGO_JUROS
                || tipo == TipoMovimentoPessoa.ENCARGO_MULTA
                || tipo == TipoMovimentoPessoa.AJUSTE_DEBITO;
    }

    /**
     * Copia dados do formulario para entidade persistente.
     */
    private void preencherCampos(PagamentoPessoa movimento, PagamentoPessoaForm form) {
        Pessoa pessoa = pessoaService.buscarPorId(form.getPessoaId());
        Cartao cartao = cartaoService.buscarPorId(form.getCartaoId());

        movimento.setPessoa(pessoa);
        movimento.setCartao(cartao);
        movimento.setDescricao(TextoUtil.paraMaiusculo(form.getDescricao()));
        movimento.setTipo(form.getTipo());
        movimento.setValor(NumeroUtil.parseMonetario(form.getValor()));
        movimento.setDataPagamento(form.getDataPagamento());
    }
}
