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

    // IDs válidos (enunciado + criatividade)
    private static final int MIN_ABYSS_ID = 0;
    private static final int MAX_ABYSS_ID = 9;
    private static final int MIN_TOOL_ID = 0;
    private static final int MAX_TOOL_ID = 5;

    private ArrayList<Programmer> programmers;
    private HashMap<Integer, Programmer> idToProgrammer;
    private int boardSize;
    private ArrayList<Integer> turnOrderIds;
    private int turnCursor;
    private boolean gameOver;
    private Integer winnerId;
    private int turnCount;

    // Abysses e Tools por posição
    private HashMap<Integer, Abyss> abyssesByPosition;
    private HashMap<Integer, Tool> toolsByPosition;

    // Info da última jogada (útil para a GUI)
    private int lastDiceValue = 0;
    private Integer lastPlayerId = null;
    private int lastFromPosition = 0;
    private int lastToPosition = 0;

    private Abyss lastAbyss = null;
    private Tool lastToolUsed = null;
    private Tool lastToolCollected = null;

    // Indica se há uma jogada pendente de reação
    private boolean pendingReaction = false;

    public GameManager() {
        this.programmers = new ArrayList<>();
        this.idToProgrammer = new HashMap<>();
        this.abyssesByPosition = new HashMap<>();
        this.toolsByPosition = new HashMap<>();
        this.turnOrderIds = new ArrayList<>();
        this.turnCursor = 0;
        this.gameOver = false;
        this.winnerId = null;
        this.turnCount = 0;
        this.pendingReaction = false;
    }

    // Parte 1: versão sem configuração de Abysses/Tools
    public boolean createInitialBoard(String[][] playerInfo, int boardSize) {
        return createInitialBoard(playerInfo, boardSize, null);
    }

    // Parte 2: versão completa com AbyssesAndTools
    public boolean createInitialBoard(String[][] playerInfo,
                                      int boardSize,
                                      String[][] abyssesAndTools) {
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

        // Validar configuração de abismos/ferramentas (se existir)
        if (abyssesAndTools != null) {
            if (!validateAbyssesAndTools(abyssesAndTools, boardSize)) {
                return false;
            }
        }

        // Reset do jogo
        this.programmers.clear();
        this.idToProgrammer.clear();
        this.abyssesByPosition.clear();
        this.toolsByPosition.clear();

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

        this.lastDiceValue = 0;
        this.lastPlayerId = null;
        this.lastFromPosition = 1;
        this.lastToPosition = 1;

        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        this.pendingReaction = false;

        // Criar abismos/ferramentas em posições fixas do array
        if (abyssesAndTools != null) {
            for (String[] row : abyssesAndTools) {
                // validação já foi feita em validateAbyssesAndTools()
                int type = Integer.parseInt(row[0]);
                int id = Integer.parseInt(row[1]);
                int pos = Integer.parseInt(row[2]);

                if (type == 0) {
                    Abyss abyss = createAbyss(id, pos);
                    if (abyss != null) {
                        abyssesByPosition.put(pos, abyss);
                    }
                } else {
                    Tool tool = createTool(id, pos);
                    if (tool != null) {
                        toolsByPosition.put(pos, tool);
                    }
                }
            }
        }

        return true;
    }

    private boolean validatePlayerRow(String[] row,
                                      HashSet<Integer> seenIds,
                                      HashSet<String> usedColors) {
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
        if (color == null) {
            return false;
        }
        if (!VALID_COLORS.contains(color)) {
            return false;
        }
        if (usedColors.contains(color)) {
            return false;
        }
        usedColors.add(color);

        return true;
    }

    private boolean validateAbyssesAndTools(String[][] abyssesAndTools, int boardSize) {
        HashSet<Integer> usedPositions = new HashSet<>();
        usedPositions.add(1);
        usedPositions.add(boardSize);

        for (String[] row : abyssesAndTools) {
            if (row == null) {
                continue;
            }

            // Se não tiver 3 colunas, ignorar (sem posição não dá para colocar)
            if (row.length < 3) {
                continue;
            }

            int type;
            int subId;
            int pos;

            try {
                type = Integer.parseInt(row[0]);
                subId = Integer.parseInt(row[1]);
                pos = Integer.parseInt(row[2]);
            } catch (NumberFormatException e) {
                return false;
            }

            if (type != 0 && type != 1) {
                return false;
            }

            if (pos <= 1 || pos >= boardSize) {
                return false;
            }

            if (usedPositions.contains(pos)) {
                // Não pode haver tool e abismo na mesma casa, nem duplicados
                return false;
            }

            if (type == 0) {
                if (!isValidAbyssId(subId)) {
                    return false;
                }
            } else {
                if (!isValidToolId(subId)) {
                    return false;
                }
            }

            usedPositions.add(pos);
        }

        return true;
    }

    private boolean isValidAbyssId(int id) {
        return id >= MIN_ABYSS_ID && id <= MAX_ABYSS_ID;
    }

    private boolean isValidToolId(int id) {
        return id >= MIN_TOOL_ID && id <= MAX_TOOL_ID;
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
        Tool tool = toolsByPosition.get(position);

        if (abyss == null && tool == null) {
            return new String[]{programmersStr, "", ""};
        }

        if (abyss != null) {
            String name = getCanonicalAbyssName(abyss.getId());
            return new String[]{programmersStr, name, "A:" + abyss.getId()};
        }

        String toolName = tool.getName();
        return new String[]{programmersStr, toolName, "T:" + tool.getId()};
    }

    public int getCurrentPlayerID() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return -1;
        }
        return turnOrderIds.get(turnCursor);
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
     * Move o jogador atual.
     * - Não avança o turno (isso acontece em reactToAbyssOrTool()).
     * - Jogador preso/derrotado não se move (retorna false), mas marca pendingReaction.
     * - Restrições por linguagem:
     *   * Assembly: só pode mover 1 ou 2
     *   * C: só pode mover até 3
     *   * Outras: sem restrições
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

        int currentId = getCurrentPlayerID();
        Programmer current = idToProgrammer.get(currentId);
        if (current == null) {
            return false;
        }

        // Preparar info base (mesmo que o movimento seja inválido)
        this.lastPlayerId = currentId;
        this.lastDiceValue = nrPositions;
        this.lastFromPosition = current.getPosition();
        this.lastToPosition = current.getPosition();
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        // Se está derrotado, não faz nada mas o turno conta (react avança)
        if (current.isDefeated()) {
            this.pendingReaction = true;
            return false;
        }

        // Se está preso, não se move mas o turno conta (react avança)
        if (current.isTrapped()) {
            this.pendingReaction = true;
            return false;
        }

        String firstLang = current.getFirstLanguage();
        if (firstLang != null) {
            if (firstLang.equalsIgnoreCase("Assembly")) {
                if (nrPositions > 2) {
                    this.pendingReaction = true;
                    return false;
                }
            }
            if (firstLang.equalsIgnoreCase("C")) {
                if (nrPositions > 3) {
                    this.pendingReaction = true;
                    return false;
                }
            }
        }

        int from = current.getPosition();
        int to = calculateNewPosition(from, nrPositions);
        this.lastFromPosition = from;
        this.lastToPosition = to;

        current.recordMove(to);

        this.pendingReaction = true;
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

    /**
     * Processa a reação a abismos e ferramentas após o movimento.
     * Ordem: Ferramenta primeiro, depois Abismo.
     * NOTA: As ferramentas permanecem no tabuleiro (outros jogadores também podem apanhar).
     */
    public String reactToAbyssOrTool() {
        if (!pendingReaction) {
            return null;
        }

        if (gameOver) {
            pendingReaction = false;
            return null;
        }

        if (lastPlayerId == null) {
            pendingReaction = false;
            return null;
        }

        Programmer current = idToProgrammer.get(lastPlayerId);
        if (current == null) {
            pendingReaction = false;
            return null;
        }

        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        String message = null;

        int currentPos = current.getPosition();

        // 1) Ferramenta
        Tool tool = toolsByPosition.get(currentPos);
        if (tool != null) {
            if (current.hasToolOfType(tool.getId())) {
                message = "Já possui a ferramenta: " + tool.getName();
            } else {
                // ferramenta permanece no tabuleiro, por isso criamos uma cópia para o inventário
                Tool toolToAdd = createTool(tool.getId(), 0);
                if (toolToAdd != null) {
                    current.addTool(toolToAdd);
                    this.lastToolCollected = toolToAdd;
                }
                message = "Recolheu ferramenta: " + tool.getName();
            }
        }

        // 2) Abismo
        Abyss abyss = abyssesByPosition.get(currentPos);
        if (abyss != null) {
            this.lastAbyss = abyss;

            // SegFault: só ativa com 2+ jogadores na mesma casa
            if (abyss.getId() == SegmentationFaultAbyss.ID) {
                boolean triggered = applySegmentationFaultIfTriggered(currentPos);
                if (triggered) {
                    message = "Segmentation Fault!";
                } else {
                    // 1 jogador apenas: sem efeito
                    if (message == null) {
                        message = null;
                    }
                }
            } else {
                Tool cancellingTool = current.findToolToCancelAbyss(abyss.getId());
                if (cancellingTool != null) {
                    current.removeTool(cancellingTool);
                    this.lastToolUsed = cancellingTool;

                    // se estava preso por ciclo infinito e anulou, volta a jogar
                    if (abyss.getId() == InfiniteLoopAbyss.ID) {
                        if (current.isTrapped()) {
                            current.setState("Em Jogo");
                        }
                    }

                    message = getCanonicalAbyssName(abyss.getId()) + " anulado por " + cancellingTool.getName();
                } else {
                    abyss.applyEffect(current, lastDiceValue, lastFromPosition);
                    message = getCanonicalAbyssName(abyss.getId()) + "!";
                }
            }
        }

        // Atualizar contador de turnos
        turnCount++;

        // Verificar fim do jogo por chegada ao fim
        if (current.getPosition() == boardSize && current.isPlaying()) {
            gameOver = true;
            winnerId = current.getId();
        }

        // Verificar fim do jogo por eliminação
        if (!gameOver) {
            checkVictoryByElimination();
        }

        // Avançar turno (se não tiver repetição)
        if (!gameOver) {
            boolean repeatTurn = false;
            if (abyss != null) {
                repeatTurn = abyss.forcesRepeatTurn();
            }

            if (!repeatTurn) {
                advanceTurnCursorSkippingDefeated();
            }
        }

        pendingReaction = false;

        return message;
    }

    private void advanceTurnCursorSkippingDefeated() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return;
        }

        int tries = 0;
        do {
            turnCursor = (turnCursor + 1) % turnOrderIds.size();
            int id = turnOrderIds.get(turnCursor);
            Programmer p = idToProgrammer.get(id);
            if (p != null && !p.isDefeated()) {
                return;
            }
            tries++;
        } while (tries < turnOrderIds.size());
    }

    /**
     * Vitória por eliminação: só um programador não derrotado.
     */
    private void checkVictoryByElimination() {
        int activeCount = 0;
        Integer lastActiveId = null;

        for (Programmer p : programmers) {
            if (!p.isDefeated()) {
                activeCount++;
                lastActiveId = p.getId();
            }
        }

        if (activeCount == 1 && lastActiveId != null) {
            gameOver = true;
            winnerId = lastActiveId;
        }

        if (activeCount == 0) {
            gameOver = true;
            winnerId = null;
        }
    }

    /**
     * Aplica o Segmentation Fault se houver 2+ jogadores na casa que contém esse abismo.
     * Quando ativa, TODOS os jogadores na casa recuam 3 casas.
     * Pode gerar cadeia (se voltarem a cair num SegFault com 2+ jogadores).
     */
    private boolean applySegmentationFaultIfTriggered(int positionWithSegFault) {
        List<Programmer> playersHere = getAlivePlayersAt(positionWithSegFault);
        if (playersHere.size() < 2) {
            return false;
        }

        // Cadeia: enquanto existir uma casa com SegFault e 2+ jogadores
        int safety = 0;
        int currentSegPos = positionWithSegFault;

        while (safety < boardSize) {
            safety++;

            Abyss abyssHere = abyssesByPosition.get(currentSegPos);
            if (abyssHere == null || abyssHere.getId() != SegmentationFaultAbyss.ID) {
                break;
            }

            List<Programmer> group = getAlivePlayersAt(currentSegPos);
            if (group.size() < 2) {
                break;
            }

            // recuam todos 3
            for (Programmer p : group) {
                int newPos = Math.max(1, p.getPosition() - SegmentationFaultAbyss.RETREAT_POSITIONS);
                p.setPosition(newPos);
            }

            // aplicar efeitos da nova casa (tool -> abyss), em cadeia, para cada um
            for (Programmer p : group) {
                applySquareEffectsChain(p, 0);
            }

            // procurar se ficou novamente 2+ numa casa SegFault
            Integer nextSegPos = findSegFaultPositionWithTwoOrMore();
            if (nextSegPos == null) {
                break;
            }
            currentSegPos = nextSegPos;
        }

        return true;
    }

    private List<Programmer> getAlivePlayersAt(int position) {
        List<Programmer> out = new ArrayList<>();
        for (Programmer p : programmers) {
            if (!p.isDefeated() && p.getPosition() == position) {
                out.add(p);
            }
        }
        return out;
    }

    private Integer findSegFaultPositionWithTwoOrMore() {
        for (Map.Entry<Integer, Abyss> entry : abyssesByPosition.entrySet()) {
            Abyss abyss = entry.getValue();
            if (abyss != null && abyss.getId() == SegmentationFaultAbyss.ID) {
                int pos = entry.getKey();
                if (getAlivePlayersAt(pos).size() >= 2) {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Aplica efeitos da casa atual (tool -> abyss), podendo repetir em cadeia.
     * Não usa instanceof.
     * @param depth limite de segurança
     */
    private void applySquareEffectsChain(Programmer programmer, int depth) {
        if (programmer == null) {
            return;
        }
        if (programmer.isDefeated()) {
            return;
        }
        if (depth > boardSize) {
            return;
        }

        int pos = programmer.getPosition();

        // tool
        Tool tool = toolsByPosition.get(pos);
        if (tool != null) {
            if (!programmer.hasToolOfType(tool.getId())) {
                Tool toolToAdd = createTool(tool.getId(), 0);
                if (toolToAdd != null) {
                    programmer.addTool(toolToAdd);
                }
            }
        }

        // abyss
        Abyss abyss = abyssesByPosition.get(pos);
        if (abyss == null) {
            return;
        }

        // SegFault nesta fase: só ativa se 2+
        if (abyss.getId() == SegmentationFaultAbyss.ID) {
            boolean triggered = applySegmentationFaultIfTriggered(pos);
            if (triggered) {
                // após recuo, a posição pode mudar; continuar cadeia
                applySquareEffectsChain(programmer, depth + 1);
            }
            return;
        }

        Tool cancellingTool = programmer.findToolToCancelAbyss(abyss.getId());
        if (cancellingTool != null) {
            programmer.removeTool(cancellingTool);
            if (abyss.getId() == InfiniteLoopAbyss.ID) {
                if (programmer.isTrapped()) {
                    programmer.setState("Em Jogo");
                }
            }
        } else {
            abyss.applyEffect(programmer, 0, pos);
        }

        // Se ficou preso, não há mais cadeia automática (fica para próximo turno)
        if (programmer.isTrapped()) {
            return;
        }

        // Se foi derrotado, termina
        if (programmer.isDefeated()) {
            return;
        }

        // caso tenha movido posição por efeito do abismo, pode haver mais efeitos
        if (programmer.getPosition() != pos) {
            applySquareEffectsChain(programmer, depth + 1);
        }
    }

    public boolean gameIsOver() {
        if (gameOver) {
            return true;
        }

        // Chegada ao fim
        for (Programmer programmer : programmers) {
            if (programmer.getPosition() == boardSize && programmer.isPlaying()) {
                gameOver = true;
                if (winnerId == null) {
                    winnerId = programmer.getId();
                }
                return true;
            }
        }

        checkVictoryByElimination();
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

    // Fábrica de Abyss
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

    // Fábrica de Tools
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

    private String getCanonicalAbyssName(int abyssId) {
        switch (abyssId) {
            case 0: {
                return "Erro de Sintaxe";
            }
            case 1: {
                return "Erro de Lógica";
            }
            case 2: {
                return "Exception";
            }
            case 3: {
                return "FileNotFoundException";
            }
            case 4: {
                return "Crash";
            }
            case 5: {
                return "Código Duplicado";
            }
            case 6: {
                return "Efeitos Secundários";
            }
            case 7: {
                return "Blue Screen of Death";
            }
            case 8: {
                return "Ciclo Infinito";
            }
            case 9: {
                return "Segmentation Fault";
            }
            default: {
                return "";
            }
        }
    }

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
                                toolIds.toString()
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
            this.pendingReaction = false;
        } catch (InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFileException("Formato de ficheiro inválido");
        }
    }

    private void validateLoadFile(File file) throws FileNotFoundException {
        if (file == null) {
            throw new FileNotFoundException("Ficheiro não encontrado");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("Ficheiro não encontrado");
        }
        if (!file.isFile()) {
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
                    int toolId = Integer.parseInt(toolIdStr.trim());
                    Tool tool = createTool(toolId, 0);
                    if (tool != null) {
                        p.addTool(tool);
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

        lastAbyss = null;
        lastToolUsed = null;
        lastToolCollected = null;
    }
}
