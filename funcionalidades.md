# 📋 Acompanhamento de Funcionalidades por Desenvolvedor

Este documento rastreia o progresso do desenvolvimento da plataforma, dividido pelas responsabilidades verticais (Back-end + Front-end) de cada membro da equipe. Marque com um `x` as tarefas concluídas.

## Tarefas Compartilhadas (Base do Projeto)
- [ ] Modelagem do Banco de Dados e API Inicial
- [ ] Validação de retorno da IA
- [ ] Sistema de bônus e penalidades automáticas
- [ ] Testes e refinamentos finais

---

## Raquel
**Foco:** Autenticação, Infraestrutura de Imagem e Tratamento de Exceções.

### Sprint 1
- [X] **UC01:** Acesso simplificado via e-mail corporativo (Tabela de usuários, login, Context API React)

### Sprint 2
- [X] **UC06:** Check-in visual com foto (Serviço de upload, interface de câmera/arquivo, timer de 10 min)

### Sprint 3
- [X] **UC05:** Cancelamento com penalidade no Trust Score (Lógica de prazo e aviso de penalidade)
- [X] **UC10:** Solicitar limpeza/manutenção (Bloqueio de sala e formulário de reporte)

---

## [Bianca]
**Foco:** Domínio de Espaços, Encerramentos e Sistema de Reputação.

### Sprint 1
- [X] **UC04:** Cadastrar salas (CRUD de salas e utilitários)
- [X] **UC02:** Buscar salas e localização (Filtros e vitrine no front-end)

### Sprint 2
- [ ] **UC08:** Check-out visual com foto (Fluxo de saída e obrigatoriedade de imagem)
- [ ] **UC09:** Auditoria de estado final via IA (Prompt de limpeza/integridade e feedback do ambiente)

### Sprint 3
- [ ] **UC11:** Atualizar dinamicamente Trust Score (Service de leitura de logs e painel de histórico do usuário)

---

## [Luigi]
**Foco:** Transações de Reserva, Integração com IA e Dashboard Administrativo.

### Sprint 1
- [X] **UC03:** Reservar sala (Validação de conflitos de horário, componente de calendário/grade)

### Sprint 2
- [ ] Integração com Gemini 1.5 Flash API (Client HTTP Spring, processamento do prompt)
- [ ] **UC07:** Auditoria de estado inicial via IA (Comparação com padrão e tela de loading/veredito)

### Sprint 3
- [ ] **UC12:** Configurar regras de negócio - Dashboard ADM (Endpoints de parametrização e painel gerencial)