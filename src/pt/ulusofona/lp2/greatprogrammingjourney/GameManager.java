package pt.ulusofona.lp2.greatprogrammingjourney;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GameManager {

    private static final Set<String> VALID_COLORS = new HashSet<>(Arrays.asList("Purple", "Green", "Brown", "Blue"));
    private String[][] playerInfo;
    private int boardSize;
    private HashMap<Integer, Integer> idToIndex;
    private ArrayList<Integer> turnOrderIds;
    private int turnCursor;
    private int[] positions;
    private String[] states;
    private boolean gameOver;
    private Integer winnerId;
    private int turnCount;
    private final Random rng = new Random();

    public GameManager() {}

    public boolean createInitialBoard(String[][] playerInfo, int boardSize) {

        if (playerInfo == null) {
            return false;
        }

        final int n = playerInfo.length;
        if (n < 2 || n > 4) {
            return false;
        }
        if (boardSize < n * 2) {
            return false;
        }

        HashSet<Integer> seenIds = new HashSet<>();
        HashSet<String> usedColors = new HashSet<>();

        for (String[] row : playerInfo) {

            if (row == null || row.length < 4) {
                return false;
            }

            int id;
            try {
                id = Integer.parseInt(row[0]);
            } catch (NumberFormatException e) {
                return false;
            }
            if (id < 1) {
                return false;
            }
            if (seenIds.contains(id)) {
                return false;
            }
            seenIds.add(id);

            String name = row[1];

            if (name == null || name.trim().isEmpty()) {
                return false;
            }

            String langs = row[2];

            if (langs == null) {
                return false;
            }

            String color = row[3];

            if (color == null || !VALID_COLORS.contains(color)) {
                return false;
            }
            if (usedColors.contains(color)) {
                return false;
            }
            usedColors.add(color);
        }

        this.playerInfo = playerInfo;
        this.boardSize = boardSize;

        this.idToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int id = Integer.parseInt(playerInfo[i][0]);
            idToIndex.put(id, i);
        }

        this.turnOrderIds = new ArrayList<>(seenIds);
        Collections.sort(this.turnOrderIds);
        this.turnCursor = 0;

        this.positions = new int[n];
        Arrays.fill(this.positions, 1);

        this.states = new String[n];
        Arrays.fill(this.states, "Em Jogo");

        this.gameOver = false;
        this.winnerId = null;
        this.turnCount = 0;

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
        if (idToIndex == null) {
            return null;
        }
        Integer idxObj = idToIndex.get(id);
        if (idxObj == null) {
            return null;
        }
        int idx = idxObj;

        String[] row = playerInfo[idx];
        if (row == null || row.length < 4) {
            return null;
        }

        String idStr = row[0];
        String name = row[1];


        String langsRaw = row[2];
        String langsOut;
        if (langsRaw.trim().isEmpty()) {
            langsOut = "";
        } else {
            ArrayList<String> langsList = new ArrayList<>();
            String[] parts = langsRaw.split(";");
            for (String part : parts) {
                String t = part == null ? "" : part.trim();
                if (!t.isEmpty()) {
                    langsList.add(t);
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
            langsOut = sb.toString();
        }


        String cor = row[3];

        String posStr = String.valueOf(positions[idx]);

        return new String[] { idStr, name, langsOut, cor, posStr };
    }

    public String getProgrammerInfoAsStr(int id) {

        if (idToIndex == null) {

            return null;
        }
        Integer idxObj = idToIndex.get(id);
        if (idxObj == null) {

            return null;
        }
        int idx = idxObj;

        String[] info = getProgrammerInfo(id);

        if (info == null) {

            return null;
        }

        String idStr  = info[0];
        String name   = info[1];
        String langs  = info[2];
        String posStr = info[4];
        String state  = states[idx];

        return idStr + " | " + name + " | " + posStr + " | " + (langs == null ? "" : langs) + " | " + state;
    }

    public String[] getSlotInfo(int position) {

        if (position < 1 || position > boardSize) {

            return null;
        }
        if (playerInfo == null) {

            return new String[] { "" };
        }

        ArrayList<Integer> idsHere = new ArrayList<>();
        for (int i = 0; i < playerInfo.length; i++) {

            if (positions[i] == position) {

                idsHere.add(Integer.parseInt(playerInfo[i][0]));
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

        return new String[] {
                sb.toString()
        };
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
        Integer idxObj = idToIndex.get(currentId);

        if (idxObj == null) {
            return false;
        }

        int idx = idxObj;

        if (!"Em Jogo".equals(states[idx])) {

            return false;
        }

        int from = positions[idx];
        int to = from + nrPositions;

        if (to > boardSize) {
            int overshoot = to - boardSize;
            to = Math.max(1, boardSize - overshoot);
        }

        positions[idx] = to;
        turnCount++;

        if (to == boardSize) {

            gameOver = true;
            winnerId = currentId;
            return true;
        }

        turnCursor = (turnCursor + 1) % turnOrderIds.size();
        return true;
    }

    public boolean gameIsOver() {

        if (gameOver) {
            return true;
        }
        if (positions == null) {
            return false;
        }

        for (int i = 0; i < positions.length; i++) {

            if (positions[i] == boardSize) {
                gameOver = true;

                if (winnerId == null) {
                    winnerId = Integer.parseInt(playerInfo[i][0]);
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
        out.add(String.valueOf(turnCount+1));
        out.add("");
        out.add("VENCEDOR");

        String winnerName = "";
        if (winnerId != null && idToIndex != null) {
            Integer wIdx = idToIndex.get(winnerId);
            if (wIdx != null) {
                winnerName = playerInfo[wIdx][1];
            }
        }

        out.add(winnerName);
        out.add("");
        out.add("RESTANTES");


        int n = playerInfo.length;
        int[] idxs = new int[Math.max(0, n - 1)];
        int k = 0;

        for (int i = 0; i < n; i++) {

            int pid = Integer.parseInt(playerInfo[i][0]);
            if (winnerId != null && pid == winnerId) {
                continue;
            }
            idxs[k++] = i;
        }


        for (int i = 0; i < k - 1; i++) {

            int best = i;

            for (int j = i + 1; j < k; j++) {

                int aIdx = idxs[j];
                int bIdx = idxs[best];
                int posA = positions[aIdx];
                int posB = positions[bIdx];

                boolean better;
                if (posA != posB) {
                    better = (posA > posB); // desc
                } else {
                    String nameA = playerInfo[aIdx][1];
                    String nameB = playerInfo[bIdx][1];
                    better = (nameA.compareToIgnoreCase(nameB) < 0); // asc
                }

                if (better) {
                    best = j;
                }
            }
            if (best != i) {
                int tmp = idxs[i];
                idxs[i] = idxs[best];
                idxs[best] = tmp;
            }
        }

        for (int i = 0; i < k; i++) {

            int idx = idxs[i];
            String name = playerInfo[idx][1];
            int pos = positions[idx];
            out.add(name + " " + pos);
        }

        return out;
    }

    public JPanel getAuthorsPanel() {

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 300));
        return panel;
    }

    public HashMap<String, String> customizeBoard() {

        return new HashMap<>();
    }
}
