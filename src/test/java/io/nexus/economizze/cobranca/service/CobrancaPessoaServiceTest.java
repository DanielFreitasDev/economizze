package io.nexus.economizze.cobranca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.cartao.service.CartaoService;
import io.nexus.economizze.categoria.service.CategoriaService;
import io.nexus.economizze.cobranca.dto.OcorrenciaCobrancaPessoaDto;
import io.nexus.economizze.cobranca.model.CobrancaPessoa;
import io.nexus.economizze.cobranca.repository.CobrancaPessoaRepository;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.pessoa.model.Pessoa;
import io.nexus.economizze.pessoa.service.PessoaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitarios para validar regras de valor por parcela em cobrancas de pessoas.
 */
@ExtendWith(MockitoExtension.class)
class CobrancaPessoaServiceTest {

    @Mock
    private CobrancaPessoaRepository cobrancaPessoaRepository;

    @Mock
    private PessoaService pessoaService;

    @Mock
    private CartaoService cartaoService;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CobrancaPessoaService cobrancaPessoaService;

    /**
     * Garante que o valor total informado em cobranca parcelada seja dividido igualmente por ocorrencia mensal.
     */
    @Test
    void deveDividirValorTotalQuandoCobrancaForParcelada() {
        CobrancaPessoa cobrancaParcelada = criarCobrancaParcelada("500.00", 5, LocalDate.of(2026, 1, 1));
        when(cobrancaPessoaRepository.findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscDescricaoAsc(any(LocalDate.class)))
                .thenReturn(List.of(cobrancaParcelada));

        List<OcorrenciaCobrancaPessoaDto> ocorrencias = cobrancaPessoaService.listarOcorrenciasDaCompetencia(YearMonth.of(2026, 3));

        assertThat(ocorrencias).hasSize(1);
        OcorrenciaCobrancaPessoaDto ocorrencia = ocorrencias.getFirst();
        assertThat(ocorrencia.parcelaAtual()).isEqualTo(3);
        assertThat(ocorrencia.totalParcelas()).isEqualTo(5);
        assertThat(ocorrencia.valor()).isEqualByComparingTo("100.00");
    }

    /**
     * Valida distribuicao de centavos residuais nas primeiras parcelas para manter soma final exata.
     */
    @Test
    void deveDistribuirCentavosRestantesNasPrimeirasParcelas() {
        CobrancaPessoa cobrancaParcelada = criarCobrancaParcelada("100.00", 3, LocalDate.of(2026, 1, 1));
        when(cobrancaPessoaRepository.findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscDescricaoAsc(any(LocalDate.class)))
                .thenReturn(List.of(cobrancaParcelada));

        BigDecimal valorParcelaUm = buscarValorDaOcorrencia(YearMonth.of(2026, 1));
        BigDecimal valorParcelaDois = buscarValorDaOcorrencia(YearMonth.of(2026, 2));
        BigDecimal valorParcelaTres = buscarValorDaOcorrencia(YearMonth.of(2026, 3));

        assertThat(valorParcelaUm).isEqualByComparingTo("33.34");
        assertThat(valorParcelaDois).isEqualByComparingTo("33.33");
        assertThat(valorParcelaTres).isEqualByComparingTo("33.33");
        assertThat(valorParcelaUm.add(valorParcelaDois).add(valorParcelaTres)).isEqualByComparingTo("100.00");
    }

    /**
     * Busca o valor gerado para a competencia informada no cenario preparado do teste.
     */
    private BigDecimal buscarValorDaOcorrencia(YearMonth competencia) {
        List<OcorrenciaCobrancaPessoaDto> ocorrencias = cobrancaPessoaService.listarOcorrenciasDaCompetencia(competencia);
        assertThat(ocorrencias).hasSize(1);
        return ocorrencias.getFirst().valor();
    }

    /**
     * Cria entidade de cobranca parcelada com dados minimos necessarios para projetar ocorrencias.
     */
    private CobrancaPessoa criarCobrancaParcelada(String valorTotal, int totalParcelas, LocalDate competenciaInicial) {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome("JOAO TESTE");

        Cartao cartao = new Cartao();
        cartao.setId(1L);
        cartao.setBanco("BANCO TESTE");
        cartao.setNumeroCartao("1111222233334444");

        CobrancaPessoa cobranca = new CobrancaPessoa();
        cobranca.setId(99L);
        cobranca.setPessoa(pessoa);
        cobranca.setCartao(cartao);
        cobranca.setDescricao("COMPRA TESTE");
        cobranca.setTipo(TipoLancamento.PARCELADO);
        cobranca.setValor(new BigDecimal(valorTotal));
        cobranca.setDataBase(competenciaInicial);
        cobranca.setCompetenciaFaturaInicial(competenciaInicial);
        cobranca.setTotalParcelas(totalParcelas);
        return cobranca;
    }
}
