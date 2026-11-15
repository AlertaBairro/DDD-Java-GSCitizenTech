# Registro de uso de IA — AlertaBairro API (DDD – Java)

Conforme os combinados da Global Solution 2026.2 ("IA como copiloto: registrem os prompts e saibam explicar cada linha"), este arquivo registra como a inteligência artificial foi usada nesta entrega.

## Resumo

| Ferramenta | Uso |
|---|---|
| **Claude Code** (extensão do VS Code) | Geração do código da API (camadas, testes), adaptação ao modelo de dados do grupo e documentação |
| **Claude** (chat) | Leitura do briefing, planejamento da entrega, dúvidas sobre Oracle, GitHub e deploy no Render |

Todo o código gerado foi revisado pela equipe. Os integrantes sabem explicar a arquitetura em camadas (model, dao, bo, controller, exception), as regras de negócio e o deploy.

Os prompts de geração de código eram longos e detalhados, então estão **resumidos** abaixo. Os textos completos podem ser mostrados se o professor pedir.

---

## Parte 1 — Geração e adaptação do código (Claude Code)

### 1.1 Estrutura inicial da API

- **Pedido (resumo):** criar uma API REST em Java 21 + Spring Boot 3 seguindo a rubrica da disciplina: camada model, DAO e BO com JDBC puro no Oracle da FIAP, controllers com GET/POST/PUT/DELETE, tratamento de exceções, credenciais por variáveis de ambiente e arquivos de deploy para o Render.
- **Resultado:** estrutura do projeto, as camadas, `ConnectionFactory`, `GlobalExceptionHandler`, Dockerfile, `render.yaml` e testes unitários.
- **Revisão da equipe:** conferência das camadas contra a rubrica e testes locais com `./mvnw test`.

### 1.2 Adaptação ao banco de dados do grupo

- **Pedido (resumo):** ajustar a API para usar exatamente as 10 tabelas `T_AB_*` do DDL da entrega de Database, sem alterá-las, com IDs por sequence e operações compostas em transação.
- **Resultado:** classes da model espelhando as tabelas, DAOs reescritos, regras de negócio (status com histórico, ponto alagado gerando alertas, rota registrada, indicadores) e script `03_api_complemento.sql`.
- **Revisão da equipe:** conferência do mapeamento tabela → classe e execução dos scripts no Oracle.

### 1.3 Documentação

- **Pedido (resumo):** completar o README e a documentação do PDF com a Ficha da CitizenTech, justificativa, objetivos, protótipos de tela e ferramentas e versões, sem alterar o código.
- **Resultado:** README e `docs/documentacao.md` atualizados e este arquivo.
- **Revisão da equipe:** leitura e ajuste dos textos com o grupo.

---

## Parte 2 — Dúvidas e passo a passo (Claude, chat)

| # | Pergunta (resumo) | O que foi feito com a resposta |
|---|---|---|
| 1 | Quais são os requisitos da disciplina de Java na GS? É preciso subir commits ou só entregar o arquivo? | Entendemos que o código é avaliado pelo GitHub e que commits após o prazo zeram a parte técnica |
| 2 | Para fazer o Java, preciso esperar o trabalho de Database ou posso começar? | Entendemos que só a camada model dependia do modelo de dados e que o restante podia começar antes |
| 3 | Como comparar o projeto com o que a atividade pede? | Levantamos o que faltava: deploy, links, protótipos, justificativa, versões e Ficha |
| 4 | Como faço o deploy no Render? | Criamos o Web Service Docker, as variáveis de ambiente e o health check |
| 5 | Como executo os scripts SQL no Oracle da FIAP? Preciso usar o login de quem criou as tabelas? | Conectamos pela extensão SQL Developer do VS Code e rodamos os scripts 01, 02 e 03 no RM usado pela API |
| 6 | Como deixo o repositório visível para o professor? | Alteramos a visibilidade no GitHub para público |

---

## Observações

- Nenhum dado pessoal real foi usado: os dados do banco são simulados (scripts `02_dml.sql` e `03_api_complemento.sql`).
- As credenciais do banco não estão no código nem no repositório; ficam nas variáveis de ambiente do Render.
