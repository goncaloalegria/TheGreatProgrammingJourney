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

    // Abysses e Tools por posição
    private HashMap<Integer, Abyss> abyssesByPosition;
    private HashMap<Integer, Tool> toolsByPosition;

    // Para saber se uma casa “era” de ferramenta, mas já foi apanhada (para devolver "" em vez de null)
    private HashSet<Integer> originalToolPositions;

    private Random random;

    // --- Info da última jogada ---
    private int lastDiceValue = 0;
    private Integer lastPlayerId = null;
    private int lastFromPosition = 0;
    private int lastToPosition = 0;
    private Abyss lastAbyss = null;
    private Tool lastToolUsed = null;
    private Tool lastToolCollected = null;

    // Se o turno do jogador atual foi “consumido” e está à espera de reação
    private boolean pendingReaction = false;

    // Para situações em que moveCurrentPlayer devolve false mas o turno tem de avançar via react
    private int pendingReason = 0;
    private static final int PENDING_REASON_NONE = 0;
    private static final int PENDING_REASON_INVALID_MOVE = 2;

    public GameManager() {
        this.programmers = new ArrayList<>();
        this.idToProgrammer = new HashMap<>();
        this.abyssesByPosition = new HashMap<>();
        this.toolsByPosition = new HashMap<>();
        this.originalToolPositions = new HashSet<>();
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

        // TurnCount: começa no turno 1
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
        originalToolPositions.clear();

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

    /**
     * Valida IDs e posições.
     * - tipo 0 (abismo) id 0..9
     * - tipo 1 (tool) id 0..5
     * - linhas com < 3 colunas (sem posição) DEVEM SER IGNORADAS
     * - posição: 2..boardSize-1
     * - NÃO pode haver 2 itens na mesma casa (nem tool+abyss)
     */
    private boolean validateAbyssesAndToolsConfig(String[][] cfg, int boardSize) {
        HashSet<Integer> occupiedSlots = new HashSet<>();

        for (String[] row : cfg) {
            if (row == null) continue;

            if (row.length < 3) continue;

            Integer tipo = parseIntOrNull(row[0]);
            Integer id = parseIntOrNull(row[1]);
            Integer pos = parseIntOrNull(row[2]);

            if (tipo == null || id == null || pos == null) return false;
            if (tipo != 0 && tipo != 1) return false;

            if (tipo == 0) {
                if (id < 0 || id > 9) return false;
            } else {
                if (id < 0 || id > 5) return false;
            }

            if (pos <= 1 || pos >= boardSize) return false;

            if (occupiedSlots.contains(pos)) return false;
            occupiedSlots.add(pos);
        }

        return true;
    }

    private void placeConfiguredItems(String[][] cfg) {
        for (String[] row : cfg) {
            if (row == null) continue;
            if (row.length < 3) continue;

            Integer tipo = parseIntOrNull(row[0]);
            Integer id = parseIntOrNull(row[1]);
            Integer pos = parseIntOrNull(row[2]);

            if (tipo == null || id == null || pos == null) continue;

            if (tipo == 0) {
                Abyss abyss = createAbyss(id, pos);
                if (abyss != null) addAbyss(abyss);
            } else {
                Tool tool = createTool(id, pos);
                if (tool != null) addTool(tool);
            }
        }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
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
        if (programmer == null) return null;
        return programmer.getInfoAsArray();
    }

    public String getProgrammerInfoAsStr(int id) {
        Programmer programmer = idToProgrammer.get(id);
        if (programmer == null) return null;
        return programmer.getInfoAsString();
    }

    public String getProgrammersInfo() {
        if (programmers == null || programmers.isEmpty()) return "";

        ArrayList<Programmer> ordered = new ArrayList<>(programmers);
        ordered.sort(Comparator.comparingInt(Programmer::getId));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            Programmer p = ordered.get(i);

            sb.append(p.getName())
                    .append(" : ")
                    .append(p.getToolsInfo());
            if (i < ordered.size() - 1) sb.append(" | ");
        }
        return sb.toString();
    }

    public String[] getSlotInfo(int position) {
        if (position < 1 || position > boardSize) return null;

        String programmersStr = "";
        if (!programmers.isEmpty()) {
            ArrayList<Integer> idsHere = new ArrayList<>();
            for (Programmer programmer : programmers) {
                if (programmer.getPosition() == position) idsHere.add(programmer.getId());
            }
            if (!idsHere.isEmpty()) {
                Collections.sort(idsHere);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < idsHere.size(); i++) {
                    if (i > 0) sb.append(",");
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
        if (turnOrderIds == null || turnOrderIds.isEmpty()) return -1;
        return turnOrderIds.get(turnCursor);
    }

    public String getCurrentPlayerName() {
        Programmer p = idToProgrammer.get(getCurrentPlayerID());
        return (p == null) ? "" : p.getName();
    }

    public String[] getCurrentPlayerInfo() {
        Programmer p = idToProgrammer.get(getCurrentPlayerID());
        return (p == null) ? null : p.getInfoAsArray();
    }

    public int getLastDiceValue() {
        return lastDiceValue;
    }

    public String getLastDiceImageName() {
        if (lastDiceValue < 1 || lastDiceValue > 6) return null;
        return "dice" + lastDiceValue + ".png";
    }

    /**
     * moveCurrentPlayer:
     * - nrPositions 1..6
     * - SKIP automático de quem não pode jogar (Preso / Derrotado)
     * - BOUNCE-BACK ao ultrapassar a meta (reflete para trás)
     * - Restrições por linguagem:
     *   Assembly: max 2
     *   C: max 3  (C# NÃO conta como C)
     *
     * Regras:
     * - moveCurrentPlayer não aplica abismos/tools, só move (ou prepara reação)
     * - o turno avança em reactToAbyssOrTool()
     */
    public boolean moveCurrentPlayer(int nrPositions) {
        if (gameOver) return false;
        if (turnOrderIds == null || turnOrderIds.isEmpty()) return false;
        if (nrPositions < 1 || nrPositions > 6) return false;

        // Reset pendências anteriores
        this.pendingReaction = false;
        this.pendingReason = PENDING_REASON_NONE;

        // Encontrar próximo jogador que pode jogar (salta Presos/Derrotados)
        if (!selectNextPlayablePlayer()) {
            return false;
        }

        int currentId = getCurrentPlayerID();
        Programmer current = idToProgrammer.get(currentId);
        if (current == null) return false;

        // Restrições por linguagem
        String firstLang = current.getFirstLanguage();
        if (firstLang != null) {
            if (firstLang.equalsIgnoreCase("Assembly") && nrPositions > 2) {
                // inválido mas consome o turno
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

    private boolean selectNextPlayablePlayer() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) return false;

        int safety = 0;
        while (safety < turnOrderIds.size()) {
            int id = turnOrderIds.get(turnCursor);
            Programmer p = idToProgrammer.get(id);

            // se não existir (não devia), salta
            if (p == null) {
                turnCursor = (turnCursor + 1) % turnOrderIds.size();
                safety++;
                continue;
            }

            // se pode jogar, para aqui
            if (p.canPlay()) {
                return true;
            }

            // se não pode jogar (Preso/Derrotado), salta
            turnCursor = (turnCursor + 1) % turnOrderIds.size();
            safety++;
        }

        // ninguém pode jogar
        return false;
    }

    // Bounce-back: se passar da meta, reflete para trás
    private int calculateNewPosition(int from, int nrPositions) {
        int to = from + nrPositions;

        if (to > boardSize) {
            int overshoot = to - boardSize;
            to = boardSize - overshoot;
            if (to < 1) to = 1;
        }

        return to;
    }

    /**
     * reactToAbyssOrTool:
     * - se não houver nada para reagir, devolve null
     * - ordem: ferramenta primeiro, depois abismo
     *
     * Regras importantes:
     * - Ferramenta: ao apanhar, SAI do tabuleiro
     * - Segmentation Fault só ativa com 2+ jogadores vivos na casa do abismo
     * - BSOD derrota só quem caiu lá e é removido da rotação
     */
    public String reactToAbyssOrTool() {
        if (!pendingReaction || gameOver || lastPlayerId == null) {
            return null;
        }

        Programmer current = idToProgrammer.get(lastPlayerId);
        if (current == null) {
            pendingReaction = false;
            pendingReason = PENDING_REASON_NONE;
            return null;
        }

        // Caso: movimento inválido por linguagem (consome turno, avança)
        if (pendingReason == PENDING_REASON_INVALID_MOVE) {
            pendingReaction = false;
            pendingReason = PENDING_REASON_NONE;

            turnCount++;
            advanceTurnCursorToNextPlayable();

            return null;
        }

        // Reset last-info
        this.lastAbyss = null;
        this.lastToolUsed = null;
        this.lastToolCollected = null;

        int pos = current.getPosition();

        String toolMsg = null;

        // 1) Tool (apanha e remove do tabuleiro)
        Tool boardTool = toolsByPosition.get(pos);
        boolean toolExistedHereOriginally = originalToolPositions.contains(pos);

        if (boardTool != null) {
            if (!current.hasToolOfType(boardTool.getId())) {
                current.addTool(boardTool);
                this.lastToolCollected = boardTool;

                // remove do tabuleiro
                toolsByPosition.remove(pos);

                toolMsg = "Recolheu ferramenta: " + boardTool.getName();
            } else {
                // se já tem, NÃO remove do tabuleiro (outros podem apanhar)
                toolMsg = "Já possui a ferramenta: " + boardTool.getName();
            }
        }

        // 2) Abyss
        Abyss abyss = abyssesByPosition.get(pos);
        boolean abyssActivated = false;
        String abyssMsg = null;

        if (abyss != null) {
            this.lastAbyss = abyss;

            // Segmentation Fault: só ativa com 2+
            if (abyss.getId() == SegmentationFaultAbyss.ID) {
                List<Programmer> here = getAlivePlayersAt(pos);
                if (here.size() >= 2) {
                    abyssActivated = true;
                    applySegmentationFaultChain();
                    abyssMsg = abyss.getName() + "!";
                }
            } else {
                // cancela com tool correta (mapeamento fixo)
                Tool canceller = findToolToCancelAbyss(current, abyss.getId());
                if (canceller != null) {
                    current.removeTool(canceller);
                    this.lastToolUsed = canceller;
                    abyssActivated = true;
                    abyssMsg = abyss.getName() + " anulado por " + canceller.getName();
                } else {
                    abyssActivated = true;
                    abyss.applyEffect(current, lastDiceValue, lastFromPosition);
                    abyssMsg = abyss.getName() + "!";
                }

                // Se ficou derrotado (ex.: BSOD), remove da ordem
                if (current.isDefeated()) {
                    removePlayerFromTurnOrder(current.getId());
                }
            }
        }

        // Verificar vitória por chegada ao fim
        if (current.getPosition() == boardSize && current.isPlaying()) {
            gameOver = true;
            winnerId = current.getId();
        }

        // Repetir turno?
        boolean repeatTurn = false;
        if (abyss != null && abyssActivated && lastToolUsed == null) {
            repeatTurn = abyss.forcesRepeatTurn();
        }

        // Incrementa turno sempre que react é chamado
        turnCount++;

        if (!gameOver) {
            if (!repeatTurn) {
                // se o jogador foi removido, o cursor já está no “próximo”
                advanceTurnCursorToNextPlayable();
            } else {
                // repete turno: garantir que cursor aponta para alguém que pode jogar
                // (se o atual foi removido por derrota, não faz sentido repetir)
                if (turnOrderIds == null || turnOrderIds.isEmpty()) {
                    gameOver = true;
                } else {
                    // se o atual saiu, cursor já aponta para outro, então não insistimos em repetir
                    advanceTurnCursorToNextPlayableIfCurrentCantPlay();
                }
            }
        }

        pendingReaction = false;
        pendingReason = PENDING_REASON_NONE;

        // Retorno: prioridade ao abismo (se ativou), senão tool msg
        if (abyssActivated && abyssMsg != null) return abyssMsg;

        if (toolMsg != null) return toolMsg;

        // Se era casa de tool mas já foi apanhada => devolver string vazia (não null)
        if (abyss == null && boardTool == null && toolExistedHereOriginally) {
            return "";
        }

        return null;
    }

    private void advanceTurnCursorToNextPlayableIfCurrentCantPlay() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) return;

        // Se o atual não pode jogar, avança até alguém poder
        int safety = 0;
        while (safety < turnOrderIds.size()) {
            int id = turnOrderIds.get(turnCursor);
            Programmer p = idToProgrammer.get(id);
            if (p != null && p.canPlay()) return;
            turnCursor = (turnCursor + 1) % turnOrderIds.size();
            safety++;
        }
    }

    private void advanceTurnCursorToNextPlayable() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) return;

        turnCursor = (turnCursor + 1) % turnOrderIds.size();
        advanceTurnCursorToNextPlayableIfCurrentCantPlay();
    }

    private void removePlayerFromTurnOrder(int id) {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) return;

        int idx = turnOrderIds.indexOf(id);
        if (idx < 0) return;

        turnOrderIds.remove(idx);

        if (turnOrderIds.isEmpty()) {
            gameOver = true;
            winnerId = null;
            turnCursor = 0;
            return;
        }

        // Ajuste do cursor:
        // se removemos alguém antes do cursor, cursor recua 1
        if (idx < turnCursor) {
            turnCursor--;
        }

        if (turnCursor < 0) turnCursor = 0;
        if (turnCursor >= turnOrderIds.size()) turnCursor = 0;
    }

    /**
     * Mapeamento “oficial” (pela tua regra):
     * 0 SyntaxError        -> IDE (4)
     * 1 LogicError         -> IDE (4)
     * 2 Exception          -> Tratamento de Excepções (3)
     * 3 FileNotFound       -> Tratamento de Excepções (3)   <-- corrige o teu FullGame
     * 4 Crash              -> Ajuda do Professor (5)
     * 5 DuplicatedCode     -> Herança (0)
     * 6 SideEffects        -> Programação Funcional (1)
     * 7 BSOD               -> IDE (4)
     * 8 Ciclo Infinito     -> Ajuda do Professor (5)
     * 9 SegmentationFault  -> (nenhuma)
     */
    private Tool findToolToCancelAbyss(Programmer p, int abyssId) {
        if (p == null) return null;

        int neededToolId;
        switch (abyssId) {
            case 0, 1, 7 -> neededToolId = 4;          // IDE
            case 2, 3 -> neededToolId = 3;             // Tratamento de Excepções
            case 4, 8 -> neededToolId = 5;             // Ajuda do Professor
            case 5 -> neededToolId = 0;                // Herança
            case 6 -> neededToolId = 1;                // Programação Funcional
            default -> {
                return null;
            }
        }

        // devolve a ferramenta desse tipo, se existir
        for (Tool t : p.getTools()) {
            if (t != null && t.getId() == neededToolId) {
                return t;
            }
        }

        return null;
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

    /**
     * Segmentation Fault (cadeia):
     * Enquanto existir uma casa com SegFault e 2+ jogadores vivos lá,
     * todos recuam 3 e (na nova casa) aplicam Tool e depois Abyss.
     */
    private void applySegmentationFaultChain() {
        int safety = 0;
        while (safety < 200) {
            safety++;

            boolean triggered = false;

            for (Map.Entry<Integer, Abyss> entry : abyssesByPosition.entrySet()) {
                Integer pos = entry.getKey();
                Abyss abyss = entry.getValue();

                if (abyss == null) continue;
                if (abyss.getId() != SegmentationFaultAbyss.ID) continue;

                List<Programmer> here = getAlivePlayersAt(pos);
                if (here.size() >= 2) {
                    triggered = true;
                    applySegmentationFaultToAll(here);
                }
            }

            if (!triggered) break;
        }
    }

    private void applySegmentationFaultToAll(List<Programmer> playersHere) {
        int retreat = SegmentationFaultAbyss.RETREAT_POSITIONS;

        for (Programmer p : playersHere) {
            int newPos = p.getPosition() - retreat;
            if (newPos < 1) newPos = 1;
            p.setPosition(newPos);
        }

        // após recuo, aplicar efeitos na nova casa
        for (Programmer p : playersHere) {
            applyLandingEffectsAfterForcedMove(p);
        }
    }

    private void applyLandingEffectsAfterForcedMove(Programmer programmer) {
        if (programmer == null) return;
        if (programmer.isDefeated()) return;

        int pos = programmer.getPosition();

        Tool tool = toolsByPosition.get(pos);
        if (tool != null) {
            if (!programmer.hasToolOfType(tool.getId())) {
                programmer.addTool(tool);
                // NOTA: aqui não removemos do tabuleiro para não “apagar” cadeias de teste
            }
        }

        Abyss abyss = abyssesByPosition.get(pos);
        if (abyss == null) return;

        if (abyss.getId() == SegmentationFaultAbyss.ID) {
            return; // cadeia tratada no loop externo
        }

        Tool canceller = findToolToCancelAbyss(programmer, abyss.getId());
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
        if (gameOver) return true;

        for (Programmer programmer : programmers) {
            if (programmer.getPosition() == boardSize && programmer.isPlaying()) {
                gameOver = true;
                winnerId = programmer.getId();
                return true;
            }
        }

        // Vitória por eliminação
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
            if (winner != null) return winner.getName();
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
        if (abyss == null) return;
        abyssesByPosition.put(abyss.getPosition(), abyss);
    }

    public void addTool(Tool tool) {
        if (tool == null) return;
        toolsByPosition.put(tool.getPosition(), tool);
        originalToolPositions.add(tool.getPosition());
    }

    private Abyss createAbyss(int abyssId, int position) {
        switch (abyssId) {
            case 0: return new SyntaxErrorAbyss(position);
            case 1: return new LogicErrorAbyss(position);
            case 2: return new ExceptionAbyss(position);
            case 3: return new FileNotFoundExceptionAbyss(position);
            case 4: return new CrashAbyss(position);
            case 5: return new DuplicatedCodeAbyss(position);
            case 6: return new SecondaryEffects(position);
            case 7: return new BlueScreenOfDeathAbyss(position);
            case 8: return new InfiniteLoopAbyss(position);
            case 9: return new SegmentationFaultAbyss(position);
            default: return null;
        }
    }

    private Tool createTool(int toolId, int position) {
        switch (toolId) {
            case 0: return new InheritanceTool(position);
            case 1: return new FunctionalProgrammingTool(position);
            case 2: return new UnitTestTool(position);
            case 3: return new ExceptionTool(position);
            case 4: return new IdeTool(position);
            case 5: return new AjudaProfessorTool(position);
            default: return null;
        }
    }

    // ---------------------- Save / Load ----------------------

    public boolean saveGame(File file) {
        if (file == null) return false;

        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println(boardSize);

            out.println(programmers.size());
            for (Programmer p : programmers) {
                StringBuilder toolIds = new StringBuilder();
                for (Tool t : p.getTools()) {
                    if (toolIds.length() > 0) toolIds.append(",");
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
            throw new InvalidFileException("Formato de ficheiro inválido");
        }
    }

    private void validateLoadFile(File file) throws FileNotFoundException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileNotFoundException("Ficheiro não encontrado");
        }
    }

    private void loadBoardSize(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) throw new InvalidFileException("Ficheiro vazio");
        boardSize = Integer.parseInt(scanner.nextLine().trim());
    }

    private void loadProgrammers(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) throw new InvalidFileException("Faltam dados de programadores");

        int numProgrammers = Integer.parseInt(scanner.nextLine().trim());
        programmers = new ArrayList<>();
        idToProgrammer = new HashMap<>();

        for (int i = 0; i < numProgrammers; i++) {
            if (!scanner.hasNextLine()) throw new InvalidFileException("Dados de programadores incompletos");

            String line = scanner.nextLine();
            String[] parts = line.split("\\|", -1);
            if (parts.length < 6) throw new InvalidFileException("Linha de programador inválida: " + line);

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
                        if (tool != null) p.addTool(tool);
                    }
                }
            }

            programmers.add(p);
            idToProgrammer.put(id, p);
        }
    }

    private void loadAbysses(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) throw new InvalidFileException("Faltam dados de abismos");

        int numAbysses = Integer.parseInt(scanner.nextLine().trim());
        abyssesByPosition = new HashMap<>();

        for (int i = 0; i < numAbysses; i++) {
            if (!scanner.hasNextLine()) throw new InvalidFileException("Dados de abismos incompletos");

            String line = scanner.nextLine();
            String[] parts = line.split("\\|", -1);
            if (parts.length < 2) throw new InvalidFileException("Linha de abismo inválida: " + line);

            int abyssId = Integer.parseInt(parts[0]);
            int pos = Integer.parseInt(parts[1]);

            Abyss a = createAbyss(abyssId, pos);
            if (a != null) abyssesByPosition.put(pos, a);
        }
    }

    private void loadTools(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) throw new InvalidFileException("Faltam dados de ferramentas");

        int numTools = Integer.parseInt(scanner.nextLine().trim());
        toolsByPosition = new HashMap<>();
        originalToolPositions = new HashSet<>();

        for (int i = 0; i < numTools; i++) {
            if (!scanner.hasNextLine()) throw new InvalidFileException("Dados de ferramentas incompletos");

            String line = scanner.nextLine();
            String[] parts = line.split("\\|", -1);
            if (parts.length < 2) throw new InvalidFileException("Linha de ferramenta inválida: " + line);

            int toolId = Integer.parseInt(parts[0]);
            int pos = Integer.parseInt(parts[1]);

            Tool t = createTool(toolId, pos);
            if (t != null) {
                toolsByPosition.put(pos, t);
                originalToolPositions.add(pos);
            }
        }
    }

    private void loadTurnOrder(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) throw new InvalidFileException("Faltam dados da ordem de jogo");

        int orderSize = Integer.parseInt(scanner.nextLine().trim());
        turnOrderIds = new ArrayList<>();

        for (int i = 0; i < orderSize; i++) {
            if (!scanner.hasNextLine()) throw new InvalidFileException("Dados da ordem de jogo incompletos");
            int id = Integer.parseInt(scanner.nextLine().trim());
            turnOrderIds.add(id);
        }
    }

    private void loadGameState(Scanner scanner) throws InvalidFileException {
        if (!scanner.hasNextLine()) throw new InvalidFileException("Faltam dados do estado do jogo");
        turnCursor = Integer.parseInt(scanner.nextLine().trim());

        if (!scanner.hasNextLine()) throw new InvalidFileException("Faltam dados do estado do jogo");
        gameOver = Boolean.parseBoolean(scanner.nextLine().trim());

        if (!scanner.hasNextLine()) throw new InvalidFileException("Faltam dados do estado do jogo");
        int winId = Integer.parseInt(scanner.nextLine().trim());
        winnerId = (winId < 0 ? null : winId);

        if (!scanner.hasNextLine()) throw new InvalidFileException("Faltam dados do estado do jogo");
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
        if (random == null) random = new Random();
    }
}
