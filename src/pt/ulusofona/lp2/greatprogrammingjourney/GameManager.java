package pt.ulusofona.lp2.greatprogrammingjourney;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

public class GameManager {

    private static final Set<String> VALID_COLORS =
            new HashSet<>(Arrays.asList("Purple", "Green", "Brown", "Blue"));
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    // --- Estado base do jogo ---
    private ArrayList<Programmer> programmers;
    private HashMap<Integer, Programmer> idToProgrammer;
    private int boardSize;
    private ArrayList<Integer> turnOrderIds;
    private int turnCursor;
    private boolean gameOver;
    private Integer winnerId;
    private int turnCount;

    // Abysses e Tools por posiÃ§Ã£o
    private HashMap<Integer, Abyss> abyssesByPosition;
    private HashMap<Integer, Tool> toolsByPosition;

    private Random random;

    // --- Info da Ãºltima jogada ---
    private int lastDiceValue = 0;
    private Integer lastPlayerId = null;
    private int lastFromPosition = 0;
    private int lastToPosition = 0;
    private Abyss lastAbyss = null;
    private Tool lastToolUsed = null;
    private Tool lastToolCollected = null;

    // Se o turno do jogador atual foi "consumido" e estÃ¡ Ã  espera de reaÃ§Ã£o
    private boolean pendingReaction = false;

    // RazÃµes para moveCurrentPlayer devolver false mas o turno avanÃ§ar via react
    private int pendingReason = 0;
    private static final int PENDING_REASON_NONE = 0;
    private static final int PENDING_REASON_TRAPPED = 1;
    private static final int PENDING_REASON_INVALID_MOVE = 2;
    private static final int PENDING_REASON_DEFEATED = 3;

    public GameManager() {
        this.programmers = new ArrayList<>();
        this.idToProgrammer = new HashMap<>();
        this.abyssesByPosition = new HashMap<>();
        this.toolsByPosition = new HashMap<>();
        this.random = new Random();
    }

    // Parte 1
    public boolean createInitialBoard(String[][] playerInfo, int boardSize) {
        return createInitialBoard(playerInfo, boardSize, null);
    }

    // Parte 2
    public boolean createInitialBoard(String[][] playerInfo, int boardSize, String[][] abyssesAndTools) {
        if (playerInfo == null) {
            return false;
        }

        int n = playerInfo.length;
        if (n < MIN_PLAYERS || n > MAX_PLAYERS) {
            return false;
        }
        if (boardSize < n * 2) {
            return false;
        }

        // 1) Validar jogadores
        HashSet<Integer> seenIds = new HashSet<>();
        HashSet<String> usedColors = new HashSet<>();
        for (String[] row : playerInfo) {
            if (!validatePlayerRow(row, seenIds, usedColors)) {
                return false;
            }
        }

        // 2) Reset e criar jogadores
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

        // 3) Reset do jogo
        this.boardSize = boardSize;
        this.turnOrderIds = new ArrayList<>(seenIds);
        Collections.sort(this.turnOrderIds);
        this.turnCursor = 0;
        this.gameOver = false;
        this.winnerId = null;

        // TurnCount: comeÃ§a no turno 1
        this.turnCount = 1;

        this.lastDiceValue = 0;
        this.lastPlayerId = null;
        this.lastFromPosition = 0;
        this.lastToPosition = 0;
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        this.pendingReaction = false;
        this.pendingReason = PENDING_REASON_NONE;

        // 4) Reset do tabuleiro
        abyssesByPosition.clear();
        toolsByPosition.clear();

        // 5) Processar config de abysses/tools
        if (abyssesAndTools != null) {
            if (!validateAbyssesAndToolsConfig(abyssesAndTools, boardSize)) {
                return false;
            }
            placeConfiguredItems(abyssesAndTools);
        }

        return true;
    }

