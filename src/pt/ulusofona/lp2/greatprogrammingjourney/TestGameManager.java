package pt.ulusofona.lp2.greatprogrammingjourney;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TestGameManager {

    private GameManager gm;

    @BeforeEach
    public void setUp() {
        gm = new GameManager();
    }

    // ==================== TESTES createInitialBoard ====================

    @Test
    public void testCreateInitialBoardValid() {
        String[][] playerInfo = {
                {"1", "Alice", "Java;Python", "Purple"},
                {"2", "Bob", "C++", "Green"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertTrue(result, "Deve criar tabuleiro válido com 2 jogadores");
    }

    @Test
    public void testCreateInitialBoardValidWithAbyssesAndTools() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "0", "3"},  // Abismo Sintaxe pos 3
                {"1", "1", "5"}   // Ferramenta Prog. Funcional pos 5
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, abyssesAndTools);
        assertTrue(result, "Deve criar tabuleiro com abismos e ferramentas");
    }

    @Test
    public void testCreateInitialBoardNullPlayerInfo() {
        boolean result = gm.createInitialBoard(null, 10, null);
        assertFalse(result, "Deve rejeitar playerInfo null");
    }

    @Test
    public void testCreateInitialBoardOnePlayer() {
        String[][] playerInfo = {{"1", "Alice", "Java", "Purple"}};
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertFalse(result, "Deve rejeitar menos de 2 jogadores");
    }

    @Test
    public void testCreateInitialBoardFivePlayers() {
        String[][] playerInfo = {
                {"1", "A", "Java", "Purple"},
                {"2", "B", "Python", "Green"},
                {"3", "C", "C++", "Brown"},
                {"4", "D", "Ruby", "Blue"},
                {"5", "E", "Go", "Purple"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 20, null);
        assertFalse(result, "Deve rejeitar mais de 4 jogadores");
    }

    @Test
    public void testCreateInitialBoardInvalidColor() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Red"},
                {"2", "Bob", "Python", "Green"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertFalse(result, "Deve rejeitar cor inválida");
    }

    @Test
    public void testCreateInitialBoardDuplicateColor() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Purple"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertFalse(result, "Deve rejeitar cores duplicadas");
    }

    @Test
    public void testCreateInitialBoardDuplicateId() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"1", "Bob", "Python", "Green"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertFalse(result, "Deve rejeitar IDs duplicados");
    }

    @Test
    public void testCreateInitialBoardInvalidId() {
        String[][] playerInfo = {
                {"0", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertFalse(result, "Deve rejeitar ID <= 0");
    }

    @Test
    public void testCreateInitialBoardSmallBoard() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 3, null);
        assertFalse(result, "Deve rejeitar tabuleiro pequeno demais");
    }

    @Test
    public void testCreateInitialBoardEmptyName() {
        String[][] playerInfo = {
                {"1", "", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertFalse(result, "Deve rejeitar nome vazio");
    }

    @Test
    public void testCreateInitialBoardNullRow() {
        String[][] playerInfo = {
                null,
                {"2", "Bob", "Python", "Green"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertFalse(result, "Deve rejeitar linha null");
    }

    @Test
    public void testCreateInitialBoardShortRow() {
        String[][] playerInfo = {
                {"1", "Alice", "Java"},
                {"2", "Bob", "Python", "Green"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertFalse(result, "Deve rejeitar linha com menos de 4 elementos");
    }

    @Test
    public void testCreateInitialBoardAbyssInvalidPosition() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "0", "1"}  // Posição 1 é inválida (jogadores começam lá)
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, abyssesAndTools);
        assertFalse(result, "Deve rejeitar abismo na posição 1");
    }

    @Test
    public void testCreateInitialBoardAbyssAtLastPosition() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "0", "10"}  // Posição 10 é a meta
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, abyssesAndTools);
        assertFalse(result, "Deve rejeitar abismo na última posição");
    }

    @Test
    public void testCreateInitialBoardDuplicatePositions() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "0", "5"},
                {"1", "1", "5"}  // Mesma posição
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, abyssesAndTools);
        assertFalse(result, "Deve rejeitar posições duplicadas");
    }

    @Test
    public void testCreateInitialBoardThreePlayers() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"},
                {"3", "Charlie", "C++", "Brown"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertTrue(result, "Deve aceitar 3 jogadores");
    }

    @Test
    public void testCreateInitialBoardFourPlayers() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"},
                {"3", "Charlie", "C++", "Brown"},
                {"4", "Diana", "Ruby", "Blue"}
        };
        boolean result = gm.createInitialBoard(playerInfo, 10, null);
        assertTrue(result, "Deve aceitar 4 jogadores");
    }

    // ==================== TESTES getProgrammerInfo ====================

    @Test
    public void testGetProgrammerInfoValid() {
        String[][] playerInfo = {
                {"1", "Alice", "Java;Python", "Purple"},
                {"2", "Bob", "C++", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String[] info = gm.getProgrammerInfo(1);
        assertNotNull(info);
        assertEquals("1", info[0]);
        assertEquals("Alice", info[1]);
        assertEquals("Java; Python", info[2]);
        assertEquals("Purple", info[3]);
        assertEquals("1", info[4]);
        assertEquals("No tools", info[5]);
        assertEquals("Em Jogo", info[6]);
    }

    @Test
    public void testGetProgrammerInfoInvalidId() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String[] info = gm.getProgrammerInfo(99);
        assertNull(info, "Deve retornar null para ID inexistente");
    }

    @Test
    public void testGetProgrammerInfoAsStr() {
        String[][] playerInfo = {
                {"1", "Alice", "Java;Python", "Purple"},
                {"2", "Bob", "C++", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String info = gm.getProgrammerInfoAsStr(1);
        assertNotNull(info);
        assertTrue(info.contains("Alice"));
        assertTrue(info.contains("1"));
    }

    @Test
    public void testGetProgrammerInfoAsStrInvalidId() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String info = gm.getProgrammerInfoAsStr(99);
        assertNull(info);
    }

    // ==================== TESTES getProgrammersInfo ====================

    @Test
    public void testGetProgrammersInfo() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String info = gm.getProgrammersInfo();
        assertTrue(info.contains("Alice"));
        assertTrue(info.contains("Bob"));
        assertTrue(info.contains("No tools"));
    }

    @Test
    public void testGetProgrammersInfoExcludesDefeated() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"},
                {"3", "Charlie", "C++", "Brown"}
        };
        String[][] abyssesAndTools = {
                {"0", "7", "5"}  // BSOD na posição 5
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        // Jogador 1 cai no BSOD
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        String info = gm.getProgrammersInfo();
        assertFalse(info.contains("Alice"), "Jogador derrotado não deve aparecer");
        assertTrue(info.contains("Bob"));
        assertTrue(info.contains("Charlie"));
    }

    // ==================== TESTES getSlotInfo ====================

    @Test
    public void testGetSlotInfoWithPlayers() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String[] info = gm.getSlotInfo(1);
        assertNotNull(info);
        assertEquals("1,2", info[0]);
    }

    @Test
    public void testGetSlotInfoWithAbyss() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "0", "5"}  // Erro de Sintaxe
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        String[] info = gm.getSlotInfo(5);
        assertNotNull(info);
        assertEquals("Erro de sintaxe", info[1]);
        assertEquals("A:0", info[2]);
    }

    @Test
    public void testGetSlotInfoWithTool() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "1", "5"}  // Programação Funcional
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        String[] info = gm.getSlotInfo(5);
        assertNotNull(info);
        assertTrue(info[2].startsWith("T:"));
    }

    @Test
    public void testGetSlotInfoEmptySlot() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String[] info = gm.getSlotInfo(5);
        assertNotNull(info);
        assertEquals("", info[0]);
        assertEquals("", info[1]);
        assertEquals("", info[2]);
    }

    @Test
    public void testGetSlotInfoInvalidPosition() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertNull(gm.getSlotInfo(0));
        assertNull(gm.getSlotInfo(11));
    }

    // ==================== TESTES getCurrentPlayer ====================

    @Test
    public void testGetCurrentPlayerID() {
        String[][] playerInfo = {
                {"5", "Alice", "Java", "Purple"},
                {"3", "Bob", "Python", "Green"},
                {"7", "Charlie", "C++", "Brown"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertEquals(3, gm.getCurrentPlayerID(), "Deve começar pelo menor ID");
    }

    @Test
    public void testGetCurrentPlayerName() {
        String[][] playerInfo = {
                {"5", "Alice", "Java", "Purple"},
                {"3", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertEquals("Bob", gm.getCurrentPlayerName());
    }

    @Test
    public void testGetCurrentPlayerInfo() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String[] info = gm.getCurrentPlayerInfo();
        assertNotNull(info);
        assertEquals("1", info[0]);
        assertEquals("Alice", info[1]);
    }

    // ==================== TESTES moveCurrentPlayer ====================

    @Test
    public void testMoveCurrentPlayerValid() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        boolean result = gm.moveCurrentPlayer(3);
        assertTrue(result);

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("4", info[4]);
    }

    @Test
    public void testMoveCurrentPlayerInvalidDice() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertFalse(gm.moveCurrentPlayer(0));
        assertFalse(gm.moveCurrentPlayer(7));
        assertFalse(gm.moveCurrentPlayer(-1));
    }

    @Test
    public void testMoveCurrentPlayerBounceBack() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        // Move para posição 7
        gm.moveCurrentPlayer(6);
        gm.reactToAbyssOrTool();

        // Jogador 2
        gm.moveCurrentPlayer(1);
        gm.reactToAbyssOrTool();

        // Jogador 1 na posição 7, move 5 → 12 → bounce → 8
        gm.moveCurrentPlayer(5);
        gm.reactToAbyssOrTool();

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("8", info[4]);
    }

    @Test
    public void testMoveCurrentPlayerAssemblyRestriction() {
        String[][] playerInfo = {
                {"1", "Alice", "Assembly", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        // Assembly só pode mover até 2 casas
        assertFalse(gm.moveCurrentPlayer(3));
        assertTrue(gm.moveCurrentPlayer(2));
    }

    @Test
    public void testMoveCurrentPlayerCRestriction() {
        String[][] playerInfo = {
                {"1", "Alice", "C", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        // C só pode mover até 3 casas
        assertFalse(gm.moveCurrentPlayer(4));
        assertTrue(gm.moveCurrentPlayer(3));
    }

    @Test
    public void testMoveCurrentPlayerCPlusPlusNoRestriction() {
        String[][] playerInfo = {
                {"1", "Alice", "C++", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        // C++ não tem restrição (não é "C")
        assertTrue(gm.moveCurrentPlayer(4));
    }

    @Test
    public void testMoveCurrentPlayerTrapped() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "8", "5"}  // Ciclo Infinito
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        // Jogador 1 cai no Ciclo Infinito
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        // Jogador 2 move
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        // Jogador 1 preso não pode mover
        assertFalse(gm.moveCurrentPlayer(2));
    }

    // ==================== TESTES reactToAbyssOrTool ====================

    @Test
    public void testReactToToolCollection() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "1", "5"}  // Programação Funcional
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("Recolheu ferramenta"));

        String[] info = gm.getProgrammerInfo(1);
        assertNotEquals("No tools", info[5]);
    }

    @Test
    public void testReactToAbyssSyntaxError() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "0", "5"}  // Erro de Sintaxe - recua 1
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertEquals("Erro de sintaxe!", msg);

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("4", info[4]);  // Recuou de 5 para 4
    }

    @Test
    public void testReactToAbyssLogicError() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "1", "5"}  // Erro de Lógica - recua dado/2
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);  // dado=4, recua 2
        String msg = gm.reactToAbyssOrTool();

        assertEquals("Erro de Lógica!", msg);

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("3", info[4]);  // 5 - 2 = 3
    }

    @Test
    public void testReactToAbyssException() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "2", "5"}  // Exception - recua 2
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertEquals("Exception!", msg);

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("3", info[4]);  // 5 - 2 = 3
    }

    @Test
    public void testReactToAbyssFileNotFoundException() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "3", "5"}  // FileNotFoundException - recua 3
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertEquals("File Not Found Exception!", msg);

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("2", info[4]);  // 5 - 3 = 2
    }

    @Test
    public void testReactToAbyssCrash() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "4", "5"}  // Crash - volta à posição 1
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("Crash"));

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("1", info[4]);
    }

    @Test
    public void testReactToAbyssDuplicatedCode() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "5", "5"}  // Código Duplicado - volta à posição anterior
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("Duplicado"));

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("1", info[4]);  // Volta para posição anterior
    }

    @Test
    public void testReactToAbyssBSOD() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"},
                {"3", "Charlie", "C++", "Brown"}
        };
        String[][] abyssesAndTools = {
                {"0", "7", "5"}  // Blue Screen of Death - elimina jogador
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("Blue Screen"));

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("Derrotado", info[6]);
    }

    @Test
    public void testReactToAbyssInfiniteLoop() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "8", "5"}  // Ciclo Infinito - fica preso
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertEquals("Ciclo Infinito!", msg);

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("Preso", info[6]);
    }

    @Test
    public void testReactToAbyssInfiniteLoopTrappedPlayerMessage() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "8", "5"}
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        // Jogador 1 fica preso
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        // Jogador 2 move
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        // Jogador 1 preso tenta mover
        gm.moveCurrentPlayer(2);
        String msg = gm.reactToAbyssOrTool();

        assertEquals("Ciclo Infinito!", msg);
    }

    @Test
    public void testReactToAbyssInfiniteLoopLiberation() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "8", "5"}
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        // Jogador 1 fica preso
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        // Jogador 2 cai na mesma casa
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        // Jogador 1 deve estar libertado
        assertEquals("Em Jogo", gm.getProgrammerInfo(1)[6]);
        assertEquals("Preso", gm.getProgrammerInfo(2)[6]);
    }

    @Test
    public void testReactToAbyssSegmentationFault() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "9", "5"}  // Segmentation Fault
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        // Jogador 1 vai para posição 5
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        // Jogador 2 vai para posição 5 (mesma casa)
        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertEquals("Segmentation Fault!", msg);

        // Ambos recuaram 3 casas (5-3=2)
        assertEquals("2", gm.getProgrammerInfo(1)[4]);
        assertEquals("2", gm.getProgrammerInfo(2)[4]);
    }

    @Test
    public void testReactToAbyssSecondaryEffects() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "6", "8"}  // Efeitos Secundários
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        // Movimento 1: 1->4
        gm.moveCurrentPlayer(3);
        gm.reactToAbyssOrTool();

        // Jogador 2
        gm.moveCurrentPlayer(1);
        gm.reactToAbyssOrTool();

        // Movimento 2: 4->8 (cai em Efeitos Secundários)
        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("Efeitos Secundários"));
        // Deve voltar para posição de 2 movimentos atrás (posição 1)
        assertEquals("1", gm.getProgrammerInfo(1)[4]);
    }

    // ==================== TESTES Tool cancela Abyss ====================

    @Test
    public void testToolCancelsAbyss() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "1", "4"},   // Programação Funcional
                {"0", "8", "8"}    // Ciclo Infinito
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        // Apanha ferramenta
        gm.moveCurrentPlayer(3);
        gm.reactToAbyssOrTool();

        // Jogador 2
        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        // Vai para Ciclo Infinito com ferramenta
        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("anulado"));
        assertEquals("Em Jogo", gm.getProgrammerInfo(1)[6]);
        assertEquals("No tools", gm.getProgrammerInfo(1)[5]);
    }

    @Test
    public void testIDECancelsSyntaxError() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "4", "4"},   // IDE
                {"0", "0", "8"}    // Erro de Sintaxe
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        gm.moveCurrentPlayer(3);
        gm.reactToAbyssOrTool();

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("anulado"));
        assertEquals("8", gm.getProgrammerInfo(1)[4]);
    }

    @Test
    public void testExceptionToolCancelsException() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "3", "4"},   // Tratamento de Exceções
                {"0", "2", "8"}    // Exception
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        gm.moveCurrentPlayer(3);
        gm.reactToAbyssOrTool();

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("anulado"));
        assertEquals("8", gm.getProgrammerInfo(1)[4]);
    }

    @Test
    public void testUnitTestCancelsLogicError() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "2", "4"},   // Testes Unitários
                {"0", "1", "8"}    // Erro de Lógica
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        gm.moveCurrentPlayer(3);
        gm.reactToAbyssOrTool();

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        gm.moveCurrentPlayer(4);
        String msg = gm.reactToAbyssOrTool();

        assertTrue(msg.contains("anulado"));
    }

    // ==================== TESTES getImagePng ====================

    @Test
    public void testGetImagePngLastPosition() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertEquals("glory.png", gm.getImagePng(10));
    }

    @Test
    public void testGetImagePngAbyss() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "0", "5"}
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        assertEquals("syntax.png", gm.getImagePng(5));
    }

    @Test
    public void testGetImagePngTool() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "1", "5"}
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        assertEquals("functional.png", gm.getImagePng(5));
    }

    @Test
    public void testGetImagePngEmptySlot() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertNull(gm.getImagePng(5));
    }

    @Test
    public void testGetImagePngInvalidPosition() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertNull(gm.getImagePng(0));
        assertNull(gm.getImagePng(11));
    }

    // ==================== TESTES gameIsOver ====================

    @Test
    public void testGameIsOverWinner() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 5, null);

        // Jogador 1 move 4 casas -> posição 5 (meta)
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        assertTrue(gm.gameIsOver());
    }

    @Test
    public void testGameIsOverLastAlive() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "7", "5"}  // BSOD
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        // Jogador 1 é eliminado
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        assertTrue(gm.gameIsOver());
    }

    @Test
    public void testGameNotOver() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        assertFalse(gm.gameIsOver());
    }

    // ==================== TESTES getGameResults ====================

    @Test
    public void testGetGameResultsNotOver() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        ArrayList<String> results = gm.getGameResults();
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetGameResultsWithWinner() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 5, null);

        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        ArrayList<String> results = gm.getGameResults();
        assertFalse(results.isEmpty());
        assertTrue(results.contains("Alice"));
    }

    // ==================== TESTES getLastDice ====================

    @Test
    public void testGetLastDiceValue() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        gm.moveCurrentPlayer(5);
        assertEquals(5, gm.getLastDiceValue());
    }

    @Test
    public void testGetLastDiceImageName() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        gm.moveCurrentPlayer(3);
        assertEquals("dice3.png", gm.getLastDiceImageName());
    }

    @Test
    public void testGetLastDiceImageNameInvalid() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertNull(gm.getLastDiceImageName());
    }

    // ==================== TESTES save/load ====================

    @Test
    public void testSaveGameNullFile() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertFalse(gm.saveGame(null));
    }

    @Test
    public void testSaveAndLoadGame(@TempDir File tempDir) throws Exception {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "0", "3"},
                {"1", "1", "5"}
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        File saveFile = new File(tempDir, "save.txt");
        assertTrue(gm.saveGame(saveFile));

        GameManager gm2 = new GameManager();
        gm2.loadGame(saveFile);

        assertEquals(gm.getCurrentPlayerID(), gm2.getCurrentPlayerID());
    }

    @Test
    public void testLoadGameFileNotFound() {
        File fakeFile = new File("nonexistent.txt");
        assertThrows(java.io.FileNotFoundException.class, () -> gm.loadGame(fakeFile));
    }

    @Test
    public void testLoadGameInvalidFile(@TempDir File tempDir) throws Exception {
        File invalidFile = new File(tempDir, "invalid.txt");
        try (FileWriter fw = new FileWriter(invalidFile)) {
            fw.write("invalid content");
        }

        assertThrows(InvalidFileException.class, () -> gm.loadGame(invalidFile));
    }

    // ==================== TESTES customizeBoard e getAuthorsPanel ====================

    @Test
    public void testCustomizeBoard() {
        assertNotNull(gm.customizeBoard());
    }

    @Test
    public void testGetAuthorsPanel() {
        assertNotNull(gm.getAuthorsPanel());
    }

    // ==================== TESTES Programmer ====================

    @Test
    public void testProgrammerCreation() {
        Programmer p = new Programmer(1, "Alice", "Java;Python", "Purple");
        assertEquals(1, p.getId());
        assertEquals("Alice", p.getName());
        assertEquals("Java;Python", p.getLanguages());
        assertEquals("Purple", p.getColor());
        assertEquals(1, p.getPosition());
        assertEquals("Em Jogo", p.getState());
    }

    @Test
    public void testProgrammerStates() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");

        assertTrue(p.isPlaying());
        assertFalse(p.isTrapped());
        assertFalse(p.isDefeated());

        p.setState("Preso");
        assertFalse(p.isPlaying());
        assertTrue(p.isTrapped());

        p.setState("Derrotado");
        assertTrue(p.isDefeated());
    }

    @Test
    public void testProgrammerFirstLanguage() {
        Programmer p1 = new Programmer(1, "Alice", "Java;Python", "Purple");
        assertEquals("Java", p1.getFirstLanguage());

        Programmer p2 = new Programmer(2, "Bob", "", "Green");
        assertNull(p2.getFirstLanguage());

        Programmer p3 = new Programmer(3, "Charlie", null, "Brown");
        assertNull(p3.getFirstLanguage());
    }

    @Test
    public void testProgrammerPositionHistory() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");

        p.recordMove(5);
        p.recordMove(8);

        assertEquals(1, p.getPositionTwoMovesAgo());
    }

    @Test
    public void testProgrammerTools() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");
        Tool tool = new FunctionalProgrammingTool(5);

        p.addTool(tool);
        assertTrue(p.hasToolOfType(1));
        assertEquals(1, p.getTools().size());

        p.removeTool(tool);
        assertFalse(p.hasToolOfType(1));
    }

    @Test
    public void testProgrammerFindToolToCancelAbyss() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");
        Tool tool = new FunctionalProgrammingTool(5);
        p.addTool(tool);

        Tool found = p.findToolToCancelAbyss(8);  // Ciclo Infinito
        assertNotNull(found);
        assertEquals(1, found.getId());

        Tool notFound = p.findToolToCancelAbyss(0);  // Erro de Sintaxe
        assertNull(notFound);
    }

    @Test
    public void testProgrammerToolsInfo() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");
        assertEquals("No tools", p.getToolsInfo());

        p.addTool(new FunctionalProgrammingTool(5));
        p.addTool(new IdeTool(3));

        String info = p.getToolsInfo();
        assertTrue(info.contains("IDE"));
        assertTrue(info.contains("Programação Funcional"));
    }

    @Test
    public void testProgrammerGetOrderedLanguages() {
        Programmer p = new Programmer(1, "Alice", "Python;Java;C++", "Purple");
        String ordered = p.getOrderedLanguages();
        assertEquals("C++; Java; Python", ordered);
    }

    @Test
    public void testProgrammerGetLanguagesInOriginalOrder() {
        Programmer p = new Programmer(1, "Alice", "Python;Java;C++", "Purple");
        String original = p.getLanguagesInOriginalOrder();
        assertEquals("Python; Java; C++", original);
    }

    @Test
    public void testProgrammerGetInfoAsArray() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");
        String[] info = p.getInfoAsArray();

        assertEquals(7, info.length);
        assertEquals("1", info[0]);
        assertEquals("Alice", info[1]);
    }

    @Test
    public void testProgrammerGetInfoAsString() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");
        String info = p.getInfoAsString();

        assertTrue(info.contains("1"));
        assertTrue(info.contains("Alice"));
        assertTrue(info.contains("Java"));
    }

    @Test
    public void testProgrammerCanPlay() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");
        assertTrue(p.canPlay());

        p.setState("Preso");
        assertFalse(p.canPlay());
    }

    @Test
    public void testProgrammerAddNullTool() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");
        p.addTool(null);
        assertEquals("No tools", p.getToolsInfo());
    }

    @Test
    public void testProgrammerRemoveNullTool() {
        Programmer p = new Programmer(1, "Alice", "Java", "Purple");
        p.removeTool(null);  // Should not throw
        assertEquals("No tools", p.getToolsInfo());
    }

    // ==================== TESTES Tool classes ====================

    @Test
    public void testInheritanceTool() {
        Tool t = new InheritanceTool(5);
        assertEquals(0, t.getId());
        assertEquals("Herança", t.getName());
        assertEquals(5, t.getPosition());
        assertEquals("inheritance.png", t.getImageName());
        assertFalse(t.canCancelAbyss(0));
    }

    @Test
    public void testFunctionalProgrammingTool() {
        Tool t = new FunctionalProgrammingTool(5);
        assertEquals(1, t.getId());
        assertTrue(t.canCancelAbyss(5));   // Código Duplicado
        assertTrue(t.canCancelAbyss(6));   // Efeitos Secundários
        assertTrue(t.canCancelAbyss(8));   // Ciclo Infinito
        assertFalse(t.canCancelAbyss(0));  // Erro Sintaxe
    }

    @Test
    public void testUnitTestTool() {
        Tool t = new UnitTestTool(5);
        assertEquals(2, t.getId());
        assertTrue(t.canCancelAbyss(1));   // Erro de Lógica
        assertFalse(t.canCancelAbyss(0));
    }

    @Test
    public void testExceptionTool() {
        Tool t = new ExceptionTool(5);
        assertEquals(3, t.getId());
        assertTrue(t.canCancelAbyss(2));   // Exception
        assertTrue(t.canCancelAbyss(3));   // FileNotFoundException
        assertFalse(t.canCancelAbyss(0));
    }

    @Test
    public void testIdeTool() {
        Tool t = new IdeTool(5);
        assertEquals(4, t.getId());
        assertTrue(t.canCancelAbyss(0));   // Erro de Sintaxe
        assertFalse(t.canCancelAbyss(1));
    }

    @Test
    public void testAjudaProfessorTool() {
        Tool t = new AjudaProfessorTool(5);
        assertEquals(5, t.getId());
        assertFalse(t.canCancelAbyss(0));
        assertFalse(t.canCancelAbyss(8));
    }

    @Test
    public void testToolGetInfoAsArray() {
        Tool t = new IdeTool(5);
        String[] info = t.getInfoAsArray();
        assertEquals("4", info[0]);
        assertEquals("IDE", info[1]);
        assertEquals("5", info[2]);
    }

    @Test
    public void testToolToString() {
        Tool t = new IdeTool(5);
        String str = t.toString();
        assertTrue(str.contains("IDE"));
        assertTrue(str.contains("4"));
    }

    // ==================== TESTES Abyss classes ====================

    @Test
    public void testSyntaxErrorAbyss() {
        Abyss a = new SyntaxErrorAbyss(5);
        assertEquals(0, a.getId());
        assertEquals("Erro de sintaxe", a.getName());
        assertEquals("syntax.png", a.getImageName());
        assertFalse(a.forcesRepeatTurn());
    }

    @Test
    public void testLogicErrorAbyss() {
        Abyss a = new LogicErrorAbyss(5);
        assertEquals(1, a.getId());
        assertEquals("logic.png", a.getImageName());
    }

    @Test
    public void testExceptionAbyss() {
        Abyss a = new ExceptionAbyss(5);
        assertEquals(2, a.getId());
        assertEquals("exception.png", a.getImageName());
    }

    @Test
    public void testFileNotFoundExceptionAbyss() {
        Abyss a = new FileNotFoundExceptionAbyss(5);
        assertEquals(3, a.getId());
    }

    @Test
    public void testCrashAbyss() {
        Abyss a = new CrashAbyss(5);
        assertEquals(4, a.getId());
        assertEquals("crash.png", a.getImageName());
    }

    @Test
    public void testDuplicatedCodeAbyss() {
        Abyss a = new DuplicatedCodeAbyss(5);
        assertEquals(5, a.getId());
        assertEquals("duplicated-code.png", a.getImageName());
    }

    @Test
    public void testSecondaryEffectsAbyss() {
        Abyss a = new SecondaryEffects(5);
        assertEquals(6, a.getId());
        assertEquals("secondary-effects.png", a.getImageName());
    }

    @Test
    public void testBlueScreenOfDeathAbyss() {
        Abyss a = new BlueScreenOfDeathAbyss(5);
        assertEquals(7, a.getId());
        assertEquals("bsod.png", a.getImageName());
        assertFalse(a.forcesRepeatTurn());
    }

    @Test
    public void testInfiniteLoopAbyss() {
        Abyss a = new InfiniteLoopAbyss(5);
        assertEquals(8, a.getId());
        assertEquals("infinite-loop.png", a.getImageName());
        assertFalse(a.forcesRepeatTurn());
    }

    @Test
    public void testSegmentationFaultAbyss() {
        Abyss a = new SegmentationFaultAbyss(5);
        assertEquals(9, a.getId());
        assertEquals("core-dumped.png", a.getImageName());
        assertFalse(a.forcesRepeatTurn());
    }

    @Test
    public void testAbyssGetInfoAsArray() {
        Abyss a = new SyntaxErrorAbyss(5);
        String[] info = a.getInfoAsArray();
        assertEquals("0", info[0]);
        assertEquals("Erro de sintaxe", info[1]);
        assertEquals("5", info[2]);
    }

    @Test
    public void testAbyssToString() {
        Abyss a = new SyntaxErrorAbyss(5);
        String str = a.toString();
        assertTrue(str.contains("Erro de sintaxe"));
    }

    @Test
    public void testAbyssApplyEffectNullProgrammer() {
        Abyss a = new SyntaxErrorAbyss(5);
        a.applyEffect(null, 3, 1);  // Should not throw
    }

    // ==================== TESTES InvalidFileException ====================

    @Test
    public void testInvalidFileExceptionNoArgs() {
        InvalidFileException ex = new InvalidFileException();
        assertNotNull(ex);
    }

    @Test
    public void testInvalidFileExceptionWithMessage() {
        InvalidFileException ex = new InvalidFileException("Test message");
        assertEquals("Test message", ex.getMessage());
    }

    // ==================== TESTES Edge Cases ====================

    @Test
    public void testTurnOrderAdvancement() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"},
                {"3", "Charlie", "C++", "Brown"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        assertEquals(1, gm.getCurrentPlayerID());

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();
        assertEquals(2, gm.getCurrentPlayerID());

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();
        assertEquals(3, gm.getCurrentPlayerID());

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();
        assertEquals(1, gm.getCurrentPlayerID());
    }

    @Test
    public void testMultipleToolsCollection() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"1", "1", "3"},
                {"1", "2", "7"}
        };
        gm.createInitialBoard(playerInfo, 15, abyssesAndTools);

        gm.moveCurrentPlayer(2);
        gm.reactToAbyssOrTool();

        gm.moveCurrentPlayer(1);
        gm.reactToAbyssOrTool();

        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        String[] info = gm.getProgrammerInfo(1);
        assertTrue(info[5].contains("Programação Funcional"));
        assertTrue(info[5].contains("Testes Unitários"));
    }

    @Test
    public void testAbyssMinimumPosition() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "4", "3"}  // Crash na posição 3
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        gm.moveCurrentPlayer(2);  // Vai para posição 3
        gm.reactToAbyssOrTool();

        String[] info = gm.getProgrammerInfo(1);
        assertEquals("1", info[4]);  // Crash volta para 1
    }



    @Test
    public void testReactWithoutMove() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 10, null);

        String msg = gm.reactToAbyssOrTool();
        assertNull(msg);
    }

    @Test
    public void testMoveAfterGameOver() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        gm.createInitialBoard(playerInfo, 5, null);

        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        assertTrue(gm.gameIsOver());
        assertFalse(gm.moveCurrentPlayer(1));
    }

    @Test
    public void testSegmentationFaultOnlyOnePlayer() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] abyssesAndTools = {
                {"0", "9", "5"}
        };
        gm.createInitialBoard(playerInfo, 10, abyssesAndTools);

        // Só um jogador na posição
        gm.moveCurrentPlayer(4);
        gm.reactToAbyssOrTool();

        // Jogador não deve recuar (só recua se houver 2+)
        assertEquals("5", gm.getProgrammerInfo(1)[4]);
    }

    @Test
    public void testStackOverflowAbyssNullProgrammerDoesNothing() {
        Abyss a = new StackOverflowAbyss(5);
        assertDoesNotThrow(() -> a.applyEffect(null, 3, 1));
        assertEquals("unknownPiece.png", a.getImageName());
        assertFalse(a.forcesRepeatTurn());
        assertEquals(10, a.getId());
        assertEquals("Stack Overflow", a.getName());
        assertEquals(5, a.getPosition());
    }

    @Test
    public void testStackOverflowAbyssNoToolsRetreatsOneAndNeverBelowOne() {
        Programmer p = new Programmer(1, "A", "Java", "Blue");
        p.setPosition(1);

        Abyss a = new StackOverflowAbyss(7);
        a.applyEffect(p, 6, 1);

        assertEquals(1, p.getPosition());
        assertTrue(p.getTools().isEmpty());
    }

    @Test
    public void testStackOverflowAbyssWithToolsClearsToolsAndRetreatsByToolCount() {
        Programmer p = new Programmer(1, "A", "Java", "Blue");
        p.setPosition(10);

        p.addTool(new InheritanceTool(2));
        p.addTool(new UnitTestTool(3));
        p.addTool(new IdeTool(4));
        assertEquals(3, p.getTools().size());

        Abyss a = new StackOverflowAbyss(8);
        a.applyEffect(p, 2, 9);

        assertEquals(7, p.getPosition()); // 10 - 3
        assertTrue(p.getTools().isEmpty());
    }

    // =========================
    // LLM (ID 20) - branches do GameManager
    // =========================

    @Test
    public void testLLMFirst3MovesWithAjudaProfessorCancelsAndConsumesTool() {
        GameManager gm = new GameManager();

        String[][] players = {
                {"1", "Alice", "Java", "Blue"},
                {"2", "Bob", "Java", "Green"}
        };

        String[][] cfg = {
                {"1", "5", "2"},   // Tool Ajuda do Professor (ID 5) na pos 2
                {"0", "20", "4"}   // Abyss LLM (ID 20) na pos 4
        };

        assertTrue(gm.createInitialBoard(players, 10, cfg));

        // P1: 1 -> 2 (apanha Ajuda do Professor)
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P2: jogada neutra
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P1: 2 -> 4 (cai no LLM nas primeiras 3 jogadas, com ferramenta)
        assertTrue(gm.moveCurrentPlayer(2));
        String msg = gm.reactToAbyssOrTool();

        assertNotNull(msg);
        assertTrue(msg.contains("LLM"));
        assertTrue(msg.contains("anulado"));
        assertTrue(msg.contains("Ajuda"));

        String[] infoP1 = gm.getProgrammerInfo(1);
        assertEquals("4", infoP1[4]);               // ficou no sítio
        assertEquals("No tools", infoP1[5]);        // ferramenta consumida
    }

    @Test
    public void testLLMFirst3MovesWithoutToolReturnsToPreviousPosition() {
        GameManager gm = new GameManager();

        String[][] players = {
                {"1", "Alice", "Java", "Blue"},
                {"2", "Bob", "Java", "Green"}
        };

        String[][] cfg = {
                {"0", "20", "4"}   // Abyss LLM (ID 20) na pos 4
        };

        assertTrue(gm.createInitialBoard(players, 10, cfg));

        // P1: 1 -> 4 (cai no LLM na 1ª jogada, sem ferramenta)
        assertTrue(gm.moveCurrentPlayer(3));
        String msg = gm.reactToAbyssOrTool();

        assertNotNull(msg);
        assertTrue(msg.contains("LLM"));

        String[] infoP1 = gm.getProgrammerInfo(1);
        assertEquals("1", infoP1[4]); // voltou para a posição anterior (1)
    }

    @Test
    public void testLLMOnOrAfter4thMoveForcesExtraAdvanceAndCollectsToolOnLanding() {
        GameManager gm = new GameManager();

        String[][] players = {
                {"1", "Alice", "Java", "Blue"},
                {"2", "Bob", "Java", "Green"}
        };

        String[][] cfg = {
                {"0", "20", "8"},  // LLM na pos 8
                {"1", "2", "11"}   // Tool Testes Unitários (ID 2) na pos 11 (para ser apanhada no avanço extra)
        };

        assertTrue(gm.createInitialBoard(players, 12, cfg));

        // P1 move 1: 1->3
        assertTrue(gm.moveCurrentPlayer(2));
        gm.reactToAbyssOrTool();

        // P2 neutro
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P1 move 2: 3->4
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P2 neutro
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P1 move 3: 4->5
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P2 neutro
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P1 move 4: 5->8 (dado=3), LLM (moveCount>=4) força avanço extra +3 => 11 e apanha tool
        assertTrue(gm.moveCurrentPlayer(3));
        String msg = gm.reactToAbyssOrTool();

        assertNotNull(msg);
        assertTrue(msg.contains("LLM"));

        String[] infoP1 = gm.getProgrammerInfo(1);
        assertEquals("11", infoP1[4]);                  // avanço extra aplicado
        assertTrue(infoP1[5].contains("Testes"));        // tool apanhada no forced move
    }

    // =========================
    // Segmentation Fault (ID 9) - branch do GameManager
    // =========================

    @Test
    public void testSegmentationFaultWithTwoPlayersRetreatsBothThreePositions() {
        GameManager gm = new GameManager();

        String[][] players = {
                {"1", "Alice", "Java", "Blue"},
                {"2", "Bob", "Java", "Green"}
        };

        String[][] cfg = {
                {"0", "9", "4"} // Segmentation Fault na pos 4
        };

        assertTrue(gm.createInitialBoard(players, 10, cfg));

        // P1: 1->4 (sozinho, nada além da mensagem)
        assertTrue(gm.moveCurrentPlayer(3));
        String msg1 = gm.reactToAbyssOrTool();
        assertNotNull(msg1);
        assertTrue(msg1.contains("Segmentation Fault"));

        assertEquals("4", gm.getProgrammerInfo(1)[4]);

        // P2: 1->4 (agora 2 jogadores na mesma casa, ambos recuam 3 => 1)
        assertTrue(gm.moveCurrentPlayer(3));
        String msg2 = gm.reactToAbyssOrTool();
        assertNotNull(msg2);
        assertTrue(msg2.contains("Segmentation Fault"));

        assertEquals("1", gm.getProgrammerInfo(1)[4]);
        assertEquals("1", gm.getProgrammerInfo(2)[4]);
    }

    // =========================
    // Empate: todos vivos presos
    // =========================

    @Test
    public void testGameEndsInTieWhenAllAlivePlayersAreTrapped() {
        GameManager gm = new GameManager();

        String[][] players = {
                {"1", "Alice", "Java", "Blue"},
                {"2", "Bob", "Java", "Green"}
        };

        String[][] cfg = {
                {"0", "8", "2"}, // Ciclo Infinito na pos 2
                {"0", "8", "3"}  // Ciclo Infinito na pos 3
        };

        assertTrue(gm.createInitialBoard(players, 10, cfg));

        // P1: 1->2 (Preso)
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P2: 1->3 (Preso)
        assertTrue(gm.moveCurrentPlayer(2));
        gm.reactToAbyssOrTool();

        assertTrue(gm.gameIsOver());
        assertNull(gm.getGameResults().isEmpty() ? null : null); // só para garantir que não crasha

        assertTrue(gm.getGameResults().contains("O jogo terminou empatado."));
    }

    @Test
    public void testCreateInitialBoardNullLanguages() {
        String[][] playerInfo = {
                {"1", "Alice", null, "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        assertFalse(gm.createInitialBoard(playerInfo, 10, null));
    }

    @Test
    public void testCreateInitialBoardNullColor() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", null},
                {"2", "Bob", "Python", "Green"}
        };
        assertFalse(gm.createInitialBoard(playerInfo, 10, null));
    }

    @Test
    public void testCreateInitialBoardConfigWithNullAndShortRowsStillValid() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] cfg = {
                null,
                {"1", "1"},          // linha curta -> ignorada
                {"1", "1", "4"}      // Tool Programação Funcional na pos 4
        };

        assertTrue(gm.createInitialBoard(playerInfo, 10, cfg));
        assertNotNull(gm.getSlotInfo(4));
        assertTrue(gm.getSlotInfo(4)[2].startsWith("T:"));
    }

    @Test
    public void testCreateInitialBoardInvalidTipoInConfig() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] cfg = {
                {"2", "0", "4"} // tipo inválido
        };
        assertFalse(gm.createInitialBoard(playerInfo, 10, cfg));
    }

    @Test
    public void testCreateInitialBoardInvalidToolIdInConfig() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] cfg = {
                {"1", "6", "4"} // toolId inválido (>5)
        };
        assertFalse(gm.createInitialBoard(playerInfo, 10, cfg));
    }

    @Test
    public void testCreateInitialBoardInvalidAbyssIdInConfig() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] cfg = {
                {"0", "11", "4"} // abyssId inválido (não é 20)
        };
        assertFalse(gm.createInitialBoard(playerInfo, 10, cfg));
    }

    @Test
    public void testCreateInitialBoardNonNumericConfig() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] cfg = {
                {"0", "x", "4"} // id não numérico
        };
        assertFalse(gm.createInitialBoard(playerInfo, 10, cfg));
    }

    @Test
    public void testToolAlreadyOwnedDoesNotIncreaseToolCount() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] cfg = {
                {"1", "1", "3"} // Programação Funcional na pos 3
        };
        assertTrue(gm.createInitialBoard(playerInfo, 5, cfg));

        // P1: 1->3 apanha tool
        assertTrue(gm.moveCurrentPlayer(2));
        String m1 = gm.reactToAbyssOrTool();
        assertNotNull(m1);

        int toolsAfterFirstPickup = gm.getProgrammerInfo(1)[5].split(";").length;

        // P2: move neutro (só para voltar ao P1)
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P1: 3->3 por bounce (3+4=7, board=5 => 5-2=3) cai outra vez no mesmo tool
        assertTrue(gm.moveCurrentPlayer(4));
        String m2 = gm.reactToAbyssOrTool();
        // pode ser null ou pode devolver mensagem, não interessa para este teste

        int toolsAfterSecondPickup = gm.getProgrammerInfo(1)[5].split(";").length;

        // O que interessa: NÃO aumentou o nº de ferramentas (ramo "já possui")
        assertEquals(toolsAfterFirstPickup, toolsAfterSecondPickup);
    }
    @Test
    public void testInfiniteLoopCancelledDoesNotLiberateTrappedPlayer() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] cfg = {
                {"0", "8", "4"}, // Ciclo Infinito na pos 4
                {"1", "1", "2"}  // Programação Funcional na pos 2 (cancela ID 8)
        };
        assertTrue(gm.createInitialBoard(playerInfo, 10, cfg));

        // P1: 1->4 fica preso
        assertTrue(gm.moveCurrentPlayer(3));
        assertEquals("Ciclo Infinito!", gm.reactToAbyssOrTool());
        assertEquals("Preso", gm.getProgrammerInfo(1)[6]);

        // P2: 1->2 apanha ferramenta
        assertTrue(gm.moveCurrentPlayer(1));
        String toolMsg = gm.reactToAbyssOrTool();
        assertNotNull(toolMsg);
        assertTrue(toolMsg.contains("Recolheu ferramenta"));

        // P1 (Preso): move devolve false, react devolve mensagem e avança turno
        assertFalse(gm.moveCurrentPlayer(1));
        assertEquals("Ciclo Infinito!", gm.reactToAbyssOrTool());

        // P2: 2->4 cai no Ciclo Infinito com tool -> anula e consome tool
        assertTrue(gm.moveCurrentPlayer(2));
        String abyssMsg = gm.reactToAbyssOrTool();
        assertNotNull(abyssMsg);
        assertTrue(abyssMsg.contains("anulado"));

        // P1 não deve ser libertado (porque anulado acontece antes da lógica de libertação)
        assertEquals("Preso", gm.getProgrammerInfo(1)[6]);

        // P2 continua em jogo e sem ferramentas
        assertEquals("Em Jogo", gm.getProgrammerInfo(2)[6]);
        assertEquals("No tools", gm.getProgrammerInfo(2)[5]);
    }

    @Test
    public void testGameResultsTieAllDefeatedShowsParticipantsAndCauses() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        String[][] cfg = {
                {"0", "7", "2"}, // BSOD na pos 2
                {"0", "7", "3"}  // BSOD na pos 3
        };
        assertTrue(gm.createInitialBoard(playerInfo, 6, cfg));

        // P1: 1->2 derrotado
        assertTrue(gm.moveCurrentPlayer(1));
        String m1 = gm.reactToAbyssOrTool();
        assertNotNull(m1);
        assertTrue(m1.contains("Blue Screen"));

        // P2: 1->3 derrotado
        assertTrue(gm.moveCurrentPlayer(2));
        String m2 = gm.reactToAbyssOrTool();
        assertNotNull(m2);
        assertTrue(m2.contains("Blue Screen"));

        assertTrue(gm.gameIsOver());

        ArrayList<String> results = gm.getGameResults();
        assertTrue(results.contains("O jogo terminou empatado."));
        assertTrue(results.contains("Participantes:"));
        assertTrue(results.toString().contains("Alice : 2 : Blue Screen of Death"));
        assertTrue(results.toString().contains("Bob : 3 : Blue Screen of Death"));
    }

    @Test
    public void testLoadGameWithoutLastMoveLinesIsValid(@TempDir File tempDir) throws Exception {
        File f = new File(tempDir, "minimal_save.txt");
        try (FileWriter fw = new FileWriter(f)) {
            fw.write("10\n");                // boardSize
            fw.write("2\n");                 // numProgrammers
            fw.write("1|Alice|Java|Blue|1|Em Jogo\n");
            fw.write("2|Bob|Python|Green|1|Em Jogo\n");
            fw.write("0\n");                 // numAbysses
            fw.write("0\n");                 // numTools
            fw.write("2\n");                 // orderSize
            fw.write("1\n");
            fw.write("2\n");
            fw.write("0\n");                 // turnCursor
            fw.write("false\n");             // gameOver
            fw.write("-1\n");                // winnerId
            fw.write("1\n");                 // turnCount
            // EOF aqui (sem lastDiceValue/lastPlayerId/etc)
        }

        GameManager gm2 = new GameManager();
        assertDoesNotThrow(() -> gm2.loadGame(f));
        assertNull(gm2.reactToAbyssOrTool()); // não há pendingReaction

        // jogo funciona
        assertEquals(1, gm2.getCurrentPlayerID());
        assertTrue(gm2.moveCurrentPlayer(1));
        gm2.reactToAbyssOrTool();
    }

    @Test
    public void testSaveGameToDirectoryReturnsFalse(@TempDir File tempDir) {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        assertTrue(gm.createInitialBoard(playerInfo, 10, null));

        File dir = new File(tempDir, "saveDir");
        assertTrue(dir.mkdirs());
        assertFalse(gm.saveGame(dir)); // FileWriter falha em diretório
    }

    @Test
    public void testMoveCurrentPlayerWithoutCreateInitialBoardReturnsFalse() {
        GameManager gm2 = new GameManager();
        assertFalse(gm2.moveCurrentPlayer(1));
        assertNull(gm2.reactToAbyssOrTool());
    }

    @Test
    public void testGetProgrammersInfoWhenNoBoardReturnsEmptyString() {
        GameManager gm2 = new GameManager();
        assertEquals("", gm2.getProgrammersInfo());
    }

    @Test
    public void testGetCurrentPlayerNameWhenTurnOrderHasMissingId(@TempDir File tempDir) throws Exception {
        File f = new File(tempDir, "weird_save.txt");
        try (FileWriter fw = new FileWriter(f)) {
            fw.write("10\n");
            fw.write("2\n");
            fw.write("1|Alice|Java|Blue|1|Em Jogo\n");
            fw.write("2|Bob|Python|Green|1|Em Jogo\n");
            fw.write("0\n");
            fw.write("0\n");
            fw.write("3\n");   // orderSize
            fw.write("99\n");  // id inexistente
            fw.write("1\n");
            fw.write("2\n");
            fw.write("0\n");
            fw.write("false\n");
            fw.write("-1\n");
            fw.write("1\n");
        }

        GameManager gm2 = new GameManager();
        gm2.loadGame(f);

        assertEquals(99, gm2.getCurrentPlayerID());
        assertEquals("", gm2.getCurrentPlayerName());
    }

    @Test
    public void testReactOnEmptySlotReturnsNull() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        assertTrue(gm.createInitialBoard(playerInfo, 10, null));

        // Move para uma casa sem tool/abyss
        assertTrue(gm.moveCurrentPlayer(1)); // 1->2
        String msg = gm.reactToAbyssOrTool();
        assertNull(msg);
    }

    @Test
    public void testMoveCurrentPlayerInvalidDoesNotAdvanceTurn() {
        String[][] playerInfo = {
                {"1", "Alice", "Assembly", "Purple"}, // Assembly não pode >2
                {"2", "Bob", "Python", "Green"}
        };
        assertTrue(gm.createInitialBoard(playerInfo, 10, null));

        assertEquals(1, gm.getCurrentPlayerID());

        assertFalse(gm.moveCurrentPlayer(3)); // inválido
        assertNull(gm.reactToAbyssOrTool());   // não há reação pendente

        // continua a ser o mesmo jogador
        assertEquals(1, gm.getCurrentPlayerID());
    }


    @Test
    public void testMoveValidAdvancesTurn() {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        assertTrue(gm.createInitialBoard(playerInfo, 10, null));

        assertEquals(1, gm.getCurrentPlayerID());
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();
        assertEquals(2, gm.getCurrentPlayerID());
    }


    @Test
    public void testGetSlotInfoPlayersSorting() {
        String[][] playerInfo = {
                {"10", "A", "Java", "Purple"},
                {"2", "B", "Python", "Green"},
                {"7", "C", "C++", "Brown"}
        };
        assertTrue(gm.createInitialBoard(playerInfo, 10, null));

        // todos começam na posição 1, deve vir ordenado
        String[] slot = gm.getSlotInfo(1);
        assertNotNull(slot);
        assertEquals("2,7,10", slot[0]);
    }

    @Test
    public void testSaveGameToDirectoryReturnsFalse_2(@TempDir File tempDir) {
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"},
                {"2", "Bob", "Python", "Green"}
        };
        assertTrue(gm.createInitialBoard(playerInfo, 10, null));

        File dir = new File(tempDir, "saveDir");
        assertTrue(dir.mkdirs());
        assertFalse(gm.saveGame(dir));
    }

}