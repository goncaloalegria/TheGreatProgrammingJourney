package pt.ulusofona.lp2.greatprogrammingjourney;

public class DuplicatedCodeAbyss extends Abyss{

    private static final int ID = 5;
    private static final String NAME = "Código Duplicado";
    private static final String IMAGE_NAME = "duplicated-code.png";

    public DuplicatedCodeAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        programmer.setPosition(previousPosition);
    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }
}
