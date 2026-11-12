-- =====================================================================
-- AlertaBairro - 02_seed.sql
-- Dados iniciais, iguais aos do prototipo. As datas sao relativas ao
-- momento da carga (SYSTIMESTAMP - INTERVAL ...).
--
-- Usuarios de exemplo (senhas guardadas com hash BCrypt):
--   morador@alertabairro.com     / morador123     (MORADOR)
--   agente@alertabairro.com      / agente123      (AGENTE)
--   gestor@alertabairro.com      / gestor123      (GESTOR)
--   comunidade@alertabairro.com  / comunidade123  (MORADOR, autor das
--                                 ocorrencias de exemplo)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bairros
-- ---------------------------------------------------------------------
INSERT INTO AB_BAIRRO (NM_BAIRRO) VALUES ('Santana');
INSERT INTO AB_BAIRRO (NM_BAIRRO) VALUES ('Jardim São Paulo');
INSERT INTO AB_BAIRRO (NM_BAIRRO) VALUES ('Tucuruvi');

-- ---------------------------------------------------------------------
-- Tipos de problema
-- ---------------------------------------------------------------------
INSERT INTO AB_TIPO_PROBLEMA (CD_TIPO, NM_TIPO, NR_PESO, FL_ALAGAMENTO) VALUES ('ALAGADO', 'Ponto alagado', 5, 'S');
INSERT INTO AB_TIPO_PROBLEMA (CD_TIPO, NM_TIPO, NR_PESO, FL_ALAGAMENTO) VALUES ('BUEIRO', 'Bueiro entupido', 4, 'N');
INSERT INTO AB_TIPO_PROBLEMA (CD_TIPO, NM_TIPO, NR_PESO, FL_ALAGAMENTO) VALUES ('BOCA', 'Boca de lobo danificada', 3, 'N');
INSERT INTO AB_TIPO_PROBLEMA (CD_TIPO, NM_TIPO, NR_PESO, FL_ALAGAMENTO) VALUES ('LIXO', 'Lixo acumulado', 2, 'N');

-- ---------------------------------------------------------------------
-- Usuarios de exemplo
-- ---------------------------------------------------------------------
INSERT INTO AB_USUARIO (NM_USUARIO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Morador', 'morador@alertabairro.com',
        '$2a$10$pAepkG5g1WMDuIIytLabJebAXXLIBqoPth690hi1QRRW.G10j7LLO', 'MORADOR');

INSERT INTO AB_USUARIO (NM_USUARIO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Agente', 'agente@alertabairro.com',
        '$2a$10$sPg8S8C7o3cm027F33VO6unie.ov7rGWU2lmy1I7uCFyQ1.oMd0QC', 'AGENTE');

INSERT INTO AB_USUARIO (NM_USUARIO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Gestor', 'gestor@alertabairro.com',
        '$2a$10$8lgZZ7YGMRvtfFBfpZRGMOSk1JpLZCGkZKlEAbS1UswoErvp1ousm', 'GESTOR');

INSERT INTO AB_USUARIO (NM_USUARIO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Comunidade', 'comunidade@alertabairro.com',
        '$2a$10$0BLtO/ofqFO4EO32R8N3meJDJOWLe/ZoTOEIy3B28u9D/anmtAHta', 'MORADOR');

-- ---------------------------------------------------------------------
-- Ocorrencias de exemplo (autor: comunidade@alertabairro.com)
-- ---------------------------------------------------------------------

-- 1. Jardim Sao Paulo - BUEIRO - ha 52 h - 9 apoios - RECEBIDA - com localizacao
INSERT INTO AB_OCORRENCIA (ID_BAIRRO, CD_TIPO, ID_USUARIO, DS_OBSERVACOES, DT_CRIACAO,
                           QT_APOIOS, ST_OCORRENCIA, DT_RESOLUCAO, NR_LATITUDE, NR_LONGITUDE)
VALUES ((SELECT ID_BAIRRO FROM AB_BAIRRO WHERE NM_BAIRRO = 'Jardim São Paulo'), 'BUEIRO',
        (SELECT ID_USUARIO FROM AB_USUARIO WHERE DS_EMAIL = 'comunidade@alertabairro.com'),
        'Rua que alaga com facilidade, perto da estação: vários bueiros entupidos de folhas e plástico.',
        SYSTIMESTAMP - INTERVAL '52' HOUR, 9, 'RECEBIDA', NULL,
        -23.492742536565853, -46.61679533666883);

-- 2. Jardim Sao Paulo - ALAGADO - ha 2 h - 6 apoios - RECEBIDA - com localizacao
INSERT INTO AB_OCORRENCIA (ID_BAIRRO, CD_TIPO, ID_USUARIO, DS_OBSERVACOES, DT_CRIACAO,
                           QT_APOIOS, ST_OCORRENCIA, DT_RESOLUCAO, NR_LATITUDE, NR_LONGITUDE)
