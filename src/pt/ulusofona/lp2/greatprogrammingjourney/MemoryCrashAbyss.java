package pt.ulusofona.lp2.greatprogrammingjourney;

public class MemoryCrashAbyss extends Abyss {

    public static final int ID = 4;
    private static final String NAME = "Crash de Memória";
    private static final String IMAGE_NAME = "crash.png"; // tem de existir em /images do jar

    public MemoryCrashAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        // O programa "crasha": volta para a posição anterior ao lançamento
        programmer.setPosition(previousPosition);
    }

    @Override
    public boolean forcesRepeatTurn() {
        // Este abismo obriga o jogador a repetir a vez
        return true;
    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }
}
