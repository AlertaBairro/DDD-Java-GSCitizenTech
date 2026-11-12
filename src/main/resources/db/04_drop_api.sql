-- AlertaBairro - Remove o complemento da API Java
-- Desfaz o 03_api_complemento.sql: apaga os usuarios de teste (e tudo o que
-- eles registraram pela API, porque as FKs do modelo nao tem cascade),
-- a ocorrencia de demonstracao e as sequences. As tabelas do modelo do
-- grupo (01_ddl.sql) e os dados do 02_dml.sql nao sao alterados.

-- Destinatarios de alertas: dos usuarios de teste e dos alertas gerados por pontos que eles reportaram
DELETE FROM T_AB_ALERTA_USUARIO
 WHERE id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                       WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com'))
    OR id_alerta IN (SELECT a.id_alerta
                       FROM T_AB_ALERTA a
                       JOIN T_AB_PONTO_ALAGADO p ON p.id_ponto_alagado = a.id_ponto_alagado
                      WHERE p.id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                                              WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com')));

-- Alertas e pontos alagados reportados pelos usuarios de teste
DELETE FROM T_AB_ALERTA
 WHERE id_ponto_alagado IN (SELECT id_ponto_alagado FROM T_AB_PONTO_ALAGADO
                             WHERE id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                                                   WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com')));

DELETE FROM T_AB_PONTO_ALAGADO
 WHERE id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                       WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com'));

-- Consultas de rota dos usuarios de teste
DELETE FROM T_AB_CONSULTA_ROTA
 WHERE id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                       WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com'));

-- Apoios e historico: feitos pelos usuarios de teste ou ligados as ocorrencias que serao removidas
DELETE FROM T_AB_APOIO
 WHERE id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                       WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com'))
    OR id_ocorrencia IN (SELECT id_ocorrencia FROM T_AB_OCORRENCIA
                          WHERE ds_endereco_ref = 'Av. Leôncio de Magalhães, saída da Estação Jardim São Paulo'
                             OR id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                                                WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com')));

DELETE FROM T_AB_HISTORICO_STATUS
 WHERE id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                       WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com'))
    OR id_ocorrencia IN (SELECT id_ocorrencia FROM T_AB_OCORRENCIA
                          WHERE ds_endereco_ref = 'Av. Leôncio de Magalhães, saída da Estação Jardim São Paulo'
                             OR id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                                                WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com')));

-- Ocorrencias registradas pelos usuarios de teste e a ocorrencia de demonstracao
DELETE FROM T_AB_OCORRENCIA
 WHERE ds_endereco_ref = 'Av. Leôncio de Magalhães, saída da Estação Jardim São Paulo'
    OR id_usuario IN (SELECT id_usuario FROM T_AB_USUARIO
                       WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com'));

-- Usuarios de teste
DELETE FROM T_AB_USUARIO
 WHERE ds_email IN ('morador@alertabairro.com', 'agente@alertabairro.com', 'gestor@alertabairro.com');

COMMIT;

-- Sequences da API (ignora as que nao existirem)
BEGIN
    FOR s IN (SELECT sequence_name FROM user_sequences
               WHERE sequence_name IN ('SQ_AB_USUARIO', 'SQ_AB_OCORRENCIA', 'SQ_AB_HISTORICO_STATUS',
                                       'SQ_AB_PONTO_ALAGADO', 'SQ_AB_ALERTA', 'SQ_AB_CONSULTA_ROTA')) LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
    END LOOP;
END;
/
