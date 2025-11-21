package pt.ulusofona.lp2.greatprogrammingjourney;

public abstract class Abyss {

    protected int id;        // ID do tipo de abismo
    protected String name;   // Nome do abismo
    protected int position;  // Posição no tabuleiro

    public Abyss(int id, String name, int position) {
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
        return new String[] {
                String.valueOf(id),
                name,
                String.valueOf(position)
        };
    }

    @Override
    public String toString() {
        return name + " (ID: " + id + ", posição: " + position + ")";
    }


      //programmer: jogador que caiu no abismo
     //diceValue: valor do dado que o levou até aqui
    public abstract void applyEffect(Programmer programmer, int diceValue, int previousPosition);

    public boolean forcesRepeatTurn(){
        return false;
    }

}