    private boolean validatePlayerRow(String[] row, HashSet<Integer> seenIds, HashSet<String> usedColors) {
        if (row == null || row.length < 4) {
            return false;
        }

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

        if (row[1] == null || row[1].trim().isEmpty()) {
            return false;
        }

        if (row[2] == null) {
            return false;
        }

        String color = row[3];
        if (color == null || !VALID_COLORS.contains(color) || usedColors.contains(color)) {
            return false;
        }
        usedColors.add(color);

        return true;
    }

    private boolean validateAbyssesAndToolsConfig(String[][] cfg, int boardSize) {
        HashSet<Integer> occupiedSlots = new HashSet<>();

        for (String[] row : cfg) {
            if (row == null) {
                continue;
            }
            if (row.length < 3) {
                continue;
            }

            Integer tipo = parseIntOrNull(row[0]);
            Integer id = parseIntOrNull(row[1]);
            Integer pos = parseIntOrNull(row[2]);

            if (tipo == null || id == null || pos == null) {
                return false;
            }
            if (tipo != 0 && tipo != 1) {
                return false;
            }

            if (tipo == 0) {
                if (id < 0 || id > 9) {
                    return false;
                }
            } else {
                if (id < 0 || id > 5) {
                    return false;
                }
            }

            if (pos <= 1 || pos >= boardSize) {
                return false;
            }

            if (occupiedSlots.contains(pos)) {
                return false;
            }
            occupiedSlots.add(pos);
        }

        return true;
    }

    private void placeConfiguredItems(String[][] cfg) {
        for (String[] row : cfg) {
            if (row == null) {
                continue;
            }
            if (row.length < 3) {
                continue;
            }

            Integer tipo = parseIntOrNull(row[0]);
            Integer id = parseIntOrNull(row[1]);
            Integer pos = parseIntOrNull(row[2]);

            if (tipo == null || id == null || pos == null) {
                continue;
            }

            if (tipo == 0) {
                Abyss abyss = createAbyss(id, pos);
                if (abyss != null) {
                    addAbyss(abyss);
                }
            } else {
                Tool tool = createTool(id, pos);
                if (tool != null) {
                    addTool(tool);
                }
            }
        }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeLanguages(String raw) {
        if (raw == null) {
            return "";
        }

        String trimmedAll = raw.trim();
        if (trimmedAll.isEmpty()) {
            return "";
        }

        String[] parts = trimmedAll.split("[;,]");
        ArrayList<String> langs = new ArrayList<>();

        for (String p : parts) {
            if (p != null) {
                String t = p.trim();
                if (!t.isEmpty()) {
                    langs.add(t);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < langs.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(langs.get(i));
        }
        return sb.toString();
    }

    public String getImagePng(int position) {
        if (position < 1 || position > boardSize) {
            return null;
        }

        if (position == boardSize) {
            return "glory.png";
        }

        Abyss abyss = abyssesByPosition.get(position);
        if (abyss != null) {
            return abyss.getImageName();
        }

        Tool tool = toolsByPosition.get(position);
        if (tool != null) {
            return tool.getImageName();
        }

        return null;
    }

    // ORDEM (7 elementos):
    // 0 ID, 1 Nome, 2 Linguagens, 3 Cor, 4 Posição, 5 Ferramentas, 6 Estado
    public String[] getProgrammerInfo(int id) {
        Programmer programmer = idToProgrammer.get(id);
        if (programmer == null) {
            return null;
        }

        String idStr = String.valueOf(programmer.getId());
        String name = programmer.getName();
        String languages = normalizeLanguages(programmer.getLanguages());
        String color = programmer.getColor();
        String position = String.valueOf(programmer.getPosition());
        String tools = programmer.getToolsInfo();
        String state = programmer.getState();

        return new String[]{idStr, name, languages, color, position, tools, state};
    }

    // Formato esperado:
    // id | name | position | toolsInfo | languages (ordenadas) | state
    public String getProgrammerInfoAsStr(int id) {
        Programmer programmer = idToProgrammer.get(id);
        if (programmer == null) {
            return null;
        }

        String idStr = String.valueOf(programmer.getId());
        String name = programmer.getName();
        String position = String.valueOf(programmer.getPosition());
        String tools = programmer.getToolsInfo();
        String languages = programmer.getOrderedLanguages();  // Ordenadas!
        String state = programmer.getState();

        return idStr + " | " + name + " | " + position + " | " + tools + " | " + languages + " | " + state;
    }

    public String getProgrammersInfo() {
        if (programmers == null || programmers.isEmpty()) {
            return "";
        }

        ArrayList<Programmer> ordered = new ArrayList<>(programmers);
        ordered.sort(Comparator.comparingInt(Programmer::getId));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            Programmer p = ordered.get(i);

            sb.append(p.getName())
                    .append(" : ")
                    .append(p.getToolsInfo());
            if (i < ordered.size() - 1) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }

    public String[] getSlotInfo(int position) {
        if (position < 1 || position > boardSize) {
            return null;
        }

        String programmersStr = "";
        if (!programmers.isEmpty()) {
            ArrayList<Integer> idsHere = new ArrayList<>();
            for (Programmer programmer : programmers) {
                if (programmer.getPosition() == position) {
                    idsHere.add(programmer.getId());
                }
            }
            if (!idsHere.isEmpty()) {
                Collections.sort(idsHere);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < idsHere.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(idsHere.get(i));
                }
                programmersStr = sb.toString();
            }
        }

        Abyss abyss = abyssesByPosition.get(position);
        if (abyss != null) {
            return new String[]{programmersStr, abyss.getName(), "A:" + abyss.getId()};
        }

        Tool tool = toolsByPosition.get(position);
        if (tool != null) {
            return new String[]{programmersStr, tool.getName(), "T:" + tool.getId()};
        }

        return new String[]{programmersStr, "", ""};
    }

    public int getCurrentPlayerID() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return -1;
        }
        return turnOrderIds.get(turnCursor);
    }

