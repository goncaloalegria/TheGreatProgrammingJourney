package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta Programação Funcional (ID 1)
 * Anula os abismos:
 * - Abismo 5 (Código Duplicado)
 * - Abismo 6 (Efeitos Secundários)
 * - Abismo 8 (Ciclo Infinito)
 */
public class FunctionalProgrammingTool extends Tool {

    public static final int ID = 1;
    private static final String NAME = "Programação Funcional";
    private static final String IMAGE_NAME = "functional.png";

    // IDs dos abismos que esta ferramenta pode anular
    private static final Set<Integer> CANCELLABLE_ABYSSES =
            new HashSet<>(Arrays.asList(5, 6, 8));

    public FunctionalProgrammingTool(int position) {
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