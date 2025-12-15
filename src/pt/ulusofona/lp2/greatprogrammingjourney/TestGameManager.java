package pt.ulusofona.lp2.greatprogrammingjourney;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestGameManager {

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