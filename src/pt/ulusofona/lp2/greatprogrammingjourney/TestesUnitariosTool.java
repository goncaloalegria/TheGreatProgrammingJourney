package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta Testes Unitários (ID 2)
 * Anula os abismos que causam erros de execução ou problemas de qualidade:
 * - Abismo 3 (FileNotFoundException)
 * - Abismo 7 (Blue Screen of Death)
 */
public class TestesUnitariosTool extends Tool {

    public static final int ID = 2;
    private static final String NAME = "Testes Unitários";
    private static final String IMAGE_NAME = "unit-tests.png";

    // IDs dos abismos que esta ferramenta pode anular
    private static final Set<Integer> CANCELLABLE_ABYSSES =
            new HashSet<>(Arrays.asList(3, 7));

    public TestesUnitariosTool(int position) {
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