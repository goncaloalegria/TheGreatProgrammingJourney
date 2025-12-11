package pt.ulusofona.lp2.greatprogrammingjourney;

/**
 * Abismo Segmentation Fault (ID 9)
 * Efeito: Se houver 2 ou mais programadores na mesma casa, todos recuam 3 casas.
 * Se apenas 1 programador estiver na casa, nada acontece.
 *
 * NOTA: O efeito deste abismo é tratado no GameManager porque precisa de
 * acesso a todos os programadores na mesma posição.
 */
public class SegmentationFaultAbyss extends Abyss {

    public static final int ID = 9;
    private static final String NAME = "Segmentation Fault";
    private static final String IMAGE_NAME = "segfault.png";
    public static final int RETREAT_POSITIONS = 3;

    public SegmentationFaultAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        // O efeito deste abismo é tratado no GameManager
        // porque precisa de verificar quantos jogadores estão na mesma casa
    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }

    @Override
    public boolean forcesRepeatTurn() {
        return false;
    }
}