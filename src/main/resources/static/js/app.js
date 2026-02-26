(function () {
  const CHAVE_TEMA = 'economizze-tema';
  const TEMA_CLARO = 'claro';
  const TEMA_ESCURO = 'escuro';

  /**
   * Garante que apenas temas suportados sejam aplicados.
   * Isso evita estados invalidos quando o valor salvo no navegador estiver corrompido.
   */
  function temaEhValido(tema) {
    return tema === TEMA_CLARO || tema === TEMA_ESCURO;
  }

  /**
   * Resolve o tema inicial com prioridade para a preferencia persistida do usuario.
   * Se nao existir valor salvo, usa a preferencia nativa do sistema operacional.
   */
  function obterTemaInicial() {
    try {
      const temaSalvo = localStorage.getItem(CHAVE_TEMA);
      if (temaEhValido(temaSalvo)) {
        return temaSalvo;
      }
    } catch (erro) {
      // Se o acesso ao storage falhar, segue com a preferencia do sistema.
    }

    const mediaQuery = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)');
    const prefereEscuro = Boolean(mediaQuery && mediaQuery.matches);
    return prefereEscuro ? TEMA_ESCURO : TEMA_CLARO;
  }

  /**
   * Persiste o tema escolhido para manter a mesma experiencia em proximos acessos.
   * A escrita e protegida por try/catch para nao impactar o restante da aplicacao.
   */
  function salvarTema(tema) {
    try {
      localStorage.setItem(CHAVE_TEMA, tema);
    } catch (erro) {
      // Falha silenciosa em ambientes com bloqueio de storage.
    }
  }

  /**
   * Normaliza textos da interface para facilitar comparacoes de rotulos.
   * O objetivo e permitir mapeamento de icones sem depender de acento, caixa ou espacos extras.
   */
  function normalizarTextoParaIcone(texto) {
    const textoBase = texto || '';
    const textoSemAcento = typeof textoBase.normalize === 'function'
      ? textoBase.normalize('NFD')
      : textoBase;

    return textoSemAcento
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/\s+/g, ' ')
      .trim()
      .toUpperCase();
  }

  /**
   * Aplica um icone no inicio do elemento mantendo o texto original como fallback visual.
   * A marcacao e idempotente para evitar duplicacao de icones em recargas parciais do DOM.
   */
  function aplicarIconeEmElemento(elemento, classeIcone) {
    if (!elemento || !classeIcone) {
      return;
    }

    if (elemento.dataset.iconeAplicado === 'true') {
      return;
    }

    if (elemento.querySelector('i.fa-solid')) {
      elemento.classList.add('com-icone');
      elemento.dataset.iconeAplicado = 'true';
      return;
    }

    const textoOriginal = (elemento.textContent || '').replace(/\s+/g, ' ').trim();
    if (!textoOriginal) {
      return;
    }

    elemento.innerHTML = `<i class="fa-solid ${classeIcone}" aria-hidden="true"></i><span>${textoOriginal}</span>`;
    elemento.classList.add('com-icone');
    elemento.dataset.iconeAplicado = 'true';
  }

  /**
   * Resolve o icone ideal para botoes de acao com base no texto exibido.
   * O mapeamento cobre os verbos mais usados do sistema para manter padrao visual entre telas.
   */
  function obterClasseIconeAcao(textoNormalizado) {
    const mapaIcones = {
      FILTRAR: 'fa-filter',
      'MES ATUAL': 'fa-calendar-days',
      SALVAR: 'fa-floppy-disk',
      VOLTAR: 'fa-arrow-left',
      VER: 'fa-eye',
      EDITAR: 'fa-pen-to-square',
      DESATIVAR: 'fa-ban',
      EXCLUIR: 'fa-trash',
      GERENCIAR: 'fa-sliders',
      CONSULTAR: 'fa-magnifying-glass',
      'GERAR PDF': 'fa-file-pdf',
      ATUALIZAR: 'fa-rotate',
      ENTRAR: 'fa-right-to-bracket',
      SENHA: 'fa-key',
      'CRIAR PRIMEIRO ADMINISTRADOR': 'fa-user-plus',
      'VOLTAR AO INICIO': 'fa-house'
    };

    if (mapaIcones[textoNormalizado]) {
      return mapaIcones[textoNormalizado];
    }

    if (textoNormalizado.startsWith('NOVO') || textoNormalizado.startsWith('NOVA')) {
      return 'fa-plus';
    }

    return null;
  }

  /**
   * Resolve o icone de titulos de cards para reforcar contexto de cada secao.
   * A regra usa termos-chave em vez de igualdade estrita para funcionar com diferentes variacoes de titulo.
   */
  function obterClasseIconeTitulo(textoNormalizado) {
    const regrasTitulo = [
      { termo: 'FLUXO MENSAL', icone: 'fa-chart-line' },
      { termo: 'RELATORIO', icone: 'fa-chart-column' },
      { termo: 'RESUMO', icone: 'fa-chart-pie' },
      { termo: 'OCORRENCIAS', icone: 'fa-calendar-check' },
      { termo: 'CADASTROS BASE', icone: 'fa-database' },
      { termo: 'DETALHES', icone: 'fa-circle-info' },
      { termo: 'LANCAMENT', icone: 'fa-list-check' },
      { termo: 'COBRANC', icone: 'fa-file-invoice-dollar' },
      { termo: 'PESSOA', icone: 'fa-users' },
      { termo: 'CART', icone: 'fa-credit-card' },
      { termo: 'CATEGOR', icone: 'fa-tags' },
      { termo: 'CONTA', icone: 'fa-wallet' },
      { termo: 'TRANSFER', icone: 'fa-right-left' },
      { termo: 'PAGAMENTO', icone: 'fa-money-check-dollar' },
      { termo: 'USUARIO', icone: 'fa-user-shield' },
      { termo: 'CONFIGURAC', icone: 'fa-sliders' },
      { termo: 'ERRO', icone: 'fa-triangle-exclamation' }
    ];

    const regraEncontrada = regrasTitulo.find((regra) => textoNormalizado.includes(regra.termo));
    return regraEncontrada ? regraEncontrada.icone : null;
  }

  /**
   * Enriquecer a UI com icones de apoio em botoes e titulos principais.
   * Essa etapa roda apos o carregamento da pagina para reutilizar o mesmo HTML em todas as telas.
   */
  function inicializarIconesContextuais() {
    document.querySelectorAll('.btn').forEach((botao) => {
      const textoNormalizado = normalizarTextoParaIcone(botao.textContent);
      const classeIcone = obterClasseIconeAcao(textoNormalizado);
      aplicarIconeEmElemento(botao, classeIcone);
    });

    document.querySelectorAll('.card h2, .card h3, .login-card h2').forEach((titulo) => {
      const textoNormalizado = normalizarTextoParaIcone(titulo.textContent);
      const classeIcone = obterClasseIconeTitulo(textoNormalizado);
      aplicarIconeEmElemento(titulo, classeIcone);
    });
  }

  /**
   * Aplica o tema no documento e sincroniza os botoes de alternancia.
   * O rotulo e o estado ARIA sao atualizados para acessibilidade e feedback visual.
   */
  function aplicarTema(tema) {
    const temaAplicado = temaEhValido(tema) ? tema : TEMA_CLARO;
    document.documentElement.setAttribute('data-tema', temaAplicado);

    document.querySelectorAll('[data-acao="alternar-tema"]').forEach((botao) => {
      const temaEscuroAtivo = temaAplicado === TEMA_ESCURO;
      const rotuloTema = temaEscuroAtivo ? 'TEMA: ESCURO' : 'TEMA: CLARO';
      const classeIconeTema = temaEscuroAtivo ? 'fa-moon' : 'fa-sun';
      botao.innerHTML = `<i class="fa-solid ${classeIconeTema}" aria-hidden="true"></i><span>${rotuloTema}</span>`;
      botao.classList.add('com-icone');
      botao.setAttribute('aria-pressed', String(temaEscuroAtivo));
      botao.setAttribute('title', temaEscuroAtivo ? 'Ativar tema claro' : 'Ativar tema escuro');
    });
  }

  /**
   * Configura os eventos de clique para alternar entre tema claro e escuro.
   * Tambem aplica o tema inicial para manter consistencia em todas as paginas.
   */
  function inicializarAlternadorTema() {
    const temaInicial = obterTemaInicial();
    aplicarTema(temaInicial);

    document.querySelectorAll('[data-acao="alternar-tema"]').forEach((botao) => {
      botao.addEventListener('click', () => {
        const temaAtual = document.documentElement.getAttribute('data-tema');
        const proximoTema = temaAtual === TEMA_ESCURO ? TEMA_CLARO : TEMA_ESCURO;
        aplicarTema(proximoTema);
        salvarTema(proximoTema);
      });
    });
  }

  function manterSomenteDigitos(valor) {
    return (valor || '').replace(/\D/g, '');
  }

  function aplicarMascaraCpf(valor) {
    const digitos = manterSomenteDigitos(valor).slice(0, 11);
    return digitos
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }

  function aplicarMascaraCartao(valor) {
    const digitos = manterSomenteDigitos(valor).slice(0, 16);
    return digitos.replace(/(\d{4})(?=\d)/g, '$1 ').trim();
  }

  function aplicarMascaraTelefone(valor) {
    const digitos = manterSomenteDigitos(valor).slice(0, 11);
    if (digitos.length <= 10) {
      return digitos
        .replace(/(\d{2})(\d)/, '($1) $2')
        .replace(/(\d{4})(\d{1,4})$/, '$1-$2');
    }
    return digitos
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{5})(\d{1,4})$/, '$1-$2');
  }

  function aplicarMascaraCep(valor) {
    const digitos = manterSomenteDigitos(valor).slice(0, 8);
    return digitos.replace(/(\d{5})(\d{1,3})$/, '$1-$2');
  }

  function formatarMoedaCampo(input) {
    const digitos = manterSomenteDigitos(input.value);
    const inteiro = digitos ? parseInt(digitos, 10) : 0;
    const numero = inteiro / 100;

    input.dataset.valorBruto = numero.toFixed(2);
    input.value = new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(numero);
  }

  function prepararValorMonetarioParaSubmit(form) {
    form.querySelectorAll('.moeda-input').forEach((input) => {
      const bruto = input.dataset.valorBruto;
      if (bruto) {
        input.value = bruto;
      }
    });
  }

  function inicializarMascaras() {
    document.querySelectorAll('.cpf-mask').forEach((input) => {
      input.addEventListener('input', () => {
        input.value = aplicarMascaraCpf(input.value);
      });
      input.value = aplicarMascaraCpf(input.value);
    });

    document.querySelectorAll('.cartao-mask').forEach((input) => {
      input.addEventListener('input', () => {
        input.value = aplicarMascaraCartao(input.value);
      });
      input.value = aplicarMascaraCartao(input.value);
    });

    document.querySelectorAll('.telefone-mask').forEach((input) => {
      input.addEventListener('input', () => {
        input.value = aplicarMascaraTelefone(input.value);
      });
      input.value = aplicarMascaraTelefone(input.value);
    });

    document.querySelectorAll('.cep-mask').forEach((input) => {
      input.addEventListener('input', () => {
        input.value = aplicarMascaraCep(input.value);
      });
      input.value = aplicarMascaraCep(input.value);
    });

    document.querySelectorAll('.moeda-input').forEach((input) => {
      input.addEventListener('focus', () => {
        if (!input.value) {
          input.value = 'R$ 0,00';
          input.dataset.valorBruto = '0.00';
        }
      });
      input.addEventListener('input', () => {
        formatarMoedaCampo(input);
      });
      if (input.value) {
        const valorInicial = Number(input.value.replace(',', '.'));
        if (!Number.isNaN(valorInicial)) {
          input.dataset.valorBruto = valorInicial.toFixed(2);
          input.value = new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
          }).format(valorInicial);
        }
      }
    });
  }

  function inicializarMaiusculo() {
    document.querySelectorAll('.uppercase-input').forEach((input) => {
      input.addEventListener('input', () => {
        const inicio = input.selectionStart;
        input.value = input.value.toUpperCase();
        if (inicio !== null) {
          input.setSelectionRange(inicio, inicio);
        }
      });
      input.value = input.value.toUpperCase();
    });
  }

  function inicializarWhatsappMesmoCelular() {
    const checkbox = document.getElementById('copiarCelularWhatsapp');
    const celular = document.getElementById('telefoneCelular');
    const whatsapp = document.getElementById('whatsapp');

    if (!checkbox || !celular || !whatsapp) {
      return;
    }

    const sincronizar = () => {
      if (checkbox.checked) {
        whatsapp.value = celular.value;
      }
    };

    checkbox.addEventListener('change', sincronizar);
    celular.addEventListener('input', sincronizar);
  }

  function inicializarViaCep() {
    const cepInput = document.getElementById('cep');
    if (!cepInput) {
      return;
    }

    const campos = {
      logradouro: document.getElementById('logradouro'),
      complemento: document.getElementById('complemento'),
      bairro: document.getElementById('bairro'),
      cidade: document.getElementById('cidade'),
      estado: document.getElementById('estado')
    };

    function resetarCampos() {
      Object.values(campos).forEach((campo) => {
        if (!campo) return;
        campo.readOnly = false;
      });
    }

    async function buscarCep() {
      const cep = manterSomenteDigitos(cepInput.value);
      if (cep.length !== 8) {
        return;
      }

      try {
        const resposta = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
        if (!resposta.ok) {
          return;
        }

        const dados = await resposta.json();
        if (dados.erro) {
          return;
        }

        const mapeamento = {
          logradouro: dados.logradouro,
          complemento: dados.complemento,
          bairro: dados.bairro,
          cidade: dados.localidade,
          estado: dados.uf
        };

        Object.entries(mapeamento).forEach(([chave, valor]) => {
          const campo = campos[chave];
          if (!campo) return;

          if (valor && valor.trim() !== '') {
            campo.value = valor.toUpperCase();
            campo.readOnly = true;
          } else {
            campo.readOnly = false;
          }
        });
      } catch (erro) {
        resetarCampos();
      }
    }

    cepInput.addEventListener('blur', buscarCep);
  }

  async function carregarCompetencias(cardSelect, competenciaSelect) {
    const cartaoId = cardSelect.value;
    competenciaSelect.innerHTML = '<option value="">SELECIONE</option>';

    if (!cartaoId) {
      return;
    }

    try {
      const resposta = await fetch(`/cartoes/${cartaoId}/competencias`);
      if (!resposta.ok) {
        return;
      }

      const competencias = await resposta.json();
      competencias.forEach((competencia) => {
        const option = document.createElement('option');
        option.value = `${competencia}-01`;
        option.textContent = competencia;
        competenciaSelect.appendChild(option);
      });

      // Por regra, seleciona por padrao a competencia em aberto (indice central da lista).
      if (competencias.length === 5) {
        competenciaSelect.value = `${competencias[2]}-01`;
      }
    } catch (erro) {
      // Falha silenciosa para nao interromper o preenchimento manual.
    }
  }

  function inicializarCompetenciasCartao() {
    const cartaoSelect = document.getElementById('cartaoId');
    const competenciaSelect = document.getElementById('competenciaFaturaInicial');

    if (!cartaoSelect || !competenciaSelect) {
      return;
    }

    cartaoSelect.addEventListener('change', () => carregarCompetencias(cartaoSelect, competenciaSelect));

    if (cartaoSelect.value && !competenciaSelect.value) {
      carregarCompetencias(cartaoSelect, competenciaSelect);
    }
  }

  function inicializarSubmissaoMoeda() {
    document.querySelectorAll('form').forEach((form) => {
      form.addEventListener('submit', () => {
        prepararValorMonetarioParaSubmit(form);
      });
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    inicializarAlternadorTema();
    inicializarIconesContextuais();
    inicializarMascaras();
    inicializarMaiusculo();
    inicializarWhatsappMesmoCelular();
    inicializarViaCep();
    inicializarCompetenciasCartao();
    inicializarSubmissaoMoeda();
  });
})();