    public String getCurrentPlayerName() {
        Programmer p = idToProgrammer.get(getCurrentPlayerID());
        if (p == null) {
            return "";
        }
        return p.getName();
    }

    public String[] getCurrentPlayerInfo() {
        return getProgrammerInfo(getCurrentPlayerID());
    }

    public int getLastDiceValue() {
        return lastDiceValue;
    }

    public String getLastDiceImageName() {
        if (lastDiceValue < 1 || lastDiceValue > 6) {
            return null;
        }
        return "dice" + lastDiceValue + ".png";
    }

    /**
     * moveCurrentPlayer:
     * - NÃƒO avanÃ§a turnCursor (isso Ã© no react)
     * - Se o jogador estiver Preso/Derrotado: devolve false e deixa pendingReason
     * - Bounce-back ao ultrapassar a meta
     * - RestriÃ§Ãµes por linguagem:
     *   Assembly: max 2
     *   C: max 3  (C# NÃƒO conta como C)
     */
    public boolean moveCurrentPlayer(int nrPositions) {
        if (gameOver) {
            return false;
        }
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return false;
        }
        if (nrPositions < 1 || nrPositions > 6) {
            return false;
        }

        // Reset pendÃªncias anteriores
        this.pendingReaction = false;
        this.pendingReason = PENDING_REASON_NONE;

        int currentId = getCurrentPlayerID();
        Programmer current = idToProgrammer.get(currentId);
        if (current == null) {
            return false;
        }

        // Se está derrotado mas ainda aparece (segurança)
        if (current.isDefeated()) {
            setLastMoveNoChange(currentId, current.getPosition(), nrPositions);
            this.pendingReaction = true;
            this.pendingReason = PENDING_REASON_DEFEATED;
            return false;
        }

