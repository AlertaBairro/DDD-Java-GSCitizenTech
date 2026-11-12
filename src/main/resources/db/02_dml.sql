-- AlertaBairro - DML

INSERT ALL
    INTO T_AB_BAIRRO VALUES (1, 'Jardim São Paulo', 'NORTE', 'Santana/Tucuruvi')
    INTO T_AB_BAIRRO VALUES (2, 'Santana',          'NORTE', 'Santana/Tucuruvi')
    INTO T_AB_BAIRRO VALUES (3, 'Tucuruvi',         'NORTE', 'Santana/Tucuruvi')
    INTO T_AB_BAIRRO VALUES (4, 'Parada Inglesa',   'NORTE', 'Santana/Tucuruvi')
    INTO T_AB_BAIRRO VALUES (5, 'Vila Mazzei',      'NORTE', 'Santana/Tucuruvi')
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_USUARIO VALUES (1,  1, 'Mariana Alves dos Santos',  'mariana.santos@email.com',  '$2b$10$Qm8vK2pLx9TzR4wE6yU1sO', '11987651001', 'MORADOR', TIMESTAMP '2026-09-01 10:15:00', 'S')
    INTO T_AB_USUARIO VALUES (2,  1, 'Marcia Gonçalves Báo',      'marcia.bao@email.com',      '$2b$10$Hn3cJ7dWq5MaV8bN2xZ4tP', '11987651002', 'MORADOR', TIMESTAMP '2026-09-01 14:40:00', 'S')
    INTO T_AB_USUARIO VALUES (3,  1, 'Eduardo Ferreira Lima',     'eduardo.lima@prefeitura.sp.gov.br', '$2b$10$Lk6fR1gYs3NbC9mQ7vX2hD', '11987651003', 'AGENTE',  TIMESTAMP '2026-09-02 09:00:00', 'S')
    INTO T_AB_USUARIO VALUES (4,  2, 'Roberto Nunes Siqueira',    'roberto.siqueira@prefeitura.sp.gov.br', '$2b$10$Pz4tG8hUe2KdF5jW1rB6nM', '11987651004', 'GESTOR',  TIMESTAMP '2026-09-02 09:30:00', 'S')
    INTO T_AB_USUARIO VALUES (5,  3, 'Carlos Henrique Moura',     'carlos.moura@email.com',    '$2b$10$Wb7yT3kIo6LsH9aE4cV8qJ', '11987651005', 'MORADOR', TIMESTAMP '2026-09-05 19:20:00', 'S')
    INTO T_AB_USUARIO VALUES (6,  4, 'Patrícia Lemos Rocha',      'patricia.rocha@email.com',  '$2b$10$Yc2uP5lAz8QwJ1sR7dN3fK', '11987651006', 'MORADOR', TIMESTAMP '2026-09-08 08:05:00', 'S')
    INTO T_AB_USUARIO VALUES (7,  5, 'Fernando Dias Okada',       'fernando.okada@email.com',  '$2b$10$Rv9iM4nBx1EtG6hY3pL7wS', '11987651007', 'MORADOR', TIMESTAMP '2026-09-10 21:45:00', 'S')
    INTO T_AB_USUARIO VALUES (8,  2, 'Beatriz Campos Ribeiro',    'beatriz.ribeiro@email.com', '$2b$10$Tx1oN6qCv4UgK8jZ2mA5eH', '11987651008', 'MORADOR', TIMESTAMP '2026-09-15 12:10:00', 'S')
    INTO T_AB_USUARIO VALUES (9,  3, 'Thiago Rezende Alves',      'thiago.alves@prefeitura.sp.gov.br', '$2b$10$Ue5aQ2wDm7IhL3kX9bF1rY', '11987651009', 'AGENTE',  TIMESTAMP '2026-09-16 09:00:00', 'S')
    INTO T_AB_USUARIO VALUES (10, 4, 'Luana Martins Freitas',     'luana.freitas@email.com',   '$2b$10$Io8sW3eFn5OjM1lC6vG4tZ', '11987651010', 'MORADOR', TIMESTAMP '2026-09-20 17:35:00', 'S')
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_TIPO_OCORRENCIA VALUES (1, 'Bueiro entupido',          'Bueiro com folhas, terra ou lixo impedindo o escoamento', 5)
    INTO T_AB_TIPO_OCORRENCIA VALUES (2, 'Boca de lobo danificada',  'Grade quebrada, tampa ausente ou estrutura comprometida',  4)
    INTO T_AB_TIPO_OCORRENCIA VALUES (3, 'Lixo acumulado',           'Lixo descartado na calçada ou na via próximo à drenagem',  3)
    INTO T_AB_TIPO_OCORRENCIA VALUES (4, 'Entulho na via',           'Restos de obra ou móveis bloqueando sarjeta ou calçada',   3)
    INTO T_AB_TIPO_OCORRENCIA VALUES (5, 'Sarjeta obstruída',        'Sarjeta com terra ou vegetação impedindo o fluxo da água', 2)
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_OCORRENCIA VALUES (1, 1, 1, 1, 'Bueiro tomado por folhas e garrafas na esquina',      'Rua Dr. Zuquim, esquina com Av. Leôncio de Magalhães', -23.492500, -46.616000, 'https://alertabairro.app/fotos/oc1.jpg', TIMESTAMP '2026-10-05 08:12:00', 5, 'RESOLVIDA',      TIMESTAMP '2026-10-09 15:30:00')
    INTO T_AB_OCORRENCIA VALUES (2, 2, 3, 1, 'Sacos de lixo acumulados ao lado da boca de lobo',     'Av. Leôncio de Magalhães, próximo à Estação Jardim São Paulo', -23.491800, -46.617000, 'https://alertabairro.app/fotos/oc2.jpg', TIMESTAMP '2026-10-12 07:45:00', 4, 'EM_ATENDIMENTO', NULL)
    INTO T_AB_OCORRENCIA VALUES (3, 5, 2, 3, 'Grade da boca de lobo quebrada e solta',               'Av. Tucuruvi, 400',            -23.479500, -46.604000, 'https://alertabairro.app/fotos/oc3.jpg', TIMESTAMP '2026-10-20 18:30:00', 4, 'RECEBIDA',       NULL)
    INTO T_AB_OCORRENCIA VALUES (4, 6, 1, 4, 'Bueiro completamente entupido com terra',              'Rua Ezequiel Freire, 120',     -23.487200, -46.609000, 'https://alertabairro.app/fotos/oc4.jpg', TIMESTAMP '2026-10-03 10:00:00', 5, 'RESOLVIDA',      TIMESTAMP '2026-10-06 11:00:00')
    INTO T_AB_OCORRENCIA VALUES (5, 7, 4, 5, 'Entulho de reforma descartado sobre a sarjeta',        'Av. Mazzei, 850',              -23.473200, -46.595800, 'https://alertabairro.app/fotos/oc5.jpg', TIMESTAMP '2026-11-02 09:20:00', 3, 'ABERTA',         NULL)
    INTO T_AB_OCORRENCIA VALUES (6, 1, 5, 1, 'Sarjeta coberta de terra após obra na calçada',        'Rua Pedro Doll, 300',          -23.495000, -46.620000, NULL,                                      TIMESTAMP '2026-11-05 07:50:00', 2, 'ABERTA',         NULL)
    INTO T_AB_OCORRENCIA VALUES (7, 5, 3, 3, 'Lixo acumulado em frente ao ponto de ônibus',          'Av. Nova Cantareira, 1500',    -23.476000, -46.601000, 'https://alertabairro.app/fotos/oc7.jpg', TIMESTAMP '2026-10-15 16:10:00', 3, 'RESOLVIDA',      TIMESTAMP '2026-10-22 14:00:00')
    INTO T_AB_OCORRENCIA VALUES (8, 6, 2, 2, 'Tampa da boca de lobo ausente',                        'Rua Voluntários da Pátria, 2100', -23.500000, -46.625500, 'https://alertabairro.app/fotos/oc8.jpg', TIMESTAMP '2026-10-28 13:25:00', 4, 'EM_ATENDIMENTO', NULL)
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_APOIO VALUES (1, 2, TIMESTAMP '2026-10-05 19:00:00')
    INTO T_AB_APOIO VALUES (1, 5, TIMESTAMP '2026-10-06 07:30:00')
    INTO T_AB_APOIO VALUES (1, 6, TIMESTAMP '2026-10-06 12:15:00')
    INTO T_AB_APOIO VALUES (2, 1, TIMESTAMP '2026-10-12 18:20:00')
    INTO T_AB_APOIO VALUES (2, 7, TIMESTAMP '2026-10-13 08:10:00')
    INTO T_AB_APOIO VALUES (3, 6, TIMESTAMP '2026-10-21 07:40:00')
    INTO T_AB_APOIO VALUES (4, 5, TIMESTAMP '2026-10-03 18:00:00')
    INTO T_AB_APOIO VALUES (4, 2, TIMESTAMP '2026-10-04 09:30:00')
    INTO T_AB_APOIO VALUES (5, 6, TIMESTAMP '2026-11-03 10:00:00')
    INTO T_AB_APOIO VALUES (8, 1, TIMESTAMP '2026-10-29 08:45:00')
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_HISTORICO_STATUS VALUES (1,  1, 3, 'ABERTA',         'RECEBIDA',       TIMESTAMP '2026-10-06 09:00:00', 'Ocorrência triada pela zeladoria')
    INTO T_AB_HISTORICO_STATUS VALUES (2,  1, 3, 'RECEBIDA',       'EM_ATENDIMENTO', TIMESTAMP '2026-10-08 08:00:00', 'Equipe enviada ao local')
    INTO T_AB_HISTORICO_STATUS VALUES (3,  1, 3, 'EM_ATENDIMENTO', 'RESOLVIDA',      TIMESTAMP '2026-10-09 15:30:00', 'Bueiro desobstruído')
    INTO T_AB_HISTORICO_STATUS VALUES (4,  4, 9, 'ABERTA',         'RECEBIDA',       TIMESTAMP '2026-10-04 08:30:00', 'Prioridade alta por risco 5')
    INTO T_AB_HISTORICO_STATUS VALUES (5,  4, 9, 'RECEBIDA',       'EM_ATENDIMENTO', TIMESTAMP '2026-10-05 07:30:00', 'Caminhão de sucção agendado')
    INTO T_AB_HISTORICO_STATUS VALUES (6,  4, 9, 'EM_ATENDIMENTO', 'RESOLVIDA',      TIMESTAMP '2026-10-06 11:00:00', 'Limpeza concluída')
    INTO T_AB_HISTORICO_STATUS VALUES (7,  2, 3, 'ABERTA',         'RECEBIDA',       TIMESTAMP '2026-10-13 09:15:00', NULL)
    INTO T_AB_HISTORICO_STATUS VALUES (8,  2, 3, 'RECEBIDA',       'EM_ATENDIMENTO', TIMESTAMP '2026-10-14 08:00:00', 'Aguardando coleta complementar')
    INTO T_AB_HISTORICO_STATUS VALUES (9,  3, 9, 'ABERTA',         'RECEBIDA',       TIMESTAMP '2026-10-21 10:00:00', 'Encaminhado para manutenção de grade')
    INTO T_AB_HISTORICO_STATUS VALUES (10, 7, 9, 'ABERTA',         'RECEBIDA',       TIMESTAMP '2026-10-16 09:00:00', NULL)
    INTO T_AB_HISTORICO_STATUS VALUES (11, 7, 9, 'RECEBIDA',       'EM_ATENDIMENTO', TIMESTAMP '2026-10-18 08:30:00', NULL)
    INTO T_AB_HISTORICO_STATUS VALUES (12, 7, 9, 'EM_ATENDIMENTO', 'RESOLVIDA',      TIMESTAMP '2026-10-22 14:00:00', 'Lixo recolhido')
    INTO T_AB_HISTORICO_STATUS VALUES (13, 8, 3, 'ABERTA',         'RECEBIDA',       TIMESTAMP '2026-10-29 09:00:00', 'Local sinalizado com cone')
    INTO T_AB_HISTORICO_STATUS VALUES (14, 8, 3, 'RECEBIDA',       'EM_ATENDIMENTO', TIMESTAMP '2026-10-30 08:00:00', 'Tampa nova solicitada')
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_PONTO_ALAGADO VALUES (1, 1, 1, 'Av. Leôncio de Magalhães, em frente à Estação Jardim São Paulo', -23.492200, -46.616600, 'JOELHO',        TIMESTAMP '2026-11-03 17:40:00', 'N', TIMESTAMP '2026-11-03 21:00:00')
    INTO T_AB_PONTO_ALAGADO VALUES (2, 2, 1, 'Rua Dr. Zuquim, 900',                                            -23.493500, -46.618000, 'TORNOZELO',     TIMESTAMP '2026-11-03 17:55:00', 'N', TIMESTAMP '2026-11-03 20:30:00')
    INTO T_AB_PONTO_ALAGADO VALUES (3, 5, 3, 'Av. Tucuruvi com Av. Nova Cantareira',                           -23.478000, -46.602500, 'JOELHO',        TIMESTAMP '2026-11-03 18:10:00', 'N', TIMESTAMP '2026-11-03 22:00:00')
    INTO T_AB_PONTO_ALAGADO VALUES (4, 7, 5, 'Av. Mazzei, 700',                                                -23.473800, -46.596500, 'INTRANSITAVEL', TIMESTAMP '2026-11-03 18:20:00', 'N', TIMESTAMP '2026-11-04 01:00:00')
    INTO T_AB_PONTO_ALAGADO VALUES (5, 6, 4, 'Rua Ezequiel Freire, 150',                                       -23.487500, -46.609300, 'CINTURA',       TIMESTAMP '2026-10-24 16:00:00', 'N', TIMESTAMP '2026-10-24 19:30:00')
    INTO T_AB_PONTO_ALAGADO VALUES (6, 1, 1, 'Av. Leôncio de Magalhães, em frente à Estação Jardim São Paulo', -23.492200, -46.616600, 'TORNOZELO',     TIMESTAMP '2026-11-11 18:00:00', 'S', NULL)
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_ALERTA VALUES (1, 1, 'Alagamento na altura do joelho em frente à Estação Jardim São Paulo. Evite o trecho.', 500, TIMESTAMP '2026-11-03 17:41:00')
    INTO T_AB_ALERTA VALUES (2, 2, 'Água na altura do tornozelo na Rua Dr. Zuquim.',                                      300, TIMESTAMP '2026-11-03 17:56:00')
    INTO T_AB_ALERTA VALUES (3, 3, 'Alagamento no cruzamento da Av. Tucuruvi com a Av. Nova Cantareira.',                 500, TIMESTAMP '2026-11-03 18:11:00')
    INTO T_AB_ALERTA VALUES (4, 4, 'Av. Mazzei intransitável. Procure rota alternativa.',                                 800, TIMESTAMP '2026-11-03 18:21:00')
    INTO T_AB_ALERTA VALUES (5, 5, 'Água na altura da cintura na Rua Ezequiel Freire. Não atravesse.',                    600, TIMESTAMP '2026-10-24 16:01:00')
    INTO T_AB_ALERTA VALUES (6, 6, 'Início de alagamento em frente à Estação Jardim São Paulo.',                          500, TIMESTAMP '2026-11-11 18:01:00')
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_ALERTA_USUARIO VALUES (1, 2,  TIMESTAMP '2026-11-03 17:41:00', TIMESTAMP '2026-11-03 17:43:00')
    INTO T_AB_ALERTA_USUARIO VALUES (1, 5,  TIMESTAMP '2026-11-03 17:41:00', TIMESTAMP '2026-11-03 17:50:00')
    INTO T_AB_ALERTA_USUARIO VALUES (1, 8,  TIMESTAMP '2026-11-03 17:41:00', NULL)
    INTO T_AB_ALERTA_USUARIO VALUES (2, 1,  TIMESTAMP '2026-11-03 17:56:00', TIMESTAMP '2026-11-03 17:58:00')
    INTO T_AB_ALERTA_USUARIO VALUES (2, 8,  TIMESTAMP '2026-11-03 17:56:00', TIMESTAMP '2026-11-03 18:05:00')
    INTO T_AB_ALERTA_USUARIO VALUES (3, 6,  TIMESTAMP '2026-11-03 18:11:00', TIMESTAMP '2026-11-03 18:12:00')
    INTO T_AB_ALERTA_USUARIO VALUES (3, 7,  TIMESTAMP '2026-11-03 18:11:00', NULL)
    INTO T_AB_ALERTA_USUARIO VALUES (4, 5,  TIMESTAMP '2026-11-03 18:21:00', TIMESTAMP '2026-11-03 18:25:00')
    INTO T_AB_ALERTA_USUARIO VALUES (4, 10, TIMESTAMP '2026-11-03 18:21:00', NULL)
    INTO T_AB_ALERTA_USUARIO VALUES (5, 2,  TIMESTAMP '2026-10-24 16:01:00', TIMESTAMP '2026-10-24 16:03:00')
    INTO T_AB_ALERTA_USUARIO VALUES (5, 10, TIMESTAMP '2026-10-24 16:01:00', TIMESTAMP '2026-10-24 16:10:00')
    INTO T_AB_ALERTA_USUARIO VALUES (6, 2,  TIMESTAMP '2026-11-11 18:01:00', TIMESTAMP '2026-11-11 18:02:00')
    INTO T_AB_ALERTA_USUARIO VALUES (6, 8,  TIMESTAMP '2026-11-11 18:01:00', NULL)
