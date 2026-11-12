-- AlertaBairro - Complemento da API Java
-- Executar DEPOIS de 01_ddl.sql e 02_dml.sql.
-- Nao altera a estrutura das tabelas do modelo do grupo: apenas cria as
-- sequences usadas pela API para gerar os IDs e insere dados de demonstracao
-- (usuarios de teste com senha BCrypt real, uma ocorrencia ativa e um alerta).
-- Pode ser executado mais de uma vez.

-- ---------------------------------------------------------------------------
-- 1) Sequences: cada uma comeca em MAX(id) + 1 da tabela correspondente
-- ---------------------------------------------------------------------------
DECLARE
    TYPE t_lista IS TABLE OF VARCHAR2(120);
    v_seqs    t_lista := t_lista('SQ_AB_USUARIO', 'SQ_AB_OCORRENCIA', 'SQ_AB_HISTORICO_STATUS',
                                 'SQ_AB_PONTO_ALAGADO', 'SQ_AB_ALERTA', 'SQ_AB_CONSULTA_ROTA');
    v_tabelas t_lista := t_lista('T_AB_USUARIO', 'T_AB_OCORRENCIA', 'T_AB_HISTORICO_STATUS',
                                 'T_AB_PONTO_ALAGADO', 'T_AB_ALERTA', 'T_AB_CONSULTA_ROTA');
    v_colunas t_lista := t_lista('id_usuario', 'id_ocorrencia', 'id_historico',
                                 'id_ponto_alagado', 'id_alerta', 'id_consulta_rota');
    v_inicio  NUMBER;
BEGIN
    FOR i IN 1 .. v_seqs.COUNT LOOP
        BEGIN
            EXECUTE IMMEDIATE 'DROP SEQUENCE ' || v_seqs(i);
        EXCEPTION
            WHEN OTHERS THEN
                IF SQLCODE != -2289 THEN -- ORA-02289: sequence inexistente
                    RAISE;
                END IF;
        END;
        EXECUTE IMMEDIATE 'SELECT NVL(MAX(' || v_colunas(i) || '), 0) + 1 FROM ' || v_tabelas(i)
            INTO v_inicio;
        EXECUTE IMMEDIATE 'CREATE SEQUENCE ' || v_seqs(i)
            || ' START WITH ' || v_inicio || ' INCREMENT BY 1 NOCACHE';
    END LOOP;
END;
/

-- ---------------------------------------------------------------------------
-- 2) Usuarios de teste (bairro 1 - Jardim Sao Paulo) com hash BCrypt real
--    morador@alertabairro.com / morador123  (MORADOR)
--    agente@alertabairro.com  / agente123   (AGENTE)
--    gestor@alertabairro.com  / gestor123   (GESTOR)
-- ---------------------------------------------------------------------------
INSERT INTO T_AB_USUARIO (id_usuario, id_bairro, nm_usuario, ds_email, ds_senha_hash, nr_telefone, tp_perfil, dt_cadastro, st_ativo)
SELECT SQ_AB_USUARIO.NEXTVAL, 1, 'Morador de Teste', 'morador@alertabairro.com',
       '$2a$10$2QRc5/P3FWPCM3BUGs.asOXm3trVlES8FZWb3me2TJ.YzEmIW30u2', '11900000001', 'MORADOR', SYSDATE, 'S'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM T_AB_USUARIO WHERE ds_email = 'morador@alertabairro.com');

INSERT INTO T_AB_USUARIO (id_usuario, id_bairro, nm_usuario, ds_email, ds_senha_hash, nr_telefone, tp_perfil, dt_cadastro, st_ativo)
SELECT SQ_AB_USUARIO.NEXTVAL, 1, 'Agente de Teste', 'agente@alertabairro.com',
       '$2a$10$h5f9rhzRbSRCeQQ0U0VZie9bdwTGdN3HIqWeQj59/ZLebl4eYGxaO', '11900000002', 'AGENTE', SYSDATE, 'S'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM T_AB_USUARIO WHERE ds_email = 'agente@alertabairro.com');

INSERT INTO T_AB_USUARIO (id_usuario, id_bairro, nm_usuario, ds_email, ds_senha_hash, nr_telefone, tp_perfil, dt_cadastro, st_ativo)
SELECT SQ_AB_USUARIO.NEXTVAL, 1, 'Gestor de Teste', 'gestor@alertabairro.com',
       '$2a$10$n7docGSVy.S4OF513.UkMOGpll81KmRwkboiN5Cr6my8pxcstrsB2', '11900000003', 'GESTOR', SYSDATE, 'S'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM T_AB_USUARIO WHERE ds_email = 'gestor@alertabairro.com');

-- ---------------------------------------------------------------------------
-- 3) Ocorrencia ativa perto da Estacao Jardim Sao Paulo (autora: usuaria 2),
--    para o morador de teste poder apoiar e o agente poder priorizar
-- ---------------------------------------------------------------------------
INSERT INTO T_AB_OCORRENCIA (id_ocorrencia, id_usuario, id_tipo_ocorrencia, id_bairro, ds_ocorrencia, ds_endereco_ref,
                             nr_latitude, nr_longitude, ds_url_foto, dt_registro, nr_nivel_risco, st_ocorrencia, dt_resolucao)
SELECT SQ_AB_OCORRENCIA.NEXTVAL, 2, 1, 1, 'Bueiro entupido com folhas ao lado da saída da estação',
       'Av. Leôncio de Magalhães, saída da Estação Jardim São Paulo', -23.492400, -46.616900, NULL,
       SYSDATE - 2, 5, 'ABERTA', NULL
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM T_AB_OCORRENCIA
                    WHERE ds_endereco_ref = 'Av. Leôncio de Magalhães, saída da Estação Jardim São Paulo');

-- ---------------------------------------------------------------------------
-- 4) O morador de teste tambem recebe o alerta 6 (ponto alagado 6, ativo)
-- ---------------------------------------------------------------------------
INSERT INTO T_AB_ALERTA_USUARIO (id_alerta, id_usuario, dt_recebimento, dt_leitura)
SELECT 6, u.id_usuario, TIMESTAMP '2026-11-11 18:01:00', NULL
  FROM T_AB_USUARIO u
 WHERE u.ds_email = 'morador@alertabairro.com'
   AND NOT EXISTS (SELECT 1 FROM T_AB_ALERTA_USUARIO au
                    WHERE au.id_alerta = 6 AND au.id_usuario = u.id_usuario);

COMMIT;
