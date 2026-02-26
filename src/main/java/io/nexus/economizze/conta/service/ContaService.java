package io.nexus.economizze.conta.service;

import io.nexus.economizze.conta.dto.ContaForm;
import io.nexus.economizze.conta.model.Conta;
import io.nexus.economizze.conta.repository.ContaRepository;
import io.nexus.economizze.lancamento.repository.LancamentoRepository;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.exception.RecursoVinculadoException;
import io.nexus.economizze.shared.util.NumeroUtil;
import io.nexus.economizze.shared.util.TextoUtil;
import io.nexus.economizze.transferencia.repository.TransferenciaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio de contas financeiras.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final TransferenciaRepository transferenciaRepository;

    /**
     * Lista contas com opcao de incluir inativas.
     */
    @Transactional(readOnly = true)
    public List<Conta> listar(boolean incluirInativas) {
        return incluirInativas ? contaRepository.findAllByOrderByNomeAsc() : contaRepository.findAllByAtivoTrueOrderByNomeAsc();
    }

    /**
     * Busca conta por id com validacao.
     */
    @Transactional(readOnly = true)
    public Conta buscarPorId(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta nao encontrada"));
    }

    /**
     * Cria nova conta.
     */
    @Transactional
    public Conta criar(ContaForm form) {
        String nomePadronizado = TextoUtil.paraMaiusculo(form.getNome());
        if (contaRepository.existsByNomeIgnoreCase(nomePadronizado)) {
            throw new RegraDeNegocioException("Ja existe conta com este nome");
        }

        Conta conta = new Conta();
        conta.setNome(nomePadronizado);
        conta.setBanco(TextoUtil.paraMaiusculo(form.getBanco()));
        conta.setTipoConta(form.getTipoConta());
        conta.setSaldoInicial(NumeroUtil.parseMonetario(form.getSaldoInicial()));

        Conta salva = contaRepository.save(conta);
        log.info("Conta criada: {}", salva.getNome());
        return salva;
    }

    /**
     * Atualiza conta existente.
     */
    @Transactional
    public Conta atualizar(Long id, ContaForm form) {
        Conta conta = buscarPorId(id);

        String nomePadronizado = TextoUtil.paraMaiusculo(form.getNome());
        if (contaRepository.existsByNomeIgnoreCaseAndIdNot(nomePadronizado, id)) {
            throw new RegraDeNegocioException("Ja existe conta com este nome");
        }

        conta.setNome(nomePadronizado);
        conta.setBanco(TextoUtil.paraMaiusculo(form.getBanco()));
        conta.setTipoConta(form.getTipoConta());
        conta.setSaldoInicial(NumeroUtil.parseMonetario(form.getSaldoInicial()));

        Conta salva = contaRepository.save(conta);
        log.info("Conta atualizada: {}", salva.getNome());
        return salva;
    }

    /**
     * Desativa conta mantendo historico de movimentacoes.
     */
    @Transactional
    public void desativar(Long id) {
        Conta conta = buscarPorId(id);
        conta.setAtivo(Boolean.FALSE);
        contaRepository.save(conta);
        log.warn("Conta desativada: {}", conta.getNome());
    }

    /**
     * Exclui conta se nao houver vinculos com lancamentos/transferencias.
     */
    @Transactional
    public void excluir(Long id) {
        Conta conta = buscarPorId(id);

        boolean possuiVinculo = lancamentoRepository.existsByContaId(id)
                || transferenciaRepository.existsByContaOrigemIdOrContaDestinoId(id, id);

        if (possuiVinculo) {
            throw new RecursoVinculadoException("Conta vinculada nao pode ser excluida");
        }

        contaRepository.delete(conta);
        log.warn("Conta excluida: {}", conta.getNome());
    }
}
