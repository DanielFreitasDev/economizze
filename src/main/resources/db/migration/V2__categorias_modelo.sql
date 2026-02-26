-- Insere categorias modelo de despesas e receitas.
-- A tabela de categorias possui nome unico, por isso INVESTIMENTOS entra uma unica vez
-- como categoria compartilhada entre os dois contextos.
INSERT INTO categorias (nome, descricao, ativo, criado_em, atualizado_em)
SELECT novas_categorias.nome,
       novas_categorias.descricao,
       TRUE,
       NOW(),
       NOW()
FROM (VALUES
          ('ALIMENTAÇÃO', 'MODELO DESPESA'),
          ('ASSINATURAS E SERVIÇOS', 'MODELO DESPESA'),
          ('BARES E RESTAURANTES', 'MODELO DESPESA'),
          ('CASA', 'MODELO DESPESA'),
          ('COMPRAS', 'MODELO DESPESA'),
          ('CUIDADOS PESSOAIS', 'MODELO DESPESA'),
          ('DÍVIDAS E EMPRÉSTIMOS', 'MODELO DESPESA'),
          ('EDUCAÇÃO', 'MODELO DESPESA'),
          ('FAMÍLIA E FILHOS', 'MODELO DESPESA'),
          ('IMPOSTOS E TAXAS', 'MODELO DESPESA'),
          ('INVESTIMENTOS', 'MODELO DESPESA/RECEITA'),
          ('LAZER E HOBBIES', 'MODELO DESPESA'),
          ('MERCADO', 'MODELO DESPESA'),
          ('OUTROS', 'MODELO DESPESA'),
          ('PETS', 'MODELO DESPESA'),
          ('PRESENTES E DOAÇÕES', 'MODELO DESPESA'),
          ('ROUPAS', 'MODELO DESPESA'),
          ('SAÚDE', 'MODELO DESPESA'),
          ('TRABALHO', 'MODELO DESPESA'),
          ('TRANSPORTE', 'MODELO DESPESA'),
          ('VIAGEM', 'MODELO DESPESA'),
          ('EMPRÉSTIMOS', 'MODELO RECEITA'),
          ('OUTRAS RECEITAS', 'MODELO RECEITA'),
          ('SALÁRIO', 'MODELO RECEITA')) AS novas_categorias(nome, descricao)
WHERE NOT EXISTS (
    SELECT 1
    FROM categorias categorias_existentes
    WHERE UPPER(categorias_existentes.nome) = UPPER(novas_categorias.nome)
);
