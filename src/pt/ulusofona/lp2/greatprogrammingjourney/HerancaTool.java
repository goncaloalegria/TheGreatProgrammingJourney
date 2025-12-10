package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta Herança (ID 0)
 * Anula os abismos relacionados a reutilização de código:
 * - Abismo 5 (Código Duplicado)
 * - Abismo 6 (Efeitos Secundários)
 */
public class HerancaTool extends Tool {

    public static final int ID = 0;
    private static final String NAME = "Herança";
    private static final String IMAGE_NAME = "inheritance.png";

    // IDs dos abismos que esta ferramenta pode anular
    private static final Set<Integer> CANCELLABLE_ABYSSES =
            new HashSet<>(Arrays.asList(5, 6));

    public HerancaTool(int position) {
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