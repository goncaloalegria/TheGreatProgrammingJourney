package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta IDE (ID 4)
 * Anula os abismos relacionados a erros de programação:
 * - Abismo 1 (Erro de Lógica)
 * - Abismo 5 (Código Duplicado)
 */
public class IdeTool extends Tool {

    public static final int ID = 4;
    private static final String NAME = "IDE";
    private static final String IMAGE_NAME = "IDE.png";

    // IDs dos abismos que esta ferramenta pode anular
    private static final Set<Integer> CANCELLABLE_ABYSSES =
            new HashSet<>(Arrays.asList(0, 1, 5));

    public IdeTool(int position) {
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