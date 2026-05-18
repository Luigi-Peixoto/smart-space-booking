# Smart Space Booking

Sistema Inteligente de Governança e Reservas para Coworking Corporativo. Uma plataforma centralizada que revoluciona a gestão de espaços através de Economia de Reputação e Auditoria Visual por IA.

O sistema vai além do agendamento comum ao implementar um modelo de Economia de Reputação, onde o zelo pelo patrimônio é a moeda de troca. Através de auditoria visual por Inteligência Artificial e uma pontuação de confiança (Trust Score), o sistema automatiza a fiscalização e garante que os espaços estejam sempre prontos para o próximo usuário.

## Tecnologias Utilizadas

* **Front-end:** React.js + Vite
* **Back-end Principal:** Java (Spring Boot)
* **Microserviço de Arquivos (ms-files):** Node.js / Express (Gestão de uploads)
* **Banco de Dados:** PostgreSQL
* **Inteligência Artificial:** Integração com Gemini 1.5 Flash API
* **Infraestrutura:** Docker e Docker Compose

## Como rodar o projeto localmente

### Pré-requisitos
Certifique-se de ter instalado em sua máquina:
* **Docker e Docker Compose** (Recomendado)
* **Java 21** (ou superior)
* **PostgreSQL**
* **Node.js** (v22+ recomendado via NVM)

### 1. Configurando o Banco de Dados

#### Opção A: Execução via Docker

1. Na raiz do projeto, execute o comando para subir os containers (ex: PostgreSQL):
   ```bash
   docker-compose up -d
   ```
2. O banco de dados smart_space_db será criado e exposto automaticamente na porta 5432.

#### Opção B: Execução manual completa

1. Abra o seu SGBD e crie um banco de dados chamado `smart_space_db`.
2. No repositório, navegue até `backend/src/main/resources/application.properties`.
3. Verifique se as credenciais de `spring.datasource.username` e `spring.datasource.password` correspondem ao seu banco local.

### 2. Rodando o Microserviço de Arquivos (ms-files)

Este serviço é responsável pelo upload de imagens (salas e check-in/out).
1. Clone o repositório na sua máquina
    ```Bash
    git clone https://github.com/Bia1000b/ms-files
    ```
2. Abra um terminal e acesse a pasta do microserviço:
    ```Bash
    cd ms-files
    ```
3. Execute o projeto maven
    ```Bash
    mvn spring-boot:run
    ```

### 3. Rodando o Back-end (Spring Boot)
1. Abra um terminal na raiz do projeto e acesse a pasta do back-end:
   ```bash
   cd backend
   ```
2. Execute a aplicação utilizando o Maven Wrapper:
    ```bash
    ./mvnw spring-boot:run
   ```
    ou
    ```bash
    mvn spring-boot:run
   ```
3. A API estará rodando em http://localhost:8080.


- Nota sobre o DataSeeder: Ao rodar o Back-end pela primeira vez, o nosso script de Database Seeding detectará que o banco está vazio e criará automaticamente:
    - Um usuário Administrador (admin@smartspace.com.br) e um usuário comum de teste.
    - O Motor de Regras dinâmicas (penalidades por cancelamento).
    - Salas iniciais e reservas de exemplo.

### 4. Rodando o Front-end (React + Vite)
1. Abra um novo terminal na raiz do projeto e acesse a pasta do front-end:
   ```bash
   cd frontend
   ```
2. Instale as dependências do Node:
    ```bash
   npm instal
   ```
3. Inicie o servidor de desenvolvimento:
    ```bash
    npm run dev
   ```
4. A interface estará disponível no navegador, geralmente em http://localhost:5173.

## Equipe

- Bianca Maciel
- Luigi Soares
- Raquel Freire