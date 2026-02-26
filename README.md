# Economizze

Aplicacao web de gestao financeira pessoal com foco em:

- Fluxo mensal de receitas e despesas (avulso, recorrente, parcelado)
- Controle de cartoes de credito com competencia de fatura
- Cobrancas de terceiros por pessoa/cartao
- Pagamentos parciais, encargos (juros/multa) e saldo em aberto
- Relatorio analitico por pessoa com geracao em PDF
- Seguranca com perfis `ADMIN` e `VIEW_ONLY`

## Tecnologias

- Java 21
- Spring Boot 3.5.x
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL
- Thymeleaf + CSS + JavaScript
- Swagger/OpenAPI (`springdoc`)
- OpenPDF

## Principais funcionalidades

- Setup inicial do primeiro administrador (`/setup`) quando nao existe usuario.
- Login por usuario/senha.
- Perfis:
  - `ADMIN`: acesso total (criar/editar/desativar/excluir)
  - `VIEW_ONLY`: acesso somente leitura
- Cadastros:
  - Pessoas
  - Categorias
  - Contas
  - Cartoes
  - Usuarios (admin)
- Movimentacoes:
  - Lancamentos (receita/despesa)
  - Transferencias
  - Cobrancas por pessoa/cartao
  - Pagamentos e encargos
- Dashboard mensal consolidado.
- Relatorio por pessoa com exportacao PDF.
- Swagger para API REST em `/swagger-ui.html`.

## Regras implementadas

- Tabelas em plural e modelos em singular.
- Campos textuais padronizados em maiusculo (exceto e-mail e campos numericos).
- CPF armazenado somente com digitos; frontend com mascara.
- Numero do cartao com 16 digitos; frontend com mascara.
- Selecao de 5 competencias de fatura por cartao (mes aberto atual, -2 e +2).
- Acoes de listagem com fluxo:
  - `VER`
  - `EDITAR`
  - `DESATIVAR`
  - `EXCLUIR` (bloqueado quando houver vinculo)
- Valores monetarios no frontend em formato BRL (`R$ 1.234,56`).
- Integracao frontend com ViaCEP para auto-preenchimento de endereco.

## Estrutura de pacotes

`src/main/java/io/nexus/economizze`

- `config`
- `shared`
- `usuario`
- `pessoa`
- `categoria`
- `conta`
- `cartao`
- `lancamento`
- `transferencia`
- `cobranca`
- `configuracaofinanceira`
- `dashboard`
- `relatorio`

## Banco de dados

Migracoes Flyway em:

- `src/main/resources/db/migration`

Migracao inicial:

- `V1__estrutura_inicial.sql`

Datasource usa variaveis de ambiente com fallback local:

```properties
DB_URL=jdbc:postgresql://localhost:5432/economizze
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

## Como executar localmente

### 1) Subir PostgreSQL

Crie o banco `economizze`.

Exemplo rapido:

```sql
CREATE DATABASE economizze;
```

### 2) Rodar aplicacao

```bash
./mvnw spring-boot:run
```

Acesso:

- App: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

### 3) Primeiro acesso

- Ao abrir o sistema sem usuarios, voce sera redirecionado para `/setup`.
- Crie o primeiro usuario administrador.

## Testes e build

Executar testes:

```bash
./mvnw test
```

Build do jar:

```bash
./mvnw clean package
```

Jar gerado:

- `target/economizze-0.0.1-SNAPSHOT.jar`

## Docker

### Build da imagem

```bash
docker build -t economizze:latest .
```

### Execucao

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/economizze \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  economizze:latest
```

## Endpoints API (resumo)

Base: `/api`

- `/api/pessoas`
- `/api/categorias`
- `/api/contas`
- `/api/cartoes`
- `/api/dashboard`
- `/api/relatorios/pessoas/{pessoaId}`

As operacoes de escrita exigem perfil `ADMIN`.

## Observacoes de operacao

- Logs configurados para facilitar rastreio de eventos e erros.
- Erros REST seguem contrato padrao:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "path": "/api/...",
  "details": []
}
```

- Para producao, recomenda-se:
  - configurar segredo/senhas via ambiente
  - habilitar observabilidade centralizada
  - criar backup recorrente do PostgreSQL
