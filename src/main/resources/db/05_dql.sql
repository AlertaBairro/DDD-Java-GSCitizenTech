-- AlertaBairro - DQL

-- 1) INNER JOIN: ocorrências com o tipo do problema, ordenadas por risco
SELECT o.id_ocorrencia,
       t.nm_tipo,
       o.ds_endereco_ref,
       o.nr_nivel_risco,
       o.st_ocorrencia,
       TO_CHAR(o.dt_registro, 'DD/MM/YYYY HH24:MI') AS dt_registro
  FROM T_AB_OCORRENCIA o
 INNER JOIN T_AB_TIPO_OCORRENCIA t ON t.id_tipo_ocorrencia = o.id_tipo_ocorrencia
 ORDER BY o.nr_nivel_risco DESC, o.dt_registro;

-- 2) INNER JOIN + GROUP BY: indicadores de zeladoria por bairro
SELECT b.nm_bairro,
       COUNT(*)                                                 AS qt_ocorrencias,
       SUM(CASE WHEN o.st_ocorrencia <> 'RESOLVIDA' THEN 1 ELSE 0 END) AS qt_pendentes,
       ROUND(AVG(o.nr_nivel_risco), 1)                          AS media_risco,
       ROUND(AVG(o.dt_resolucao - o.dt_registro), 1)            AS media_dias_resolucao
  FROM T_AB_OCORRENCIA o
 INNER JOIN T_AB_BAIRRO b ON b.id_bairro = o.id_bairro
 GROUP BY b.nm_bairro
 ORDER BY qt_ocorrencias DESC, b.nm_bairro;

-- 3) LEFT OUTER JOIN + WHERE + ORDER BY: todos os moradores e suas ocorrências,
--    incluindo quem ainda não registrou nenhuma
SELECT u.nm_usuario,
       NVL(TO_CHAR(o.id_ocorrencia), '-')      AS id_ocorrencia,
       NVL(o.ds_endereco_ref, 'Sem registros') AS ds_endereco_ref,
       NVL(o.st_ocorrencia, '-')               AS st_ocorrencia
  FROM T_AB_USUARIO u
  LEFT OUTER JOIN T_AB_OCORRENCIA o ON o.id_usuario = u.id_usuario
 WHERE u.tp_perfil = 'MORADOR'
   AND u.st_ativo  = 'S'
 ORDER BY u.nm_usuario, o.dt_registro;
