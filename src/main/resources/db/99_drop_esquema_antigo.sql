-- AlertaBairro - Remove o esquema antigo da API (tabelas AB_*)
-- A primeira versao da API criava tabelas proprias (AB_BAIRRO, AB_USUARIO, ...).
-- Agora a API usa o modelo oficial do grupo (tabelas T_AB_*). Rode este script
-- uma vez, se as tabelas antigas existirem no seu usuario do Oracle.
-- Tabelas inexistentes sao ignoradas (ORA-00942).

BEGIN
    FOR t IN (SELECT table_name FROM user_tables
               WHERE table_name IN ('AB_APOIO', 'AB_OCORRENCIA', 'AB_USUARIO',
                                    'AB_TIPO_PROBLEMA', 'AB_BAIRRO')) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
    END LOOP;
END;
/
