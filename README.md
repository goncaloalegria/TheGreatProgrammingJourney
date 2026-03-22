# 🎮 The Great Programming Journey

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![OOP](https://img.shields.io/badge/OOP-Design_Patterns-blue?style=for-the-badge&logo=abstract&logoColor=white)](#-arquitetura)
[![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

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
| **C** (exato, não C++ nem C#) | Máximo 3 casas por turno |
| Outras | Sem restrição |

### Estados do Jogador

| Estado | Significado |
|---|---|
| `Em Jogo` | Pode jogar normalmente |
| `Preso` | Não pode mover (preso no Ciclo Infinito) |
| `Derrotado` | Eliminado (Blue Screen of Death) |

---

## 💻 Instalação

### Pré-requisitos

| Requisito | Descrição |
|---|---|
| **Java JDK 11+** | Runtime e compilação |
| **IntelliJ IDEA** | IDE recomendado (Community ou Ultimate) |
| **JUnit 5** | Para correr os testes unitários |

### 1. Clonar o Repositório

```bash
git clone https://github.com/goncaloalegria/TheGreatProgrammingJourney.git
cd TheGreatProgrammingJourney
```

### 2. Abrir no IntelliJ

1. Abrir o **IntelliJ IDEA**
2. **File → Open** → selecionar a pasta do projeto
3. Esperar que o IntelliJ indexe o projeto

### 3. Configurar o SDK

1. Ir a **File → Project Structure → Project**
2. Em **SDK**, selecionar **Java 11** ou superior
3. Em **Language level**, selecionar **11** ou superior
4. Clicar **Apply → OK**

### 4. Adicionar o GUI Viewer como Biblioteca

O projeto inclui um JAR com a interface gráfica na pasta `lib/`:

```
lib/
└── LP2-GuiViewer2526-recurso-1.0.0.jar
```

Para o adicionar ao projeto:

1. No painel do projeto, clicar direito na pasta **`lib`**
2. Selecionar **Add as Library...**
3. Confirmar com **OK**
4. Verificar que o JAR aparece em **External Libraries** no painel lateral

### 5. Configurar a Run Configuration

O projeto utiliza um launcher Kotlin incluído no JAR do GUI Viewer:

1. Ir a **Run → Edit Configurations...**
2. Clicar no **+** → **Application**
3. Preencher os campos:

| Campo | Valor |
|---|---|
| **Name** | `TheGreatProgrammingJourney` |
| **Main class** | `pt.ulusofona.lp2.greatprogrammingjourney.guiSimulator.AppLauncherKt` |
| **Module classpath** | Selecionar o módulo do projeto |

4. Clicar **OK**

### 6. Executar

Clicar no ▶️ **Run** ou pressionar `Shift + F10`. A interface gráfica do jogo deverá abrir.

---

## 📁 Estrutura do Projeto

```
TheGreatProgrammingJourney/
├── README.md
├── DiagramaUML.png
│
├── lib/
│   └── LP2-GuiViewer2526-recurso-1.0.0.jar   # Interface gráfica (GUI)
│
├── src/
│   └── pt/ulusofona/lp2/greatprogrammingjourney/
│       ├── Main.java                          # Entry point (vazio — usar AppLauncherKt)
│       ├── GameManager.java                   # Motor de jogo principal
│       ├── Programmer.java                    # Modelo do jogador
│       │
│       ├── Abyss.java                         # Classe abstrata dos abismos
│       │   ├── SyntaxErrorAbyss.java          # ID 0 — Recua 1
│       │   ├── LogicErrorAbyss.java           # ID 1 — Recua dado/2
│       │   ├── ExceptionAbyss.java            # ID 2 — Recua 2
│       │   ├── FileNotFoundExceptionAbyss.java# ID 3 — Recua 3
│       │   ├── CrashAbyss.java                # ID 4 — Volta a posição 1
│       │   ├── DuplicatedCodeAbyss.java       # ID 5 — Volta à posição anterior
│       │   ├── SecondaryEffects.java           # ID 6 — Volta 2 movimentos atrás
│       │   ├── BlueScreenOfDeathAbyss.java    # ID 7 — Elimina jogador
│       │   ├── InfiniteLoopAbyss.java         # ID 8 — Prende jogador
│       │   ├── SegmentationFaultAbyss.java    # ID 9 — Todos recuam 3
│       │   ├── StackOverflowAbyss.java        # ID 10 — Perde ferramentas
│       │   └── LLMAbyss.java                  # ID 20 — Efeito especial
│       │
│       ├── Tool.java                          # Classe abstrata das ferramentas
│       │   ├── InheritanceTool.java           # ID 0 — Herança
│       │   ├── FunctionalProgrammingTool.java # ID 1 — Programação Funcional
│       │   ├── UnitTestTool.java              # ID 2 — Testes Unitários
│       │   ├── ExceptionTool.java             # ID 3 — Tratamento de Exceções
│       │   ├── IdeTool.java                   # ID 4 — IDE
│       │   └── AjudaProfessorTool.java        # ID 5 — Ajuda do Professor
│       │
│       ├── InvalidFileException.java          # Exceção para ficheiros inválidos
│       └── TestGameManager.java               # Testes unitários (JUnit 5)
│
└── out/                                       # Ficheiros compilados
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

### Executar Testes no IntelliJ

1. Abrir o ficheiro `TestGameManager.java`
2. Clicar direito → **Run 'TestGameManager'**
3. Os resultados aparecem no painel de testes em baixo

> **Nota**: é necessário ter o JUnit 5 configurado. Se o IntelliJ pedir para adicionar a dependência, aceitar automaticamente.

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
| Jogo não abre / `Main.java` não faz nada | O entry point é `AppLauncherKt` do JAR, não o `Main.java`. Configurar a Run Configuration conforme [passo 5 da instalação](#5-configurar-a-run-configuration) |
| `Class not found: AppLauncherKt` | O JAR em `lib/` não foi adicionado como biblioteca. Clicar direito em `lib/` → **Add as Library** |
| `No SDK specified` | Ir a **File → Project Structure → Project** e selecionar Java 11+ |
| `createInitialBoard` retorna `false` | Verificar: 2–4 jogadores, cores válidas e únicas, IDs > 0 e únicos, board ≥ 2×jogadores |
| Jogador não se move | Verificar se está `Preso` ou `Derrotado`, ou se excede limite da linguagem |
| Ferramenta não anula abismo | Confirmar que a ferramenta certa está no inventário (ver tabela de anulações) |
| Testes falham com `No JUnit 5` | IntelliJ deve sugerir adicionar a dependência automaticamente; aceitar o prompt |

---

## 👤 Autor

- **Gonçalo Alegria** — [@goncaloalegria](https://github.com/goncaloalegria)

---

## 🙏 Agradecimentos

- [Universidade Lusófona](https://www.ulusofona.pt/) — Instituição de ensino
- [JUnit 5](https://junit.org/junit5/) — Framework de testes
- [Java](https://www.java.com/) — Linguagem de programação
