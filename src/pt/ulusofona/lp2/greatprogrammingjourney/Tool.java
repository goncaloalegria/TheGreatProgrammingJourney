package pt.ulusofona.lp2.greatprogrammingjourney;

public abstract class Tool {

    protected int id;        // ID do tipo de ferramenta
    protected String name;   // Nome da ferramenta
    protected int position;  // Posição no tabuleiro (onde foi colocada)

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

    // Nome do ficheiro de imagem (ex: "inheritance.png")
    public abstract String getImageName();

    /**
     * Verifica se esta ferramenta pode anular um determinado abismo.
     * @param abyssId o ID do abismo a verificar
     * @return true se a ferramenta pode anular o abismo, false caso contrário
     */
    public abstract boolean canCancelAbyss(int abyssId);
}