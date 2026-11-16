<div align="center">

# AlertaBairro

## API REST em Java — Domain Driven Design

### "Informe. Previna. Desvie."

**Global Solution 2026.2 — FIAP — Tema CitizenTech**

**Turma:** 2ESPG

| Integrante | RM |
|---|---|
| Arthur Araújo Tenório | RM562272 |
| Breno Gonçalves Báo | RM564037 |
| Rafael Falaguasta Ferraz | RM561714 |
| Vinicius Cavalcanti dos Reis | RM562063 |

**Repositório:** https://github.com/AlertaBairro/DDD-Java-GSCitizenTech

**Deploy (Render):** https://alertabairro-api.onrender.com

**Swagger:** https://alertabairro-api.onrender.com/swagger-ui.html

São Paulo — novembro de 2026

</div>

<div style="page-break-after: always;"></div>

## Sumário

1. Problema
2. Solução
3. Justificativa
4. Objetivos
5. Arquitetura em camadas
6. Modelo de dados
7. Regras de negócio
8. Protótipos de tela
9. Endpoints
10. Tratamento de exceções
11. Testes
12. Deploy no Render
13. Ferramentas e versões
14. Como executar localmente
15. Evidências (prints)

---

## 1. Problema

Na região da **Subprefeitura Santana/Tucuruvi** (Jardim São Paulo, Santana, Tucuruvi, Parada Inglesa e Vila Mazzei), na zona norte de São Paulo, chuvas fortes transformam rapidamente bueiros entupidos e bocas de lobo danificadas em pontos de alagamento. Hoje:

- o morador só descobre que a rua alagou quando já está nela;
- as reclamações de zeladoria chegam dispersas, sem indicar o que é mais urgente;
- a subprefeitura não tem números para saber onde o problema se repete nem quanto tempo leva para resolvê-lo.

## 2. Solução

O **AlertaBairro** junta **zeladoria do bairro**, **alerta de alagamento** e **rota segura** em um só serviço. Esta entrega é a **API REST em Java**, construída sobre o **mesmo modelo de dados da entrega de Database do grupo**: as 10 tabelas `T_AB_*` do Oracle, sem nenhuma alteração.

| Perfil | Funcionalidades |
|---|---|
| **Morador** | Cadastra-se e faz login. Registra ocorrências de zeladoria e apoia as dos vizinhos. Reporta pontos alagados, que geram alertas para o bairro. Lê os próprios alertas e consulta a rota mais segura, que fica registrada. |
| **Agente de zeladoria** | Vê as ocorrências ordenadas por prioridade e muda o status, com histórico. Encerra pontos alagados. |
| **Gestor da subprefeitura** | Vê os indicadores de pendências, tempo de resolução, bairros, tipos, alertas, taxa de leitura e reincidência. Encerra pontos alagados. |

## 3. Justificativa

Alagamentos são um problema recorrente na cidade de São Paulo, e boa parte deles começa em algo pequeno e evitável: um bueiro entupido, uma boca de lobo quebrada ou lixo acumulado na sarjeta. Hoje esses problemas são relatados de forma dispersa, sem indicação do que é mais urgente, e o morador só descobre que a rua alagou quando já está nela.

O AlertaBairro se justifica por atacar o problema nas duas pontas:

- **Antes da chuva (prevenção):** o morador registra o problema e os vizinhos apoiam a ocorrência. A API calcula um nível de risco e entrega ao agente de zeladoria uma fila **priorizada**, para que a equipe limpe primeiro o que tem mais chance de causar alagamento.
- **Durante a chuva (proteção):** quem vê a rua alagando reporta o ponto, e a API gera um **alerta** para os moradores do bairro e permite consultar uma **rota mais segura**, que desvia dos pontos alagados ativos.
- **Depois (gestão):** o gestor da subprefeitura passa a ter **indicadores**, como pendências, tempo médio de resolução, ocorrências por bairro e tipo, taxa de leitura dos alertas e reincidência, para decidir onde investir.

Assim, a tecnologia chega a quem convive com o problema todos os dias: moradores, trabalhadores e estudantes dos bairros que alagam.

**ODS conectados:**

| ODS | Como o projeto contribui |
|---|---|
| **11 – Cidades e comunidades sustentáveis** | Dá ao cidadão um canal para participar da zeladoria do bairro e reduz o impacto dos alagamentos na mobilidade e na segurança. |
| **13 – Ação contra a mudança global do clima** | Aumenta a capacidade de resposta a eventos de chuva intensa, com alerta e rota segura. |
| **9 – Indústria, inovação e infraestrutura** | Usa dados para priorizar a manutenção da infraestrutura de drenagem urbana. |

## 4. Objetivos

**Objetivo geral:** disponibilizar uma API REST que permita registrar, priorizar e acompanhar problemas de zeladoria ligados a alagamentos, alertar os moradores quando um ponto alagar e indicar rotas mais seguras.

