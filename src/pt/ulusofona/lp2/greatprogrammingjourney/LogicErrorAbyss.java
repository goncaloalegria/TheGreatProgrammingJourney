package pt.ulusofona.lp2.greatprogrammingjourney;

public class LogicErrorAbyss extends Abyss {

    public static final int ID = 0;
    private static final String NAME = "Erro de Lógica";

    public LogicErrorAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        // N = floor(dado / 2) → divisão inteira em Java
        int n = diceValue / 2;

        int currentPos = programmer.getPosition();
        int newPos = currentPos - n;

        // Nunca pode ir para posição < 1
        if (newPos < 1) {
            newPos = 1;
        }

        programmer.setPosition(newPos);
    }
}
