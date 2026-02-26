CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    username VARCHAR(80) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE pessoas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    logradouro VARCHAR(180),
    numero VARCHAR(30),
    complemento VARCHAR(120),
    ponto_referencia VARCHAR(180),
    bairro VARCHAR(120),
    cidade VARCHAR(120),
    estado VARCHAR(60),
    cep VARCHAR(8),
    telefone_celular VARCHAR(20),
    whatsapp VARCHAR(20),
    email VARCHAR(180),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL UNIQUE,
    descricao VARCHAR(220),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE contas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL UNIQUE,
    banco VARCHAR(120),
    tipo_conta VARCHAR(40) NOT NULL,
    saldo_inicial NUMERIC(15, 2) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cartoes (
    id BIGSERIAL PRIMARY KEY,
    numero_cartao VARCHAR(16) NOT NULL UNIQUE,
    bandeira VARCHAR(60) NOT NULL,
    banco VARCHAR(120) NOT NULL,
    dia_fechamento INTEGER NOT NULL,
    dia_vencimento INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_cartoes_dia_fechamento CHECK (dia_fechamento BETWEEN 1 AND 31),
    CONSTRAINT chk_cartoes_dia_vencimento CHECK (dia_vencimento BETWEEN 1 AND 31),
    CONSTRAINT chk_cartoes_numero CHECK (numero_cartao ~ '^[0-9]{16}$')
);

CREATE TABLE lancamentos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(180) NOT NULL,
    natureza VARCHAR(20) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    categoria_id BIGINT NOT NULL REFERENCES categorias (id),
    conta_id BIGINT REFERENCES contas (id),
    cartao_id BIGINT REFERENCES cartoes (id),
    data_base DATE NOT NULL,
    data_fim_recorrencia DATE,
    total_parcelas INTEGER,
    competencia_fatura_inicial DATE,
    observacao VARCHAR(320),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_lancamentos_valor CHECK (valor >= 0),
    CONSTRAINT chk_lancamentos_origem CHECK (
        (conta_id IS NOT NULL AND cartao_id IS NULL)
        OR (conta_id IS NULL AND cartao_id IS NOT NULL)
    )
);

CREATE TABLE transferencias (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(200) NOT NULL,
    conta_origem_id BIGINT NOT NULL REFERENCES contas (id),
    conta_destino_id BIGINT NOT NULL REFERENCES contas (id),
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    data_base DATE NOT NULL,
    data_fim_recorrencia DATE,
    total_parcelas INTEGER,
    observacao VARCHAR(320),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_transferencias_valor CHECK (valor > 0),
    CONSTRAINT chk_transferencias_contas_diferentes CHECK (conta_origem_id <> conta_destino_id)
);

CREATE TABLE cobrancas_pessoas (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL REFERENCES pessoas (id),
    cartao_id BIGINT NOT NULL REFERENCES cartoes (id),
    categoria_id BIGINT REFERENCES categorias (id),
    descricao VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    data_base DATE NOT NULL,
    data_fim_recorrencia DATE,
    total_parcelas INTEGER,
    competencia_fatura_inicial DATE,
    observacao VARCHAR(320),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_cobrancas_pessoas_valor CHECK (valor >= 0)
);

CREATE TABLE pagamentos_pessoas (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL REFERENCES pessoas (id),
    cartao_id BIGINT NOT NULL REFERENCES cartoes (id),
    descricao VARCHAR(200) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    data_pagamento DATE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_pagamentos_pessoas_valor CHECK (valor >= 0)
);

CREATE TABLE configuracoes_financeiras (
    id BIGSERIAL PRIMARY KEY,
    juros_percentual_padrao NUMERIC(8, 4) NOT NULL DEFAULT 0,
    multa_percentual_padrao NUMERIC(8, 4) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO configuracoes_financeiras (juros_percentual_padrao, multa_percentual_padrao) VALUES (0, 0);

CREATE INDEX idx_lancamentos_categoria_id ON lancamentos (categoria_id);
CREATE INDEX idx_lancamentos_conta_id ON lancamentos (conta_id);
CREATE INDEX idx_lancamentos_cartao_id ON lancamentos (cartao_id);
CREATE INDEX idx_lancamentos_data_base ON lancamentos (data_base);
CREATE INDEX idx_transferencias_data_base ON transferencias (data_base);
CREATE INDEX idx_cobrancas_pessoas_data_base ON cobrancas_pessoas (data_base);
CREATE INDEX idx_pagamentos_pessoas_data_pagamento ON pagamentos_pessoas (data_pagamento);
