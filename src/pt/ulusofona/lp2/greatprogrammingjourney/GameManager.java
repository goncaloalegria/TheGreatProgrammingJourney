package pt.ulusofona.lp2.greatprogrammingjourney;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class GameManager {
    private String [][] playerInfo;
    private int worldSize;
    private int [] playerPositions;
    private int currentPlayer;
    private boolean gameOver;
    private Random random;

    public GameManager() {
    }

    public GameManager(String[][] playerInfo, int worldSize, int[] playerPositions,int currentPlayer , boolean gameOver, Random random) {
        this.playerInfo = playerInfo;
        this.worldSize = worldSize;
        this.playerPositions = playerPositions;
        this.currentPlayer = currentPlayer;
        this.gameOver = gameOver;
        this.random = random;
    }

    public boolean createInitialBoard(String[][] playerInfo, int worldSize) {
        this.playerInfo = playerInfo;
        this.worldSize = worldSize;
        this.playerPositions = new int [playerInfo.length];
        this.currentPlayer = 0;
        this.gameOver = false;
        this.random = new Random();
        return true;

    }

    public String getImagePng(int nrSquare) {
        return "images/slot" + nrSquare + ".png";
    }

    public String[] getProgrammerInfo(int id) {
        return playerInfo[id];
    }

    public String getProgrammerInfoAsStr(int id) {
        return Arrays.deepToString(playerInfo);

    }

    public String[] getSlotInfo(int position) {


    }

    public int getCurrentPlayerID() {
        return currentPlayer;

    }

    public boolean moveCurrentPlayer(int nrSpaces) {


    }

    public boolean gameIsOver() {
        return gameOver;

    }

    public ArrayList<String> getGameResults() {


    }

    public JPanel getAuthorsPanel() {


    }

    public HashMap<String, String> customizeBoard() {


    }



}
