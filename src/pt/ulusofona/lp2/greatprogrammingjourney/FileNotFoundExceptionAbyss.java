package pt.ulusofona.lp2.greatprogrammingjourney;

public class FileNotFoundExceptionAbyss extends Abyss{

    private static final int ID = 3;
    private static final String NAME = "File Not Found Exception";
    private static final int RETREAT_POSITIONS = 3;
    private static final String IMAGE_NAME = "file-not-found-exception.png";

    public FileNotFoundExceptionAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        int currentPosition = programmer.getPosition();
        int newPosition = currentPosition - RETREAT_POSITIONS;

        programmer.setPosition(newPosition);
    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }
}
