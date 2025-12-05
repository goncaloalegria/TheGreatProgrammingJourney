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
    private Tool lastTool = null;

    // Indica se há uma jogada pendente de reação (entre moveCurrentPlayer e reactToAbyssOrTool)
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
        this.lastTool = null;
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
                    // Linha inválida → ignora
                    continue;
                }

                int tipo;
                int abyssOrToolId;
                int posFromConfig = -1;

                try {
                    // Esperamos: [0] = tipo (0=Abyss, 1=Tool), [1] = id,
                    // [2] = posição (opcional)
                    tipo = Integer.parseInt(row[0]);
                    abyssOrToolId = Integer.parseInt(row[1]);

                    if (row.length >= 3) {
                        posFromConfig = Integer.parseInt(row[2]);
                    }
                } catch (NumberFormatException e) {
                    // Linha com valores inválidos → ignora
                    continue;
                }

                if (tipo != 0 && tipo != 1) {
                    // Tipo desconhecido → ignora
                    continue;
                }

                // 1º: tentar usar a posição do ficheiro se for válida e livre
                int pos;
                if (posFromConfig > 1 && posFromConfig < boardSize
                        && !usedSlots.contains(posFromConfig)) {
                    pos = posFromConfig;
                    usedSlots.add(pos);
                } else {
                    // 2º: escolher uma posição aleatória livre
                    try {
                        pos = getRandomFreeSlot(usedSlots);
                    } catch (IllegalStateException ex) {
                        // Sem casas livres → não conseguimos colocar mais nada
                        break;
                    }
                }

                if (tipo == 0) {
                    // 0 = Abyss
                    Abyss abyss = createAbyss(abyssOrToolId, pos);

                    if (abyss == null) {
                        // Ainda não sei criar este tipo de abismo → ignora a linha
                        continue;
                    }

                    addAbyss(abyss);

                } else {
                    // 1 = Tool
                    Tool tool = createTool(abyssOrToolId, pos);

                    if (tool == null) {
                        // Ainda não sei criar esta Tool → ignora
                        continue;
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
        if (color == null || !VALID_COLORS.contains(color)
                || usedColors.contains(color)) {
            return false;
        }
        usedColors.add(color);

        return true;
    }

    // posições para colocar os abismos e tools
    private int getRandomFreeSlot(Set<Integer> usedSlots) {
        // Vamos tentar algumas vezes até encontrar uma casa livre
        for (int tries = 0; tries < boardSize * 5; tries++) {
            // casas válidas para Abyss/Tool: 2 .. boardSize-1
            int pos = 2 + random.nextInt(Math.max(1, boardSize - 2));
            if (!usedSlots.contains(pos)) {
                usedSlots.add(pos);
                return pos;
            }
        }
        throw new IllegalStateException(
                "Não há casas livres suficientes para colocar Abysses/Tools");
    }

    // Imagem de cada casa (a GUI chama isto)
    public String getImagePng(int position) {
        if (position < 1 || position > boardSize) {
            return null;
        }

        if (position == boardSize) {
            return "glory.png";
        }

        // 1) Abyss na casa?
        Abyss abyss = abyssesByPosition.get(position);
        if (abyss != null) {
            return abyss.getImageName(); // ex: "crash.png"
        }

        // 2) Tool na casa?
        Tool tool = toolsByPosition.get(position);
        if (tool != null) {
            return tool.getImageName();
        }

        // 3) Casa normal sem imagem especial
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

    // Pedido pela GUI
    public String getProgrammersInfo() {
        if (programmers == null || programmers.isEmpty()) {
            return "";
        }

        ArrayList<Programmer> ordered = new ArrayList<>(programmers);
        ordered.sort(Comparator.comparingInt(Programmer::getId));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            Programmer p = ordered.get(i);
            sb.append(p.getInfoAsString());
            if (i < ordered.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // getSlotInfo: [0]=jogadores, [1]=idAbyss, [2]=idTool
    public String[] getSlotInfo(int position) {
        if (position < 1 || position > boardSize) {
            return null;
        }

        // 0) jogadores na casa
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

        // 1) id do abismo (numérico, -1 se não houver)
        int abyssId = -1;
        Abyss abyss = abyssesByPosition.get(position);
        if (abyss != null) {
            abyssId = abyss.getId();
        }

        // 2) id da ferramenta (-1 se não houver)
        int toolId = -1;
        Tool tool = toolsByPosition.get(position);
        if (tool != null) {
            toolId = tool.getId();
        }

        return new String[]{
                programmersStr,
                String.valueOf(abyssId),
                String.valueOf(toolId)
        };
    }

    // --------- Jogador atual / dado ---------

    public int getCurrentPlayerID() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) {
            return -1;
        }
        return turnOrderIds.get(turnCursor);
    }

    // Métodos extra (podem ser úteis)
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

    // Opcional para a GUI, se precisar de nome da imagem do dado
    public String getLastDiceImageName() {
        if (lastDiceValue < 1 || lastDiceValue > 6) {
            return null;
        }
        return "dice" + lastDiceValue + ".png";
    }

    /**
     * Agora este método apenas:
     *  - valida o movimento
     *  - move o jogador para a nova posição
     *  - guarda informação da jogada
     *  - marca pendingReaction = true
     * A aplicação dos Abismos/Tools e avanço do turno acontece em reactToAbyssOrTool().
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

        // 🔹 Procurar um jogador "Em Jogo", saltando derrotados
        Programmer currentProgrammer = null;
        int currentId = -1;
        int attempts = 0;

        while (attempts < turnOrderIds.size()) {
            currentId = turnOrderIds.get(turnCursor);
            Programmer candidate = idToProgrammer.get(currentId);

            if (candidate != null && candidate.isPlaying()) {
                currentProgrammer = candidate;
                break;
            }

            // Jogador nulo ou derrotado → passa ao próximo
            turnCursor = (turnCursor + 1) % turnOrderIds.size();
            attempts++;
        }

        // Se não encontrarmos ninguém "Em Jogo", o jogo acabou
        if (currentProgrammer == null) {
            gameOver = true;
            return false;
        }

        // guardar info da jogada
        this.lastDiceValue = nrPositions;
        this.lastPlayerId = currentId;
        this.lastAbyss = null;
        this.lastTool = null;

        int from = currentProgrammer.getPosition();
        int to = calculateNewPosition(from, nrPositions);
        this.lastFromPosition = from;
        this.lastToPosition = to;

        //guarda o movimento do jogador num histórico implementado no programmer
        currentProgrammer.recordMove(to);

        // marca que há uma jogada pendente para o reactToAbyssOrTool tratar
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
        // podes acrescentar chaves como "hasNewAbyss" / "hasNewTool" aqui mais tarde
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

    /**
     * Aplica o Abismo (se existir) na posição atual do jogador.
     * Devolve true se o jogador tiver de repetir o turno.
     */
    private boolean applyAbyssIfAny(Programmer programmer,
                                    int fromPosition,
                                    int diceValue) {
        if (programmer == null) {
            return false;
        }

        int currentPos = programmer.getPosition();
        Abyss abyss = abyssesByPosition.get(currentPos);

        if (abyss == null) {
            // (no futuro podes tratar Tools aqui também, mas a aplicação de Tools
            //  provavelmente será feita primeiro, antes dos Abismos)
            return false;
        }

        // guardar o abismo que foi ativado nesta jogada
        this.lastAbyss = abyss;

        abyss.applyEffect(programmer, diceValue, fromPosition);

        // true se o jogador tem de repetir a vez (ex: Crash de Memória)
        return abyss.forcesRepeatTurn();
    }

    /**
     * Agora este método:
     *  - só faz alguma coisa se existir uma jogada pendente (pendingReaction == true)
     *  - aplica o Abismo (e no futuro Tools) ao jogador que acabou de se mover
     *  - atualiza turnCount, gameOver, winnerId e turnCursor
     *  - devolve uma mensagem textual a descrever o que aconteceu
     */
    public String reactToAbyssOrTool() {
        // Se não há jogada pendente, ou jogo já terminou, não há reação
        if (!pendingReaction || gameOver || lastPlayerId == null) {
            return null;
        }

        Programmer current = idToProgrammer.get(lastPlayerId);
        if (current == null || !current.isPlaying()) {
            pendingReaction = false;
            return null;
        }

        // Aplicar Abismo, se houver, na posição atual do jogador
        boolean repeatTurn = applyAbyssIfAny(current, lastFromPosition, lastDiceValue);

        // (No futuro: aqui também se aplicam Tools, antes dos Abismos,
        //  e podes introduzir lógica de ferramentas que anulam abismos.)

        // Atualizar número de turnos
        turnCount++;

        // Verificar se o jogador chegou ao fim depois da reação
        if (current.getPosition() == boardSize) {
            gameOver = true;
            winnerId = current.getId();
        } else if (!repeatTurn) {
            // Se não repete turno, avançamos para o próximo jogador
            if (turnOrderIds != null && !turnOrderIds.isEmpty()) {
                turnCursor = (turnCursor + 1) % turnOrderIds.size();
            }
        }
        // Se repeatTurn == true e o jogo não acabou, o turnCursor mantém-se
        // → o mesmo jogador joga outra vez

        // Construir mensagem textual para GUI/DP
        String playerName = "O jogador";
        if (lastPlayerId != null) {
            Programmer p = idToProgrammer.get(lastPlayerId);
            if (p != null) {
                playerName = p.getName();
            }
        }

        StringBuilder sb = new StringBuilder();

        if (lastAbyss != null) {
            sb.append(playerName)
                    .append(" caiu no abismo \"")
                    .append(lastAbyss.getName())
                    .append("\".");
        }

        if (lastTool != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(playerName)
                    .append(" encontrou a ferramenta \"")
                    .append(lastTool.getName())
                    .append("\".");
        }

        // Limpar flag de jogada pendente
        pendingReaction = false;

        if (sb.length() == 0) {
            // Não houve abismo nem ferramenta nesta jogada
            return null;
        }

        return sb.toString();
    }

    // Fábrica de Abyss com base no ID
    // (garante que os IDs usados aqui batem com os das classes concretas)
    private Abyss createAbyss(int abyssId, int position) {
        switch (abyssId) {
            case 0:
                return new SyntaxErrorAbyss(position);
            case 1:
                return new LogicErrorAbyss(position);
            case 2:
                return new ExceptionAbyss(position);
            case 4:
                return new MemoryCrashAbyss(position);
            case 5:
                return new DuplicatedCodeAbyss(position);
            case 6:
                return new SecondaryEffects(position);
            case 7:
                return new BlueScreenOfDeathAbyss(position);
            default:
                // IDs ainda não implementados → devolve null
                return null;
        }
    }

    // Fábrica de Tools com base no ID
    private Tool createTool(int toolId, int position) {
        switch (toolId) {
            // TODO: quando tiveres Tools, cria as instâncias aqui
            // case 0: return new SomeTool(position);
            // case 1: ...
            default:
                return null;
        }
    }


    public boolean saveGame(File file) {
        if (file == null) {
            return false;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            // 1) Tamanho do tabuleiro
            out.println(boardSize);

            // 2) Programadores
            out.println(programmers.size());
            for (Programmer p : programmers) {
                // id|name|languages|color|position|state
                out.println(
                        p.getId() + "|" +
                                p.getName() + "|" +
                                p.getLanguages() + "|" +
                                p.getColor() + "|" +
                                p.getPosition() + "|" +
                                p.getState()
                );
            }

            // 3) Abysses
            List<Integer> abyssPositions =
                    new ArrayList<>(abyssesByPosition.keySet());
            Collections.sort(abyssPositions);
            out.println(abyssPositions.size());
            for (Integer pos : abyssPositions) {
                Abyss a = abyssesByPosition.get(pos);
                // idAbyss|position
                out.println(a.getId() + "|" + pos);
            }

            // 4) Tools
            List<Integer> toolPositions =
                    new ArrayList<>(toolsByPosition.keySet());
            Collections.sort(toolPositions);
            out.println(toolPositions.size());
            for (Integer pos : toolPositions) {
                Tool t = toolsByPosition.get(pos);
                // idTool|position
                out.println(t.getId() + "|" + pos);
            }

            // 5) Ordem de jogada
            if (turnOrderIds == null) {
                out.println(0);
            } else {
                out.println(turnOrderIds.size());
                for (Integer id : turnOrderIds) {
                    out.println(id);
                }
            }

            // 6) Estado do jogo
            out.println(turnCursor);
            out.println(gameOver);
            out.println(winnerId == null ? -1 : winnerId);
            out.println(turnCount);

            // 7) Info da última jogada
            out.println(lastDiceValue);
            out.println(lastPlayerId == null ? -1 : lastPlayerId);
            out.println(lastFromPosition);
            out.println(lastToPosition);

            // Nota: pendingReaction, lastAbyss, lastTool não são persistidos aqui.
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void loadGame(File file)
            throws InvalidFileException, FileNotFoundException {
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
            // Depois de carregar de ficheiro, assumimos que não há jogada pendente
            this.pendingReaction = false;
        } catch (InvalidFileException e) {
            // Reencaminhamos a exceção específica
            throw e;
        } catch (Exception e) {
            // Qualquer outra coisa é formato inválido
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
                throw new InvalidFileException(
                        "Linha de programador inválida: " + line);
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
                throw new InvalidFileException(
                        "Linha de ferramenta inválida: " + line);
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
                throw new InvalidFileException(
                        "Dados da ordem de jogo incompletos");
            }
            int id = Integer.parseInt(scanner.nextLine().trim());
            turnOrderIds.add(id);
        }
    }

    private void loadGameState(Scanner scanner) throws InvalidFileException {
        // turnCursor
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados do estado do jogo");
        }
        turnCursor = Integer.parseInt(scanner.nextLine().trim());

        // gameOver
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados do estado do jogo");
        }
        gameOver = Boolean.parseBoolean(scanner.nextLine().trim());

        // winnerId
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados do estado do jogo");
        }
        int winId = Integer.parseInt(scanner.nextLine().trim());
        winnerId = (winId < 0 ? null : winId);

        // turnCount
        if (!scanner.hasNextLine()) {
            throw new InvalidFileException("Faltam dados do estado do jogo");
        }
        turnCount = Integer.parseInt(scanner.nextLine().trim());
    }

    private void loadLastMoveInfo(Scanner scanner) {
        // Estes campos são opcionais no ficheiro. Se não existirem, usamos defaults.
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
        lastTool = null;
    }

    private void ensureRandom() {
        if (random == null) {
            random = new Random();
        }
    }
}