**Objetivos específicos:**

1. Permitir que o morador se cadastre, faça login e registre ocorrências de zeladoria (bueiro entupido, boca de lobo danificada, lixo e entulho) no seu bairro.
2. Permitir que os vizinhos **apoiem** ocorrências já registradas, aumentando a prioridade delas.
3. Calcular o **nível de risco** de cada ocorrência e oferecer ao agente de zeladoria uma **fila priorizada**.
4. Controlar o **status** das ocorrências (aberta, em atendimento, resolvida) e guardar o **histórico** de cada mudança.
5. Registrar **pontos alagados** e gerar **alertas** automáticos para os moradores do bairro.
6. Avaliar rotas candidatas e indicar a **rota mais segura**, evitando os pontos alagados ativos.
7. Oferecer ao gestor **indicadores** de pendências, tempo de resolução, reincidência e leitura dos alertas.
8. Publicar a API na nuvem (**Render**), conectada ao **Oracle da FIAP**, com documentação no **Swagger**.

## 5. Arquitetura em camadas

```
            ┌───────────────────────────────┐
 HTTP  ───► │ controller (@RestController)  │ ◄── dto (records + Bean Validation)
            └──────────────┬────────────────┘
                           ▼
            ┌───────────────────────────────┐
            │ bo (regras de negócio)        │ ◄── model (1 classe por tabela + enums)
            └──────────────┬────────────────┘
                           ▼
            ┌───────────────────────────────┐
            │ dao (JDBC puro, transações)   │ ◄── ConnectionFactory (variáveis de ambiente)
            └──────────────┬────────────────┘
                           ▼
                 Oracle FIAP (tabelas T_AB_*)

   exception: @RestControllerAdvice + tradução dos erros ORA em JSON padronizado
```

| Pacote | Responsabilidade | Classes |
|---|---|---|
| `model` | Uma classe por tabela do DDL do grupo, com encapsulamento, construtores e getters/setters; enums para os CHECKs | `Bairro`, `Usuario`, `TipoOcorrencia`, `Ocorrencia`, `Apoio`, `HistoricoStatus`, `PontoAlagado`, `Alerta`, `AlertaUsuario`, `ConsultaRota`, `Zona`, `Perfil`, `StatusOcorrencia`, `NivelAgua`, `NivelRisco`, `AvaliacaoRisco` |
| `dao` | Acesso ao Oracle com `Connection`, `PreparedStatement` e `ResultSet`; um DAO por tabela; IDs pelas sequences `SQ_AB_*`; operações compostas em transação | `ConnectionFactory`, `JdbcUtils`, `BairroDAO`, `UsuarioDAO`, `TipoOcorrenciaDAO`, `OcorrenciaDAO`, `ApoioDAO`, `HistoricoStatusDAO`, `PontoAlagadoDAO`, `AlertaDAO`, `AlertaUsuarioDAO`, `ConsultaRotaDAO`, `IndicadorDAO`, `HealthDAO` |
| `bo` | Regras de negócio, validações e permissões por perfil | `UsuarioBO`, `CatalogoBO`, `OcorrenciaBO`, `ApoioBO`, `RiscoBO`, `PontoAlagadoBO`, `AlertaBO`, `RotaBO`, `IndicadorBO`, `HealthBO` |
| `controller` | Endpoints REST com GET, POST, PUT e DELETE; chamam somente o BO | `AuthController`, `UsuarioController`, `CatalogoController`, `OcorrenciaController`, `PontoAlagadoController`, `AlertaController`, `RotaController`, `IndicadorController`, `HealthController` |
| `dto` | Objetos de entrada e saída da API | `UsuarioCadastroRequest`, `LoginRequest`, `OcorrenciaRequest`, `StatusRequest`, `PontoAlagadoRequest`, `AvaliacaoRotaRequest`, respostas... |
| `exception` | Exceções próprias, tratador global e tradução dos erros do Oracle | `ValidacaoException`, `NaoAutenticadoException`, `AcessoNegadoException`, `RecursoNaoEncontradoException`, `ConflitoException`, `RegraDeNegocioException`, `PersistenciaException`, `GlobalExceptionHandler`, `TradutorErroOracle` |
| `config` | CORS, Swagger, BCrypt e relógio | `CorsConfig`, `OpenApiConfig`, `BeansConfig` |

**Segurança das credenciais:** a `ConnectionFactory` lê `DB_URL`, `DB_USER` e `DB_PASSWORD` de variáveis de ambiente. Nenhuma credencial está no código nem no repositório.

**Autenticação:** é simples, sem Spring Security nem JWT. O login devolve o id do usuário, e o front envia esse id no header `X-Usuario-Id`. O BO confere se o usuário existe, se está ativo e se o perfil tem permissão. As senhas ficam guardadas com hash **BCrypt**.

