-- AlertaBairro - DDL
-- Arthur Araújo Tenório (RM 562272) | Breno Gonçalves Báo (RM 564037)
-- Rafael Falaguasta Ferraz (RM 561714) | Vinicius Cavalcanti dos Reis (RM 562063)

DROP TABLE T_AB_ALERTA_USUARIO   CASCADE CONSTRAINTS;
DROP TABLE T_AB_ALERTA           CASCADE CONSTRAINTS;
DROP TABLE T_AB_PONTO_ALAGADO    CASCADE CONSTRAINTS;
DROP TABLE T_AB_HISTORICO_STATUS CASCADE CONSTRAINTS;
DROP TABLE T_AB_APOIO            CASCADE CONSTRAINTS;
DROP TABLE T_AB_CONSULTA_ROTA    CASCADE CONSTRAINTS;
DROP TABLE T_AB_OCORRENCIA       CASCADE CONSTRAINTS;
DROP TABLE T_AB_TIPO_OCORRENCIA  CASCADE CONSTRAINTS;
DROP TABLE T_AB_USUARIO          CASCADE CONSTRAINTS;
DROP TABLE T_AB_BAIRRO           CASCADE CONSTRAINTS;

CREATE TABLE T_AB_BAIRRO (
    id_bairro        NUMBER(5)    NOT NULL,
    nm_bairro        VARCHAR2(60) NOT NULL,
    nm_zona          VARCHAR2(20) NOT NULL,
    nm_subprefeitura VARCHAR2(60) NOT NULL,
    CONSTRAINT PK_AB_BAIRRO      PRIMARY KEY (id_bairro),
    CONSTRAINT UK_AB_BAIRRO_NOME UNIQUE (nm_bairro),
    CONSTRAINT CK_AB_BAIRRO_ZONA CHECK (nm_zona IN ('NORTE','SUL','LESTE','OESTE','CENTRO'))
);

CREATE TABLE T_AB_USUARIO (
    id_usuario    NUMBER(10)    NOT NULL,
    id_bairro     NUMBER(5)     NOT NULL,
    nm_usuario    VARCHAR2(100) NOT NULL,
    ds_email      VARCHAR2(120) NOT NULL,
    ds_senha_hash VARCHAR2(255) NOT NULL,
    nr_telefone   VARCHAR2(15),
    tp_perfil     VARCHAR2(10)  NOT NULL,
    dt_cadastro   DATE          DEFAULT SYSDATE NOT NULL,
    st_ativo      CHAR(1)       DEFAULT 'S' NOT NULL,
    CONSTRAINT PK_AB_USUARIO        PRIMARY KEY (id_usuario),
    CONSTRAINT UK_AB_USUARIO_EMAIL  UNIQUE (ds_email),
    CONSTRAINT CK_AB_USUARIO_PERFIL CHECK (tp_perfil IN ('MORADOR','AGENTE','GESTOR')),
    CONSTRAINT CK_AB_USUARIO_ATIVO  CHECK (st_ativo IN ('S','N'))
);

CREATE TABLE T_AB_TIPO_OCORRENCIA (
    id_tipo_ocorrencia NUMBER(3)     NOT NULL,
    nm_tipo            VARCHAR2(50)  NOT NULL,
    ds_tipo            VARCHAR2(200),
    nr_peso_risco      NUMBER(1)     NOT NULL,
    CONSTRAINT PK_AB_TIPO_OCORRENCIA PRIMARY KEY (id_tipo_ocorrencia),
    CONSTRAINT UK_AB_TIPO_NOME       UNIQUE (nm_tipo),
    CONSTRAINT CK_AB_TIPO_PESO       CHECK (nr_peso_risco BETWEEN 1 AND 5)
);

CREATE TABLE T_AB_OCORRENCIA (
    id_ocorrencia      NUMBER(10)    NOT NULL,
    id_usuario         NUMBER(10)    NOT NULL,
    id_tipo_ocorrencia NUMBER(3)     NOT NULL,
    id_bairro          NUMBER(5)     NOT NULL,
    ds_ocorrencia      VARCHAR2(500),
    ds_endereco_ref    VARCHAR2(150) NOT NULL,
    nr_latitude        NUMBER(9,6)   NOT NULL,
    nr_longitude       NUMBER(9,6)   NOT NULL,
    ds_url_foto        VARCHAR2(300),
    dt_registro        DATE          DEFAULT SYSDATE NOT NULL,
    nr_nivel_risco     NUMBER(1)     NOT NULL,
    st_ocorrencia      VARCHAR2(15)  DEFAULT 'ABERTA' NOT NULL,
    dt_resolucao       DATE,
    CONSTRAINT PK_AB_OCORRENCIA        PRIMARY KEY (id_ocorrencia),
    CONSTRAINT CK_AB_OCORRENCIA_LAT    CHECK (nr_latitude  BETWEEN -90  AND 90),
    CONSTRAINT CK_AB_OCORRENCIA_LONG   CHECK (nr_longitude BETWEEN -180 AND 180),
    CONSTRAINT CK_AB_OCORRENCIA_RISCO  CHECK (nr_nivel_risco BETWEEN 1 AND 5),
    CONSTRAINT CK_AB_OCORRENCIA_STATUS CHECK (st_ocorrencia IN ('ABERTA','RECEBIDA','EM_ATENDIMENTO','RESOLVIDA')),
    CONSTRAINT CK_AB_OCORRENCIA_RESOL  CHECK (
        (st_ocorrencia = 'RESOLVIDA'  AND dt_resolucao IS NOT NULL AND dt_resolucao >= dt_registro) OR
        (st_ocorrencia <> 'RESOLVIDA' AND dt_resolucao IS NULL))
);

