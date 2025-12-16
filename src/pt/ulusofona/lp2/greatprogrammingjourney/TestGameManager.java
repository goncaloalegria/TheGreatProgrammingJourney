package pt.ulusofona.lp2.greatprogrammingjourney;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestGameManager {


    @Test
    public void testCompleteScenario() {
        GameManager gm = new GameManager();

        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };

        // Ferramenta Prog. Funcional (ID 1) na posição 5
        // Abismo Erro Lógica (ID 1) na posição 10
        String[][] abyssesAndTools = {
                {"1", "1", "5"},
                {"0", "1", "10"}
        };

        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        System.out.println("=== TURNO 1: Jogador 1 vai para posição 5 ===");
        boolean m1 = gm.moveCurrentPlayer(4);
        System.out.println("move: " + m1);
        String r1 = gm.reactToAbyssOrTool();
        System.out.println("react: " + r1);
        String[] info1 = gm.getProgrammerInfo(1);
        System.out.println("Ferramentas: " + info1[5]);
        System.out.println("Posição: " + info1[4]);

        System.out.println("\n=== TURNO 2: Jogador 2 move ===");
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        System.out.println("\n=== TURNO 3: Jogador 1 vai para posição 10 (abismo) ===");
        boolean m3 = gm.moveCurrentPlayer(5);
        System.out.println("move: " + m3);
        String r3 = gm.reactToAbyssOrTool();
        System.out.println("react: " + r3);
        String[] info3 = gm.getProgrammerInfo(1);
        System.out.println("Ferramentas: " + info3[5]);
        System.out.println("Posição: " + info3[4] + " (esperado: 10)");
        System.out.println("Estado: " + info3[6]);

        // Verificações
        System.out.println("\n=== VERIFICAÇÕES ===");
        System.out.println("Posição é 10? " + info3[4].equals("10"));
        System.out.println("Ferramentas é 'No tools'? " + info3[5].equals("No tools"));
        System.out.println("React contém 'anulado'? " + (r3 != null && r3.contains("anulado")));
    }

    @Test
    public void testCicloInfinitoExact() {
        GameManager gm = new GameManager();

        // Configuração típica do professor
        String[][] playerInfo = {
                {"1", "P1", "Java", "Purple"},
                {"2", "P2", "Python", "Green"}
        };

        String[][] abyssesAndTools = {
                {"0", "8", "5"}  // Abismo ID 8 (Ciclo Infinito) na posição 5
        };

        boolean result = gm.createInitialBoard(playerInfo, 10, abyssesAndTools);
        System.out.println("createInitialBoard: " + result);

        // Movimento para cair no abismo
        System.out.println("Current player before move: " + gm.getCurrentPlayerID());

        boolean moveResult = gm.moveCurrentPlayer(4);  // 1 + 4 = 5
        System.out.println("moveCurrentPlayer(4): " + moveResult);

        String reactResult = gm.reactToAbyssOrTool();
        System.out.println("reactToAbyssOrTool(): '" + reactResult + "'");
        System.out.println("reactToAbyssOrTool() is null: " + (reactResult == null));

        // Verificar estado
        String[] info = gm.getProgrammerInfo(1);
        System.out.println("Estado do jogador: " + info[6]);
        System.out.println("Posição do jogador: " + info[4]);
    }

    @Test
    public void testLogicErrorVsTool_Simulation() {
        GameManager gm = new GameManager();
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        // Simular o que o teste do professor pode estar a fazer
        String[][] abyssesAndTools = {
                {"1", "1", "5"},   // Ferramenta Prog. Funcional (ID 1) na posição 5
                {"0", "1", "10"}   // Abismo Erro Lógica (ID 1) na posição 10
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        // Verificar estado inicial
        System.out.println("Jogador atual: " + gm.getCurrentPlayerID());

        // Turno 1
        System.out.println("\n=== TURNO 1 ===");
        boolean m1 = gm.moveCurrentPlayer(4);
        System.out.println("move(4): " + m1);
        String r1 = gm.reactToAbyssOrTool();
        System.out.println("react: " + r1);
        System.out.println("P1: " + gm.getProgrammerInfoAsStr(1));
        System.out.println("Jogador atual: " + gm.getCurrentPlayerID());

        // Turno 2
        System.out.println("\n=== TURNO 2 ===");
        boolean m2 = gm.moveCurrentPlayer(2);
        System.out.println("move(2): " + m2);
        String r2 = gm.reactToAbyssOrTool();
        System.out.println("react: " + r2);
        System.out.println("Jogador atual: " + gm.getCurrentPlayerID());

        // Turno 3
        System.out.println("\n=== TURNO 3 ===");
        boolean m3 = gm.moveCurrentPlayer(5);
        System.out.println("move(5): " + m3);
        String r3 = gm.reactToAbyssOrTool();
        System.out.println("react: " + r3);
        System.out.println("P1: " + gm.getProgrammerInfoAsStr(1));

        String[] info = gm.getProgrammerInfo(1);
        System.out.println("\nPosição final: " + info[4] + " (esperado: 10)");
        System.out.println("Ferramentas: " + info[5] + " (esperado: No tools)");
    }

    @Test
    public void testAssemblyRestriction() {
        GameManager gm = new GameManager();
        String[][] playerInfo = {
                {"1", "Alice", "Assembly", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 15, null);

        System.out.println("First lang: '" + gm.getProgrammerInfo(1)[2].split(";")[0].trim() + "'");

        boolean move3 = gm.moveCurrentPlayer(3);
        System.out.println("Assembly move 3: " + move3 + " (esperado: false)");
    }


    @Test
    public void testInfiniteLoopWithTool() {
        GameManager gm = new GameManager();
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "5", "4"},  // Ferramenta Ajuda Do Professor (ID 5) na posição 4
                {"0", "8", "8"}   // Abismo Ciclo Infinito (ID 8) na posição 8
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        System.out.println("=== Turno 1: Jogador 1 apanha ferramenta ===");
        boolean moved1 = gm.moveCurrentPlayer(3);  // pos 1 -> 4
        System.out.println("move: " + moved1);
        String react1 = gm.reactToAbyssOrTool();
        System.out.println("react: " + react1);
        System.out.println("Ferramentas: " + gm.getProgrammerInfo(1)[5]);

        System.out.println("\n=== Turno 2: Jogador 2 move ===");
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        System.out.println("\n=== Turno 3: Jogador 1 vai para Ciclo Infinito ===");
        boolean moved2 = gm.moveCurrentPlayer(4);  // pos 4 -> 8
        System.out.println("move: " + moved2 + " (esperado: true)");
        String react2 = gm.reactToAbyssOrTool();
        System.out.println("react: " + react2 + " (esperado: anulado)");
        System.out.println("Estado: " + gm.getProgrammerInfo(1)[6] + " (esperado: Em Jogo)");
        System.out.println("Posição: " + gm.getProgrammerInfo(1)[4] + " (esperado: 8)");
    }


    @Test
    public void testCicloInfinito() {
        GameManager gm = new GameManager();
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "8", "5"}  // Abismo Ciclo Infinito (ID 8) na posição 5
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        System.out.println("=== Turno 1: Jogador 1 move 4 casas (vai para posição 5 - Ciclo Infinito) ===");
        boolean moved1 = gm.moveCurrentPlayer(4);
        System.out.println("moveCurrentPlayer returned: " + moved1 + " (esperado: true)");

        String react1 = gm.reactToAbyssOrTool();
        System.out.println("reactToAbyssOrTool returned: " + react1 + " (esperado: Ciclo Infinito!)");
        System.out.println("Player 1 state: " + gm.getProgrammerInfo(1)[6] + " (esperado: Preso)");

        System.out.println("\n=== Turno 2: Jogador 2 move ===");
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        System.out.println("\n=== Turno 3: Jogador 1 (preso) tenta mover ===");
        boolean moved2 = gm.moveCurrentPlayer(3);
        System.out.println("moveCurrentPlayer returned: " + moved2 + " (esperado: false)");

        String react2 = gm.reactToAbyssOrTool();
        System.out.println("reactToAbyssOrTool returned: " + react2 + " (esperado: null)");

        System.out.println("\nJogador atual: " + gm.getCurrentPlayerID() + " (esperado: 2)");
    }

    @Test
    public void testToolCancelsAbyss() {
        GameManager gm = new GameManager();
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "1", "5"},  // Tool Prog. Funcional (ID 1) at position 5
                {"0", "1", "10"}  // Abyss Erro Lógica (ID 1) at position 10
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        // Turno 1: Jogador 1 apanha ferramenta
        gm.moveCurrentPlayer(4);  // pos 1 -> 5
        String msg1 = gm.reactToAbyssOrTool();
        System.out.println("Turno 1 - React: " + msg1);
        System.out.println("Turno 1 - Player 1: " + gm.getProgrammerInfoAsStr(1));

        // Turno 2: Jogador 2 move
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        // Turno 3: Jogador 1 vai para abismo
        boolean moved = gm.moveCurrentPlayer(5);  // pos 5 -> 10
        System.out.println("\nTurno 3 - moveCurrentPlayer returned: " + moved);

        String msg2 = gm.reactToAbyssOrTool();
        System.out.println("Turno 3 - React: " + msg2);
        System.out.println("Turno 3 - Player 1: " + gm.getProgrammerInfoAsStr(1));

        String[] info = gm.getProgrammerInfo(1);
        int pos = Integer.parseInt(info[4]);
        System.out.println("\nPosição final: " + pos);
        System.out.println("Esperado: 10");
        System.out.println("Ferramenta anulou: " + (msg2 != null && msg2.contains("anulado")));
    }

    @Test
    public void testToolIsAdded() {
        GameManager gm = new GameManager();
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "1", "5"}  // Tool ID 1 at position 5
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);  // Move to position 5
        String msg = gm.reactToAbyssOrTool();

        String[] info = gm.getProgrammerInfo(1);
        System.out.println("Ferramentas: " + info[5]);
        System.out.println("React msg: " + msg);

        assertNotEquals("No tools", info[5]);
    }



    @Test
    public void createInitialBoardValid(){
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"1","Filipe","Java;python", "Purple"},
                {"2","Alegria","C++,JavaScript", "Green"},
        };

        int boardSize = 10;
        // terceiro parÃ¢metro: AbyssesAndTools -> null porque nÃ£o estamos a testar abismos
        boolean resultado = gameManager.createInitialBoard(playerInfo, boardSize, null);

        assertTrue(resultado,"createInitialBoardValid deve retornar true");

    }

    @Test
    public void testCreateInitialBoardInvalidoUmJogador() {
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"}
        };
        int boardSize = 10;

        boolean resultado = gameManager.createInitialBoard(playerInfo, boardSize, null);

        assertFalse(resultado, "createInitialBoard deve retornar false com apenas 1 jogador");
    }

    @Test
    public void testCreateInitialBoardInvalidColor(){
        GameManager gameManager = new GameManager();
        String [][] playerInfo = {
                {"1","Ana","Java","Red"}
        };
        int boardSize = 10;

        boolean resultado = gameManager.createInitialBoard(playerInfo, boardSize, null);

        assertFalse(resultado,"testCreateInitialBoardInvalidColor, deve retornar false pois cor esta indisponivel");
    }

    @Test
    public void testMoveCurrentPlayerMovimentoValido() {
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"1", "Hugo", "Haskell", "Purple"},
                {"2", "Iris", "Kotlin", "Green"}
        };
        gameManager.createInitialBoard(playerInfo, 10, null);

        boolean resultado = gameManager.moveCurrentPlayer(3);

        assertTrue(resultado, "moveCurrentPlayer deve retornar true para movimento vÃ¡lido");

        String[] info = gameManager.getProgrammerInfo(1);
        // assumindo que getInfoAsArray() devolve a posiÃ§Ã£o no Ã­ndice 4
        assertEquals("4", info[4], "Jogador 1 deve estar na posiÃ§Ã£o 4 apÃ³s mover 3 casas");
    }

    @Test
    public void testGetSlotInfoRetornaJogadoresNaPosicao() {
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"10", "Jack", "Perl", "Blue"},
                {"20", "Kate", "Swift", "Brown"}
        };
        gameManager.createInitialBoard(playerInfo, 15, null);

        String[] slotInfo = gameManager.getSlotInfo(1);

        assertNotNull(slotInfo, "getSlotInfo nÃ£o deve retornar null");
        assertEquals("10,20", slotInfo[0], "PosiÃ§Ã£o 1 deve conter jogadores 10,20");
    }

    @Test
    public void testGetCurrentPlayerIDRetornaPrimeiroJogador() {
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"5", "Eva", "JavaScript", "Green"},
                {"3", "Frank", "Go", "Purple"},
                {"7", "Grace", "Rust", "Brown"}
        };
        gameManager.createInitialBoard(playerInfo, 12, null);

        int currentPlayerId = gameManager.getCurrentPlayerID();

        assertEquals(3, currentPlayerId, "getCurrentPlayerID deve retornar 3 (menor ID)");
    }

    @Test
    public void testBSOD_ThreePlayersOneDefeated() {
        GameManager gm = new GameManager();

        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"},
                {"3", "Charlie", "C++", "Brown"}
        };

        String[][] abyssesAndTools = {
                {"0", "7", "5"}  // BSOD na posiÃ§Ã£o 5
        };

        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        System.out.println("=== ESTADO INICIAL ===");
        for (int i = 1; i <= 3; i++) {
            System.out.println(gm.getProgrammerInfoAsStr(i));
        }

        // Jogador 1 move 4 casas â†’ vai para posiÃ§Ã£o 5 (BSOD)
        System.out.println("\n=== JOGADOR 1 MOVE 4 CASAS ===");
        boolean moved = gm.moveCurrentPlayer(4);
        System.out.println("Move result: " + moved);

        String msg = gm.reactToAbyssOrTool();
        System.out.println("React message: " + msg);

        System.out.println("\n=== ESTADO APÃ“S REACT ===");
        for (int i = 1; i <= 3; i++) {
            String info = gm.getProgrammerInfoAsStr(i);
            if (info != null) {
                System.out.println(info);
            }
        }

        // Contar jogadores vivos
        int alive = 0;
        for (int i = 1; i <= 3; i++) {
            String[] info = gm.getProgrammerInfo(i);
            if (info != null && !"Derrotado".equals(info[6])) {
                alive++;
            }
        }

        System.out.println("\n=== CONTAGEM ===");
        System.out.println("Jogadores vivos: " + alive);
        System.out.println("Esperado: 2");

        assertEquals(2, alive, "Deveria haver 2 jogadores vivos apÃ³s um BSOD");
    }


    @Test
    public void test_LogicErrorVsTool() {
        GameManager gm = new GameManager();

        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };

        String[][] abyssesAndTools = {
                {"1", "1", "5"},  // Ferramenta Prog. Funcional na posiÃ§Ã£o 5
                {"0", "1", "10"}  // Abismo Erro LÃ³gica na posiÃ§Ã£o 10
        };

        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        System.out.println("=== TURNO 1: Jogador 1 move 4 (vai para pos 5 - ferramenta) ===");
        boolean moved1 = gm.moveCurrentPlayer(4);
        System.out.println("Moved: " + moved1);

        String msg1 = gm.reactToAbyssOrTool();
        System.out.println("React: " + msg1);
        System.out.println("Player 1: " + gm.getProgrammerInfoAsStr(1));

        System.out.println("\n=== TURNO 2: Jogador 2 move 2 ===");
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        System.out.println("\n=== TURNO 3: Jogador 1 move 5 (vai para pos 10 - abismo) ===");
        boolean moved2 = gm.moveCurrentPlayer(5);
        System.out.println("Moved: " + moved2);

        String msg2 = gm.reactToAbyssOrTool();
        System.out.println("React: " + msg2);
        System.out.println("Player 1: " + gm.getProgrammerInfoAsStr(1));

        String[] info = gm.getProgrammerInfo(1);
        int pos = Integer.parseInt(info[4]);

        System.out.println("\n=== RESULTADO ===");
        System.out.println("PosiÃ§Ã£o esperada: 10");
        System.out.println("PosiÃ§Ã£o obtida: " + pos);
        System.out.println("Ferramenta anulou? " + msg2.contains("anulado"));

        assertEquals(10, pos, "Jogador deveria estar na posiÃ§Ã£o 10 (ferramenta anulou)");
    }

    @Test
    public void test_ExceptionVsTool() {
        GameManager gm = new GameManager();

        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };

        String[][] abyssesAndTools = {
                {"1", "3", "4"},  // Ferramenta Trat. ExcepÃ§Ãµes na posiÃ§Ã£o 4
                {"0", "2", "8"}   // Abismo Exception na posiÃ§Ã£o 8
        };

        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        System.out.println("=== TURNO 1: Jogador 1 apanha ferramenta ===");
        gm.moveCurrentPlayer(3);
        String msg1 = gm.reactToAbyssOrTool();
        System.out.println("React: " + msg1);
        System.out.println("Player 1: " + gm.getProgrammerInfoAsStr(1));

        System.out.println("\n=== TURNO 2: Jogador 2 ===");
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        System.out.println("\n=== TURNO 3: Jogador 1 vai para abismo ===");
        gm.moveCurrentPlayer(4);
        String msg2 = gm.reactToAbyssOrTool();
        System.out.println("React: " + msg2);
        System.out.println("Player 1: " + gm.getProgrammerInfoAsStr(1));

        String[] info = gm.getProgrammerInfo(1);
        int pos = Integer.parseInt(info[4]);

        System.out.println("\n=== RESULTADO ===");
        System.out.println("PosiÃ§Ã£o esperada: 8");
        System.out.println("PosiÃ§Ã£o obtida: " + pos);

        assertEquals(8, pos, "Jogador deveria estar na posiÃ§Ã£o 8 (ferramenta anulou)");
    }



}