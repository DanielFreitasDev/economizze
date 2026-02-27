package io.nexus.economizze.lancamento.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.categoria.model.Categoria;
import io.nexus.economizze.conta.model.Conta;
import io.nexus.economizze.conta.model.TipoConta;
import io.nexus.economizze.lancamento.model.Lancamento;
import io.nexus.economizze.lancamento.model.NaturezaLancamento;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Testes de persistencia para validar carregamento de relacionamentos de lancamento.
 */
@DataJpaTest
class LancamentoRepositoryTest {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private TestEntityManager entidadeManager;

    /**
     * Garante que categoria e conta venham inicializadas ao buscar um lancamento por id.
     */
    @Test
    void deveCarregarRelacionamentosAoBuscarPorIdQuandoOrigemForConta() {
        Categoria categoria = criarCategoria("CATEGORIA TESTE CONTA");
        Conta conta = criarConta("CONTA TESTE");
        Lancamento lancamento = criarLancamentoComConta(categoria, conta);

        // Limpa o contexto para garantir que a consulta execute no banco e respeite o EntityGraph.
        entidadeManager.clear();

        Lancamento encontrado = lancamentoRepository.findById(lancamento.getId()).orElseThrow();

        assertThat(Hibernate.isInitialized(encontrado.getCategoria())).isTrue();
        assertThat(Hibernate.isInitialized(encontrado.getConta())).isTrue();
        assertThat(encontrado.getCategoria().getNome()).isEqualTo("CATEGORIA TESTE CONTA");
        assertThat(encontrado.getConta().getNome()).isEqualTo("CONTA TESTE");
    }

    /**
     * Garante que categoria e cartao venham inicializadas ao buscar um lancamento por id.
     */
    @Test
    void deveCarregarRelacionamentosAoBuscarPorIdQuandoOrigemForCartao() {
        Categoria categoria = criarCategoria("CATEGORIA TESTE CARTAO");
        Cartao cartao = criarCartao("1111222233334444");
        Lancamento lancamento = criarLancamentoComCartao(categoria, cartao);

        // Limpa o contexto para validar o comportamento real da consulta por id.
        entidadeManager.clear();

        Lancamento encontrado = lancamentoRepository.findById(lancamento.getId()).orElseThrow();

        assertThat(Hibernate.isInitialized(encontrado.getCategoria())).isTrue();
        assertThat(Hibernate.isInitialized(encontrado.getCartao())).isTrue();
        assertThat(encontrado.getCategoria().getNome()).isEqualTo("CATEGORIA TESTE CARTAO");
        assertThat(encontrado.getCartao().getBanco()).isEqualTo("BANCO TESTE");
    }

    /**
     * Cria e persiste uma categoria para uso nos cenarios de teste.
     */
    private Categoria criarCategoria(String nomeCategoria) {
        Categoria categoria = new Categoria();
        categoria.setNome(nomeCategoria);
        categoria.setDescricao("Categoria criada para validar carregamento eager no findById");
        return entidadeManager.persistAndFlush(categoria);
    }

    /**
     * Cria e persiste uma conta para representar lancamentos com origem em conta.
     */
    private Conta criarConta(String nomeConta) {
        Conta conta = new Conta();
        conta.setNome(nomeConta);
        conta.setBanco("BANCO TESTE");
        conta.setTipoConta(TipoConta.CORRENTE);
        conta.setSaldoInicial(new BigDecimal("1000.00"));
        return entidadeManager.persistAndFlush(conta);
    }

    /**
     * Cria e persiste um cartao para representar lancamentos com origem em cartao.
     */
    private Cartao criarCartao(String numeroCartao) {
        Cartao cartao = new Cartao();
        cartao.setNumeroCartao(numeroCartao);
        cartao.setBandeira("VISA");
        cartao.setBanco("BANCO TESTE");
        cartao.setDiaFechamento(10);
        cartao.setDiaVencimento(20);
        return entidadeManager.persistAndFlush(cartao);
    }

    /**
     * Cria e persiste um lancamento vinculado a categoria e conta.
     */
    private Lancamento criarLancamentoComConta(Categoria categoria, Conta conta) {
        Lancamento lancamento = criarLancamentoBase(categoria);
        lancamento.setConta(conta);
        return entidadeManager.persistAndFlush(lancamento);
    }

    /**
     * Cria e persiste um lancamento vinculado a categoria e cartao.
     */
    private Lancamento criarLancamentoComCartao(Categoria categoria, Cartao cartao) {
        Lancamento lancamento = criarLancamentoBase(categoria);
        lancamento.setCartao(cartao);
        return entidadeManager.persistAndFlush(lancamento);
    }

    /**
     * Centraliza os campos obrigatorios comuns para evitar duplicacao no preparo dos cenarios.
     */
    private Lancamento criarLancamentoBase(Categoria categoria) {
        Lancamento lancamento = new Lancamento();
        lancamento.setNome("LANCAMENTO TESTE");
        lancamento.setNatureza(NaturezaLancamento.DESPESA);
        lancamento.setTipo(TipoLancamento.AVULSO);
        lancamento.setValor(new BigDecimal("150.00"));
        lancamento.setCategoria(categoria);
        lancamento.setDataBase(LocalDate.of(2026, 2, 15));
        return lancamento;
    }
}
