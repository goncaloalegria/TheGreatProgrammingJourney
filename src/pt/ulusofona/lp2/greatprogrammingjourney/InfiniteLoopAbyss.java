package pt.ulusofona.lp2.greatprogrammingjourney;

public class InfiniteLoopAbyss extends Abyss{
    private static final int ID = 8;
    private static final String NAME = "Loop Infinito";
    private static final String IMAGE_NAME = "infinite-loop.png";

    public InfiniteLoopAbyss(int position) {
        super(ID, NAME, position);
    }


    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {

        // o efeito deste abismo é tratado no GameManager no ApplyAbyssIfAny

    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;

    }
}