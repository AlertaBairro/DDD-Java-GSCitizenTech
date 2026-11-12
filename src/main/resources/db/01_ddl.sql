-- =====================================================================
-- AlertaBairro - 01_ddl.sql
-- Criacao das tabelas no Oracle da FIAP.
-- Todas as tabelas usam o prefixo AB_ para nao colidir com outras
-- tabelas da conta. Datas usam TIMESTAMP WITH TIME ZONE para que o
-- calculo de "dias desde a criacao" seja correto independentemente do
-- fuso do servidor do banco e do servidor da aplicacao.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bairros atendidos (Santana, Jardim Sao Paulo, Tucuruvi)
-- ---------------------------------------------------------------------
CREATE TABLE AB_BAIRRO (
    ID_BAIRRO   NUMBER(10)    GENERATED ALWAYS AS IDENTITY,
    NM_BAIRRO   VARCHAR2(80)  NOT NULL,
    CONSTRAINT PK_AB_BAIRRO PRIMARY KEY (ID_BAIRRO),
    CONSTRAINT UK_AB_BAIRRO_NOME UNIQUE (NM_BAIRRO)
);

-- ---------------------------------------------------------------------
-- Tipos de problema (com peso para o calculo de risco)
-- ---------------------------------------------------------------------
CREATE TABLE AB_TIPO_PROBLEMA (
    CD_TIPO        VARCHAR2(20)  NOT NULL,
    NM_TIPO        VARCHAR2(80)  NOT NULL,
    NR_PESO        NUMBER(2)     NOT NULL,
    FL_ALAGAMENTO  CHAR(1)       DEFAULT 'N' NOT NULL,
    CONSTRAINT PK_AB_TIPO_PROBLEMA PRIMARY KEY (CD_TIPO),
    CONSTRAINT UK_AB_TIPO_PROBLEMA_NOME UNIQUE (NM_TIPO),
    CONSTRAINT CK_AB_TIPO_PESO CHECK (NR_PESO BETWEEN 1 AND 10),
    CONSTRAINT CK_AB_TIPO_ALAGAMENTO CHECK (FL_ALAGAMENTO IN ('S', 'N'))
);

-- ---------------------------------------------------------------------
-- Usuarios (morador, agente de zeladoria e gestor da subprefeitura)
-- ---------------------------------------------------------------------
CREATE TABLE AB_USUARIO (
    ID_USUARIO     NUMBER(10)                 GENERATED ALWAYS AS IDENTITY,
    NM_USUARIO     VARCHAR2(120)              NOT NULL,
    DS_EMAIL       VARCHAR2(150)              NOT NULL,
    DS_SENHA_HASH  VARCHAR2(100)              NOT NULL,
    TP_PERFIL      VARCHAR2(10)               NOT NULL,
    DT_CADASTRO    TIMESTAMP WITH TIME ZONE   DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_AB_USUARIO PRIMARY KEY (ID_USUARIO),
    CONSTRAINT UK_AB_USUARIO_EMAIL UNIQUE (DS_EMAIL),
    CONSTRAINT CK_AB_USUARIO_PERFIL CHECK (TP_PERFIL IN ('MORADOR', 'AGENTE', 'GESTOR'))
);