CREATE TABLE T_AB_APOIO (
    id_ocorrencia NUMBER(10) NOT NULL,
    id_usuario    NUMBER(10) NOT NULL,
    dt_apoio      DATE       DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_AB_APOIO PRIMARY KEY (id_ocorrencia, id_usuario)
);

CREATE TABLE T_AB_HISTORICO_STATUS (
    id_historico  NUMBER(10)    NOT NULL,
    id_ocorrencia NUMBER(10)    NOT NULL,
    id_usuario    NUMBER(10)    NOT NULL,
    st_anterior   VARCHAR2(15)  NOT NULL,
    st_novo       VARCHAR2(15)  NOT NULL,
    dt_alteracao  DATE          DEFAULT SYSDATE NOT NULL,
    ds_observacao VARCHAR2(300),
    CONSTRAINT PK_AB_HISTORICO_STATUS PRIMARY KEY (id_historico),
    CONSTRAINT CK_AB_HIST_ANTERIOR    CHECK (st_anterior IN ('ABERTA','RECEBIDA','EM_ATENDIMENTO','RESOLVIDA')),
    CONSTRAINT CK_AB_HIST_NOVO        CHECK (st_novo     IN ('ABERTA','RECEBIDA','EM_ATENDIMENTO','RESOLVIDA')),
    CONSTRAINT CK_AB_HIST_DIFERENTE   CHECK (st_anterior <> st_novo)
);

CREATE TABLE T_AB_PONTO_ALAGADO (
    id_ponto_alagado NUMBER(10)    NOT NULL,
    id_usuario       NUMBER(10)    NOT NULL,
    id_bairro        NUMBER(5)     NOT NULL,
    ds_endereco_ref  VARCHAR2(150) NOT NULL,
    nr_latitude      NUMBER(9,6)   NOT NULL,
    nr_longitude     NUMBER(9,6)   NOT NULL,
    tp_nivel_agua    VARCHAR2(15)  NOT NULL,
    dt_reporte       DATE          DEFAULT SYSDATE NOT NULL,
    st_ativo         CHAR(1)       DEFAULT 'S' NOT NULL,
    dt_encerramento  DATE,
    CONSTRAINT PK_AB_PONTO_ALAGADO PRIMARY KEY (id_ponto_alagado),
    CONSTRAINT CK_AB_PONTO_LAT     CHECK (nr_latitude  BETWEEN -90  AND 90),
    CONSTRAINT CK_AB_PONTO_LONG    CHECK (nr_longitude BETWEEN -180 AND 180),
    CONSTRAINT CK_AB_PONTO_NIVEL   CHECK (tp_nivel_agua IN ('TORNOZELO','JOELHO','CINTURA','INTRANSITAVEL')),
    CONSTRAINT CK_AB_PONTO_ATIVO   CHECK (
        (st_ativo = 'S' AND dt_encerramento IS NULL) OR
        (st_ativo = 'N' AND dt_encerramento IS NOT NULL AND dt_encerramento >= dt_reporte))
);

CREATE TABLE T_AB_ALERTA (
    id_alerta        NUMBER(10)    NOT NULL,
    id_ponto_alagado NUMBER(10)    NOT NULL,
    ds_mensagem      VARCHAR2(300) NOT NULL,
    nr_raio_metros   NUMBER(5)     NOT NULL,
    dt_envio         DATE          DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_AB_ALERTA      PRIMARY KEY (id_alerta),
    CONSTRAINT CK_AB_ALERTA_RAIO CHECK (nr_raio_metros > 0)
);

CREATE TABLE T_AB_ALERTA_USUARIO (
    id_alerta      NUMBER(10) NOT NULL,
    id_usuario     NUMBER(10) NOT NULL,
    dt_recebimento DATE       DEFAULT SYSDATE NOT NULL,
    dt_leitura     DATE,
    CONSTRAINT PK_AB_ALERTA_USUARIO PRIMARY KEY (id_alerta, id_usuario),
    CONSTRAINT CK_AB_ALERTA_LEITURA CHECK (dt_leitura IS NULL OR dt_leitura >= dt_recebimento)
);

