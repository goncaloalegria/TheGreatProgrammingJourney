package pt.ulusofona.lp2.greatprogrammingjourney;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GameManager {

    private static final Set<String> VALID_COLORS = new HashSet<>(Arrays.asList("Purple", "Green", "Brown", "Blue"));
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    private ArrayList<Programmer> programmers;
    private HashMap<Integer, Programmer> idToProgrammer;
    private int boardSize;
    private ArrayList<Integer> turnOrderIds;
    private int turnCursor;
    private boolean gameOver;
    private Integer winnerId;
    private int turnCount;
    private HashMap<Integer, Abyss> abyssesByPosition;


    public GameManager() {
        this.programmers = new ArrayList<>();
        this.idToProgrammer = new HashMap<>();
        this.abyssesByPosition = new HashMap<>();
    }

    public boolean createInitialBoard(String[][] playerInfo, int boardSize, String[][] AbyssesAndTools) {
        if (playerInfo == null) {
            return false;
        }

        final int n = playerInfo.length;
        if (n < MIN_PLAYERS || n > MAX_PLAYERS) {
            return false;
        }
        if (boardSize < n * 2) {
            return false;
        }

        HashSet<Integer> seenIds = new HashSet<>();
        HashSet<String> usedColors = new HashSet<>();

        // Validar todos os jogadores antes de criar
        for (String[] row : playerInfo) {
            if (!validatePlayerRow(row, seenIds, usedColors)) {
                return false;
            }
        }

        // Criar objetos Programmer
        this.programmers.clear();
        this.idToProgrammer.clear();

        for (String[] row : playerInfo) {
            int id = Integer.parseInt(row[0]);
            String name = row[1];
            String languages = row[2];
            String color = row[3];

            Programmer programmer = new Programmer(id, name, languages, color);
            programmers.add(programmer);
            idToProgrammer.put(id, programmer);
        }

        this.boardSize = boardSize;
        this.turnOrderIds = new ArrayList<>(seenIds);
        Collections.sort(this.turnOrderIds);
        this.turnCursor = 0;
        this.gameOver = false;
        this.winnerId = null;
        this.turnCount = 0;

        // -------------------------------
        // NOVO: processar AbyssesAndTools
        // -------------------------------
        abyssesByPosition.clear();

        if (AbyssesAndTools != null) {
            for (String[] row : AbyssesAndTools) {
                if (row == null) {
                    continue;
                }

                // Esperamos pelo menos: tipo, id, posição
                if (row.length < 3) {
                    return false;
                }

                int tipo;
                int abyssOrToolId;
                int pos;

                try {
                    tipo = Integer.parseInt(row[0]);
                    abyssOrToolId = Integer.parseInt(row[1]);
                    pos = Integer.parseInt(row[2]);
                } catch (NumberFormatException e) {
                    return false;
                }

                // Validar posição dentro do tabuleiro
                // (tipicamente não se colocam coisas na casa 1 nem na última)
                if (pos <= 1 || pos >= boardSize) {
                    return false;
                }

                if (tipo == 0) { // 0 = Abyss
                    Abyss abyss = createAbyss(abyssOrToolId, pos);
                    if (abyss == null) {
                        // ID de abismo desconhecido
                        return false;
                    }

                    // Se já existir um abyss nesta posição, podes:
                    // - retornar false (configuração inválida)
                    // - ou substituir (eu vou retornar false para ser mais seguro)
                    if (abyssesByPosition.containsKey(pos)) {
                        return false;
                    }

                    addAbyss(abyss);
                } else {
                    // 1 = Tool (por exemplo) -> ainda não tratamos,
                    // por isso ignoramos por agora.
                }
            }
        }

        return true;
    }

    private boolean validatePlayerRow(String[] row, HashSet<Integer> seenIds, HashSet<String> usedColors) {
        if (row == null || row.length < 4) {
            return false;
        }

        // Validar ID
        int id;
        try {
            id = Integer.parseInt(row[0]);
        } catch (NumberFormatException e) {
            return false;
        }
        if (id < 1 || seenIds.contains(id)) {
            return false;
        }
        seenIds.add(id);

        // Validar nome
        if (row[1] == null || row[1].trim().isEmpty()) {
            return false;
        }

        // Validar linguagens
        if (row[2] == null) {
            return false;
        }

        // Validar cor
        String color = row[3];
        if (color == null || !VALID_COLORS.contains(color) || usedColors.contains(color)) {
            return false;
        }
        usedColors.add(color);

        return true;
    }

    public String getImagePng(int position) {
        if (position < 1 || position > boardSize) {
            return null;
        }

        if (position == boardSize) {
            return "glory.png";
        }
        return null;
    }

    public String[] getProgrammerInfo(int id) {
        Programmer programmer = idToProgrammer.get(id);
        if (programmer == null) {
            return null;
        }
        return programmer.getInfoAsArray();
    }

    public String getProgrammerInfoAsStr(int id) {
        Programmer programmer = idToProgrammer.get(id);
        if (programmer == null) {
            return null;
        }
        return programmer.getInfoAsString();
    }

    public String[] getSlotInfo(int position) {
        if (position < 1 || position > boardSize) {
            return null;
        }

        if (programmers.isEmpty()) {
            return new String[] { "" };
        }

        ArrayList<Integer> idsHere = new ArrayList<>();
        for (Programmer programmer : programmers) {
            if (programmer.getPosition() == position) {
                idsHere.add(programmer.getId());
            }
        }

        if (idsHere.isEmpty()) {
            return new String[] { "" };
        }

        Collections.sort(idsHere);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < idsHere.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(idsHere.get(i));
        }

        return new String[] { sb.toString() };
    }

    public int getCurrentPlayerID() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return -1;
        }
        return turnOrderIds.get(turnCursor);
    }

    public boolean moveCurrentPlayer(int nrPositions) {
        if (gameOver) {
            return false;
        }

        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return false;
        }

        if (nrPositions < 0 || nrPositions > 6) {
            return false;
        }

        int currentId = turnOrderIds.get(turnCursor);
        Programmer currentProgrammer = idToProgrammer.get(currentId);

        if (currentProgrammer == null || !currentProgrammer.isPlaying()) {
            return false;
        }

        int from = currentProgrammer.getPosition();
        int to = calculateNewPosition(from, nrPositions);

        currentProgrammer.setPosition(to);
        turnCount++;

        boolean repeatTurn = applyAbyssIfAny(currentProgrammer, from, nrPositions);

        if (to == boardSize) {
            gameOver = true;
            winnerId = currentId;
            return true;
        }

        if (!repeatTurn) {
            turnCursor = (turnCursor + 1) % turnOrderIds.size();
        }
        return true;
    }

    private int calculateNewPosition(int from, int nrPositions) {
        int to = from + nrPositions;

        if (to > boardSize) {
            int overshoot = to - boardSize;
            to = Math.max(1, boardSize - overshoot);
        }

        return to;
    }

    public boolean gameIsOver() {
        if (gameOver) {
            return true;
        }

        for (Programmer programmer : programmers) {
            if (programmer.getPosition() == boardSize) {
                gameOver = true;
                if (winnerId == null) {
                    winnerId = programmer.getId();
                }
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getGameResults() {
        ArrayList<String> out = new ArrayList<>();

        if (!gameIsOver()) {
            return out;
        }

        out.add("THE GREAT PROGRAMMING JOURNEY");
        out.add("");
        out.add("NR. DE TURNOS");
        out.add(String.valueOf(turnCount + 1));
        out.add("");
        out.add("VENCEDOR");
        out.add(getWinnerName());
        out.add("");
        out.add("RESTANTES");

        ArrayList<Programmer> remainingPlayers = getRemainingPlayers();
        sortProgrammersByPositionAndName(remainingPlayers);

        for (Programmer programmer : remainingPlayers) {
            out.add(programmer.getName() + " " + programmer.getPosition());
        }

        return out;
    }

    private String getWinnerName() {
        if (winnerId != null) {
            Programmer winner = idToProgrammer.get(winnerId);
            if (winner != null) {
                return winner.getName();
            }
        }
        return "";
    }

    private ArrayList<Programmer> getRemainingPlayers() {
        ArrayList<Programmer> remaining = new ArrayList<>();
        for (Programmer programmer : programmers) {
            if (winnerId == null || programmer.getId() != winnerId) {
                remaining.add(programmer);
            }
        }
        return remaining;
    }

    private void sortProgrammersByPositionAndName(ArrayList<Programmer> programmers) {
        programmers.sort((a, b) -> {
            int posA = a.getPosition();
            int posB = b.getPosition();

            if (posA != posB) {
                return Integer.compare(posB, posA); // Descendente por posição
            }

            return a.getName().compareToIgnoreCase(b.getName()); // Ascendente por nome
        });
    }

    public JPanel getAuthorsPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 300));
        return panel;
    }

    public HashMap<String, String> customizeBoard() {
        return new HashMap<>();
    }

    public void addAbyss(Abyss abyss){
        if(abyss == null){
            return;
        }
        abyssesByPosition.put(abyss.getPosition(),abyss);
        // isto faz com que possa adicionar os abyss ao gameManager
        // Ex: gameManager.addAbyss(new MemoryCrashAbyss(5));   // posição 5 → ID 0
    }

    private boolean applyAbyssIfAny(Programmer programmer, int fromPosition, int diceValue) {
        if (programmer == null) {
            return false;
        }

        int currentPos = programmer.getPosition();
        Abyss abyss = abyssesByPosition.get(currentPos);

        if (abyss == null) {
            return false;
        }

        abyss.applyEffect(programmer, diceValue, fromPosition);

        // true se o jogador tem de repetir a vez (ex: Crash de Memória)
        return abyss.forcesRepeatTurn();
    }

    // --------------------------------------------------
    // NOVO: fábrica de Abyss com base no ID
    // --------------------------------------------------
    private Abyss createAbyss(int abyssId, int position) {
        // Aqui fazes o mapeamento ID -> tipo de Abyss.
        // Exemplo baseado no teu comentário:
        // "gameManager.addAbyss(new MemoryCrashAbyss(5));   // posição 5 → ID 0"
        switch (abyssId) {
            case 0:
                // Crash de Memória
                return new MemoryCrashAbyss(position);

            // Quando criares mais abismos, vais adicionando aqui:
             case 1:
                 return new LogicErrorAbyss(position);
            // case 2: return new LogicErrorAbyss(position);
            // ...

            default:
                // ID desconhecido → configuração inválida
                return null;
        }
    }

}