**Transações:** quando uma operação mexe em mais de uma tabela, ela roda numa única transação (`setAutoCommit(false)`, `commit` e `rollback` em caso de erro). São três casos:
- mudança de status junto com o histórico;
- ponto alagado junto com o alerta e os destinatários;
- exclusão de ocorrência junto com os apoios e o histórico.

<figure><img src="prints/01-estrutura-pacotes.png" alt="Estrutura de pacotes do projeto no VS Code"><figcaption>Figura 1 — Estrutura de pacotes do projeto no VS Code</figcaption></figure>

## 6. Modelo de dados

### 6.1 Origem do modelo e scripts

O modelo é o da entrega de **Database** do grupo. Os scripts estão em `src/main/resources/db/`:

| Script | Origem | Conteúdo |
|---|---|---|
| `01_ddl.sql` | grupo (cópia exata) | criação das 10 tabelas, PKs, UKs, CHECKs e FKs |
| `02_dml.sql` | grupo (cópia exata) | 5 bairros, 10 usuários, 5 tipos, 8 ocorrências, apoios, histórico, 6 pontos alagados, alertas e consultas de rota |
| `03_api_complemento.sql` | API | sequences `SQ_AB_*` (começando em `MAX(id) + 1`), 3 usuários de teste com BCrypt real, 1 ocorrência ABERTA perto da estação e o morador de teste como destinatário do alerta 6 |
| `04_drop_api.sql` | API | desfaz o `03` |
| `05_dql.sql` | grupo (cópia exata) | consultas com INNER JOIN, GROUP BY e LEFT JOIN |
| `99_drop_esquema_antigo.sql` | API | remove as tabelas `AB_*` da primeira versão da API |

Ordem de execução: `01_ddl.sql`, depois `02_dml.sql`, depois `03_api_complemento.sql`. Na primeira execução, os `DROP TABLE` do início do DDL dão **ORA-00942** porque as tabelas ainda não existem. Isso é esperado e não impede a criação.

### 6.2 Tabelas e classes

| Tabela | Classe | Principais restrições |
|---|---|---|
| `T_AB_BAIRRO` | `Bairro` | PK, UNIQUE no nome, CHECK da zona |
| `T_AB_USUARIO` | `Usuario` | PK, UNIQUE no e-mail, CHECK do perfil e de `st_ativo` (S/N), FK para bairro |
| `T_AB_TIPO_OCORRENCIA` | `TipoOcorrencia` | PK, UNIQUE no nome, peso de risco de 1 a 5 |
| `T_AB_OCORRENCIA` | `Ocorrencia` | FKs para usuário, tipo e bairro; CHECK de latitude, longitude, risco (1 a 5), status e coerência RESOLVIDA ⇔ data de resolução |
| `T_AB_APOIO` | `Apoio` | PK composta (ocorrência, usuário) |
| `T_AB_HISTORICO_STATUS` | `HistoricoStatus` | CHECK dos status e de status anterior ≠ novo |
| `T_AB_PONTO_ALAGADO` | `PontoAlagado` | CHECK de coordenadas, nível da água e ativo ⇔ sem data de encerramento |
| `T_AB_ALERTA` | `Alerta` | FK para o ponto alagado, raio > 0 |
| `T_AB_ALERTA_USUARIO` | `AlertaUsuario` | PK composta (alerta, usuário), leitura ≥ recebimento |
| `T_AB_CONSULTA_ROTA` | `ConsultaRota` | CHECK de `st_rota_calculada` (S/N) e pontos evitados ≥ 0 |

Correspondência de tipos:
- `DATE` vira `LocalDateTime`;
- `NUMBER(9,6)` vira `double`, gravado com 6 casas;
- `CHAR(1)` 'S'/'N' vira `boolean`;
- os domínios dos CHECKs viram enums: `Zona`, `Perfil`, `StatusOcorrencia` e `NivelAgua`.

### 6.3 Diagrama entidade-relacionamento

<figure><img src="prints/03-der.png" alt="Diagrama entidade-relacionamento (DER) das 10 tabelas do modelo do grupo"><figcaption>Figura 3 — Diagrama entidade-relacionamento (DER) das 10 tabelas do modelo do grupo</figcaption></figure>

O código-fonte Mermaid do diagrama está em `docs/diagramas/der.mmd`.

### 6.4 Diagrama de classes (camada model)

<figure><img src="prints/04-diagrama-classes.png" alt="Diagrama de classes da camada model"><figcaption>Figura 2 — Diagrama de classes da camada model</figcaption></figure>

O código-fonte Mermaid do diagrama está em `docs/diagramas/classes.mmd`.

## 7. Regras de negócio

Todas as regras ficam na camada **BO**.

