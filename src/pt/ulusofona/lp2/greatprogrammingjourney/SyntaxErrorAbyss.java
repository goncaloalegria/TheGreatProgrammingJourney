package pt.ulusofona.lp2.greatprogrammingjourney;

public class SyntaxErrorAbyss extends Abyss {

    public static final int ID = 0;
    private static final String NAME = "Erro de sintaxe";
    private static final String IMAGE_NAME = "syntax.png";

    public SyntaxErrorAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {

        if (programmer == null) {
            return;
        }


        int currentPos = programmer.getPosition();
        int newPos = currentPos - 1;

        // Nunca pode ir para posição < 1
        if (newPos < 1) {
            newPos = 1;
        }

        programmer.setPosition(newPos);

    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }
}
