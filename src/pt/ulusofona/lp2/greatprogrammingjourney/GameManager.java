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
    private Tool lastToolUsed = null;      // Ferramenta usada para anular abismo
    private Tool lastToolCollected = null; // Ferramenta recolhida

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

        // Casas já ocupadas (para evitar conflitos)
        HashSet<Integer> usedSlots = new HashSet<>();
        usedSlots.add(1);          // casa inicial nunca tem Abyss/Tool
        usedSlots.add(boardSize);  // casa final também não

        if (abyssesAndTools != null) {
            for (String[] row : abyssesAndTools) {
                if (row == null || row.length < 2) {
                    continue;
                }

                int tipo;
                int abyssOrToolId;
                int posFromConfig = -1;

                try {
                    tipo = Integer.parseInt(row[0]);
                    abyssOrToolId = Integer.parseInt(row[1]);

                    if (row.length >= 3) {
                        posFromConfig = Integer.parseInt(row[2]);
                    }
                } catch (NumberFormatException e) {
                    continue;
                }

                if (tipo != 0 && tipo != 1) {
                    continue;
                }

                // Tentar usar a posição do ficheiro se for válida e livre
                int pos;
                if (posFromConfig > 1 && posFromConfig < boardSize
                        && !usedSlots.contains(posFromConfig)) {
                    pos = posFromConfig;
                    usedSlots.add(pos);
                } else {
                    try {
                        pos = getRandomFreeSlot(usedSlots);
                    } catch (IllegalStateException ex) {
                        break;
                    }
                }

                if (tipo == 0) {
                    Abyss abyss = createAbyss(abyssOrToolId, pos);
                    if (abyss == null) {
                        continue;  // ID de abismo inválido
                    }
                    addAbyss(abyss);
                } else {
                    Tool tool = createTool(abyssOrToolId, pos);
                    if (tool == null) {
                        continue;  // ID de ferramenta inválido
                    }
                    addTool(tool);
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
        if (color == null || !VALID_COLORS.contains(color)
                || usedColors.contains(color)) {
            return false;
        }
        usedColors.add(color);

        return true;
    }

    private int getRandomFreeSlot(Set<Integer> usedSlots) {
        for (int tries = 0; tries < boardSize * 5; tries++) {
            int pos = 2 + random.nextInt(Math.max(1, boardSize - 2));
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

        int elementId;
        String elementType;

        if (abyss != null) {
            return new String[]{
                    programmersStr,
                    abyss.getName(),
                    "A:" + abyss.getId()
            };
        } else {
            return new String[]{
                    programmersStr,
                    tool.getName(),
                    "T:" + tool.getId()
            };
        }
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

    /**
     * Move o jogador atual.
     * Verifica primeiro se há apenas um jogador ativo (vitória automática).
     * Salta jogadores presos ou derrotados.
     */
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

        // Verificar vitória por eliminação no início do turno
        if (checkVictoryByElimination()) {
            return false;
        }

        // Procurar um jogador que pode jogar, saltando derrotados e presos
        Programmer currentProgrammer = null;
        int currentId = -1;
        int attempts = 0;

        while (attempts < turnOrderIds.size()) {
            currentId = turnOrderIds.get(turnCursor);
            Programmer candidate = idToProgrammer.get(currentId);

            if (candidate != null && candidate.canPlay()) {
                currentProgrammer = candidate;
                break;
            }

            // Jogador preso ou derrotado → passa ao próximo
            turnCursor = (turnCursor + 1) % turnOrderIds.size();
            attempts++;
        }

        if (currentProgrammer == null) {
            gameOver = true;
            return false;
        }

        // Verificar restrições de movimento por linguagem
        String firstLanguage = currentProgrammer.getFirstLanguage();
        if (firstLanguage != null) {
            if (firstLanguage.equalsIgnoreCase("Assembly") && nrPositions > 2) {
                return false;  // Assembly só pode mover 1 ou 2
            }
            if (firstLanguage.equalsIgnoreCase("C") && nrPositions > 3) {
                return false;  // C só pode mover até 3
            }
        }

        // Guardar info da jogada
        this.lastDiceValue = nrPositions;
        this.lastPlayerId = currentId;
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        int from = currentProgrammer.getPosition();
        int to = calculateNewPosition(from, nrPositions);
        this.lastFromPosition = from;
        this.lastToPosition = to;

        // Registar movimento e mover jogador
        currentProgrammer.recordMove(to);

        this.pendingReaction = true;

        return true;
    }
    /**
     * Verifica se há vitória por eliminação (só um jogador ativo).
     * Jogadores presos ainda contam como ativos (não foram eliminados).
     */
    private boolean checkVictoryByElimination() {
        int activeCount = 0;
        Integer lastActiveId = null;

        for (Programmer p : programmers) {
            // Jogadores "Em Jogo" ou "Preso" ainda estão ativos
            // Só jogadores "Derrotado" é que foram eliminados
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
     * Ordem: 1) Recolher ferramenta, 2) Verificar colisão (Segmentation Fault), 3) Aplicar abismo
     */
    public String reactToAbyssOrTool() {
        if (!pendingReaction || gameOver || lastPlayerId == null) {
            return null;
        }

        Programmer current = idToProgrammer.get(lastPlayerId);
        if (current == null) {
            pendingReaction = false;
            return null;
        }

        // Reset
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        StringBuilder sb = new StringBuilder();
        String playerName = current.getName();

        // 1) Recolher ferramenta se houver na posição atual
        Tool collectedTool = collectToolIfAny(current);
        if (collectedTool != null) {
            this.lastToolCollected = collectedTool;
            sb.append(playerName)
                    .append(" apanhou a ferramenta \"")
                    .append(collectedTool.getName())
                    .append("\".");
        }

        // 2) Verificar Segmentation Fault (colisão de jogadores na mesma casa)
        checkSegmentationFault(current, sb);

        // 3) Aplicar Abismo, se houver (na nova posição após possível Segmentation Fault)
        boolean repeatTurn = applyAbyssIfAny(current, lastFromPosition, lastDiceValue, sb, playerName);

        // Atualizar número de turnos
        turnCount++;

        // Verificar se o jogador chegou ao fim
        if (current.getPosition() == boardSize && current.isPlaying()) {
            gameOver = true;
            winnerId = current.getId();
        } else if (!repeatTurn) {
            // Avançar para o próximo jogador
            if (turnOrderIds != null && !turnOrderIds.isEmpty()) {
                turnCursor = (turnCursor + 1) % turnOrderIds.size();
            }
        }

        pendingReaction = false;

        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }


    private void checkSegmentationFault(Programmer current, StringBuilder sb) {
        if (current == null || current.isDefeated()) {
            return;
        }

        int currentPos = current.getPosition();

        // Contar OUTROS jogadores na mesma posição (excluindo o jogador atual)
        List<Programmer> othersHere = new ArrayList<>();
        for (Programmer p : programmers) {
            if (p != current && p.getPosition() == currentPos && !p.isDefeated()) {
                othersHere.add(p);
            }
        }

        // Só há Segmentation Fault se o jogador atual se moveu para uma casa
        // onde JÁ ESTAVA outro jogador
        if (othersHere.isEmpty()) {
            return;
        }

        // Adicionar o jogador atual à lista para todos recuarem
        othersHere.add(current);

        // Segmentation Fault! Todos recuam 3 casas
        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append("Segmentation Fault!");

        int retreat = 3;
        for (Programmer p : othersHere) {
            int newPos = Math.max(1, p.getPosition() - retreat);
            p.setPosition(newPos);
        }
    }

    /**
     * Recolhe uma ferramenta se existir na posição atual do jogador.
     * Só recolhe se o jogador não tiver já uma ferramenta do mesmo tipo.
     */
    private Tool collectToolIfAny(Programmer programmer) {
        if (programmer == null) {
            return null;
        }

        int currentPos = programmer.getPosition();
        Tool tool = toolsByPosition.get(currentPos);

        if (tool == null) {
            return null;
        }

        // Verificar se o jogador já tem uma ferramenta deste tipo
        if (programmer.hasToolOfType(tool.getId())) {
            // Já tem - não recolhe (ferramenta fica na casa)
            return null;
        }

        // Adicionar ferramenta ao inventário do jogador
        programmer.addTool(tool);

        // Remover ferramenta do tabuleiro
        toolsByPosition.remove(currentPos);

        return tool;
    }

    /**
     * Aplica o abismo na posição atual do jogador.
     * Verifica se o jogador tem ferramenta para anular.
     * Trata casos especiais: Segmentation Fault, Ciclo Infinito.
     */
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

        // Verificar se o jogador tem uma ferramenta que anula este abismo
        Tool cancellingTool = programmer.findToolToCancelAbyss(abyss.getId());

        if (cancellingTool != null) {
            // Usar a ferramenta para anular o abismo
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

        // Não tem ferramenta - aplicar efeito do abismo
        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append(playerName)
                .append(" caiu no abismo \"")
                .append(abyss.getName())
                .append("\".");

        // Aplicar efeito normal do abismo
        abyss.applyEffect(programmer, diceValue, fromPosition);

        return abyss.forcesRepeatTurn();
    }

    public boolean gameIsOver() {
        if (gameOver) {
            return true;
        }

        // Verificar se alguém chegou ao fim
        for (Programmer programmer : programmers) {
            if (programmer.getPosition() == boardSize && programmer.isPlaying()) {
                gameOver = true;
                if (winnerId == null) {
                    winnerId = programmer.getId();
                }
                return true;
            }
        }

        // Verificar vitória por eliminação
        return checkVictoryByElimination();
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

    // Fábrica de Abyss
    private Abyss createAbyss(int abyssId, int position) {
        switch (abyssId) {
            case 0:
                return new SyntaxErrorAbyss(position);
            case 1:
                return new LogicErrorAbyss(position);
            case 2:
                return new ExceptionAbyss(position);
            case 3:
                return new FileNotFoundExceptionAbyss(position);
            case 4:
                return new CrashAbyss(position);
            case 5:
                return new DuplicatedCodeAbyss(position);
            case 6:
                return new SecondaryEffects(position);
            case 7:
                return new BlueScreenOfDeathAbyss(position);
            case 8:
                return new InfiniteLoopAbyss(position);
            // case 9 (Segmentation Fault) não é um abismo de casa
            // é uma regra global que se aplica quando 2+ jogadores estão na mesma casa
            default:
                return null;
        }
    }

    // Fábrica de Tools
    private Tool createTool(int toolId, int position) {
        switch (toolId) {
            case 0:
                return new InheritanceTool(position);
            case 1:
                return new FunctionalProgrammingTool(position);
            case 2:
                return new UnitTestTool(position);
            case 3:
                return new ExceptionTool(position);
            case 4:
                return new IdeTool(position);
            case 5:
                return new AjudaProfessorTool(position);
            default:
                return null;
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
                // Guardar também as ferramentas do jogador
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
            ensureRandom();
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

            // Carregar ferramentas do jogador (se existirem)
            if (parts.length >= 7 && !parts[6].isEmpty()) {
                String[] toolIdStrs = parts[6].split(",");
                for (String toolIdStr : toolIdStrs) {
                    int toolId = Integer.parseInt(toolIdStr.trim());
                    Tool tool = createTool(toolId, 0); // posição 0 porque está no inventário
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

    private void ensureRandom() {
        if (random == null) {
            random = new Random();
        }
    }
}