1. **Cadastro de usuário** (público):
   - campos: nome, e-mail único (409 se já existir), senha (gravada como hash BCrypt), telefone opcional, bairro obrigatório e perfil;
   - o usuário entra ativo.
2. **Login:**
   - só autentica usuário existente, ativo e com senha válida; caso contrário, 401;
   - não cria conta;
   - devolve id, nome, perfil e bairro.
3. **Exclusão de usuário:**
   - é lógica (`st_ativo = 'N'`), porque o usuário é referenciado por várias FKs;
   - usuário inativo não loga.
4. **Registrar ocorrência** (MORADOR):
   - obrigatórios: tipo, bairro, endereço de referência, latitude e longitude;
   - descrição de até 500 caracteres e URL da foto opcional;
   - nasce **ABERTA**, com `nivelRisco` igual ao peso do tipo;
   - o autor não apoia a própria ocorrência; os apoios são contados em `T_AB_APOIO`.
5. **Editar e excluir ocorrência:**
   - só o autor, e só enquanto estiver ABERTA;
   - a exclusão remove antes os apoios e o histórico, na mesma transação.
6. **Apoiar e retirar apoio** (MORADOR):
   - um apoio por usuário (PK composta; 409 se repetir);
   - não é possível apoiar a própria ocorrência nem uma RESOLVIDA (422).
7. **Alterar status** (AGENTE):
   - o novo status precisa ser diferente do atual, e não é possível voltar para ABERTA (422);
   - grava uma linha em `T_AB_HISTORICO_STATUS` (agente, status anterior, status novo e observação) na mesma transação;
   - ir para RESOLVIDA preenche `dt_resolucao`; sair de RESOLVIDA limpa a data.
8. **Priorização do agente:**
   - `pontuação = nivelRisco × 2 + apoios + dias desde o registro × 0,8`, e uma ocorrência resolvida vale 0;
   - nível ALTO a partir de 16, MÉDIO a partir de 10;
   - a fila é decrescente e, no empate, vem primeiro a mais recente.
9. **Reportar ponto alagado** (MORADOR). Na mesma transação, a API:
   - cria o ponto ativo;
   - cria um alerta com a mensagem gerada a partir do endereço e do nível da água, e o raio dado pelo nível (TORNOZELO 300 m, JOELHO 500 m, CINTURA 600 m, INTRANSITAVEL 800 m);
   - registra como destinatários todos os usuários ativos do bairro, exceto quem reportou.
10. **Encerrar ponto alagado** (AGENTE ou GESTOR): `st_ativo = 'N'` e `dt_encerramento` igual ao momento atual.
11. **Alertas:**
    - lista os alertas ativos (de pontos ainda ativos);
    - lista os alertas recebidos pelo usuário, lidos e não lidos;
    - marca um alerta como lido gravando `dt_leitura`.
12. **Zonas de risco:** os pontos alagados ativos, cada um com raio de desvio de **40 m**. Esse raio é diferente do raio do alerta.
13. **Avaliar rota:**
    - uma rota atravessa a zona quando a distância do ponto até a polilinha é de no máximo 40 m;
    - o cálculo usa 110.540 m por grau de latitude, 111.320 × cos(lat) m por grau de longitude e a distância de ponto a segmento;
    - a mais segura é a que atravessa menos zonas e, no empate, a de menor duração;
    - a consulta é gravada em `T_AB_CONSULTA_ROTA`, com `st_rota_calculada` e `nr_pontos_evitados` (zonas da rota padrão − zonas da segura, no mínimo 0).
14. **Indicadores do gestor:**
    - total de ocorrências e total pendente;
    - tempo médio entre registro e resolução;
    - por bairro (a consulta 2 do DQL do grupo) e por tipo;
    - pontos alagados ativos e total;
    - alertas enviados, usuários alertados e taxa de leitura;
    - reincidência: pontos alagados repetidos no mesmo endereço, e combinações de bairro e tipo com mais de uma ocorrência.

**Exemplos com os dados do DML do grupo e do complemento** (calculados para 12/11/2026):
- **Priorização:** a ocorrência 2 (risco 4, 2 apoios, registrada em 12/10/2026) tem pontuação `4×2 + 2 + 31,1×0,8 ≈ 34,9`, portanto **risco alto**.
- **Indicadores:**
  - 9 ocorrências, 6 pendentes;
  - tempo médio de resolução ≈ 114 h ("4 d 18 h");
  - 1 ponto alagado ativo de 6;
  - 14 notificações entregues, com taxa de leitura de 64,3%;
  - reincidência no endereço "Av. Leôncio de Magalhães, em frente à Estação Jardim São Paulo" (2 reportes) e em "Jardim São Paulo + Bueiro entupido" (2 ocorrências).

## 8. Protótipos de tela

