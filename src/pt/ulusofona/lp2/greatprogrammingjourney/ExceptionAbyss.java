package pt.ulusofona.lp2.greatprogrammingjourney;

public class ExceptionAbyss extends Abyss {

    private static final int ABYSS_ID = 2;
    private static final String ABYSS_NAME = "Exception";
    private static final int RETREAT_POSITIONS = 2;

    public ExceptionAbyss(int position) {
        super(ABYSS_ID, ABYSS_NAME, position);
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
        return "exception.png";
    }
}