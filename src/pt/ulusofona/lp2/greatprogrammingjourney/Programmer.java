package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.ArrayList;


public class Programmer {

    private int id;
    private String name;
    private String languages;
    private String color;
    private int position;
    private String state;

    public Programmer(int id, String name, String languages, String color) {
        this.id = id;
        this.name = name;
        this.languages = languages;
        this.color = color;
        this.position = 1; // Posição inicial
        this.state = "Em Jogo"; // Estado inicial
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

    // Setters
    public void setPosition(int position) {
        this.position = position;
    }

    public void setState(String state) {
        this.state = state;
    }


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
        return new String[] {
                String.valueOf(id),
                name,
                getOrderedLanguages(),
                color,
                String.valueOf(position)
        };
    }


    public String getInfoAsString() {
        return id + " | " + name + " | " + position + " | " +
                getOrderedLanguages() + " | " + state;
    }


    public void moveTo(int newPosition) {
        this.position = newPosition;
    }


    public boolean isPlaying() {
        return "Em Jogo".equals(state);
    }
}