        // Se está preso: verificar se tem ferramenta para se libertar
        if (current.isTrapped()) {
            // Verificar se tem ferramenta que anula Ciclo Infinito (abismo ID 8)
            Tool liberator = current.findToolToCancelAbyss(InfiniteLoopAbyss.ID);
            if (liberator != null) {
                // Tem ferramenta! Liberta-se e usa a ferramenta
                current.removeTool(liberator);
                current.setState("Em Jogo");
                // Continua o movimento normalmente (não retorna aqui)
            } else {
                // Não tem ferramenta, fica preso
                setLastMoveNoChange(currentId, current.getPosition(), nrPositions);
                this.pendingReaction = true;
                this.pendingReason = PENDING_REASON_TRAPPED;
                return false;
            }
        }

        // RestriÃ§Ãµes por linguagem
        String firstLang = current.getFirstLanguage();
        if (firstLang != null) {
            if (firstLang.equalsIgnoreCase("Assembly") && nrPositions > 2) {
                setLastMoveNoChange(currentId, current.getPosition(), nrPositions);
                this.pendingReaction = true;
                this.pendingReason = PENDING_REASON_INVALID_MOVE;
                return false;
            }

            if (firstLang.equalsIgnoreCase("C") && nrPositions > 3) {
                setLastMoveNoChange(currentId, current.getPosition(), nrPositions);
                this.pendingReaction = true;
                this.pendingReason = PENDING_REASON_INVALID_MOVE;
                return false;
            }
        }

        // Movimento normal com bounce-back
        this.lastDiceValue = nrPositions;
        this.lastPlayerId = currentId;
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        int from = current.getPosition();
        int to = calculateNewPosition(from, nrPositions);

        this.lastFromPosition = from;
        this.lastToPosition = to;

        current.recordMove(to);

        this.pendingReaction = true;
        this.pendingReason = PENDING_REASON_NONE;

