package io.nexus.economizze.cartao.service;

import io.nexus.economizze.cartao.dto.CartaoForm;
import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.cartao.repository.CartaoRepository;
import io.nexus.economizze.cobranca.repository.CobrancaPessoaRepository;
import io.nexus.economizze.cobranca.repository.PagamentoPessoaRepository;
import io.nexus.economizze.lancamento.repository.LancamentoRepository;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.exception.RecursoVinculadoException;
import io.nexus.economizze.shared.util.TextoUtil;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio de cartoes e calculo de competencias de fatura.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CobrancaPessoaRepository cobrancaPessoaRepository;
    private final PagamentoPessoaRepository pagamentoPessoaRepository;

    /**
     * Lista cartoes com opcao de incluir inativos.
     */
    @Transactional(readOnly = true)
    public List<Cartao> listar(boolean incluirInativas) {
        return incluirInativas ? cartaoRepository.findAllByOrderByBancoAscNumeroCartaoAsc() : cartaoRepository.findAllByAtivoTrueOrderByBancoAscNumeroCartaoAsc();
    }

    /**
     * Busca cartao por id com validacao.
     */
    @Transactional(readOnly = true)
    public Cartao buscarPorId(Long id) {
        return cartaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cartao nao encontrado"));
    }

    /**
     * Cria cartao com validacoes de numero e dias.
     */
    @Transactional
    public Cartao criar(CartaoForm form) {
        String numero = TextoUtil.manterSomenteDigitos(form.getNumeroCartao());
        validarNumeroCartao(numero, null);
        validarDias(form.getDiaFechamento(), form.getDiaVencimento());

        Cartao cartao = new Cartao();
        cartao.setNumeroCartao(numero);
        cartao.setBandeira(TextoUtil.paraMaiusculo(form.getBandeira()));
        cartao.setBanco(TextoUtil.paraMaiusculo(form.getBanco()));
        cartao.setDiaFechamento(form.getDiaFechamento());
        cartao.setDiaVencimento(form.getDiaVencimento());

        Cartao salvo = cartaoRepository.save(cartao);
        log.info("Cartao criado no banco {} terminando em {}", salvo.getBanco(), ultimosQuatro(salvo.getNumeroCartao()));
        return salvo;
    }

    /**
     * Atualiza cartao existente mantendo rastreabilidade de configuracao.
     */
    @Transactional
    public Cartao atualizar(Long id, CartaoForm form) {
        Cartao cartao = buscarPorId(id);

        String numero = TextoUtil.manterSomenteDigitos(form.getNumeroCartao());
        validarNumeroCartao(numero, id);
        validarDias(form.getDiaFechamento(), form.getDiaVencimento());

        cartao.setNumeroCartao(numero);
        cartao.setBandeira(TextoUtil.paraMaiusculo(form.getBandeira()));
        cartao.setBanco(TextoUtil.paraMaiusculo(form.getBanco()));
        cartao.setDiaFechamento(form.getDiaFechamento());
        cartao.setDiaVencimento(form.getDiaVencimento());

        Cartao salvo = cartaoRepository.save(cartao);
        log.info("Cartao atualizado no banco {} terminando em {}", salvo.getBanco(), ultimosQuatro(salvo.getNumeroCartao()));
        return salvo;
    }

    /**
     * Desativa cartao sem remover historico de cobrancas e lancamentos.
     */
    @Transactional
    public void desativar(Long id) {
        Cartao cartao = buscarPorId(id);
        cartao.setAtivo(Boolean.FALSE);
        cartaoRepository.save(cartao);
        log.warn("Cartao desativado terminando em {}", ultimosQuatro(cartao.getNumeroCartao()));
    }

    /**
     * Exclui cartao somente se nao existir nenhuma referencia vinculada.
     */
    @Transactional
    public void excluir(Long id) {
        Cartao cartao = buscarPorId(id);

        boolean possuiVinculo = lancamentoRepository.existsByCartaoId(id)
                || cobrancaPessoaRepository.existsByCartaoId(id)
                || pagamentoPessoaRepository.existsByCartaoId(id);

        if (possuiVinculo) {
            throw new RecursoVinculadoException("Cartao vinculado nao pode ser excluido");
        }

        cartaoRepository.delete(cartao);
        log.warn("Cartao excluido terminando em {}", ultimosQuatro(cartao.getNumeroCartao()));
    }

    /**
     * Calcula a competencia da fatura para uma compra realizada em determinada data.
     */
    @Transactional(readOnly = true)
    public YearMonth calcularCompetenciaDaCompra(Cartao cartao, LocalDate dataCompra) {
        YearMonth competencia = YearMonth.from(dataCompra);

        // Compras no dia de fechamento ou apos ele vao para a proxima competencia.
        if (dataCompra.getDayOfMonth() >= cartao.getDiaFechamento()) {
            return competencia.plusMonths(1);
        }

        return competencia;
    }

    /**
     * Calcula a competencia atual em aberto para um cartao.
     */
    @Transactional(readOnly = true)
    public YearMonth calcularCompetenciaAberta(Cartao cartao, LocalDate referencia) {
        return calcularCompetenciaDaCompra(cartao, referencia);
    }

    /**
     * Lista cinco competencias disponiveis: aberta atual, duas anteriores e duas futuras.
     */
    @Transactional(readOnly = true)
    public List<YearMonth> listarCincoCompetencias(Long cartaoId, LocalDate referencia) {
        Cartao cartao = buscarPorId(cartaoId);
        YearMonth competenciaAberta = calcularCompetenciaAberta(cartao, referencia);

        List<YearMonth> competencias = new ArrayList<>();
        for (int deslocamento = -2; deslocamento <= 2; deslocamento++) {
            competencias.add(competenciaAberta.plusMonths(deslocamento));
        }
        return competencias;
    }

    /**
     * Valida numero do cartao conforme regra de 16 digitos e unicidade.
     */
    private void validarNumeroCartao(String numero, Long idIgnorado) {
        if (numero == null || !numero.matches("^[0-9]{16}$")) {
            throw new RegraDeNegocioException("Numero do cartao deve conter exatamente 16 digitos");
        }

        boolean duplicado = idIgnorado == null
                ? cartaoRepository.existsByNumeroCartao(numero)
                : cartaoRepository.existsByNumeroCartaoAndIdNot(numero, idIgnorado);

        if (duplicado) {
            throw new RegraDeNegocioException("Ja existe cartao com este numero");
        }
    }

    /**
     * Valida limites de dia de fechamento e vencimento.
     */
    private void validarDias(Integer diaFechamento, Integer diaVencimento) {
        if (diaFechamento == null || diaFechamento < 1 || diaFechamento > 31) {
            throw new RegraDeNegocioException("Dia de fechamento invalido");
        }
        if (diaVencimento == null || diaVencimento < 1 || diaVencimento > 31) {
            throw new RegraDeNegocioException("Dia de vencimento invalido");
        }
    }

    /**
     * Retorna os quatro ultimos digitos para log seguro.
     */
    private String ultimosQuatro(String numeroCartao) {
        if (numeroCartao == null || numeroCartao.length() < 4) {
            return "****";
        }
        return numeroCartao.substring(numeroCartao.length() - 4);
    }
}