O protótipo do grupo é uma aplicação HTML navegável, com login por perfil (morador, agente de zeladoria e gestor). Ele funciona sem backend: os dados ficam no navegador. A tabela mostra cada tela e os endpoints da API que ela consumiria numa integração.

| Tela | Perfil | O que mostra | Endpoints correspondentes |
|---|---|---|---|
| Login | todos | e-mail, senha e escolha do perfil | `POST /api/auth/login` |
| Área do morador | Morador | alerta de alagamento ativo, filtros por bairro e problema, ocorrências com botão de apoiar | `GET /api/alertas/meus`, `GET /api/ocorrencias`, `POST /api/ocorrencias/{id}/apoios` |
| Registrar ocorrência | Morador | bairro, problema e observações (data e hora automáticas) | `GET /api/bairros`, `GET /api/tipos-ocorrencia`, `POST /api/ocorrencias` |
| Mapa e rota segura | Morador | mapa com pontos alagados e área de risco, rota segura × rota padrão | `GET /api/zonas-de-risco`, `POST /api/rotas/avaliar` |
| Painel da zeladoria | Agente | ocorrências ordenadas por risco e troca de status | `GET /api/ocorrencias/priorizadas`, `PUT /api/ocorrencias/{id}/status` |
| Indicadores de impacto | Gestor | totais, tempo médio, ocorrências por bairro e por problema, reincidência | `GET /api/indicadores` |

<figure><img src="prints/prototipo-01-login.png" alt="Protótipo: login com escolha de perfil"><figcaption>Figura 4 — Protótipo: login com escolha de perfil</figcaption></figure>

<figure><img src="prints/prototipo-02-morador.png" alt="Protótipo: área do morador, com alerta de alagamento e ocorrências do bairro"><figcaption>Figura 5 — Protótipo: área do morador, com alerta de alagamento e ocorrências do bairro</figcaption></figure>

<figure><img src="prints/prototipo-03-registrar.png" alt="Protótipo: registrar ocorrência"><figcaption>Figura 6 — Protótipo: registrar ocorrência</figcaption></figure>

<figure><img src="prints/prototipo-06-mapa-rota.png" alt="Protótipo: mapa com o ponto alagado e a rota segura desviando da área de risco"><figcaption>Figura 7 — Protótipo: mapa com o ponto alagado e a rota segura desviando da área de risco</figcaption></figure>

<figure><img src="prints/prototipo-04-agente.png" alt="Protótipo: painel da zeladoria (agente), com ocorrências ordenadas por risco"><figcaption>Figura 8 — Protótipo: painel da zeladoria (agente), com ocorrências ordenadas por risco</figcaption></figure>

<figure><img src="prints/prototipo-05-gestor.png" alt="Protótipo: indicadores de impacto (gestor)"><figcaption>Figura 9 — Protótipo: indicadores de impacto (gestor)</figcaption></figure>

## 9. Endpoints

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| GET | `/api/health` | público | status da API e do banco |
| GET | `/api/bairros` · `/api/bairros/{id}` | público | bairros |
| GET | `/api/tipos-ocorrencia` · `/api/tipos-ocorrencia/{id}` | público | tipos de ocorrência |
| POST | `/api/usuarios` | público | cadastro |
| GET / PUT / DELETE | `/api/usuarios/{id}` | o próprio | consultar, atualizar, exclusão lógica |
| POST | `/api/auth/login` | público | login |
| GET | `/api/ocorrencias?bairro=&tipo=&status=` | autenticado | lista com filtros |
| GET | `/api/ocorrencias/{id}` | autenticado | detalhe |
| POST | `/api/ocorrencias` | MORADOR | registrar |
| PUT / DELETE | `/api/ocorrencias/{id}` | autor, se ABERTA | editar / excluir |
| POST / DELETE | `/api/ocorrencias/{id}/apoios` | MORADOR | apoiar / retirar apoio |
| GET | `/api/ocorrencias/priorizadas` | AGENTE | fila por prioridade |
| PUT | `/api/ocorrencias/{id}/status` | AGENTE | alterar status + histórico |
| GET | `/api/ocorrencias/{id}/historico` | autenticado | histórico de status |
| GET | `/api/pontos-alagados?ativos=` | autenticado | todos ou só ativos |
| GET | `/api/pontos-alagados/{id}` | autenticado | detalhe |
| POST | `/api/pontos-alagados` | MORADOR | reportar (gera alerta) |
| PUT | `/api/pontos-alagados/{id}/encerrar` | AGENTE ou GESTOR | encerrar |
| GET | `/api/alertas` | autenticado | alertas ativos |
| GET | `/api/alertas/meus` | autenticado | alertas recebidos |
| PUT | `/api/alertas/{id}/leitura` | destinatário | marcar como lido |
| GET | `/api/zonas-de-risco` | autenticado | zonas de 40 m |
| POST | `/api/rotas/avaliar` | autenticado | avaliar rotas e gravar consulta |
| GET | `/api/rotas/minhas` | autenticado | histórico de consultas |
| GET | `/api/indicadores` | GESTOR | painel do gestor |