-- ---------------------------------------------------------------------
-- Ocorrencias registradas pelos moradores
-- ---------------------------------------------------------------------
CREATE TABLE AB_OCORRENCIA (
    ID_OCORRENCIA   NUMBER(10)                GENERATED ALWAYS AS IDENTITY,
    ID_BAIRRO       NUMBER(10)                NOT NULL,
    CD_TIPO         VARCHAR2(20)              NOT NULL,
    ID_USUARIO      NUMBER(10)                NOT NULL,
    DS_OBSERVACOES  VARCHAR2(400),
    DT_CRIACAO      TIMESTAMP WITH TIME ZONE  DEFAULT SYSTIMESTAMP NOT NULL,
    QT_APOIOS       NUMBER(6)                 DEFAULT 1 NOT NULL,
    ST_OCORRENCIA   VARCHAR2(20)              DEFAULT 'RECEBIDA' NOT NULL,
    DT_RESOLUCAO    TIMESTAMP WITH TIME ZONE,
    NR_LATITUDE     NUMBER(18, 15),
    NR_LONGITUDE    NUMBER(18, 15),
    CONSTRAINT PK_AB_OCORRENCIA PRIMARY KEY (ID_OCORRENCIA),
    CONSTRAINT FK_AB_OCORRENCIA_BAIRRO FOREIGN KEY (ID_BAIRRO)
        REFERENCES AB_BAIRRO (ID_BAIRRO),
    CONSTRAINT FK_AB_OCORRENCIA_TIPO FOREIGN KEY (CD_TIPO)
        REFERENCES AB_TIPO_PROBLEMA (CD_TIPO),
    CONSTRAINT FK_AB_OCORRENCIA_USUARIO FOREIGN KEY (ID_USUARIO)
        REFERENCES AB_USUARIO (ID_USUARIO),
    CONSTRAINT CK_AB_OCORRENCIA_STATUS
        CHECK (ST_OCORRENCIA IN ('RECEBIDA', 'EM_ATENDIMENTO', 'RESOLVIDA')),
    CONSTRAINT CK_AB_OCORRENCIA_APOIOS CHECK (QT_APOIOS >= 0),
    CONSTRAINT CK_AB_OCORRENCIA_LATITUDE CHECK (NR_LATITUDE BETWEEN -90 AND 90),
    CONSTRAINT CK_AB_OCORRENCIA_LONGITUDE CHECK (NR_LONGITUDE BETWEEN -180 AND 180),
    -- latitude e longitude sao informadas juntas ou nenhuma das duas
    CONSTRAINT CK_AB_OCORRENCIA_LOCALIZACAO CHECK (
        (NR_LATITUDE IS NULL AND NR_LONGITUDE IS NULL) OR
        (NR_LATITUDE IS NOT NULL AND NR_LONGITUDE IS NOT NULL)
    ),
    -- so existe data de resolucao quando o status e RESOLVIDA
    CONSTRAINT CK_AB_OCORRENCIA_RESOLUCAO CHECK (
        (ST_OCORRENCIA = 'RESOLVIDA' AND DT_RESOLUCAO IS NOT NULL) OR
        (ST_OCORRENCIA <> 'RESOLVIDA' AND DT_RESOLUCAO IS NULL)
    )
);

CREATE INDEX IX_AB_OCORRENCIA_BAIRRO ON AB_OCORRENCIA (ID_BAIRRO);
CREATE INDEX IX_AB_OCORRENCIA_TIPO ON AB_OCORRENCIA (CD_TIPO);
CREATE INDEX IX_AB_OCORRENCIA_USUARIO ON AB_OCORRENCIA (ID_USUARIO);

-- ---------------------------------------------------------------------
-- Apoios: um apoio por usuario por ocorrencia
-- ---------------------------------------------------------------------
CREATE TABLE AB_APOIO (
    ID_APOIO       NUMBER(10)                GENERATED ALWAYS AS IDENTITY,
    ID_USUARIO     NUMBER(10)                NOT NULL,
    ID_OCORRENCIA  NUMBER(10)                NOT NULL,
    DT_APOIO       TIMESTAMP WITH TIME ZONE  DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_AB_APOIO PRIMARY KEY (ID_APOIO),
    CONSTRAINT UK_AB_APOIO_USUARIO_OCORRENCIA UNIQUE (ID_USUARIO, ID_OCORRENCIA),
    CONSTRAINT FK_AB_APOIO_USUARIO FOREIGN KEY (ID_USUARIO)
        REFERENCES AB_USUARIO (ID_USUARIO) ON DELETE CASCADE,
    CONSTRAINT FK_AB_APOIO_OCORRENCIA FOREIGN KEY (ID_OCORRENCIA)
        REFERENCES AB_OCORRENCIA (ID_OCORRENCIA) ON DELETE CASCADE
);

CREATE INDEX IX_AB_APOIO_OCORRENCIA ON AB_APOIO (ID_OCORRENCIA);