        return true;
    }

    private void setLastMoveNoChange(int playerId, int pos, int dice) {
        this.lastDiceValue = dice;
        this.lastPlayerId = playerId;
        this.lastFromPosition = pos;
        this.lastToPosition = pos;
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;
    }

    // Bounce-back: se passar da meta, o jogador "bate" e volta para trÃ¡s
    private int calculateNewPosition(int from, int nrPositions) {
        int to = from + nrPositions;

        if (to > boardSize) {
            int overshoot = to - boardSize;
            to = boardSize - overshoot;

            if (to < 1) {
                to = 1;
            }
        }

        return to;
    }

    public String reactToAbyssOrTool() {
        if (!pendingReaction || gameOver || lastPlayerId == null) {
            return null;
        }

        Programmer current = idToProgrammer.get(lastPlayerId);
        if (current == null) {
            clearPendingState();
            return null;
        }

        // Trata casos especiais (preso, derrotado, movimento invÃ¡lido)
        String specialCaseResult = handleSpecialCases(current);
        if (specialCaseResult != null) {
            return specialCaseResult;
        }

        // Reset last-info
        resetLastActionInfo();

        int pos = current.getPosition();

        // Processa ferramenta na posiÃ§Ã£o
        String toolMsg = processTool(current, pos);

        // Processa abismo na posiÃ§Ã£o
        String abyssMsg = processAbyss(current, pos);

        // Verifica vitÃ³ria
        checkForVictory(current);

        // AvanÃ§a turno se necessÃ¡rio
        advanceTurnIfNeeded(pos);

        clearPendingState();

        return determineReturnMessage(abyssMsg, toolMsg);
    }

    private void clearPendingState() {
        pendingReaction = false;
        pendingReason = PENDING_REASON_NONE;
    }

    private String handleSpecialCases(Programmer current) {
        // Jogador preso sem ferramenta
        if (pendingReason == PENDING_REASON_TRAPPED) {
            clearPendingState();
            turnCount++;
            advanceTurnCursor();
            return "Ciclo Infinito!";
        }

        if (pendingReason == PENDING_REASON_DEFEATED) {
            clearPendingState();
            turnCount++;
            advanceTurnCursor();
            return null;
        }

        if (pendingReason == PENDING_REASON_INVALID_MOVE) {
            clearPendingState();
            turnCount++;
            advanceTurnCursor();
            return null;
        }

        return null;
    }

    private void resetLastActionInfo() {
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;
    }

    // âœ… Remove a tool do tabuleiro apenas quando alguÃ©m a apanha (e nÃ£o a tinha)
    private String processTool(Programmer current, int pos) {
        Tool boardTool = toolsByPosition.get(pos);

        if (boardTool == null) {
            return null;
        }

        if (!current.hasToolOfType(boardTool.getId())) {
            // Criar uma nova instância da ferramenta para o jogador
            Tool newTool = createTool(boardTool.getId(), pos);
            current.addTool(newTool);
            this.lastToolCollected = newTool;

            // Ferramenta PERMANECE no tabuleiro - pode ser apanhada por todos
            return "Recolheu ferramenta: " + boardTool.getName();
        } else {
            return "Já possui a ferramenta: " + boardTool.getName();
        }
    }

    private String processAbyss(Programmer current, int pos) {
        Abyss abyss = abyssesByPosition.get(pos);

        if (abyss == null) {
            return null;
        }

        this.lastAbyss = abyss;

        if (abyss.getId() == SegmentationFaultAbyss.ID) {
            return handleSegmentationFault(pos, abyss);
        }

        return handleRegularAbyss(current, abyss);
    }

    private String handleSegmentationFault(int pos, Abyss abyss) {
        List<Programmer> here = getAlivePlayersAt(pos);
        if (here.size() >= 2) {
            applySegmentationFaultToAll(here);
            return abyss.getName() + "!";
        }
        return abyss.getName() + "!";
    }

    private String handleRegularAbyss(Programmer current, Abyss abyss) {
        Tool canceller = current.findToolToCancelAbyss(abyss.getId());

        if (canceller != null) {
            current.removeTool(canceller);
            this.lastToolUsed = canceller;
            return abyss.getName() + " anulado por " + canceller.getName();
        }

        abyss.applyEffect(current, lastDiceValue, lastFromPosition);

        if (current.isDefeated()) {
            removePlayerFromTurnOrder(current.getId());
        }

        return abyss.getName() + "!";
    }

    private void checkForVictory(Programmer current) {
        if (current.getPosition() == boardSize && current.isPlaying()) {
            gameOver = true;
            winnerId = current.getId();
        }
    }

    private void advanceTurnIfNeeded(int pos) {
        Abyss abyss = abyssesByPosition.get(pos);
        boolean repeatTurn = false;

        if (abyss != null && lastToolUsed == null) {
            repeatTurn = abyss.forcesRepeatTurn();
        }

        turnCount++;

        if (!gameOver && !repeatTurn) {
            advanceTurnCursor();
        }
    }

    private String determineReturnMessage(String abyssMsg, String toolMsg) {
        if (abyssMsg != null) {
            return abyssMsg;
        }
        if (toolMsg != null) {
            return toolMsg;
        }
        return null;
    }

    private void advanceTurnCursor() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return;
        }
        turnCursor = (turnCursor + 1) % turnOrderIds.size();
    }

    private void removePlayerFromTurnOrder(int id) {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return;
        }

        int idx = turnOrderIds.indexOf(id);
        if (idx < 0) {
            return;
        }

        turnOrderIds.remove(idx);

        if (turnOrderIds.isEmpty()) {
            gameOver = true;
            winnerId = null;
            turnCursor = 0;
            return;
        }

        if (idx <= turnCursor) {
            turnCursor--;
        }

        if (turnCursor < 0) {
            turnCursor = turnOrderIds.size() - 1;
        }
        if (turnCursor >= turnOrderIds.size()) {
            turnCursor = 0;
        }
    }

    private List<Programmer> getAlivePlayersAt(int position) {
        ArrayList<Programmer> out = new ArrayList<>();
        for (Programmer pr : programmers) {
            if (pr != null && !pr.isDefeated() && pr.getPosition() == position) {
                out.add(pr);
            }
        }
        return out;
    }

    private void applySegmentationFaultToAll(List<Programmer> playersHere) {
        int retreat = SegmentationFaultAbyss.RETREAT_POSITIONS;

        for (Programmer p : playersHere) {
            int newPos = p.getPosition() - retreat;
            if (newPos < 1) {
                newPos = 1;
            }
            p.setPosition(newPos);
        }

        for (Programmer p : playersHere) {
            applyLandingEffectsAfterForcedMove(p);
        }
    }

    private void applyLandingEffectsAfterForcedMove(Programmer programmer) {
        if (programmer == null) {
            return;
        }
        if (programmer.isDefeated()) {
            return;
        }

        int pos = programmer.getPosition();

        Tool tool = toolsByPosition.get(pos);
        if (tool != null) {
            if (!programmer.hasToolOfType(tool.getId())) {
                // Criar nova instância da ferramenta
                Tool newTool = createTool(tool.getId(), pos);
                programmer.addTool(newTool);
                // Ferramenta permanece no tabuleiro
            }
        }

        Abyss abyss = abyssesByPosition.get(pos);
        if (abyss == null) {
            return;
        }

        if (abyss.getId() == SegmentationFaultAbyss.ID) {
            return;
        }

        Tool canceller = programmer.findToolToCancelAbyss(abyss.getId());
        if (canceller != null) {
            programmer.removeTool(canceller);
            return;
        }

        abyss.applyEffect(programmer, 0, pos);

        if (programmer.isDefeated()) {
            removePlayerFromTurnOrder(programmer.getId());
        }
    }

    public boolean gameIsOver() {
        if (gameOver) {
            return true;
        }

        for (Programmer programmer : programmers) {
            if (programmer.getPosition() == boardSize && programmer.isPlaying()) {
                gameOver = true;
                winnerId = programmer.getId();
                return true;
            }
        }

        int alive = 0;
        Integer lastAlive = null;
        for (Programmer p : programmers) {
            if (!p.isDefeated()) {
                alive++;
                lastAlive = p.getId();
            }
        }

        if (alive == 1 && lastAlive != null) {
            gameOver = true;
            winnerId = lastAlive;
            return true;
        }

        if (alive == 0) {
            gameOver = true;
            winnerId = null;
            return true;
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
        out.add(String.valueOf(turnCount));
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
                return Integer.compare(posB, posA);
            }

            return a.getName().compareToIgnoreCase(b.getName());
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

    public void addAbyss(Abyss abyss) {
        if (abyss == null) {
            return;
        }
        abyssesByPosition.put(abyss.getPosition(), abyss);
    }

    public void addTool(Tool tool) {
        if (tool == null) {
            return;
        }
        toolsByPosition.put(tool.getPosition(), tool);
    }

    private Abyss createAbyss(int abyssId, int position) {
        switch (abyssId) {
            case 0: {
                return new SyntaxErrorAbyss(position);
            }
            case 1: {
                return new LogicErrorAbyss(position);
            }
            case 2: {
                return new ExceptionAbyss(position);
            }
            case 3: {
                return new FileNotFoundExceptionAbyss(position);
            }
            case 4: {
                return new CrashAbyss(position);
            }
            case 5: {
                return new DuplicatedCodeAbyss(position);
            }
            case 6: {
                return new SecondaryEffects(position);
            }
            case 7: {
                return new BlueScreenOfDeathAbyss(position);
            }
            case 8: {
                return new InfiniteLoopAbyss(position);
            }
            case 9: {
                return new SegmentationFaultAbyss(position);
            }
            default: {
                return null;
            }
        }
    }

    private Tool createTool(int toolId, int position) {
        switch (toolId) {
            case 0: {
                return new InheritanceTool(position);
            }
            case 1: {
                return new FunctionalProgrammingTool(position);
            }
            case 2: {
                return new UnitTestTool(position);
            }
            case 3: {
                return new ExceptionTool(position);
            }
            case 4: {
                return new IdeTool(position);
            }
            case 5: {
                return new AjudaProfessorTool(position);
            }
            default: {
                return null;
            }
        }
    }

    // ---------------------- Save / Load ----------------------

    public boolean saveGame(File file) {
        if (file == null) {
            return false;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println(boardSize);

            out.println(programmers.size());
            for (Programmer p : programmers) {
                StringBuilder toolIds = new StringBuilder();
                for (Tool t : p.getTools()) {
                    if (toolIds.length() > 0) {
                        toolIds.append(",");
                    }
                    toolIds.append(t.getId());
                }

                out.println(
                        p.getId() + "|" +
                                p.getName() + "|" +
                                p.getLanguages() + "|" +
                                p.getColor() + "|" +
                                p.getPosition() + "|" +
                                p.getState() + "|" +
                                toolIds
                );
            }

            List<Integer> abyssPositions = new ArrayList<>(abyssesByPosition.keySet());
            Collections.sort(abyssPositions);
            out.println(abyssPositions.size());
            for (Integer pos : abyssPositions) {
                Abyss a = abyssesByPosition.get(pos);
                out.println(a.getId() + "|" + pos);
            }

            List<Integer> toolPositions = new ArrayList<>(toolsByPosition.keySet());
            Collections.sort(toolPositions);
            out.println(toolPositions.size());
            for (Integer pos : toolPositions) {
                Tool t = toolsByPosition.get(pos);
                out.println(t.getId() + "|" + pos);
            }

            if (turnOrderIds == null) {
                out.println(0);
            } else {
                out.println(turnOrderIds.size());
                for (Integer id : turnOrderIds) {
                    out.println(id);
                }
            }

            out.println(turnCursor);
            out.println(gameOver);
            out.println(winnerId == null ? -1 : winnerId);
            out.println(turnCount);

            out.println(lastDiceValue);
            out.println(lastPlayerId == null ? -1 : lastPlayerId);
            out.println(lastFromPosition);
            out.println(lastToPosition);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void loadGame(File file) throws InvalidFileException, FileNotFoundException {
        validateLoadFile(file);

        try (Scanner scanner = new Scanner(file)) {
            loadBoardSize(scanner);
            loadProgrammers(scanner);
            loadAbysses(scanner);
            loadTools(scanner);
            loadTurnOrder(scanner);
            loadGameState(scanner);
            loadLastMoveInfo(scanner);
            ensureRandom();

            this.pendingReaction = (lastPlayerId != null && lastToPosition > 0);
            this.pendingReason = PENDING_REASON_NONE;
        } catch (InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFileException("Formato de ficheiro invÃ¡lido");
        }
    }

    private void validateLoadFile(File file) throws FileNotFoundException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileNotFoundException("Ficheiro nÃ£o encontrado");
        }
    }

    private void loadBoardSize(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Ficheiro vazio");
        }
        boardSize = Integer.parseInt(scanner.nextLine().trim());
    }

    private void loadProgrammers(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados de programadores");
        }

        int numProgrammers = Integer.parseInt(scanner.nextLine().trim());
        programmers = new ArrayList<>();
        idToProgrammer = new HashMap<>();

        for (int i = 0; i < numProgrammers; i++) {
            if (!scanner.hasNextLine()) {
                throw new InvalidFileException("Dados de programadores incompletos");
            }

            String line = scanner.nextLine();
            String[] parts = line.split("\\|", -1);
            if (parts.length < 6) {
                throw new InvalidFileException("Linha de programador invÃ¡lida: " + line);
            }

            int id = Integer.parseInt(parts[0]);
            String name = parts[1];
            String languages = parts[2];
            String color = parts[3];
            int position = Integer.parseInt(parts[4]);
            String state = parts[5];

            Programmer p = new Programmer(id, name, languages, color);
            p.setPosition(position);
            p.setState(state);

            if (parts.length >= 7 && !parts[6].isEmpty()) {
                String[] toolIdStrs = parts[6].split(",");
                for (String toolIdStr : toolIdStrs) {
                    Integer toolId = parseIntOrNull(toolIdStr);
                    if (toolId != null) {
                        Tool tool = createTool(toolId, 0);
                        if (tool != null) {
                            p.addTool(tool);
                        }
                    }
                }
            }

            programmers.add(p);
            idToProgrammer.put(id, p);
        }
    }

    private void loadAbysses(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados de abismos");
        }

        int numAbysses = Integer.parseInt(scanner.nextLine().trim());
        abyssesByPosition = new HashMap<>();

        for (int i = 0; i < numAbysses; i++) {
            if (!scanner.hasNextLine()) {
                throw new InvalidFileException("Dados de abismos incompletos");
            }

            String line = scanner.nextLine();
            String[] parts = line.split("\\|", -1);
            if (parts.length < 2) {
                throw new InvalidFileException("Linha de abismo invÃ¡lida: " + line);
            }

            int abyssId = Integer.parseInt(parts[0]);
            int pos = Integer.parseInt(parts[1]);

            Abyss a = createAbyss(abyssId, pos);
            if (a != null) {
                abyssesByPosition.put(pos, a);
            }
        }
    }

    private void loadTools(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados de ferramentas");
        }

        int numTools = Integer.parseInt(scanner.nextLine().trim());
        toolsByPosition = new HashMap<>();

        for (int i = 0; i < numTools; i++) {
            if (!scanner.hasNextLine()) {
                throw new InvalidFileException("Dados de ferramentas incompletos");
            }

            String line = scanner.nextLine();
            String[] parts = line.split("\\|", -1);
            if (parts.length < 2) {
                throw new InvalidFileException("Linha de ferramenta invÃ¡lida: " + line);
            }

            int toolId = Integer.parseInt(parts[0]);
            int pos = Integer.parseInt(parts[1]);

            Tool t = createTool(toolId, pos);
            if (t != null) {
                toolsByPosition.put(pos, t);
            }
        }
    }

    private void loadTurnOrder(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados da ordem de jogo");
        }

        int orderSize = Integer.parseInt(scanner.nextLine().trim());
        turnOrderIds = new ArrayList<>();

        for (int i = 0; i < orderSize; i++) {
            if (!scanner.hasNextLine()) {
                throw new InvalidFileException("Dados da ordem de jogo incompletos");
            }
            int id = Integer.parseInt(scanner.nextLine().trim());
            turnOrderIds.add(id);
        }
    }

    private void loadGameState(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados do estado do jogo");
        }
        turnCursor = Integer.parseInt(scanner.nextLine().trim());

        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados do estado do jogo");
        }
        gameOver = Boolean.parseBoolean(scanner.nextLine().trim());

        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados do estado do jogo");
        }
        int winId = Integer.parseInt(scanner.nextLine().trim());
        winnerId = (winId < 0 ? null : winId);

        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados do estado do jogo");
        }
        turnCount = Integer.parseInt(scanner.nextLine().trim());
    }

    private void loadLastMoveInfo(Scanner scanner) {
        if (scanner.hasNextLine()) {
            lastDiceValue = Integer.parseInt(scanner.nextLine().trim());
        } else {
            lastDiceValue = 0;
            lastPlayerId = null;
            lastFromPosition = 0;
            lastToPosition = 0;
            lastAbyss = null;
            lastToolUsed = null;
            lastToolCollected = null;
            return;
        }

        if (scanner.hasNextLine()) {
            int lastPlayer = Integer.parseInt(scanner.nextLine().trim());
            lastPlayerId = (lastPlayer < 0 ? null : lastPlayer);
        } else {
            lastPlayerId = null;
        }

        if (scanner.hasNextLine()) {
            lastFromPosition = Integer.parseInt(scanner.nextLine().trim());
        } else {
            lastFromPosition = 0;
        }

        if (scanner.hasNextLine()) {
            lastToPosition = Integer.parseInt(scanner.nextLine().trim());
        } else {
            lastToPosition = 0;
        }

        lastAbyss = null;
        lastToolUsed = null;
        lastToolCollected = null;
    }

    private void ensureRandom() {
        if (random == null) {
            random = new Random();
        }
    }
}