Ao todo são 24 rotas e 31 operações: 17 GET, 6 POST, 5 PUT e 3 DELETE. Os exemplos completos estão no `README.md` e no `requests.http`.

### Testes dos endpoints na API publicada

Os prints abaixo foram feitos no Swagger da API publicada no Render, conectada ao Oracle da FIAP. Usuários: morador de teste (id 11), agente (id 12), gestor (id 13) e a moradora Joana Teste (id 14), cadastrada no próprio teste.

<figure><img src="prints/05-swagger.png" alt="Swagger UI da API publicada no Render"><figcaption>Figura 10 — Swagger UI da API publicada no Render</figcaption></figure>

<figure><img src="prints/06-cadastro.png" alt="POST /api/usuarios: cadastro respondendo 201, sem devolver a senha"><figcaption>Figura 11 — POST /api/usuarios: cadastro respondendo 201, sem devolver a senha</figcaption></figure>

<figure><img src="prints/07-login.png" alt="POST /api/auth/login: login do morador respondendo 200 com o id para o header X-Usuario-Id"><figcaption>Figura 12 — POST /api/auth/login: login do morador respondendo 200 com o id para o header X-Usuario-Id</figcaption></figure>

<figure><img src="prints/08-registrar-ocorrencia.png" alt="POST /api/ocorrencias: ocorrência 10 criada com status ABERTA"><figcaption>Figura 13 — POST /api/ocorrencias: ocorrência 10 criada com status ABERTA</figcaption></figure>

<figure><img src="prints/10-status.png" alt="PUT /api/ocorrencias/10/status: agente muda o status para EM_ATENDIMENTO"><figcaption>Figura 14 — PUT /api/ocorrencias/10/status: agente muda o status para EM_ATENDIMENTO</figcaption></figure>

<figure><img src="prints/10b-historico.png" alt="GET /api/ocorrencias/10/historico: histórico ABERTA → EM_ATENDIMENTO gravado pelo agente"><figcaption>Figura 15 — GET /api/ocorrencias/10/historico: histórico ABERTA → EM_ATENDIMENTO gravado pelo agente</figcaption></figure>

<figure><img src="prints/11b-usuarios-notificados.png" alt="POST /api/pontos-alagados: ponto 7 criado, alerta 7 gerado e 6 usuários notificados"><figcaption>Figura 16 — POST /api/pontos-alagados: ponto 7 criado, alerta 7 gerado e 6 usuários notificados</figcaption></figure>

<figure><img src="prints/12a-meus-alertas.png" alt="GET /api/alertas/meus: a moradora recebe o alerta 7, ainda não lido"><figcaption>Figura 17 — GET /api/alertas/meus: a moradora recebe o alerta 7, ainda não lido</figcaption></figure>

<figure><img src="prints/12b-alerta-lido.png" alt="PUT /api/alertas/7/leitura: alerta marcado como lido, com data de leitura"><figcaption>Figura 18 — PUT /api/alertas/7/leitura: alerta marcado como lido, com data de leitura</figcaption></figure>

<figure><img src="prints/13-rota-segura.png" alt="POST /api/rotas/avaliar: a rota "Direta" atravessa o ponto 7 e a rota "Desvio" é a mais segura"><figcaption>Figura 19 — POST /api/rotas/avaliar: a rota "Direta" atravessa o ponto 7 e a rota "Desvio" é a mais segura</figcaption></figure>

<figure><img src="prints/14-indicadores.png" alt="GET /api/indicadores: painel do gestor com totais, tempo médio e resumo por bairro"><figcaption>Figura 20 — GET /api/indicadores: painel do gestor com totais, tempo médio e resumo por bairro</figcaption></figure>

<figure><img src="prints/15-delete.png" alt="DELETE /api/ocorrencias/11: exclusão respondendo 204"><figcaption>Figura 21 — DELETE /api/ocorrencias/11: exclusão respondendo 204</figcaption></figure>

## 10. Tratamento de exceções

A camada `exception` tem exceções próprias e o `GlobalExceptionHandler` (`@RestControllerAdvice`), que sempre devolve o mesmo formato JSON:

```json
{
  "timestamp": "2026-11-12T14:29:52-03:00",
  "status": 422,
  "erro": "Unprocessable Entity",
  "mensagem": "Não é possível voltar uma ocorrência para ABERTA.",
  "caminho": "/api/ocorrencias/9/status"
}
```

