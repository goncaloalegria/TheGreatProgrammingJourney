package pt.ulusofona.lp2.greatprogrammingjourney;

public abstract class Tool {

    protected int id;        // ID do tipo de ferramenta
    protected String name;   // Nome da ferramenta
    protected int position;  // Posição no tabuleiro

    public Tool(int id, String name, int position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public String[] getInfoAsArray() {
        return new String[]{
                String.valueOf(id),
                name,
                String.valueOf(position)
        };
    }

    @Override
    public String toString() {
        return name + " (ID: " + id + ", posição: " + position + ")";
    }

    // Nome do ficheiro de imagem (ex: "tool-laptop.png")
    public abstract String getImageName();
}