CREATE TABLE T_AB_CONSULTA_ROTA (
    id_consulta_rota   NUMBER(10)    NOT NULL,
    id_usuario         NUMBER(10)    NOT NULL,
    ds_origem          VARCHAR2(150) NOT NULL,
    nr_lat_origem      NUMBER(9,6)   NOT NULL,
    nr_long_origem     NUMBER(9,6)   NOT NULL,
    ds_destino         VARCHAR2(150) NOT NULL,
    nr_lat_destino     NUMBER(9,6)   NOT NULL,
    nr_long_destino    NUMBER(9,6)   NOT NULL,
    dt_consulta        DATE          DEFAULT SYSDATE NOT NULL,
    st_rota_calculada  CHAR(1)       NOT NULL,
    nr_pontos_evitados NUMBER(3)     DEFAULT 0 NOT NULL,
    CONSTRAINT PK_AB_CONSULTA_ROTA  PRIMARY KEY (id_consulta_rota),
    CONSTRAINT CK_AB_ROTA_CALCULADA CHECK (st_rota_calculada IN ('S','N')),
    CONSTRAINT CK_AB_ROTA_PONTOS    CHECK (nr_pontos_evitados >= 0)
);

ALTER TABLE T_AB_USUARIO          ADD CONSTRAINT FK_AB_USUARIO_BAIRRO       FOREIGN KEY (id_bairro)          REFERENCES T_AB_BAIRRO (id_bairro);
ALTER TABLE T_AB_OCORRENCIA       ADD CONSTRAINT FK_AB_OCORRENCIA_USUARIO   FOREIGN KEY (id_usuario)         REFERENCES T_AB_USUARIO (id_usuario);
ALTER TABLE T_AB_OCORRENCIA       ADD CONSTRAINT FK_AB_OCORRENCIA_TIPO      FOREIGN KEY (id_tipo_ocorrencia) REFERENCES T_AB_TIPO_OCORRENCIA (id_tipo_ocorrencia);
ALTER TABLE T_AB_OCORRENCIA       ADD CONSTRAINT FK_AB_OCORRENCIA_BAIRRO    FOREIGN KEY (id_bairro)          REFERENCES T_AB_BAIRRO (id_bairro);
ALTER TABLE T_AB_APOIO            ADD CONSTRAINT FK_AB_APOIO_OCORRENCIA     FOREIGN KEY (id_ocorrencia)      REFERENCES T_AB_OCORRENCIA (id_ocorrencia);
ALTER TABLE T_AB_APOIO            ADD CONSTRAINT FK_AB_APOIO_USUARIO        FOREIGN KEY (id_usuario)         REFERENCES T_AB_USUARIO (id_usuario);
ALTER TABLE T_AB_HISTORICO_STATUS ADD CONSTRAINT FK_AB_HISTORICO_OCORRENCIA FOREIGN KEY (id_ocorrencia)      REFERENCES T_AB_OCORRENCIA (id_ocorrencia);
ALTER TABLE T_AB_HISTORICO_STATUS ADD CONSTRAINT FK_AB_HISTORICO_USUARIO    FOREIGN KEY (id_usuario)         REFERENCES T_AB_USUARIO (id_usuario);
ALTER TABLE T_AB_PONTO_ALAGADO    ADD CONSTRAINT FK_AB_PONTO_USUARIO        FOREIGN KEY (id_usuario)         REFERENCES T_AB_USUARIO (id_usuario);
ALTER TABLE T_AB_PONTO_ALAGADO    ADD CONSTRAINT FK_AB_PONTO_BAIRRO         FOREIGN KEY (id_bairro)          REFERENCES T_AB_BAIRRO (id_bairro);
ALTER TABLE T_AB_ALERTA           ADD CONSTRAINT FK_AB_ALERTA_PONTO         FOREIGN KEY (id_ponto_alagado)   REFERENCES T_AB_PONTO_ALAGADO (id_ponto_alagado);
ALTER TABLE T_AB_ALERTA_USUARIO   ADD CONSTRAINT FK_AB_ALERTA_USU_ALERTA    FOREIGN KEY (id_alerta)          REFERENCES T_AB_ALERTA (id_alerta);
ALTER TABLE T_AB_ALERTA_USUARIO   ADD CONSTRAINT FK_AB_ALERTA_USU_USUARIO   FOREIGN KEY (id_usuario)         REFERENCES T_AB_USUARIO (id_usuario);
ALTER TABLE T_AB_CONSULTA_ROTA    ADD CONSTRAINT FK_AB_ROTA_USUARIO         FOREIGN KEY (id_usuario)         REFERENCES T_AB_USUARIO (id_usuario);