SELECT 1 FROM DUAL;

INSERT ALL
    INTO T_AB_CONSULTA_ROTA VALUES (1, 2, 'Rua Dr. Zuquim, 900',              -23.493500, -46.618000, 'Estação Jardim São Paulo-Ayrton Senna', -23.492200, -46.616600, TIMESTAMP '2026-11-03 18:05:00', 'S', 2)
    INTO T_AB_CONSULTA_ROTA VALUES (2, 1, 'Rua Pedro Doll, 300',              -23.495000, -46.620000, 'Estação Jardim São Paulo-Ayrton Senna', -23.492200, -46.616600, TIMESTAMP '2026-11-03 17:50:00', 'S', 1)
    INTO T_AB_CONSULTA_ROTA VALUES (3, 5, 'Av. Nova Cantareira, 1500',        -23.476000, -46.601000, 'Estação Tucuruvi',                      -23.480000, -46.603500, TIMESTAMP '2026-11-03 18:15:00', 'N', 0)
    INTO T_AB_CONSULTA_ROTA VALUES (4, 2, 'Rua Dr. Zuquim, 900',              -23.493500, -46.618000, 'Rua Voluntários da Pátria, 2100',       -23.500000, -46.625500, TIMESTAMP '2026-10-24 16:20:00', 'S', 1)
    INTO T_AB_CONSULTA_ROTA VALUES (5, 7, 'Av. Mazzei, 850',                  -23.473200, -46.595800, 'Estação Tucuruvi',                      -23.480000, -46.603500, TIMESTAMP '2026-11-03 18:30:00', 'S', 1)
    INTO T_AB_CONSULTA_ROTA VALUES (6, 8, 'Rua Voluntários da Pátria, 1800',  -23.498000, -46.624000, 'Estação Jardim São Paulo-Ayrton Senna', -23.492200, -46.616600, TIMESTAMP '2026-11-11 18:10:00', 'S', 1)
SELECT 1 FROM DUAL;

COMMIT;
