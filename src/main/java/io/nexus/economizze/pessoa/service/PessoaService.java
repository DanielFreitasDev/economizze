package io.nexus.economizze.pessoa.service;

import io.nexus.economizze.cobranca.repository.CobrancaPessoaRepository;
import io.nexus.economizze.cobranca.repository.PagamentoPessoaRepository;
import io.nexus.economizze.pessoa.dto.PessoaForm;
import io.nexus.economizze.pessoa.model.Pessoa;
import io.nexus.economizze.pessoa.repository.PessoaRepository;
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
 * Regras de negocio para cadastro e manutencao de pessoas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final CobrancaPessoaRepository cobrancaPessoaRepository;
    private final PagamentoPessoaRepository pagamentoPessoaRepository;

    /**
     * Lista pessoas com opcao de incluir inativas.
     */
    @Transactional(readOnly = true)
    public List<Pessoa> listar(boolean incluirInativas) {
        return incluirInativas ? pessoaRepository.findAllByOrderByNomeAsc() : pessoaRepository.findAllByAtivoTrueOrderByNomeAsc();
    }

    /**
     * Busca pessoa por id validando existencia.
     */
    @Transactional(readOnly = true)
    public Pessoa buscarPorId(Long id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa nao encontrada"));
    }

    /**
     * Cria pessoa com padronizacao de campos e validacao de CPF unico.
     */
    @Transactional
    public Pessoa criar(PessoaForm form) {
        String cpf = validarCpf(form.getCpf(), null);

        Pessoa pessoa = new Pessoa();
        preencherCampos(pessoa, form, cpf);

        Pessoa salva = pessoaRepository.save(pessoa);
        log.info("Pessoa criada: {} - CPF {}", salva.getNome(), salva.getCpf());
        return salva;
    }

    /**
     * Atualiza pessoa existente com revalidacao de CPF.
     */
    @Transactional
    public Pessoa atualizar(Long id, PessoaForm form) {
        Pessoa pessoa = buscarPorId(id);
        String cpf = validarCpf(form.getCpf(), id);

        preencherCampos(pessoa, form, cpf);

        Pessoa salva = pessoaRepository.save(pessoa);
        log.info("Pessoa atualizada: {} - CPF {}", salva.getNome(), salva.getCpf());
        return salva;
    }

    /**
     * Desativa pessoa para impedir novos vinculos sem perder historico.
     */
    @Transactional
    public void desativar(Long id) {
        Pessoa pessoa = buscarPorId(id);
        pessoa.setAtivo(Boolean.FALSE);
        pessoaRepository.save(pessoa);
        log.warn("Pessoa desativada: {}", pessoa.getNome());
    }

    /**
     * Exclui pessoa apenas se nao existir vinculo financeiro.
     */
    @Transactional
    public void excluir(Long id) {
        Pessoa pessoa = buscarPorId(id);

        boolean possuiVinculo = cobrancaPessoaRepository.existsByPessoaId(id)
                || pagamentoPessoaRepository.existsByPessoaId(id);

        if (possuiVinculo) {
            throw new RecursoVinculadoException("Pessoa vinculada a cobrancas/pagamentos nao pode ser excluida");
        }

        pessoaRepository.delete(pessoa);
        log.warn("Pessoa excluida: {}", pessoa.getNome());
    }

    /**
     * Valida CPF e devolve somente digitos para persistencia.
     */
    private String validarCpf(String cpfOriginal, Long idIgnorado) {
        String cpf = TextoUtil.manterSomenteDigitos(cpfOriginal);
        if (cpf == null || !cpf.matches("^[0-9]{11}$")) {
            throw new RegraDeNegocioException("CPF deve conter 11 digitos numericos");
        }

        boolean duplicado = idIgnorado == null
                ? pessoaRepository.existsByCpf(cpf)
                : pessoaRepository.existsByCpfAndIdNot(cpf, idIgnorado);

        if (duplicado) {
            throw new RegraDeNegocioException("Ja existe pessoa com este CPF");
        }

        return cpf;
    }

    /**
     * Preenche campos aplicando regra de maiusculo para todos os textos nao numericos.
     */
    private void preencherCampos(Pessoa pessoa, PessoaForm form, String cpfNormalizado) {
        // Nome e obrigatorio e sempre armazenado padronizado em maiusculo.
        pessoa.setNome(TextoUtil.paraMaiusculo(form.getNome()));
        pessoa.setCpf(cpfNormalizado);

        // Campos de endereco seguem padrao em maiusculo para manter consistencia global.
        pessoa.setLogradouro(TextoUtil.paraMaiusculo(form.getLogradouro()));
        pessoa.setNumero(TextoUtil.paraMaiusculo(form.getNumero()));
        pessoa.setComplemento(TextoUtil.paraMaiusculo(form.getComplemento()));
        pessoa.setPontoReferencia(TextoUtil.paraMaiusculo(form.getPontoReferencia()));
        pessoa.setBairro(TextoUtil.paraMaiusculo(form.getBairro()));
        pessoa.setCidade(TextoUtil.paraMaiusculo(form.getCidade()));
        pessoa.setEstado(TextoUtil.paraMaiusculo(form.getEstado()));

        // Campos numericos sao persistidos somente com digitos para facilitar consulta e integracao.
        pessoa.setCep(TextoUtil.manterSomenteDigitos(form.getCep()));
        pessoa.setTelefoneCelular(TextoUtil.manterSomenteDigitos(form.getTelefoneCelular()));
        pessoa.setWhatsapp(TextoUtil.manterSomenteDigitos(form.getWhatsapp()));

        // E-mail e preservado sem maiusculizacao para respeitar regra solicitada.
        pessoa.setEmail(form.getEmail() == null ? null : form.getEmail().trim());
    }
}
