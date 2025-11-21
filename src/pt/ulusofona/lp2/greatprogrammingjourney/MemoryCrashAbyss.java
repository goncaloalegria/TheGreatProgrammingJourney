package pt.ulusofona.lp2.greatprogrammingjourney;

public class MemoryCrashAbyss extends Abyss {

    public static final int ID = 0;
    private static final String NAME = "Crash de Memória";

    public MemoryCrashAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        // O programa "crasha" e o jogador tem de recomeçar a vez:
        // volta para a posição onde estava no início da jogada.
        programmer.setPosition(previousPosition);
    }

    @Override
    public boolean forcesRepeatTurn() {
        // Este abismo obriga o jogador a repetir a vez.
        return true;
    }
}