| Exceção | HTTP | Exemplo |
|---|---|---|
| `ValidacaoException` e Bean Validation | 400 | latitude fora da faixa, e-mail inválido (o campo `detalhes` traz os erros por campo) |
| `NaoAutenticadoException` | 401 | header ausente, usuário inativo, senha errada |
| `AcessoNegadoException` | 403 | agente registrando ocorrência; morador alterando status |
| `RecursoNaoEncontradoException` | 404 | ocorrência, ponto ou alerta inexistente |
| `ConflitoException` | 409 | e-mail já cadastrado, apoio duplicado; erro **ORA-00001** |
| `RegraDeNegocioException` | 422 | voltar para ABERTA, apoiar a própria ocorrência; erros **ORA-02290, ORA-02291, ORA-02292** |
| `PersistenciaException` e erros inesperados | 500 | falha no banco (o detalhe técnico fica só no log) |

O `TradutorErroOracle` lê o código ORA e o nome da constraint do modelo do grupo e devolve uma mensagem clara. Por exemplo, `UK_AB_USUARIO_EMAIL` vira "Já existe um usuário cadastrado com este e-mail.". Assim, uma violação de constraint vira 409 ou 422, e não 500.

<figure><img src="prints/erro-409.png" alt="Erro 409: apoio duplicado na mesma ocorrência"><figcaption>Figura 22 — Erro 409: apoio duplicado na mesma ocorrência</figcaption></figure>

<figure><img src="prints/erro-422.png" alt="Erro 422: regra de negócio impede voltar a ocorrência para ABERTA"><figcaption>Figura 23 — Erro 422: regra de negócio impede voltar a ocorrência para ABERTA</figcaption></figure>

<figure><img src="prints/erro-400.png" alt="Erro 400: validação lista os campos obrigatórios em "detalhes""><figcaption>Figura 24 — Erro 400: validação lista os campos obrigatórios em "detalhes"</figcaption></figure>

## 11. Testes

São **86 testes unitários** com **JUnit 5 e Mockito**. Os DAOs ficam mockados, então os testes não precisam do banco.

| Classe de teste | O que cobre |
|---|---|
| `UsuarioBOTest` | cadastro (BCrypt, e-mail duplicado, bairro inexistente), login sem auto-cadastro, usuário inativo, hash fictício do DML, conferência dos hashes do `03_api_complemento.sql`, exclusão lógica, permissões |
| `OcorrenciaBOTest` | registro ABERTA com risco igual ao peso do tipo, sem apoio do autor; edição e exclusão só pelo autor e só em ABERTA; status com histórico; transições proibidas; data de resolução; concorrência; fila priorizada |
| `ApoioBOTest` | apoio, apoio na própria (422), em resolvida (422), duplicado (409), retirada |
| `RiscoBOTest` | fórmula, limites dos níveis, resolvida = 0, ordenação e desempate |
| `PontoAlagadoBOTest` | ponto + alerta + destinatários, raio por nível da água, mensagem, encerramento e permissões |
| `AlertaBOTest` | alertas ativos, alertas do usuário, marcar como lido (idempotente), 404 |
| `RotaBOTest` | zonas de 40 m, rota mais segura, pontos evitados, gravação da consulta, rota não calculada, geometria |
| `IndicadorBOTest` | composição do painel, taxa de leitura, formatação do tempo médio, acesso só do gestor |
| `ModeloTest`, `TradutorErroOracleTest`, `TransacaoTest` | regras da model, tradução dos ORA e commit/rollback das transações |

Execução: `./mvnw test`

<figure><img src="prints/02-testes.png" alt="Saída do ./mvnw test: 86 testes, 0 falhas, BUILD SUCCESS"><figcaption>Figura 25 — Saída do ./mvnw test: 86 testes, 0 falhas, BUILD SUCCESS</figcaption></figure>

**Teste integrado com o Oracle:** os testes unitários usam DAOs simulados e não executam o SQL. Ao testar a API publicada, o endpoint `GET /api/indicadores` devolveu erro 500. A causa era o erro **ORA-00937** ("not a single-group group function") na consulta de totais de alertas do `IndicadorDAO`, que misturava uma subconsulta com funções de agregação sem `GROUP BY`. A consulta foi reescrita com as contagens numa subconsulta, validada no Oracle e publicada (commit `63782bb`). A Figura do painel de indicadores mostra o endpoint funcionando depois da correção.

## 12. Deploy no Render

A aplicação é publicada no **Render** como Web Service **Docker**:

- `Dockerfile` multi-stage: build com Maven + JDK 21 e execução com JRE 21;
- a porta vem da variável `PORT` (`server.port=${PORT:8080}`);
- o `render.yaml` (Blueprint) declara o serviço, o health check `/api/health` e as variáveis de ambiente, sem os valores secretos.

Passo a passo:

