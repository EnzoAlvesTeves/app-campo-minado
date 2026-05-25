# 💣 Campo Minado - Android & PHP API

Este é um jogo clássico de **Campo Minado** desenvolvido para Android, com um backend em PHP rodando em Docker para persistência de ranking e pontuações. O projeto possui um design moderno no estilo "Dark Mode" e um sistema competitivo de pontuação.

## 🚀 Tecnologias Utilizadas

### Mobile (Android)
*   **Kotlin**: Linguagem principal do app.
*   **Retrofit 2**: Cliente HTTP para comunicação com a API.
*   **Material Components**: Design moderno e componentes de interface.

### Backend (API)
*   **PHP 8.2**: Processamento da lógica de salvamento e recuperação de scores.
*   **MySQL 8.0**: Banco de dados para armazenamento do ranking global.
*   **Docker & Docker Compose**: Orquestração do ambiente de servidor e banco de dados.

## 🎮 O Jogo

### O que é?
O Campo Minado é um jogo de lógica onde o objetivo é revelar todas as casas de uma grade que não contenham minas terrestres. Se o jogador clicar em uma mina, o jogo acaba imediatamente.

### Lógica por trás do código
1.  **Geração do Mapa**: O mapa é gerado dinamicamente com base na dificuldade escolhida (Fácil, Médio ou Difícil).
2.  **Distribuição Aleatória**: As minas são posicionadas aleatoriamente na matriz no início da partida.
3.  **Cálculo de Adjacência**: Ao clicar em uma casa segura, o app calcula quantas minas existem nas 8 casas vizinhas e exibe esse número.
4.  **Limpeza Automática (Flood Fill)**: Se você clicar em uma casa com "0" minas vizinhas, o jogo abre automaticamente todas as casas vazias adjacentes até encontrar números.
5.  **Sistema de Pontuação**:
    *   **Progresso**: 100 pontos por cada casa revelada.
    *   **Multiplicador**: Fácil (x1.0), Médio (x1.5), Difícil (x2.0).
    *   **Bônus de Vitória**: Bônus fixo por vencer + bônus de agilidade (tempo restante).

## 🌐 API Endpoints

A API roda em `http://localhost:8080` (ou `10.0.2.2:8080` no emulador Android).

### 1. `GET /get_scores.php`
Retorna a lista dos 50 melhores jogadores ordenada por pontuação.

**Exemplo de Resposta (JSON):**
```json
[
  {
    "name": "Enzo Teves",
    "avatar": "👱‍♂️",
    "time": 2,
    "difficulty": "Fácil",
    "points": 3100,
    "win": true
  },
  {
    "name": "Fulano de Tal",
    "avatar": "💂‍♂️",
    "time": 4,
    "difficulty": "Fácil",
    "points": 1000,
    "win": false
  },
  {
    "name": "Maria Silva",
    "avatar": "🤠",
    "time": 3,
    "difficulty": "Médio",
    "points": 600,
    "win": false
  }
]
```

### 2. `POST /save_score.php`
Salva uma nova pontuação no banco de dados.

**Corpo da Requisição (JSON):**
```json
{
  "name": "Enzo Teves",
  "avatar": "👦",
  "time": 45,
  "difficulty": "Médio",
  "points": 2500,
  "win": true
}
```

## 🛠️ Como Configurar e Rodar

### 1. Backend (Docker)
Certifique-se de ter o Docker e Docker Compose instalados em sua máquina.

1.  Abra o terminal na pasta raiz do projeto.
2.  Suba os containers:
    ```bash
    docker-compose up --build -d
    ```
3.  A API estará disponível em `http://localhost:8080/`.

### 2. Mobile (Android Studio)
1.  Abra o projeto na pasta `app` com o Android Studio.
2.  Aguarde a sincronização do Gradle.
3.  Execute o app em um emulador (conectará automaticamente à API via `10.0.2.2`).

## 📁 Estrutura do Repositório
*   `/app`: Código fonte do aplicativo Android.
*   `/api`: Código fonte da API PHP e Dockerfile do servidor.
*   `/db`: Scripts SQL de inicialização do banco de dados.
*   `docker-compose.yml`: Configuração da infraestrutura local.

---
Desenvolvido por **Enzo Teves**. 🚩
