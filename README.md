# 🎮 The Great Programming Journey

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![OOP](https://img.shields.io/badge/OOP-Design_Patterns-blue?style=for-the-badge&logo=abstract&logoColor=white)](#-arquitetura)
[![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

**Jogo de tabuleiro em Java onde programadores enfrentam abismos de código e recolhem ferramentas para chegar à glória**

[Descrição](#-descrição) •
[Funcionalidades](#-funcionalidades) •
[Arquitetura](#-arquitetura) •
[Como Jogar](#-como-jogar) •
[Instalação](#-instalação) •
[Testes](#-testes)

---

## 📋 Descrição

**The Great Programming Journey** é um jogo de tabuleiro digital para 2 a 4 jogadores, onde cada participante assume o papel de um programador numa corrida até à meta. Pelo caminho, os jogadores enfrentam **abismos** — armadilhas temáticas do mundo da programação — e podem recolher **ferramentas** que os protegem desses perigos.

O jogo combina estratégia, sorte (dado de 1 a 6) e gestão de recursos, num cenário que reflete de forma lúdica os desafios reais de um programador.

---

## ✨ Funcionalidades

| Funcionalidade | Descrição |
|---|---|
| 🎲 **Sistema de Turnos** | Ordem de jogo por ID crescente, com dado de 1 a 6 |
| 🕳️ **11 Tipos de Abismos** | Cada um com efeito único sobre o jogador |
| 🔧 **6 Tipos de Ferramentas** | Recolhíveis no tabuleiro, anulam abismos específicos |
| 🔄 **Bounce-back** | Jogador que ultrapassa a meta "bate" e volta para trás |
| 🚫 **Restrições por Linguagem** | Assembly (máx. 2 casas), C (máx. 3 casas) |
| 💾 **Save / Load** | Gravar e carregar o estado completo do jogo |
| 🏆 **Resultados Finais** | Ranking com vencedor, empate e estatísticas |
| 🧪 **Testes Unitários** | Suite completa com JUnit 5 |

---

## 🏗️ Arquitetura

O projeto segue princípios de Programação Orientada a Objetos — herança, polimorfismo e abstração — para manter o código extensível e organizado.

```
┌──────────────────────────────────────────────────────────────────┐
│                         GameManager                              │
│                                                                  │
│   Controlo de turnos · Movimentos · Abismos · Ferramentas       │
│   Vitória / Derrota · Save / Load · Resultados                  │
└───────┬──────────────────┬───────────────────┬───────────────────┘
        │                  │                   │
        ▼                  ▼                   ▼
┌───────────────┐  ┌──────────────┐   ┌──────────────┐
│  Programmer   │  │    Abyss     │   │     Tool     │
│               │  │  (abstract)  │   │  (abstract)  │
│ • id, name    │  │              │   │              │
│ • languages   │  │ applyEffect()│   │canCancelAbyss│
│ • tools[]     │  │ getImageName │   │ getImageName │
│ • state       │  │              │   │              │
│ • history     │  └──────┬───────┘   └──────┬───────┘
└───────────────┘         │                  │
                          ▼                  ▼
              ┌───────────────────┐  ┌───────────────────┐
              │  11 Subclasses    │  │   6 Subclasses    │
              │                   │  │                   │
              │ SyntaxError       │  │ Inheritance       │
              │ LogicError        │  │ FunctionalProg.   │
              │ Exception         │  │ UnitTest          │
              │ FileNotFound      │  │ ExceptionTool     │
              │ Crash             │  │ IDE               │
              │ DuplicatedCode    │  │ AjudaProfessor    │
              │ SecondaryEffects  │  └───────────────────┘
              │ BlueScreenOfDeath │
              │ InfiniteLoop      │
              │ SegmentationFault │
              │ StackOverflow     │
              └───────────────────┘
```

### Decisões de Design

- **`GameManager`** centraliza toda a lógica de jogo, mantendo `Programmer`, `Abyss` e `Tool` focados nos seus próprios dados e comportamentos
- **Classes abstratas** (`Abyss`, `Tool`) definem contratos comuns; cada subclasse implementa o seu efeito específico
- **Polimorfismo** permite adicionar novos abismos/ferramentas sem alterar o núcleo do motor de jogo
- **Encapsulamento** no `Programmer` isola estado, histórico de posições e inventário de ferramentas

---

## 🕳️ Abismos

Cada abismo tem um efeito distinto quando o jogador cai nele:

| ID | Abismo | Efeito |
|---|---|---|
| 0 | **Erro de Sintaxe** | Recua 1 casa |
| 1 | **Erro de Lógica** | Recua `dado / 2` casas |
| 2 | **Exception** | Recua 2 casas |
| 3 | **File Not Found Exception** | Recua 3 casas |
| 4 | **Crash de Memória** | Volta à posição 1 |
| 5 | **Código Duplicado** | Volta à posição anterior ao movimento |
| 6 | **Efeitos Secundários** | Volta à posição de 2 movimentos atrás |
| 7 | **Blue Screen of Death** | Jogador eliminado |
| 8 | **Ciclo Infinito** | Jogador fica preso (libertado se outro cair na mesma casa) |
| 9 | **Segmentation Fault** | Se 2+ jogadores na mesma casa, todos recuam 3 |
| 10 | **Stack Overflow** | Perde todas as ferramentas e recua N casas (N = nº ferramentas) |

---

## 🔧 Ferramentas

As ferramentas são recolhidas ao passar na casa correspondente e anulam abismos específicos:

| ID | Ferramenta | Anula Abismos |
|---|---|---|
| 0 | **Herança** | Nenhum (passiva) |
| 1 | **Programação Funcional** | Código Duplicado, Efeitos Secundários, Ciclo Infinito |
| 2 | **Testes Unitários** | Erro de Lógica |
| 3 | **Tratamento de Exceções** | Exception, File Not Found Exception |
| 4 | **IDE** | Erro de Sintaxe |
| 5 | **Ajuda do Professor** | LLM |

> Quando um jogador cai num abismo e possui a ferramenta correspondente, o abismo é **anulado** e a ferramenta é **consumida**.

---

## 🎯 Como Jogar

### Regras Básicas

1. **2 a 4 jogadores**, cada um com uma cor única (`Purple`, `Green`, `Brown`, `Blue`)
2. Todos começam na **posição 1** — o primeiro a chegar à última casa vence
3. A cada turno, lança-se um **dado** (1–6) e avança-se esse número de casas
4. Se ultrapassar a meta, o jogador **"bate" e volta para trás** (bounce-back)
5. O jogo termina quando alguém chega à meta ou resta apenas 1 jogador vivo

### Restrições por Linguagem

| 1ª Linguagem | Restrição |
|---|---|
| **Assembly** | Máximo 2 casas por turno |
| **C** | Máximo 3 casas por turno |
| Outras | Sem restrição |

### Estados do Jogador

| Estado | Significado |
|---|---|
| `Em Jogo` | Pode jogar normalmente |
| `Preso` | Não pode mover (preso no Ciclo Infinito) |
| `Derrotado` | Eliminado (Blue Screen of Death) |

---

## 💻 Instalação

### 1. Clonar o Repositório

```bash
git clone https://github.com/goncaloalegria/great-programming-journey.git
cd great-programming-journey
```

### 2. Estrutura do Projeto

```
great-programming-journey/
├── README.md
├── src/
│   └── pt/ulusofona/lp2/greatprogrammingjourney/
│       ├── Main.java
│       ├── GameManager.java
│       ├── Programmer.java
│       ├── Abyss.java                    # Classe abstrata
│       │   ├── SyntaxErrorAbyss.java
│       │   ├── LogicErrorAbyss.java
│       │   ├── ExceptionAbyss.java
│       │   ├── FileNotFoundExceptionAbyss.java
│       │   ├── CrashAbyss.java
│       │   ├── DuplicatedCodeAbyss.java
│       │   ├── SecondaryEffects.java
│       │   ├── BlueScreenOfDeathAbyss.java
│       │   ├── InfiniteLoopAbyss.java
│       │   ├── SegmentationFaultAbyss.java
│       │   ├── StackOverflowAbyss.java
│       │   └── LLMAbyss.java
│       ├── Tool.java                     # Classe abstrata
│       │   ├── InheritanceTool.java
│       │   ├── FunctionalProgrammingTool.java
│       │   ├── UnitTestTool.java
│       │   ├── ExceptionTool.java
│       │   ├── IdeTool.java
│       │   └── AjudaProfessorTool.java
│       ├── InvalidFileException.java
│       └── TestGameManager.java          # JUnit 5
└── images/
    ├── syntax.png
    ├── logic.png
    ├── crash.png
    ├── glory.png
    └── ...
```

### 3. Compilar e Executar

```bash
# Compilar
javac -d out src/pt/ulusofona/lp2/greatprogrammingjourney/*.java

# Executar
java -cp out pt.ulusofona.lp2.greatprogrammingjourney.Main
```

---

## 🧪 Testes

O projeto inclui uma suite completa de testes unitários com **JUnit 5**, cobrindo:

| Categoria | Exemplos |
|---|---|
| **Criação do tabuleiro** | Validação de jogadores, cores, IDs, tamanho do board |
| **Movimento** | Avanço normal, bounce-back, restrições por linguagem |
| **Abismos** | Todos os 11 tipos de abismos e os seus efeitos |
| **Ferramentas** | Recolha, anulação de abismos, consumo |
| **Estado do jogo** | Vitória, derrota, empate, turnos |
| **Save / Load** | Gravação e carregamento de ficheiros |
| **Edge cases** | Jogador preso, múltiplas ferramentas, posições mínimas |

### Executar Testes

```bash
# Com Maven
mvn test

# Com Gradle
gradle test

# Manualmente com JUnit
java -cp .:junit-platform-console-standalone.jar \
  org.junit.platform.console.ConsoleLauncher \
  --select-class pt.ulusofona.lp2.greatprogrammingjourney.TestGameManager
```

---

## 🔌 API Principal

### `GameManager`

| Método | Descrição |
|---|---|
| `createInitialBoard(playerInfo, boardSize, abyssesAndTools)` | Inicializa o tabuleiro |
| `moveCurrentPlayer(nrPositions)` | Move o jogador atual |
| `reactToAbyssOrTool()` | Processa efeitos da casa atual |
| `getProgrammerInfo(id)` | Info de um jogador (array 7 elementos) |
| `getProgrammersInfo()` | Info de todos os jogadores vivos |
| `getSlotInfo(position)` | Info de uma posição do tabuleiro |
| `gameIsOver()` | Verifica se o jogo terminou |
| `getGameResults()` | Resultados finais |
| `saveGame(file)` / `loadGame(file)` | Persistência |

---

## 🔧 Resolução de Problemas

| Problema | Solução |
|---|---|
| `createInitialBoard` retorna `false` | Verificar: 2–4 jogadores, cores válidas e únicas, IDs > 0 e únicos, board ≥ 2×jogadores |
| Jogador não se move | Verificar se está `Preso` ou `Derrotado`, ou se excede limite da linguagem |
| Ferramenta não anula abismo | Confirmar que a ferramenta certa está no inventário (ver tabela de anulações) |
| `loadGame` lança exceção | Verificar formato do ficheiro de save |
| Bounce-back inesperado | Posição = `boardSize - (posição_calculada - boardSize)` |

---

## 👤 Autor

- **Gonçalo Alegria** — [@goncaloalegria](https://github.com/goncaloalegria)

---

## 🙏 Agradecimentos

- [Universidade Lusófona](https://www.ulusofona.pt/) — Instituição de ensino
- [JUnit 5](https://junit.org/junit5/) — Framework de testes
- [Java](https://www.java.com/) — Linguagem de programação