VALUES ((SELECT ID_BAIRRO FROM AB_BAIRRO WHERE NM_BAIRRO = 'Jardim São Paulo'), 'ALAGADO',
        (SELECT ID_USUARIO FROM AB_USUARIO WHERE DS_EMAIL = 'comunidade@alertabairro.com'),
        'Água na altura do tornozelo na rua e na calçada, perto da entrada da estação.',
        SYSTIMESTAMP - INTERVAL '2' HOUR, 6, 'RECEBIDA', NULL,
        -23.492702536565853, -46.61682533666883);

-- 3. Tucuruvi - LIXO - ha 30 h - 3 apoios - EM_ATENDIMENTO - sem localizacao
INSERT INTO AB_OCORRENCIA (ID_BAIRRO, CD_TIPO, ID_USUARIO, DS_OBSERVACOES, DT_CRIACAO,
                           QT_APOIOS, ST_OCORRENCIA, DT_RESOLUCAO, NR_LATITUDE, NR_LONGITUDE)
VALUES ((SELECT ID_BAIRRO FROM AB_BAIRRO WHERE NM_BAIRRO = 'Tucuruvi'), 'LIXO',
        (SELECT ID_USUARIO FROM AB_USUARIO WHERE DS_EMAIL = 'comunidade@alertabairro.com'),
        'Lixo acumulado na esquina há vários dias.',
        SYSTIMESTAMP - INTERVAL '30' HOUR, 3, 'EM_ATENDIMENTO', NULL,
        NULL, NULL);

-- 4. Santana - BOCA - ha 76 h - 4 apoios - RECEBIDA - sem localizacao
INSERT INTO AB_OCORRENCIA (ID_BAIRRO, CD_TIPO, ID_USUARIO, DS_OBSERVACOES, DT_CRIACAO,
                           QT_APOIOS, ST_OCORRENCIA, DT_RESOLUCAO, NR_LATITUDE, NR_LONGITUDE)
VALUES ((SELECT ID_BAIRRO FROM AB_BAIRRO WHERE NM_BAIRRO = 'Santana'), 'BOCA',
        (SELECT ID_USUARIO FROM AB_USUARIO WHERE DS_EMAIL = 'comunidade@alertabairro.com'),
        'Boca de lobo quebrada, risco para pedestres.',
        SYSTIMESTAMP - INTERVAL '76' HOUR, 4, 'RECEBIDA', NULL,
        NULL, NULL);

-- 5. Santana - BUEIRO - ha 120 h - 7 apoios - RESOLVIDA ha 70 h - sem localizacao
INSERT INTO AB_OCORRENCIA (ID_BAIRRO, CD_TIPO, ID_USUARIO, DS_OBSERVACOES, DT_CRIACAO,
                           QT_APOIOS, ST_OCORRENCIA, DT_RESOLUCAO, NR_LATITUDE, NR_LONGITUDE)
VALUES ((SELECT ID_BAIRRO FROM AB_BAIRRO WHERE NM_BAIRRO = 'Santana'), 'BUEIRO',
        (SELECT ID_USUARIO FROM AB_USUARIO WHERE DS_EMAIL = 'comunidade@alertabairro.com'),
        'Bueiro entupido que alaga sempre que chove.',
        SYSTIMESTAMP - INTERVAL '120' HOUR, 7, 'RESOLVIDA', SYSTIMESTAMP - INTERVAL '70' HOUR,
        NULL, NULL);

-- 6. Jardim Sao Paulo - BUEIRO - ha 200 h - 5 apoios - RESOLVIDA ha 150 h - com localizacao
INSERT INTO AB_OCORRENCIA (ID_BAIRRO, CD_TIPO, ID_USUARIO, DS_OBSERVACOES, DT_CRIACAO,
                           QT_APOIOS, ST_OCORRENCIA, DT_RESOLUCAO, NR_LATITUDE, NR_LONGITUDE)
VALUES ((SELECT ID_BAIRRO FROM AB_BAIRRO WHERE NM_BAIRRO = 'Jardim São Paulo'), 'BUEIRO',
        (SELECT ID_USUARIO FROM AB_USUARIO WHERE DS_EMAIL = 'comunidade@alertabairro.com'),
        'Bueiro entupido na mesma rua da ocorrência anterior.',
        SYSTIMESTAMP - INTERVAL '200' HOUR, 5, 'RESOLVIDA', SYSTIMESTAMP - INTERVAL '150' HOUR,
        -23.492562536565853, -46.61701533666883);

-- ---------------------------------------------------------------------
-- Apoio do autor em cada ocorrencia de exemplo.
-- O contador QT_APOIOS reproduz o prototipo; somente o apoio de quem
-- registrou vira linha em AB_APOIO (os demais apoiadores do prototipo
-- nao existem como usuarios).
-- ---------------------------------------------------------------------
INSERT INTO AB_APOIO (ID_USUARIO, ID_OCORRENCIA, DT_APOIO)
SELECT O.ID_USUARIO, O.ID_OCORRENCIA, O.DT_CRIACAO
  FROM AB_OCORRENCIA O;

COMMIT;
