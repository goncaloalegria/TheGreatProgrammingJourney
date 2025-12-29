package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Programmer {

    private int id;
    private String name;
    private String languages;
    private String color;
    private int position;
    private String state;
    private String cause;  // Causa da derrota ou prisão (nome do abismo)
    private int moveCount;  // Contador de movimentos/jogadas

    private Deque<Integer> positionHistory;
    private List<Tool> tools;

    public Programmer(int id, String name, String languages, String color) {
        this.id = id;
        this.name = name;
        this.languages = languages;
        this.color = color;
        this.position = 1;
        this.state = "Em Jogo";
        this.cause = null;
        this.moveCount = 0;
        this.positionHistory = new ArrayDeque<>();
        this.tools = new ArrayList<>();
    }

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

    public void setPosition(int position) {
        this.position = position;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public void incrementMoveCount() {
        this.moveCount++;
    }

    public boolean isPlaying() {
        return "Em Jogo".equals(state);
    }

    public boolean isTrapped() {
        return "Preso".equals(state);
    }

    public boolean isDefeated() {
        return "Derrotado".equals(state);
    }

    public boolean canPlay() {
        return isPlaying();
    }

    public String getFirstLanguage() {
        if (languages == null || languages.trim().isEmpty()) {
            return null;
        }

        String[] parts = languages.split("[;,]");
        if (parts.length > 0) {
            String first = parts[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }

        return null;
    }

    public void recordMove(int newPosition) {
        positionHistory.addLast(this.position);

        if (positionHistory.size() > 2) {
            positionHistory.removeFirst();
        }

        this.position = newPosition;
    }

    public int getPositionTwoMovesAgo() {
        if (positionHistory.size() < 2) {
            return 1;
        }
        return positionHistory.peekFirst();
    }

    public void addTool(Tool tool) {
        if (tool != null) {
            tools.add(tool);
        }
    }

    public void removeTool(Tool tool) {
        if (tool != null) {
            tools.remove(tool);
        }
    }

    public List<Tool> getTools() {
        return tools;
    }

    public boolean hasToolOfType(int toolId) {
        for (Tool tool : tools) {
            if (tool != null && tool.getId() == toolId) {
                return true;
            }
        }
        return false;
    }

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

    public String getToolsInfo() {
        if (tools == null || tools.isEmpty()) {
            return "No tools";
        }

        List<String> names = new ArrayList<>();
        for (Tool t : tools) {
            if (t != null && t.getName() != null) {
                String nm = t.getName().trim();
                if (!nm.isEmpty()) {
                    names.add(nm);
                }
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

    public String getLanguagesInOriginalOrder() {
        if (languages == null || languages.trim().isEmpty()) {
            return "";
        }

        String[] parts = languages.split("[;,]");
        List<String> out = new ArrayList<>();

        for (String part : parts) {
            if (part != null) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    out.add(trimmed);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < out.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(out.get(i));
        }

        return sb.toString();
    }

    public String getOrderedLanguages() {
        if (languages == null || languages.trim().isEmpty()) {
            return "";
        }

        String[] parts = languages.split("[;,]");
        ArrayList<String> langsList = new ArrayList<>();

        for (String part : parts) {
            if (part != null) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    langsList.add(trimmed);
                }
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

    /**
     * ORDEM do getProgrammerInfo(int id):
     * [0] ID
     * [1] Nome
     * [2] Linguagens (ordem original, separadas por "; ")
     * [3] Cor (EXATAMENTE como foi dada, ex: "Blue")
     * [4] PosiÃ§Ã£o
     * [5] Ferramentas (ordenadas, separadas por "; ")
     * [6] Estado ("Em Jogo", "Preso", "Derrotado")
     */
    public String[] getInfoAsArray() {
        String safeColor = "";
        if (color != null) {
            safeColor = color;
        }

        return new String[]{
                String.valueOf(id),
                name,
                getLanguagesInOriginalOrder(),
                safeColor,
                String.valueOf(position),
                getToolsInfo(),
                state
        };
    }

    /**
     * Formato do getProgrammerInfoAsStr():
     * id | name | position | toolsInfo | orderedLanguages | state
     */
    public String getInfoAsString() {
        return id + " | " + name + " | " + position + " | " +
                getToolsInfo() + " | " + getOrderedLanguages() +
                " | " + state;
    }
}