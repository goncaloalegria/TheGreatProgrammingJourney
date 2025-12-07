package pt.ulusofona.lp2.greatprogrammingjourney;

public class BlueScreenOfDeathAbyss extends Abyss{
    private static final int ID = 7;
    private static final String NAME = "Blue Screen of Death";
    private static final String IMAGE_NAME = "bsod.png";

    public BlueScreenOfDeathAbyss(int position) {
        super(ID, NAME, position);
    }
    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;

        }
        // Aqui coloco o efeito do Blue Screen of Death
        programmer.setState("Derrotado");

    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }

    @Override
    public boolean forcesRepeatTurn() {
        // Não faz sentido repetir a vez: o jogador foi eliminado
        return false;
    }
}