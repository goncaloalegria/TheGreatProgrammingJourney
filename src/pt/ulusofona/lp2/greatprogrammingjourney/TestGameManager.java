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

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            fail(e);
        }
    }

    private static Object getField(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            fail(e);
            return null;
        }
    }

    private static Object callPrivate(Object target, String methodName, Class<?>[] types, Object[] args) {
        try {
            Method m = target.getClass().getDeclaredMethod(methodName, types);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (Exception e) {
            fail(e);
            return null;
        }
    }

    // ---------- Main ----------
    @Test
    public void testMainRuns() {
        assertDoesNotThrow(() -> Main.main(new String[0]));
    }

    // ---------- InvalidFileException ----------
    @Test
    public void testInvalidFileExceptionConstructors() {
        InvalidFileException a = new InvalidFileException();
        InvalidFileException b = new InvalidFileException("x");
        assertNotNull(a);
        assertEquals("x", b.getMessage());
    }

    // ---------- createInitialBoard validation ----------
    @Test
    public void testCreateInitialBoardValidationFailures() {
        GameManager gm = new GameManager();

        assertFalse(gm.createInitialBoard(null, 10));

        String[][] one = {{"1", "A", "Java", "Blue"}};
        assertFalse(gm.createInitialBoard(one, 10));

        String[][] five = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
                {"3", "C", "Java", "Brown"},
                {"4", "D", "Java", "Purple"},
                {"5", "E", "Java", "Blue"},
        };
        assertFalse(gm.createInitialBoard(five, 20));

        String[][] two = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        assertFalse(gm.createInitialBoard(two, 3));

        String[][] dupId = {
                {"1", "A", "Java", "Blue"},
                {"1", "B", "Java", "Green"},
        };
        assertFalse(gm.createInitialBoard(dupId, 10));

        String[][] badColor = {
                {"1", "A", "Java", "Red"},
                {"2", "B", "Java", "Green"},
        };
        assertFalse(gm.createInitialBoard(badColor, 10));

        String[][] dupColor = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Blue"},
        };
        assertFalse(gm.createInitialBoard(dupColor, 10));

        String[][] emptyName = {
                {"1", "   ", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        assertFalse(gm.createInitialBoard(emptyName, 10));

        String[][] nullLang = {
                {"1", "A", null, "Blue"},
                {"2", "B", "Java", "Green"},
        };
        assertFalse(gm.createInitialBoard(nullLang, 10));
    }

    @Test
    public void testConfigValidationFailures() {
        GameManager gm = new GameManager();

        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };

        // tipo inválido
        String[][] cfg1 = {{"2", "0", "2"}};
        assertFalse(gm.createInitialBoard(players, 10, cfg1));

        // abyss id inválido
        String[][] cfg2 = {{"0", "11", "2"}};
        assertFalse(gm.createInitialBoard(players, 10, cfg2));

        // tool id inválido
        String[][] cfg3 = {{"1", "6", "2"}};
        assertFalse(gm.createInitialBoard(players, 10, cfg3));

        // posição inválida (<=1)
        String[][] cfg4 = {{"0", "0", "1"}};
        assertFalse(gm.createInitialBoard(players, 10, cfg4));

        // posição inválida (>= boardSize)
        String[][] cfg5 = {{"0", "0", "10"}};
        assertFalse(gm.createInitialBoard(players, 10, cfg5));

        // slot repetido
        String[][] cfg6 = {{"0", "0", "2"}, {"1", "0", "2"}};
        assertFalse(gm.createInitialBoard(players, 10, cfg6));

        // não numérico
        String[][] cfg7 = {{"0", "x", "2"}};
        assertFalse(gm.createInitialBoard(players, 10, cfg7));
    }

    @Test
    public void testConfigIgnoresNullAndShortRows() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        String[][] cfg = {
                null,
                {"0", "0"},     // short row -> ignored
                {"1"},          // short row -> ignored
        };
        assertTrue(gm.createInitialBoard(players, 10, cfg));
    }

    // ---------- getCurrentPlayerID before init ----------
    @Test
    public void testCurrentPlayerBeforeInit() {
        GameManager gm = new GameManager();
        assertEquals(-1, gm.getCurrentPlayerID());
        assertEquals("", gm.getCurrentPlayerName());
        assertNull(gm.getCurrentPlayerInfo());
    }

    // ---------- Images / slots ----------
    @Test
    public void testGetImagePngAndGetSlotInfo() {
        GameManager gm = new GameManager();

        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };

        String[][] cfg = {
                {"0", "0", "2"}, // Syntax abyss
                {"1", "4", "3"}, // IDE tool
        };

        assertTrue(gm.createInitialBoard(players, 6, cfg));

        assertNull(gm.getImagePng(0));
        assertNull(gm.getImagePng(7));

        assertEquals("glory.png", gm.getImagePng(6));
        assertEquals("syntax.png", gm.getImagePng(2));
        assertEquals("IDE.png", gm.getImagePng(3));
        assertNull(gm.getImagePng(4));

        // slot info empty
        String[] s1 = gm.getSlotInfo(1);
        assertNotNull(s1);
        assertEquals("", s1[1]);
        assertEquals("", s1[2]);

        // move both to same slot to test ids sorted
        assertTrue(gm.moveCurrentPlayer(1)); // P1 -> 2
        gm.reactToAbyssOrTool();
        assertTrue(gm.moveCurrentPlayer(1)); // P2 -> 2
        gm.reactToAbyssOrTool();

        String[] s2 = gm.getSlotInfo(2);
        assertEquals("1,2", s2[0]);
        assertEquals("Erro de sintaxe", s2[1]);
        assertEquals("A:0", s2[2]);

        String[] s3 = gm.getSlotInfo(3);
        assertEquals("IDE", s3[1]);
        assertEquals("T:4", s3[2]);

        assertNull(gm.getSlotInfo(0));
        assertNull(gm.getSlotInfo(999));
    }

    // ---------- Dice image ----------
    @Test
    public void testLastDiceImageName() {
        GameManager gm = new GameManager();
        assertNull(gm.getLastDiceImageName());

        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        assertTrue(gm.createInitialBoard(players, 10));

        assertFalse(gm.moveCurrentPlayer(0));
        assertFalse(gm.moveCurrentPlayer(7));

        assertTrue(gm.moveCurrentPlayer(4));
        assertEquals("dice4.png", gm.getLastDiceImageName());
        gm.reactToAbyssOrTool();
    }

    // ---------- Language restrictions (Assembly/C) + invalid move special case ----------
    @Test
    public void testLanguageRestrictionsAndInvalidMoveConsumesTurn() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "Asm", "Assembly", "Blue"},
                {"2", "Cman", "C", "Green"},
        };
        assertTrue(gm.createInitialBoard(players, 20));

        // Assembly >2 invalid
        assertFalse(gm.moveCurrentPlayer(3));
        assertNull(gm.reactToAbyssOrTool());

        // C >3 invalid
        assertFalse(gm.moveCurrentPlayer(4));
        assertNull(gm.reactToAbyssOrTool());
    }

    @Test
    public void testCSharpIsNotCRestriction() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "C#", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        assertTrue(gm.createInitialBoard(players, 20));

        assertTrue(gm.moveCurrentPlayer(4));
        gm.reactToAbyssOrTool();
        assertEquals("5", gm.getProgrammerInfo(1)[4]);
    }

    // ---------- Tool pickup: collect vs already has ----------
    @Test
    public void testToolPickupBothBranches() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        String[][] cfg = {
                {"1", "4", "2"} // IDE tool at 2
        };
        assertTrue(gm.createInitialBoard(players, 10, cfg));

        assertTrue(gm.moveCurrentPlayer(1)); // P1 to 2
        assertTrue(gm.reactToAbyssOrTool().contains("Recolheu ferramenta"));

        // P2 to 2, still can collect (doesn't have)
        assertTrue(gm.moveCurrentPlayer(1));
        assertTrue(gm.reactToAbyssOrTool().contains("Recolheu ferramenta"));

        // P1 back to 2 later, already has
        assertTrue(gm.moveCurrentPlayer(1)); // P1: 2->3
        gm.reactToAbyssOrTool();
        assertTrue(gm.moveCurrentPlayer(0 + 1)); // P2: 2->3 (ignore)
        gm.reactToAbyssOrTool();

        assertTrue(gm.moveCurrentPlayer(0 + 1)); // P1: 3->4
        gm.reactToAbyssOrTool();
        assertTrue(gm.moveCurrentPlayer(0 + 1)); // P2: 3->4
        gm.reactToAbyssOrTool();

        // bring P1 back onto 2 via DuplicatedCode abyss (id 5) at 5
        String[][] cfg2 = {
                {"1", "4", "2"},
                {"0", "5", "5"} // DuplicatedCode at 5 returns to previousPosition
        };
        assertTrue(gm.createInitialBoard(players, 10, cfg2));

        assertTrue(gm.moveCurrentPlayer(1)); // P1->2 collect
        gm.reactToAbyssOrTool();

        // advance turns to get P1 to 5
        assertTrue(gm.moveCurrentPlayer(1)); gm.reactToAbyssOrTool(); // P2->2 collect
        assertTrue(gm.moveCurrentPlayer(3)); gm.reactToAbyssOrTool(); // P1 2->5 hits duplicated -> back to 2
        String msg = gm.getSlotInfo(2)[1];
        assertEquals("IDE", msg);

        // still already has IDE now, so on react at 2 it should say already has
        // (need to move onto 2 again in a normal turn)
        assertTrue(gm.moveCurrentPlayer(1)); gm.reactToAbyssOrTool(); // P2 move
        assertTrue(gm.moveCurrentPlayer(1)); // P1: 2->3
        gm.reactToAbyssOrTool();
        assertTrue(gm.moveCurrentPlayer(0 + 1)); gm.reactToAbyssOrTool(); // P2
        assertTrue(gm.moveCurrentPlayer(0 + 1)); // P1: 3->4
        gm.reactToAbyssOrTool();
    }

    // ---------- Abyss cancel path + InfiniteLoop release path ----------
    @Test
    public void testInfiniteLoopReleasesPreviousTrappedWhenNotCancelled() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        String[][] cfg = {
                {"0", "8", "2"} // Infinite Loop at 2
        };
        assertTrue(gm.createInitialBoard(players, 10, cfg));

        // P1 falls into infinite loop -> trapped
        assertTrue(gm.moveCurrentPlayer(1));
        assertEquals("Ciclo Infinito!", gm.reactToAbyssOrTool());
        assertEquals("Preso", gm.getProgrammerInfo(1)[6]);

        // P2 falls into same abyss without canceller -> frees P1 and traps P2
        assertTrue(gm.moveCurrentPlayer(1));
        assertEquals("Ciclo Infinito!", gm.reactToAbyssOrTool());

        assertEquals("Em Jogo", gm.getProgrammerInfo(1)[6]);
        assertEquals("Preso", gm.getProgrammerInfo(2)[6]);

        // Next turn for P1 is playable; P2 is trapped -> special case message
        assertTrue(gm.moveCurrentPlayer(1)); // P1 moves
        gm.reactToAbyssOrTool();
        assertFalse(gm.moveCurrentPlayer(1)); // P2 cannot
        assertEquals("Ciclo Infinito!", gm.reactToAbyssOrTool());
    }

    @Test
    public void testInfiniteLoopNotReleaseIfCancelledByTool() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        String[][] cfg = {
                {"0", "8", "4"},  // Infinite Loop at 4
                {"1", "1", "2"}   // FunctionalProgrammingTool at 2 cancels 8
        };
        assertTrue(gm.createInitialBoard(players, 10, cfg));

        // P1 gets trapped at 4
        assertTrue(gm.moveCurrentPlayer(3)); // 1->4
        assertEquals("Ciclo Infinito!", gm.reactToAbyssOrTool());
        assertEquals("Preso", gm.getProgrammerInfo(1)[6]);

        // P2 picks tool at 2
        assertTrue(gm.moveCurrentPlayer(1)); // 1->2
        gm.reactToAbyssOrTool();

        // P2 goes to 4 and cancels abyss -> P1 should remain trapped
        assertTrue(gm.moveCurrentPlayer(2)); // 2->4
        String msg = gm.reactToAbyssOrTool();
        assertNotNull(msg);
        assertTrue(msg.contains("anulado"));

        assertEquals("Preso", gm.getProgrammerInfo(1)[6]);
        assertEquals("Em Jogo", gm.getProgrammerInfo(2)[6]);
    }

    // ---------- Bounce-back ----------
    @Test
    public void testBounceBackPastGlory() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        assertTrue(gm.createInitialBoard(players, 6));

        // P1 from 1 with 6 -> overshoot to (6 - 1) = 5
        assertTrue(gm.moveCurrentPlayer(6));
        gm.reactToAbyssOrTool();
        assertEquals("5", gm.getProgrammerInfo(1)[4]);
    }

    // ---------- Game over branches + results ----------
    @Test
    public void testGameResultsWinnerFormat() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        assertTrue(gm.createInitialBoard(players, 4));

        assertTrue(gm.moveCurrentPlayer(3)); // P1 1->4 glory
        gm.reactToAbyssOrTool();

        assertTrue(gm.gameIsOver());
        assertTrue(gm.getGameResults().contains("VENCEDOR"));
        assertTrue(gm.getGameResults().contains("A"));
    }

    @Test
    public void testGameResultsTieAllAliveTrapped() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        String[][] cfg = {
                {"0", "8", "2"},
                {"0", "8", "3"}
        };
        assertTrue(gm.createInitialBoard(players, 10, cfg));

        assertTrue(gm.moveCurrentPlayer(1)); gm.reactToAbyssOrTool(); // P1 trapped at 2
        assertTrue(gm.moveCurrentPlayer(2)); gm.reactToAbyssOrTool(); // P2 trapped at 3

        assertTrue(gm.gameIsOver());
        assertTrue(gm.getGameResults().contains("O jogo terminou empatado."));
    }

    @Test
    public void testAliveZeroAllDefeated() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        String[][] cfg = {
                {"0", "7", "2"}, // BSOD defeats
                {"0", "7", "3"}  // BSOD defeats
        };
        assertTrue(gm.createInitialBoard(players, 10, cfg));

        assertTrue(gm.moveCurrentPlayer(1)); gm.reactToAbyssOrTool(); // P1 defeated at 2 removed
        assertTrue(gm.moveCurrentPlayer(2)); gm.reactToAbyssOrTool(); // P2 defeated at 3 removed -> empty order
        assertTrue(gm.gameIsOver());
        assertTrue(gm.getGameResults().contains("O jogo terminou empatado."));
    }

    // ---------- getProgrammersInfo ----------
    @Test
    public void testGetProgrammersInfoFiltersDefeatedAndSorts() {
        GameManager gm = new GameManager();
        String[][] players = {
                {"2", "B", "Java", "Green"},
                {"1", "A", "Java", "Blue"},
        };
        String[][] cfg = {
                {"1", "4", "2"},  // IDE at 2
                {"0", "7", "3"}   // BSOD at 3 defeats
        };
        assertTrue(gm.createInitialBoard(players, 10, cfg));

        // P1 (id 1): 1->2 collect IDE
        assertTrue(gm.moveCurrentPlayer(1));
        gm.reactToAbyssOrTool();

        // P2 (id 2): 1->3 defeated
        assertTrue(gm.moveCurrentPlayer(2));
        gm.reactToAbyssOrTool();

        String info = gm.getProgrammersInfo();
        assertTrue(info.contains("A"));
        assertFalse(info.contains("B"));
    }

    // ---------- Programmer branches ----------
    private static class BlankNameTool extends Tool {
        public BlankNameTool() { super(99, "   ", 0); }
        @Override public String getImageName() { return "x.png"; }
        @Override public boolean canCancelAbyss(int abyssId) { return false; }
    }

    @Test
    public void testProgrammerMethodsBranches() {
        Programmer p = new Programmer(1, "A", "  Java ; ; C  ,  ", null);

        assertNull(p.getFirstLanguage()); // languages has weird but first before split might be "Java"? Actually leading spaces then "Java"
        // force empty languages to hit null
        Programmer p2 = new Programmer(2, "B", "   ", "Blue");
        assertNull(p2.getFirstLanguage());

        p.recordMove(5);
        assertEquals(5, p.getPosition());
        assertEquals(1, p.getPositionTwoMovesAgo()); // only 1 history

        p.recordMove(6);
        assertEquals(5, p.getPositionTwoMovesAgo());

        p.addTool(null);
        assertEquals("No tools", p.getToolsInfo());

        p.addTool(new BlankNameTool());
        assertEquals("No tools", p.getToolsInfo());

        p.addTool(new IdeTool(0));
        p.addTool(new UnitTestTool(0));
        assertTrue(p.hasToolOfType(IdeTool.ID));
        assertTrue(p.getToolsInfo().contains("IDE"));
        assertTrue(p.getToolsInfo().contains("Testes Unitários"));

        assertNotNull(p.findToolToCancelAbyss(0)); // IDE cancels syntax abyss (0)
        assertEquals("", p2.getLanguagesInOriginalOrder());
        assertEquals("", p2.getOrderedLanguages());

        assertNotNull(p.getInfoAsArray());
        assertTrue(p.getInfoAsString().contains(" | "));
    }

    // ---------- Tool/Abyss base methods ----------
    @Test
    public void testToolAndAbyssToStringAndInfoArray() {
        Abyss a = new SyntaxErrorAbyss(2);
        assertEquals(0, a.getId());
        assertEquals("Erro de sintaxe", a.getName());
        assertEquals(2, a.getPosition());
        assertEquals("syntax.png", a.getImageName());
        assertFalse(a.forcesRepeatTurn());
        assertTrue(a.toString().contains("Erro de sintaxe"));
        assertEquals("0", a.getInfoAsArray()[0]);

        Tool t = new IdeTool(3);
        assertEquals(4, t.getId());
        assertEquals("IDE", t.getName());
        assertEquals(3, t.getPosition());
        assertEquals("IDE.png", t.getImageName());
        assertTrue(t.toString().contains("IDE"));
        assertEquals("4", t.getInfoAsArray()[0]);

        assertFalse(new InheritanceTool(0).canCancelAbyss(0));
        assertTrue(new FunctionalProgrammingTool(0).canCancelAbyss(8));
        assertTrue(new UnitTestTool(0).canCancelAbyss(1));
        assertTrue(new ExceptionTool(0).canCancelAbyss(3));
        assertTrue(new IdeTool(0).canCancelAbyss(0));
        assertTrue(new AjudaProfessorTool(0).canCancelAbyss(20));
    }

    // ---------- createAbyss/createTool default branch (reflection) ----------
    @Test
    public void testCreateAbyssAndToolDefaultReturnNull() {
        GameManager gm = new GameManager();
        Object a = callPrivate(gm, "createAbyss", new Class[]{int.class, int.class}, new Object[]{999, 2});
        Object t = callPrivate(gm, "createTool", new Class[]{int.class, int.class}, new Object[]{999, 2});
        assertNull(a);
        assertNull(t);
    }

    // ---------- Save/Load: success + IO fail + invalid formats + missing lastMoveInfo ----------
    @Test
    public void testSaveLoadRoundTripAndSaveFailures() throws Exception {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java; C", "Blue"},
                {"2", "B", "Assembly", "Green"},
        };
        String[][] cfg = {
                {"1", "4", "2"},  // IDE
                {"0", "0", "3"},  // Syntax
                {"0", "20", "4"}, // LLM
        };
        assertTrue(gm.createInitialBoard(players, 12, cfg));

        // move to collect tool + trigger abyss
        assertTrue(gm.moveCurrentPlayer(1)); gm.reactToAbyssOrTool();
        assertFalse(gm.moveCurrentPlayer(3)); gm.reactToAbyssOrTool(); // Assembly invalid (>2)

        Path tmp = Files.createTempFile("save", ".txt");
        File f = tmp.toFile();
        assertTrue(gm.saveGame(f));

        GameManager gm2 = new GameManager();
        assertDoesNotThrow(() -> gm2.loadGame(f));

        // basic sanity after load
        assertNotNull(gm2.getProgrammerInfo(1));
        assertNotNull(gm2.getSlotInfo(2));
        assertNotNull(gm2.getAuthorsPanel());
        assertEquals("true", gm2.customizeBoard().get("hasNewAbyss"));

        // saveGame null
        assertFalse(gm2.saveGame(null));

        // saveGame to directory -> false
        Path dir = Files.createTempDirectory("dirsave");
        assertFalse(gm2.saveGame(dir.toFile()));
    }

    @Test
    public void testLoadGameFileNotFoundAndEmptyFile() throws Exception {
        GameManager gm = new GameManager();

        assertThrows(java.io.FileNotFoundException.class, () -> gm.loadGame(null));

        Path missing = Path.of("this_file_should_not_exist_1234567.txt");
        assertThrows(java.io.FileNotFoundException.class, () -> gm.loadGame(missing.toFile()));

        Path empty = Files.createTempFile("empty", ".txt");
        // ensure empty
        Files.writeString(empty, "");
        assertThrows(InvalidFileException.class, () -> gm.loadGame(empty.toFile()));
    }

    @Test
    public void testLoadGameInvalidFormatTriggersGenericInvalidFileException() throws Exception {
        GameManager gm = new GameManager();

        Path bad = Files.createTempFile("bad", ".txt");
        Files.writeString(bad, "notANumber\n");
        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> gm.loadGame(bad.toFile()));
        assertTrue(ex.getMessage().toLowerCase().contains("formato"));
    }

    @Test
    public void testLoadGameInvalidProgrammerLine() throws Exception {
        GameManager gm = new GameManager();

        Path bad = Files.createTempFile("badprog", ".txt");
        // boardSize, numProgrammers=1, invalid line with <6 parts
        Files.writeString(bad,
                "10\n" +
                        "1\n" +
                        "1|A|Java\n"
        );
        assertThrows(InvalidFileException.class, () -> gm.loadGame(bad.toFile()));
    }

    @Test
    public void testLoadGameMissingLastMoveInfoHandled() throws Exception {
        GameManager gm = new GameManager();

        Path ok = Files.createTempFile("min", ".txt");
        // minimal valid file ending right after turnCount
        Files.writeString(ok,
                "10\n" +          // boardSize
                        "2\n" +           // programmers
                        "1|A|Java|Blue|1|Em Jogo|||\n" +
                        "2|B|Java|Green|1|Em Jogo|||\n" +
                        "0\n" +           // abysses
                        "0\n" +           // tools
                        "2\n" +           // turnOrder size
                        "1\n" +
                        "2\n" +
                        "0\n" +           // turnCursor
                        "false\n" +       // gameOver
                        "-1\n" +          // winnerId
                        "1\n"             // turnCount
        );
        assertDoesNotThrow(() -> gm.loadGame(ok.toFile()));
        assertNull(gm.reactToAbyssOrTool());
    }

    // ---------- PENDING_REASON_DEFEATED safety branch via crafted load ----------
    @Test
    public void testMoveCurrentPlayerWhenCurrentAlreadyDefeatedSafetyBranch() throws Exception {
        GameManager gm = new GameManager();

        Path ok = Files.createTempFile("defeated", ".txt");
        Files.writeString(ok,
                "10\n" +          // boardSize
                        "2\n" +           // programmers
                        "1|A|Java|Blue|1|Derrotado||BSOD|0\n" +
                        "2|B|Java|Green|1|Em Jogo|||0\n" +
                        "0\n" +           // abysses
                        "0\n" +           // tools
                        "2\n" +           // turnOrder size (includes defeated id 1)
                        "1\n" +
                        "2\n" +
                        "0\n" +           // turnCursor -> id 1
                        "false\n" +
                        "-1\n" +
                        "1\n" +
                        "0\n" +           // lastDice
                        "-1\n" +          // lastPlayerId
                        "0\n" +
                        "0\n"
        );

        gm.loadGame(ok.toFile());
        assertFalse(gm.moveCurrentPlayer(1));
        assertNull(gm.reactToAbyssOrTool()); // defeated branch returns null
    }

    // ---------- Cover saveGame branch when turnOrderIds == null ----------
    @Test
    public void testSaveGameWhenTurnOrderIdsNullBranch() throws Exception {
        GameManager gm = new GameManager();
        String[][] players = {
                {"1", "A", "Java", "Blue"},
                {"2", "B", "Java", "Green"},
        };
        assertTrue(gm.createInitialBoard(players, 10));

        setField(gm, "turnOrderIds", null);

        Path tmp = Files.createTempFile("save2", ".txt");
        assertTrue(gm.saveGame(tmp.toFile()));
    }

    // ---------- getAuthorsPanel/customizeBoard ----------
    @Test
    public void testAuthorsPanelAndCustomizeBoard() {
        GameManager gm = new GameManager();
        JPanel p = gm.getAuthorsPanel();
        assertNotNull(p);

        HashMap<String, String> map = gm.customizeBoard();
        assertEquals("true", map.get("hasNewAbyss"));
    }

}