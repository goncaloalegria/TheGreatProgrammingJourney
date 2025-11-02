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
        boolean resultado = gameManager.createInitialBoard(playerInfo,boardSize);

        assertTrue(resultado,"createInitialBoardValid deve retornar true");

    }

    @Test
    public void testCreateInitialBoardInvalidoUmJogador() {
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"1", "Alice", "Java", "Purple"}
        };
        int boardSize = 10;

        boolean resultado = gameManager.createInitialBoard(playerInfo, boardSize);

        assertFalse(resultado, "createInitialBoard deve retornar false com apenas 1 jogador");
    }

    @Test
    public void testCreateInitialBoardInvalidColor(){
        GameManager gameManager = new GameManager();
        String [][] playerInfo = {
            {"1","Ana","Java","Red"}
        };
        int boardSize = 10;

        boolean resultado = gameManager.createInitialBoard(playerInfo,boardSize);

        assertFalse(resultado,"testCreateInitialBoardInvalidColor, deve retornar false pois cor esta indesponivel");
    }

    @Test
    public void testMoveCurrentPlayerMovimentoValido() {
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"1", "Hugo", "Haskell", "Purple"},
                {"2", "Iris", "Kotlin", "Green"}
        };
        gameManager.createInitialBoard(playerInfo, 10);

        boolean resultado = gameManager.moveCurrentPlayer(3);

        assertTrue(resultado, "moveCurrentPlayer deve retornar true para movimento válido");

        String[] info = gameManager.getProgrammerInfo(1);
        assertEquals("4", info[4], "Jogador 1 deve estar na posição 4 após mover 3 casas");
    }

    @Test
    public void testGetSlotInfoRetornaJogadoresNaPosicao() {
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"10", "Jack", "Perl", "Blue"},
                {"20", "Kate", "Swift", "Brown"}
        };
        gameManager.createInitialBoard(playerInfo, 15);

        String[] slotInfo = gameManager.getSlotInfo(1);

        assertNotNull(slotInfo, "getSlotInfo não deve retornar null");
        assertEquals("10,20", slotInfo[0], "Posição 1 deve conter jogadores 10,20");
    }

    @Test
    public void testGetCurrentPlayerIDRetornaPrimeiroJogador() {
        GameManager gameManager = new GameManager();
        String[][] playerInfo = {
                {"5", "Eva", "JavaScript", "Green"},
                {"3", "Frank", "Go", "Purple"},
                {"7", "Grace", "Rust", "Brown"}
        };
        gameManager.createInitialBoard(playerInfo, 12);

        int currentPlayerId = gameManager.getCurrentPlayerID();

        assertEquals(3, currentPlayerId, "getCurrentPlayerID deve retornar 3 (menor ID)");
    }


}