1. Em https://dashboard.render.com, clique em **New → Web Service** e conecte o repositório do GitHub.
2. Escolha **Docker** como runtime, a branch `main` e o plano **Free**.
3. Cadastre as variáveis:
   - `DB_URL`: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`;
   - `DB_USER`;
   - `DB_PASSWORD`;
   - `CORS_ORIGINS`;
   - `TZ`: `America/Sao_Paulo`.
4. Defina o **Health Check Path** como `/api/health`.
5. Clique em **Create Web Service** e aguarde o build.
6. Teste `https://<serviço>.onrender.com/api/health` e `https://<serviço>.onrender.com/swagger-ui.html`.

**URL do deploy:** https://alertabairro-api.onrender.com

<figure><img src="prints/render-live.png" alt="Render: serviço alertabairro-api Live, com o histórico de deploys"><figcaption>Figura 26 — Render: serviço alertabairro-api Live, com o histórico de deploys</figcaption></figure>

<figure><img src="prints/render-variaveis.png" alt="Render: variáveis de ambiente cadastradas, com os valores ocultos"><figcaption>Figura 27 — Render: variáveis de ambiente cadastradas, com os valores ocultos</figcaption></figure>

<figure><img src="prints/render-health.png" alt="GET /api/health na URL pública: API e banco UP"><figcaption>Figura 28 — GET /api/health na URL pública: API e banco UP</figcaption></figure>

## 13. Ferramentas e versões

| Ferramenta | Versão / uso |
|---|---|
| Java (JDK) | 21 |
| Spring Boot | 3.3.5 (Web e Validation) |
| Maven | Maven Wrapper (`./mvnw`); 3.9.9 na imagem Docker de build |
| Banco de dados | Oracle Database da FIAP (`oracle.fiap.com.br:1521/ORCL`) |
| Driver JDBC | `ojdbc11` 23.5.0.24.07 |
| Pool de conexões | HikariCP (versão gerenciada pelo Spring Boot) |
| Hash de senha | BCrypt (`spring-security-crypto`) |
| Documentação da API | springdoc-openapi 2.6.0 (Swagger UI) |
| Testes | JUnit 5, Mockito e AssertJ |
| IDE | Visual Studio Code |
| Cliente do banco | Oracle SQL Developer Extension for VS Code |
| Versionamento | Git e GitHub |
| Deploy | Docker (imagem `eclipse-temurin:21-jre-jammy`) e Render (plano Free) |

**Links:**

- Repositório: https://github.com/AlertaBairro/DDD-Java-GSCitizenTech
- Deploy: https://alertabairro-api.onrender.com
- Swagger: https://alertabairro-api.onrender.com/swagger-ui.html
- Health check: https://alertabairro-api.onrender.com/api/health

## 14. Como executar localmente

1. Execute no Oracle, nesta ordem: `01_ddl.sql`, `02_dml.sql` e `03_api_complemento.sql`, da pasta `src/main/resources/db`. Se existirem tabelas `AB_*` da versão anterior, rode antes o `99_drop_esquema_antigo.sql`.
2. Defina as variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD`.
3. Rode `./mvnw clean package` e depois `java -jar target/alertabairro-api.jar`.
4. Acesse `http://localhost:8080/swagger-ui.html`.

Usuários de teste:

| Perfil | E-mail | Senha |
|---|---|---|
| MORADOR | morador@alertabairro.com | morador123 |
| AGENTE | agente@alertabairro.com | agente123 |
| GESTOR | gestor@alertabairro.com | gestor123 |

<figure><img src="prints/oracle-historico.png" alt="Oracle FIAP: as 10 tabelas T_AB_ e o SELECT na T_AB_HISTORICO_STATUS com a mudança feita pela API"><figcaption>Figura 29 — Oracle FIAP: as 10 tabelas T_AB_ e o SELECT na T_AB_HISTORICO_STATUS com a mudança feita pela API</figcaption></figure>

<figure><img src="prints/oracle-alerta-usuario.png" alt="Oracle FIAP: os 6 destinatários do alerta 7 e a leitura gravada para a usuária 14"><figcaption>Figura 30 — Oracle FIAP: os 6 destinatários do alerta 7 e a leitura gravada para a usuária 14</figcaption></figure>

## 15. Evidências

| Requisito da disciplina | Onde está a evidência |
|---|---|
| Camada model de acordo com o banco | seção 6 (tabelas × classes, DER e diagrama de classes) |
| Camadas DAO e BO | seção 5 (arquitetura) e seção 7 (regras de negócio) |
| Controller com GET, POST, PUT e DELETE | seção 9 (cadastro, login, registro, status, histórico, ponto alagado, alertas, rota, indicadores e exclusão) |
| Tratamento de exceções | seção 10 (erros 400, 409 e 422 com JSON padronizado) |
| Deploy no Render | seção 12 (serviço Live, variáveis e health check com banco UP) |
| Protótipos, endpoints e DER | seções 8, 9 e 6.3 |
| Ferramentas, versões, links e execução | seções 13 e 14 |
