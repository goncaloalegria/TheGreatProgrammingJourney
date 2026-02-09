package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta Tratamento de Excepções (ID 3)
 * Anula os abismos:
 * - Abismo 2 (Exception)
 * - Abismo 3 (FileNotFoundException)
 */
public class MarteloDouradoTool extends Tool {

    public static final int ID = 100;
    private static final String NAME = "Martelo Dourado";
    private static final String IMAGE_NAME = "unknownPiece.png";

    // IDs dos abismos que esta ferramenta pode anular
    private static final Set<Integer> CANCELLABLE_ABYSSES =
            new HashSet<>(Arrays.asList(2));

    public MarteloDouradoTool(int position) {
        super(ID, NAME, position);
    }



    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }

    @Override
    public boolean canCancelAbyss(int abyssId) {
        return CANCELLABLE_ABYSSES.contains(abyssId);
    }
}