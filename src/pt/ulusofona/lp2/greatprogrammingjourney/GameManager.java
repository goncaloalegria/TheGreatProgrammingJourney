package pt.ulusofona.lp2.greatprogrammingjourney;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

public class GameManager {

    private static final Set<String> VALID_COLORS =
            new HashSet<>(Arrays.asList("Purple", "Green", "Brown", "Blue"));
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

    // Abysses e Tools por posição
    private HashMap<Integer, Abyss> abyssesByPosition;
    private HashMap<Integer, Tool> toolsByPosition;

    // Para posições aleatórias
    private Random random;

    // Info da última jogada (útil para a GUI)
    private int lastDiceValue = 0;
    private Integer lastPlayerId = null;
    private int lastFromPosition = 0;
    private int lastToPosition = 0;
    private Abyss lastAbyss = null;
    private Tool lastToolUsed = null;       // Ferramenta usada para anular abismo
    private Tool lastToolCollected = null;  // Ferramenta recolhida

    // Indica se há uma jogada pendente de reação
    private boolean pendingReaction = false;

    public GameManager() {
        this.programmers = new ArrayList<>();
        this.idToProgrammer = new HashMap<>();
        this.abyssesByPosition = new HashMap<>();
        this.toolsByPosition = new HashMap<>();
        this.random = new Random();
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
            if (!validateAbyssesAndToolsConfig(abyssesAndTools, boardSize)) {
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

        this.lastDiceValue = 0;
        this.lastPlayerId = null;
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;
        this.lastFromPosition = 0;
        this.lastToPosition = 0;
        this.pendingReaction = false;

        // Processar AbyssesAndTools
        abyssesByPosition.clear();
        toolsByPosition.clear();

        // IMPORTANTE:
        // Pode existir Tool + Abyss na mesma casa.
        // O que NÃO pode: 2 Abysses na mesma casa, ou 2 Tools na mesma casa.
        HashSet<Integer> usedAbyssSlots = new HashSet<>();
        HashSet<Integer> usedToolSlots = new HashSet<>();

        usedAbyssSlots.add(1);
        usedAbyssSlots.add(boardSize);

        usedToolSlots.add(1);
        usedToolSlots.add(boardSize);

        if (abyssesAndTools != null) {
            for (String[] row : abyssesAndTools) {
                int tipo;
                int id;
                Integer posConfig;

                try {
                    tipo = Integer.parseInt(row[0].trim());
                    id = Integer.parseInt(row[1].trim());
                    posConfig = parseOptionalPosition(row);
                } catch (Exception e) {
                    return false;
                }

                if (tipo == 0) {
                    int pos = resolvePositionForType(posConfig, usedAbyssSlots);
                    Abyss abyss = createAbyss(id, pos);
                    if (abyss == null) {
                        return false;
                    }
                    addAbyss(abyss);
                } else if (tipo == 1) {
                    int pos = resolvePositionForType(posConfig, usedToolSlots);
                    Tool tool = createTool(id, pos);
                    if (tool == null) {
                        return false;
                    }
                    addTool(tool);
                } else {
                    return false;
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
        if (color == null || !VALID_COLORS.contains(color) || usedColors.contains(color)) {
            return false;
        }
        usedColors.add(color);

        return true;
    }

    private boolean validateAbyssesAndToolsConfig(String[][] abyssesAndTools, int boardSize) {
        HashSet<Integer> abyssPositions = new HashSet<>();
        HashSet<Integer> toolPositions = new HashSet<>();

        for (String[] row : abyssesAndTools) {
            if (row == null || row.length < 2) {
                return false;
            }

            int tipo;
            int id;

            try {
                tipo = Integer.parseInt(row[0].trim());
                id = Integer.parseInt(row[1].trim());
            } catch (Exception e) {
                return false;
            }

            if (tipo != 0 && tipo != 1) {
                return false;
            }

            // ranges esperados (A: 0..9, T: 0..5)
            if (tipo == 0) {
                if (id < 0 || id > 9) {
                    return false;
                }
            } else {
                if (id < 0 || id > 5) {
                    return false;
                }
            }

            // validar posição se existir
            if (row.length >= 3 && row[2] != null && !row[2].trim().isEmpty()) {
                int pos;
                try {
                    pos = Integer.parseInt(row[2].trim());
                } catch (Exception e) {
                    return false;
                }

                if (pos <= 1 || pos >= boardSize) {
                    return false;
                }

                if (tipo == 0) {
                    if (abyssPositions.contains(pos)) {
                        return false;
                    }
                    abyssPositions.add(pos);
                } else {
                    if (toolPositions.contains(pos)) {
                        return false;
                    }
                    toolPositions.add(pos);
                }
            }
        }

        return true;
    }

    private Integer parseOptionalPosition(String[] row) {
        if (row == null) {
            return null;
        }
        if (row.length < 3) {
            return null;
        }
        if (row[2] == null) {
            return null;
        }
        String s = row[2].trim();
        if (s.isEmpty()) {
            return null;
        }
        return Integer.parseInt(s);
    }

    // resolve posição: se vier no config, tem de estar livre para o tipo; se não vier, escolhe a primeira livre
    private int resolvePositionForType(Integer posFromConfig, Set<Integer> usedSlots) {
        if (posFromConfig != null) {
            if (posFromConfig <= 1 || posFromConfig >= boardSize) {
                throw new IllegalStateException("Posição inválida");
            }
            if (usedSlots.contains(posFromConfig)) {
                throw new IllegalStateException("Posição repetida para este tipo");
            }
            usedSlots.add(posFromConfig);
            return posFromConfig;
        }

        // determinístico: primeira casa livre (permite overlap com o outro tipo)
        for (int pos = 2; pos < boardSize; pos++) {
            if (!usedSlots.contains(pos)) {
                usedSlots.add(pos);
                return pos;
            }
        }

        throw new IllegalStateException("Não há casas livres suficientes");
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

        // PRIORIDADE À TOOL se houver Tool + Abyss na mesma casa
        if (tool != null) {
            return new String[]{
                    programmersStr,
                    tool.getName(),
                    "T:" + tool.getId()
            };
        }

        return new String[]{
                programmersStr,
                abyss.getName(),
                "A:" + abyss.getId()
        };
    }

    public int getCurrentPlayerID() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return -1;
        }
        return turnOrderIds.get(turnCursor);
    }

    public String getCurrentPlayerName() {
        int id = getCurrentPlayerID();
        Programmer p = idToProgrammer.get(id);
        if (p == null) {
            return "";
        }
        return p.getName();
    }

    public String[] getCurrentPlayerInfo() {
        int id = getCurrentPlayerID();
        Programmer p = idToProgrammer.get(id);
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

    // parsing robusto da primeira linguagem: aceita ";" "," "|"
    private String getFirstLanguageFlexible(Programmer programmer) {
        if (programmer == null) {
            return null;
        }

        String languages = programmer.getLanguages();
        if (languages == null) {
            return null;
        }

        String raw = languages.trim();
        if (raw.isEmpty()) {
            return null;
        }

        String[] parts = raw.split("[;,|]");
        for (String part : parts) {
            if (part != null) {
                String t = part.trim();
                if (!t.isEmpty()) {
                    return t;
                }
            }
        }

        return raw;
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

        int currentId = getCurrentPlayerID();
        Programmer currentProgrammer = idToProgrammer.get(currentId);

        if (currentProgrammer == null) {
            return false;
        }

        // Preso/Derrotado: não joga
        if (!currentProgrammer.canPlay()) {
            return false;
        }

        // Restrições por linguagem
        String firstLang = getFirstLanguageFlexible(currentProgrammer);
        if (firstLang != null) {
            if (firstLang.equalsIgnoreCase("Assembly")) {
                if (nrPositions > 2) {
                    return false;
                }
            }
            if (firstLang.equalsIgnoreCase("C")) {
                if (nrPositions > 3) {
                    return false;
                }
            }
        }

        this.lastDiceValue = nrPositions;
        this.lastPlayerId = currentId;
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        int from = currentProgrammer.getPosition();
        int to = calculateNewPosition(from, nrPositions);

        this.lastFromPosition = from;
        this.lastToPosition = to;

        currentProgrammer.recordMove(to);
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

    public String reactToAbyssOrTool() {
        if (!pendingReaction || gameOver || lastPlayerId == null) {
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

        StringBuilder sb = new StringBuilder();
        String playerName = current.getName();

        // 1) Recolher ferramenta (NÃO desaparece do tabuleiro)
        Tool collectedTool = collectToolIfAny(current);
        if (collectedTool != null) {
            this.lastToolCollected = collectedTool;
            sb.append(playerName)
                    .append(" apanhou a ferramenta \"")
                    .append(collectedTool.getName())
                    .append("\".");
        }

        // 2) Aplicar Abismo, se houver
        boolean repeatTurn = applyAbyssIfAny(current, lastFromPosition, lastDiceValue, sb, playerName);

        // Atualizar número de turnos
        turnCount++;

        // Verificar se o jogador chegou ao fim
        if (current.getPosition() == boardSize && current.isPlaying()) {
            gameOver = true;
            winnerId = current.getId();
        } else {
            if (!repeatTurn) {
                if (turnOrderIds != null && !turnOrderIds.isEmpty()) {
                    turnCursor = (turnCursor + 1) % turnOrderIds.size();
                }
            }
        }

        pendingReaction = false;

        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }

    // Tool NÃO desaparece do tabuleiro. Cada jogador só apanha 1 por tipo.
    private Tool collectToolIfAny(Programmer programmer) {
        if (programmer == null) {
            return null;
        }

        int currentPos = programmer.getPosition();
        Tool tool = toolsByPosition.get(currentPos);

        if (tool == null) {
            return null;
        }

        if (programmer.hasToolOfType(tool.getId())) {
            return null;
        }

        programmer.addTool(tool);
        return tool;
    }

    private boolean applyAbyssIfAny(Programmer programmer, int fromPosition,
                                    int diceValue, StringBuilder sb, String playerName) {
        if (programmer == null) {
            return false;
        }

        int currentPos = programmer.getPosition();
        Abyss abyss = abyssesByPosition.get(currentPos);

        if (abyss == null) {
            return false;
        }

        this.lastAbyss = abyss;

        Tool cancellingTool = programmer.findToolToCancelAbyss(abyss.getId());

        if (cancellingTool != null) {
            this.lastToolUsed = cancellingTool;
            programmer.removeTool(cancellingTool);

            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(playerName)
                    .append(" caiu no abismo \"")
                    .append(abyss.getName())
                    .append("\" mas usou a ferramenta \"")
                    .append(cancellingTool.getName())
                    .append("\" para anular o efeito.");

            return false;
        }

        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append(playerName)
                .append(" caiu no abismo \"")
                .append(abyss.getName())
                .append("\".");

        if (abyss.getId() == SegmentationFaultAbyss.ID) {
            return applySegmentationFault(programmer, currentPos, sb);
        }

        abyss.applyEffect(programmer, diceValue, fromPosition);
        return abyss.forcesRepeatTurn();
    }

    private boolean applySegmentationFault(Programmer triggerer, int position, StringBuilder sb) {
        Abyss abyss = abyssesByPosition.get(position);
        if (abyss == null) {
            return false;
        }
        if (abyss.getId() != SegmentationFaultAbyss.ID) {
            return false;
        }

        List<Programmer> playersHere = new ArrayList<>();
        for (Programmer p : programmers) {
            if (p.getPosition() == position && !p.isDefeated()) {
                playersHere.add(p);
            }
        }

        if (playersHere.size() < 2) {
            return false;
        }

        int retreat = SegmentationFaultAbyss.RETREAT_POSITIONS;
        for (Programmer p : playersHere) {
            int newPos = Math.max(1, p.getPosition() - retreat);
            p.setPosition(newPos);
        }

        for (Programmer p : playersHere) {
            checkAbyssAfterRetreat(p);
        }

        return false;
    }

    private void checkAbyssAfterRetreat(Programmer programmer) {
        if (programmer == null) {
            return;
        }
        if (programmer.isDefeated()) {
            return;
        }

        int pos = programmer.getPosition();
        Abyss abyss = abyssesByPosition.get(pos);

        if (abyss == null) {
            return;
        }

        if (abyss.getId() == SegmentationFaultAbyss.ID) {
            applySegmentationFault(programmer, pos, new StringBuilder());
            return;
        }

        Tool cancellingTool = programmer.findToolToCancelAbyss(abyss.getId());
        if (cancellingTool != null) {
            programmer.removeTool(cancellingTool);
            return;
        }

        abyss.applyEffect(programmer, 0, pos);
    }

    public boolean gameIsOver() {
        if (gameOver) {
            return true;
        }

        for (Programmer programmer : programmers) {
            if (programmer.getPosition() == boardSize && programmer.isPlaying()) {
                gameOver = true;
                if (winnerId == null) {
                    winnerId = programmer.getId();
                }
                return true;
            }
        }

        return checkVictoryByElimination();
    }

    private boolean checkVictoryByElimination() {
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
            return true;
        }

        if (activeCount == 0) {
            gameOver = true;
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

            if (random == null) {
                random = new Random();
            }

            this.pendingReaction = false;
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
