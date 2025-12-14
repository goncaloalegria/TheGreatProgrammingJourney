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

    // TurnCount começa em 1
    private int turnCount;

    // Pode haver tool+abyss na mesma casa
    private HashMap<Integer, Abyss> abyssesByPosition;
    private HashMap<Integer, Tool> toolsByPosition;

    private Random random;

    // --- Info da última jogada ---
    private int lastDiceValue = 0;
    private Integer lastPlayerId = null;
    private int lastFromPosition = 0;
    private int lastToPosition = 0;

    private Abyss lastAbyss = null;
    private Tool lastToolUsed = null;
    private Tool lastToolCollected = null;

    // “Só pode lançar o dado 1 vez por turno”: depois de mover, fica à espera do react.
    private boolean pendingReaction = false;

    private static final int PENDING_REASON_NONE = 0;
    private static final int PENDING_REASON_TRAPPED = 1;
    private static final int PENDING_REASON_INVALID_LANGUAGE_MOVE = 2;

    private int pendingReason = PENDING_REASON_NONE;

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

        HashSet<Integer> seenIds = new HashSet<>();
        HashSet<String> usedColors = new HashSet<>();
        for (String[] row : playerInfo) {
            if (!validatePlayerRow(row, seenIds, usedColors)) {
                return false;
            }
        }

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

        this.abyssesByPosition.clear();
        this.toolsByPosition.clear();

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

    /**
     * Regras importantes:
     * - Linhas com <3 colunas (sem posição) são ignoradas.
     * - Pode haver Tool + Abyss na mesma casa.
     * - Não pode haver 2 Tools na mesma casa.
     * - Não pode haver 2 Abysses na mesma casa.
     */
    private boolean validateAbyssesAndToolsConfig(String[][] cfg, int boardSize) {
        HashSet<Integer> abyssSlots = new HashSet<>();
        HashSet<Integer> toolSlots = new HashSet<>();

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

            if (tipo == 0) {
                if (abyssSlots.contains(pos)) {
                    return false;
                }
                abyssSlots.add(pos);
            } else {
                if (toolSlots.contains(pos)) {
                    return false;
                }
                toolSlots.add(pos);
            }
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
            }

            if (tipo == 1) {
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

    public String getProgrammersInfo() {
        if (programmers == null || programmers.isEmpty()) {
            return "";
        }

        ArrayList<Programmer> ordered = new ArrayList<>(programmers);
        ordered.sort(Comparator.comparingInt(Programmer::getId));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            Programmer p = ordered.get(i);
            sb.append(p.getName()).append(" : ").append(p.getToolsInfo());
            if (i < ordered.size() - 1) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }

    /**
     * getSlotInfo:
     * [0] ids separados por ','
     * [1] nome do elemento
     * [2] tipo "T:id" ou "A:id"
     *
     * Se existirem os dois, mostra o ABISMO.
     */
    public String[] getSlotInfo(int position) {
        if (position < 1 || position > boardSize) {
            return null;
        }

        String programmersStr = "";
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
        Programmer p = idToProgrammer.get(getCurrentPlayerID());
        if (p == null) {
            return null;
        }
        return p.getInfoAsArray();
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
     * BounceBack alterado COMO PEDISTE:
     * Se ultrapassar a meta, o movimento é inválido, não move e o turno não avança.
     *
     * Regras extra:
     * - Só pode lançar o dado 1 vez por turno: se já houve move válido (pendingReaction), bloqueia.
     * - Assembly max 2; C max 3; (C# não é C)
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

        if (pendingReaction) {
            return false;
        }

        ensureTurnCursorValid();

        if (turnOrderIds.isEmpty()) {
            return false;
        }

        int currentId = getCurrentPlayerID();
        Programmer current = idToProgrammer.get(currentId);
        if (current == null) {
            return false;
        }

        // Se estiver preso, não move mas consome a jogada (react vai avançar turno e devolver mensagem)
        if (current.isTrapped()) {
            lastDiceValue = nrPositions;
            lastPlayerId = currentId;
            lastFromPosition = current.getPosition();
            lastToPosition = current.getPosition();

            lastAbyss = null;
            lastToolUsed = null;
            lastToolCollected = null;

            pendingReaction = true;
            pendingReason = PENDING_REASON_TRAPPED;

            return false;
        }

        // Restrições de linguagem (movimento inválido mas consome a jogada)
        String firstLang = current.getFirstLanguage();
        if (firstLang != null) {
            if (firstLang.equalsIgnoreCase("Assembly") && nrPositions > 2) {
                lastDiceValue = nrPositions;
                lastPlayerId = currentId;
                lastFromPosition = current.getPosition();
                lastToPosition = current.getPosition();

                lastAbyss = null;
                lastToolUsed = null;
                lastToolCollected = null;

                pendingReaction = true;
                pendingReason = PENDING_REASON_INVALID_LANGUAGE_MOVE;

                return false;
            }

            if (firstLang.equalsIgnoreCase("C") && nrPositions > 3) {
                lastDiceValue = nrPositions;
                lastPlayerId = currentId;
                lastFromPosition = current.getPosition();
                lastToPosition = current.getPosition();

                lastAbyss = null;
                lastToolUsed = null;
                lastToolCollected = null;

                pendingReaction = true;
                pendingReason = PENDING_REASON_INVALID_LANGUAGE_MOVE;

                return false;
            }
        }

        int from = current.getPosition();
        int toCandidate = from + nrPositions;

        // BounceBack pedido: ultrapassou meta => inválido, não move e turno não avança
        if (toCandidate > boardSize) {
            lastDiceValue = nrPositions;
            lastPlayerId = currentId;
            lastFromPosition = from;
            lastToPosition = from;

            lastAbyss = null;
            lastToolUsed = null;
            lastToolCollected = null;

            pendingReaction = false;
            pendingReason = PENDING_REASON_NONE;

            return false;
        }

        // Movimento válido
        lastDiceValue = nrPositions;
        lastPlayerId = currentId;

        lastFromPosition = from;
        lastToPosition = toCandidate;

        lastAbyss = null;
        lastToolUsed = null;
        lastToolCollected = null;

        current.recordMove(toCandidate);

        pendingReaction = true;
        pendingReason = PENDING_REASON_NONE;

        return true;
    }

    /**
     * Tool primeiro, depois Abyss.
     * - Tool fica no tabuleiro.
     * - Se Abyss for anulado, Tool usada é consumida do inventário.
     * - BSOD derrota só o jogador que cai e remove-o da ordem.
     */
    public String reactToAbyssOrTool() {
        if (!pendingReaction) {
            return null;
        }
        if (gameOver) {
            pendingReaction = false;
            pendingReason = PENDING_REASON_NONE;
            return null;
        }
        if (lastPlayerId == null) {
            pendingReaction = false;
            pendingReason = PENDING_REASON_NONE;
            return null;
        }

        // Garante que o cursor ainda aponta para um jogador existente
        ensureTurnCursorValid();

        Programmer current = idToProgrammer.get(lastPlayerId);
        if (current == null) {
            pendingReaction = false;
            pendingReason = PENDING_REASON_NONE;
            return null;
        }

        // Contabiliza turno em TODA a chamada ao react
        turnCount++;

        // Preso: perde a vez, devolve mensagem e passa ao próximo
        if (pendingReason == PENDING_REASON_TRAPPED) {
            pendingReaction = false;
            pendingReason = PENDING_REASON_NONE;

            advanceTurnCursor();
            ensureTurnCursorValid();

            return "Ciclo Infinito!";
        }

        // Movimento inválido por linguagem: consome a vez mas sem evento
        if (pendingReason == PENDING_REASON_INVALID_LANGUAGE_MOVE) {
            pendingReaction = false;
            pendingReason = PENDING_REASON_NONE;

            advanceTurnCursor();
            ensureTurnCursorValid();

            return null;
        }

        pendingReason = PENDING_REASON_NONE;

        lastAbyss = null;
        lastToolUsed = null;
        lastToolCollected = null;

        int posBeforeAbyss = current.getPosition();

        // 1) Tool primeiro
        String toolMsg = null;
        Tool boardTool = toolsByPosition.get(posBeforeAbyss);
        if (boardTool != null) {
            if (!current.hasToolOfType(boardTool.getId())) {
                current.addTool(boardTool);
                lastToolCollected = boardTool;
                toolMsg = "Recolheu ferramenta: " + boardTool.getName();
            } else {
                toolMsg = "Já possui a ferramenta: " + boardTool.getName();
            }
        }

        // 2) Abyss
        String abyssMsg = null;
        Abyss abyss = abyssesByPosition.get(posBeforeAbyss);
        if (abyss != null) {
            lastAbyss = abyss;

            if (abyss.getId() == SegmentationFaultAbyss.ID) {
                List<Programmer> here = getAlivePlayersAt(posBeforeAbyss);
                if (here.size() >= 2) {
                    applySegFaultChain();
                    abyssMsg = abyss.getName() + "!";
                } else {
                    abyssMsg = null;
                }
            } else {
                Tool canceller = current.findToolToCancelAbyss(abyss.getId());
                if (canceller != null) {
                    current.removeTool(canceller);
                    lastToolUsed = canceller;
                    abyssMsg = abyss.getName() + " anulado por " + canceller.getName();
                } else {
                    abyss.applyEffect(current, lastDiceValue, lastFromPosition);
                    lastToPosition = current.getPosition();
                    abyssMsg = abyss.getName() + "!";
                }

                // Se morreu (BSOD ou outros), remove da ordem
                if (current.isDefeated()) {
                    removeFromTurnOrder(current.getId());
                }
            }
        }

        // Vitória por chegada ao fim
        if (!gameOver) {
            if (current.getPosition() == boardSize && current.isPlaying()) {
                gameOver = true;
                winnerId = current.getId();
            }
        }

        // Vitória por eliminação
        if (!gameOver) {
            checkEliminationWin();
        }

        pendingReaction = false;

        if (!gameOver) {
            advanceTurnCursor();
            ensureTurnCursorValid();
        }

        if (abyssMsg != null) {
            return abyssMsg;
        }
        return toolMsg;
    }

    private void applySegFaultChain() {
        boolean changed = true;

        while (changed) {
            changed = false;

            ArrayList<Integer> segSlots = new ArrayList<>();
            for (Map.Entry<Integer, Abyss> entry : abyssesByPosition.entrySet()) {
                if (entry.getValue() != null && entry.getValue().getId() == SegmentationFaultAbyss.ID) {
                    segSlots.add(entry.getKey());
                }
            }

            for (Integer pos : segSlots) {
                List<Programmer> here = getAlivePlayersAt(pos);
                if (here.size() >= 2) {
                    for (Programmer p : here) {
                        int newPos = Math.max(1, p.getPosition() - SegmentationFaultAbyss.RETREAT_POSITIONS);
                        p.setPosition(newPos);
                    }

                    for (Programmer p : here) {
                        applyLandingEffectsAfterForcedMove(p);
                    }

                    changed = true;
                }
            }
        }
    }

    private void applyLandingEffectsAfterForcedMove(Programmer p) {
        if (p == null) {
            return;
        }
        if (p.isDefeated()) {
            return;
        }

        int pos = p.getPosition();

        Tool tool = toolsByPosition.get(pos);
        if (tool != null) {
            if (!p.hasToolOfType(tool.getId())) {
                p.addTool(tool);
            }
        }

        Abyss abyss = abyssesByPosition.get(pos);
        if (abyss == null) {
            return;
        }

        if (abyss.getId() == SegmentationFaultAbyss.ID) {
            return;
        }

        Tool canceller = p.findToolToCancelAbyss(abyss.getId());
        if (canceller != null) {
            p.removeTool(canceller);
            return;
        }

        abyss.applyEffect(p, 0, pos);

        if (p.isDefeated()) {
            removeFromTurnOrder(p.getId());
        }
    }

    private List<Programmer> getAlivePlayersAt(int position) {
        ArrayList<Programmer> out = new ArrayList<>();
        for (Programmer p : programmers) {
            if (p != null) {
                if (!p.isDefeated()) {
                    if (p.getPosition() == position) {
                        out.add(p);
                    }
                }
            }
        }
        return out;
    }

    private void removeFromTurnOrder(int id) {
        if (turnOrderIds == null) {
            return;
        }
        if (turnOrderIds.isEmpty()) {
            return;
        }

        int idx = turnOrderIds.indexOf(id);
        if (idx < 0) {
            return;
        }

        turnOrderIds.remove(idx);

        if (turnOrderIds.isEmpty()) {
            return;
        }

        if (turnCursor >= turnOrderIds.size()) {
            turnCursor = 0;
        }
    }

    private void advanceTurnCursor() {
        if (turnOrderIds == null) {
            return;
        }
        if (turnOrderIds.isEmpty()) {
            return;
        }
        turnCursor = (turnCursor + 1) % turnOrderIds.size();
    }

    private void ensureTurnCursorValid() {
        if (turnOrderIds == null) {
            return;
        }
        if (turnOrderIds.isEmpty()) {
            return;
        }

        boolean removedSomething = true;
        while (removedSomething) {
            removedSomething = false;

            if (turnOrderIds.isEmpty()) {
                return;
            }

            int id = turnOrderIds.get(turnCursor);
            Programmer p = idToProgrammer.get(id);
            if (p != null) {
                if (p.isDefeated()) {
                    turnOrderIds.remove(turnCursor);
                    removedSomething = true;

                    if (!turnOrderIds.isEmpty()) {
                        if (turnCursor >= turnOrderIds.size()) {
                            turnCursor = 0;
                        }
                    }
                }
            }
        }
    }

    private void checkEliminationWin() {
        int alive = 0;
        Integer lastAlive = null;

        for (Programmer p : programmers) {
            if (p != null) {
                if (!p.isDefeated()) {
                    alive++;
                    lastAlive = p.getId();
                }
            }
        }

        if (alive == 1 && lastAlive != null) {
            gameOver = true;
            winnerId = lastAlive;
        }

        if (alive == 0) {
            gameOver = true;
            winnerId = null;
        }
    }

    public boolean gameIsOver() {
        if (gameOver) {
            return true;
        }

        for (Programmer programmer : programmers) {
            if (programmer != null) {
                if (programmer.getPosition() == boardSize && programmer.isPlaying()) {
                    gameOver = true;
                    winnerId = programmer.getId();
                    return true;
                }
            }
        }

        checkEliminationWin();
        return gameOver;
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
            if (programmer != null) {
                if (winnerId == null || programmer.getId() != winnerId) {
                    remaining.add(programmer);
                }
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

            out.println(pendingReaction);
            out.println(pendingReason);

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
            loadPendingInfo(scanner);
            ensureRandom();
        } catch (InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFileException("Formato de ficheiro inválido");
        }
    }

    private void validateLoadFile(File file) throws FileNotFoundException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileNotFoundException("Ficheiro não encontrado");
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
                throw new InvalidFileException("Linha de programador inválida: " + line);
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
                throw new InvalidFileException("Linha de abismo inválida: " + line);
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
                throw new InvalidFileException("Linha de ferramenta inválida: " + line);
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
    }

    private void loadPendingInfo(Scanner scanner) {
        if (scanner.hasNextLine()) {
            pendingReaction = Boolean.parseBoolean(scanner.nextLine().trim());
        } else {
            pendingReaction = false;
        }

        if (scanner.hasNextLine()) {
            pendingReason = Integer.parseInt(scanner.nextLine().trim());
        } else {
            pendingReason = PENDING_REASON_NONE;
        }
    }

    private void ensureRandom() {
        if (random == null) {
            random = new Random();
        }
    }
}
