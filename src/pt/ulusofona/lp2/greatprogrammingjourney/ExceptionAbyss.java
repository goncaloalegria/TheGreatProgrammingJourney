package pt.ulusofona.lp2.greatprogrammingjourney;

public class ExceptionAbyss extends Abyss {

    private static final int ID = 2;
    private static final String NAME = "Exception";
    private static final int RETREAT_POSITIONS = 2;
    private static final String IMAGE_NAME = "exception.png";

    public ExceptionAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        int currentPosition = programmer.getPosition();
        int newPosition = Math.max(1, currentPosition - RETREAT_POSITIONS);

        programmer.setPosition(newPosition);
    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }
}