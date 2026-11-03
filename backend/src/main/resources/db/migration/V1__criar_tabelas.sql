-- Sistema de Moeda Estudantil - Lab03S02
-- Esquema do banco (implementa o Modelo ER: docs/sprint-02/modelo-er.puml)
-- Compatível com MySQL 8 e com H2 em modo MySQL (perfil h2 e testes).

CREATE TABLE instituicao_ensino (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    CONSTRAINT pk_instituicao_ensino PRIMARY KEY (id),
    CONSTRAINT uk_instituicao_ensino_nome UNIQUE (nome)
);

CREATE TABLE conta (
    id    BIGINT NOT NULL AUTO_INCREMENT,
    saldo INT    NOT NULL DEFAULT 0,
    CONSTRAINT pk_conta PRIMARY KEY (id),
    CONSTRAINT ck_conta_saldo CHECK (saldo >= 0)
);

-- Herança JOINED: dados comuns em "usuario"; "tipo" é o discriminador do subtipo.
CREATE TABLE usuario (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    tipo  VARCHAR(20)  NOT NULL,
    nome  VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL,
    senha VARCHAR(60)  NOT NULL,
    ativo BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_tipo CHECK (tipo IN ('ALUNO', 'PROFESSOR', 'EMPRESA'))
);

CREATE TABLE aluno (
    id             BIGINT       NOT NULL,
    cpf            VARCHAR(11)  NOT NULL,
    rg             VARCHAR(20)  NOT NULL,
    curso          VARCHAR(100) NOT NULL,
    logradouro     VARCHAR(150) NOT NULL,
    numero         VARCHAR(10)  NOT NULL,
    complemento    VARCHAR(60),
    bairro         VARCHAR(80)  NOT NULL,
    cidade         VARCHAR(80)  NOT NULL,
    estado         VARCHAR(2)   NOT NULL,
    cep            VARCHAR(8)   NOT NULL,
    instituicao_id BIGINT       NOT NULL,
    conta_id       BIGINT       NOT NULL,
    CONSTRAINT pk_aluno PRIMARY KEY (id),
    CONSTRAINT uk_aluno_cpf UNIQUE (cpf),
    CONSTRAINT uk_aluno_conta UNIQUE (conta_id),
    CONSTRAINT fk_aluno_usuario FOREIGN KEY (id) REFERENCES usuario (id),
    CONSTRAINT fk_aluno_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao_ensino (id),
    CONSTRAINT fk_aluno_conta FOREIGN KEY (conta_id) REFERENCES conta (id)
);

CREATE TABLE professor (
    id             BIGINT       NOT NULL,
    cpf            VARCHAR(11)  NOT NULL,
    departamento   VARCHAR(100) NOT NULL,
    instituicao_id BIGINT       NOT NULL,
    conta_id       BIGINT       NOT NULL,
    CONSTRAINT pk_professor PRIMARY KEY (id),
    CONSTRAINT uk_professor_cpf UNIQUE (cpf),
    CONSTRAINT uk_professor_conta UNIQUE (conta_id),
    CONSTRAINT fk_professor_usuario FOREIGN KEY (id) REFERENCES usuario (id),
    CONSTRAINT fk_professor_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao_ensino (id),
    CONSTRAINT fk_professor_conta FOREIGN KEY (conta_id) REFERENCES conta (id)
);

CREATE TABLE empresa_parceira (
    id   BIGINT      NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    CONSTRAINT pk_empresa_parceira PRIMARY KEY (id),
    CONSTRAINT uk_empresa_parceira_cnpj UNIQUE (cnpj),
    CONSTRAINT fk_empresa_parceira_usuario FOREIGN KEY (id) REFERENCES usuario (id)
);

CREATE TABLE vantagem (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    nome         VARCHAR(120)  NOT NULL,
    descricao    VARCHAR(1000) NOT NULL,
    foto_url     VARCHAR(500)  NOT NULL,
    custo_moedas INT           NOT NULL,
    ativa        BOOLEAN       NOT NULL DEFAULT TRUE,
    empresa_id   BIGINT        NOT NULL,
    CONSTRAINT pk_vantagem PRIMARY KEY (id),
    CONSTRAINT fk_vantagem_empresa FOREIGN KEY (empresa_id) REFERENCES empresa_parceira (id),
    CONSTRAINT ck_vantagem_custo CHECK (custo_moedas > 0)
);

-- Herança SINGLE_TABLE: os três tipos de transação ficam na mesma tabela; "tipo" é o discriminador.
-- O CHECK garante, para cada tipo, as colunas obrigatórias e o uso de origem/destino.
CREATE TABLE transacao (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    tipo             VARCHAR(30) NOT NULL,
    valor            INT         NOT NULL,
    data_hora        DATETIME(6) NOT NULL,
    conta_origem_id  BIGINT,
    conta_destino_id BIGINT,
    semestre         VARCHAR(6),
    motivo           VARCHAR(500),
    codigo_cupom     VARCHAR(20),
    vantagem_id      BIGINT,
    CONSTRAINT pk_transacao PRIMARY KEY (id),
    CONSTRAINT uk_transacao_codigo_cupom UNIQUE (codigo_cupom),
    CONSTRAINT uk_transacao_credito_semestral UNIQUE (conta_destino_id, semestre),
    CONSTRAINT fk_transacao_conta_origem FOREIGN KEY (conta_origem_id) REFERENCES conta (id),
    CONSTRAINT fk_transacao_conta_destino FOREIGN KEY (conta_destino_id) REFERENCES conta (id),
    CONSTRAINT fk_transacao_vantagem FOREIGN KEY (vantagem_id) REFERENCES vantagem (id),
    CONSTRAINT ck_transacao_valor CHECK (valor > 0),
    CONSTRAINT ck_transacao_tipo CHECK (
        (tipo = 'CREDITO_SEMESTRAL' AND semestre IS NOT NULL
            AND conta_origem_id IS NULL AND conta_destino_id IS NOT NULL)
        OR (tipo = 'ENVIO_MOEDAS' AND motivo IS NOT NULL
            AND conta_origem_id IS NOT NULL AND conta_destino_id IS NOT NULL)
        OR (tipo = 'RESGATE_VANTAGEM' AND codigo_cupom IS NOT NULL AND vantagem_id IS NOT NULL
            AND conta_origem_id IS NOT NULL AND conta_destino_id IS NULL)
    )
);

CREATE INDEX ix_transacao_data_hora ON transacao (data_hora);
