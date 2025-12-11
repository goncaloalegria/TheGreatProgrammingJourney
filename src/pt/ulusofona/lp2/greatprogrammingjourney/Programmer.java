package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Programmer {

    private int id;
    private String name;
    private String languages;
    private String color;
    private int position;
    private String state;

    // Histórico das últimas posições ANTES de cada jogada "normal"
    // Vamos guardar até 2 posições anteriores
    private Deque<Integer> positionHistory;

    // Ferramentas no inventário do jogador
    private List<Tool> tools;

    public Programmer(int id, String name, String languages, String color) {
        this.id = id;
        this.name = name;
        this.languages = languages;
        this.color = color;
        this.position = 1;           // Posição inicial
        this.state = "Em Jogo";      // Estado inicial
        this.positionHistory = new ArrayDeque<>();
        this.tools = new ArrayList<>();
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLanguages() {
        return languages;
    }

    public String getColor() {
        return color;
    }

    public int getPosition() {
        return position;
    }

    public String getState() {
        return state;
    }

    /**
     * Retorna a primeira linguagem do programador.
     * As linguagens estão separadas por ";" no atributo languages.
     */
    public String getFirstLanguage() {
        if (languages == null || languages.trim().isEmpty()) {
            return null;
        }
        String[] parts = languages.split(";");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        return null;
    }

    // Setters básicos: usados por abismos, loadGame, etc.
    // NÃO mexem no histórico (porque não são movimentos "normais" do dado)
    public void setPosition(int position) {
        this.position = position;
    }

    public void setState(String state) {
        this.state = state;
    }

    // ---------- Histórico de posições ----------

    /**
     * Deve ser usado quando o jogador se move por causa do dado
     * (movimento normal). Atualiza o histórico e a posição atual.
     */
    public void recordMove(int newPosition) {
        // guarda a posição antes de se mover
        positionHistory.addLast(this.position);

        // Só precisamos das duas últimas posições anteriores
        if (positionHistory.size() > 2) {
            positionHistory.removeFirst();
        }

        this.position = newPosition;
    }

    /**
     * Devolve a posição onde o jogador estava há 2 movimentos atrás.
     * Se não houver histórico suficiente, devolve 1 (início do tabuleiro).
     */
    public int getPositionTwoMovesAgo() {
        if (positionHistory.size() < 2) {
            // Não houve 2 movimentos "normais" ainda
            return 1;
        }
        // history guarda [pos há 2 jogadas, pos há 1 jogada]
        return positionHistory.peekFirst();
    }

    // ---------- Tools ----------

    /**
     * Adiciona uma ferramenta ao inventário do jogador.
     */
    public void addTool(Tool tool) {
        if (tool != null) {
            tools.add(tool);
        }
    }

    /**
     * Verifica se o jogador já tem uma ferramenta de um determinado tipo (ID).
     */
    public boolean hasToolOfType(int toolId) {
        for (Tool tool : tools) {
            if (tool != null && tool.getId() == toolId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Procura e devolve uma ferramenta que possa anular o abismo dado.
     * Devolve a ferramenta com o ID mais baixo (prioridade).
     * @return a ferramenta encontrada, ou null se não houver nenhuma compatível
     */
    public Tool findToolToCancelAbyss(int abyssId) {
        Tool bestTool = null;

        for (Tool tool : tools) {
            if (tool != null && tool.canCancelAbyss(abyssId)) {
                if (bestTool == null || tool.getId() < bestTool.getId()) {
                    bestTool = tool;
                }
            }
        }

        return bestTool;
    }

    /**
     * Remove uma ferramenta do inventário do jogador.
     */
    public void removeTool(Tool tool) {
        if (tool != null) {
            tools.remove(tool);
        }
    }

    public List<Tool> getTools() {
        return tools;
    }

    public String getToolsInfo() {
        if (tools == null || tools.isEmpty()) {
            return "No tools";
        }

        List<String> names = new ArrayList<>();
        for (Tool t : tools) {
            if (t != null && t.getName() != null) {
                names.add(t.getName());
            }
        }

        if (names.isEmpty()) {
            return "No tools";
        }

        names.sort(String.CASE_INSENSITIVE_ORDER);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(names.get(i));
        }
        return sb.toString();
    }

    // ---------- Linguagens ----------

    public String getOrderedLanguages() {
        if (languages == null || languages.trim().isEmpty()) {
            return "";
        }

        ArrayList<String> langsList = new ArrayList<>();
        String[] parts = languages.split(";");

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                langsList.add(trimmed);
            }
        }

        langsList.sort(String.CASE_INSENSITIVE_ORDER);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < langsList.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(langsList.get(i));
        }

        return sb.toString();
    }

    public String[] getInfoAsArray() {
        return new String[]{
                String.valueOf(id),
                name,
                getOrderedLanguages(),
                color,
                String.valueOf(position),
                state,
                getToolsInfo()
        };
    }

    /**
     * Formato esperado pelos testes do professor:
     * id | name | position | toolsInfo | orderedLanguages | state
     */
    public String getInfoAsString() {
        return id + " | " + name + " | " + position + " | " +
                getToolsInfo() + " | " + getOrderedLanguages() +
                " | " + state;
    }

    // Mantemos por compatibilidade, se já estiver a ser usado
    public void moveTo(int newPosition) {
        this.position = newPosition;
    }

    /**
     * Verifica se o jogador está em jogo (não derrotado nem preso).
     */
    public boolean isPlaying() {
        return "Em Jogo".equals(state);
    }

    /**
     * Verifica se o jogador está preso (Ciclo Infinito).
     */
    public boolean isTrapped() {
        return "Preso".equals(state);
    }

    /**
     * Verifica se o jogador foi derrotado (Blue Screen of Death).
     */
    public boolean isDefeated() {
        return "Derrotado".equals(state);
    }

    /**
     * Verifica se o jogador pode jogar neste turno.
     * Só pode jogar se estiver "Em Jogo" (não preso nem derrotado).
     */
    public boolean canPlay() {
        return isPlaying();
    }
}