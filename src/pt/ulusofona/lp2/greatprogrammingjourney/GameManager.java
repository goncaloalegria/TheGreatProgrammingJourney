package pt.ulusofona.lp2.greatprogrammingjourney;

import javax.swing.*;
import java.awt.*;
import java.util.*;


public class GameManager {

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;
    private static final Set<String> VALID_COLORS = new HashSet<String>(Arrays.asList("Purple", "Green", "Brown", "Blue"));
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

    public GameManager() {
    }


    public boolean createInitialBoard(String[][] playerInfo, int boardSize) {

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
            if (id < 1 || seenIds.contains(id)) {
                return false;
            }
            seenIds.add(id);


            String name = row[1];
            if (name == null || name.trim().isEmpty()) {
                return false;
            }

            //linguagens pode ser "", mas null nepia
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

                String t = part.trim();
                if (!t.isEmpty()) langsList.add(t);
            }

            Collections.sort(langsList, new Comparator<>() {

                @Override public int compare(String a, String b) {
                    return a.compareToIgnoreCase(b);
                }
            });

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < langsList.size(); i++) {

                if (i > 0) sb.append(";");
                sb.append(langsList.get(i));
            }
            langsOut = sb.toString();
        }


        String cor = row[3];

        String posStr = String.valueOf(positions[idx]);

        return new String[] {

                idStr, name, langsOut, cor, posStr
        };
    }

    //id,nome,pos,linguagens,estado
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
            if (i > 0) sb.append(",");
            sb.append(idsHere.get(i));
        }
        return new String[] { sb.toString() };
    }

    public int getCurrentPlayerID() {
        if (turnOrderIds == null || turnOrderIds.isEmpty()) return -1;
        return turnOrderIds.get(turnCursor);
    }


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

        // ricochete
        if (to > boardSize) {
            int overshoot = to - boardSize;
            to = boardSize - overshoot;
            if (to < 1) to = 1;
        }

        positions[idx] = to;
        turnCount++;

        if (to == boardSize) {
            gameOver = true;
            winnerId = currentId;
            return true;
        }

        // roda turno circularmente
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

        out.add("TEXT");
        out.add("THE GREAT PROGRAMMING JOURNEY");
        out.add("");
        out.add("NR. DE TURNOS");
        out.add(String.valueOf(turnCount));
        out.add("");
        out.add("VENCEDOR");

        String winnerName = "";

        if (winnerId != null && idToIndex != null) {

            Integer wIdx = idToIndex.get(winnerId);
            if (wIdx != null) winnerName = playerInfo[wIdx][1];
        }
        out.add(winnerName);
        out.add("");
        out.add("RESTANTES");


        ArrayList<int[]> rest = new ArrayList<>();

        for (int i = 0; i < playerInfo.length; i++) {

            int pid = Integer.parseInt(playerInfo[i][0]);
            if (winnerId != null && pid == winnerId) {
                continue;
            }
            rest.add(new int[]{ i, positions[i] });
        }

        Collections.sort(rest, new Comparator<>() {

            @Override public int compare(int[] a, int[] b) {

                int byPos = Integer.compare(b[1], a[1]);
                if (byPos != 0) {
                    return byPos;
                }
                String nameA = playerInfo[a[0]][1];
                String nameB = playerInfo[b[0]][1];
                return nameA.compareToIgnoreCase(nameB);
            }
        });

        for (int[] ints : rest) {

            int idx = ints[0];
            String name = playerInfo[idx][1];
            int pos = positions[idx];
            out.add(name + " " + pos);
        }

        return out;
    }


    public JPanel getAuthorsPanel() {

        JPanel panel = new JPanel();

        return panel;
    }


    public HashMap<String, String> customizeBoard() {

        return new HashMap<>();
    }
}
