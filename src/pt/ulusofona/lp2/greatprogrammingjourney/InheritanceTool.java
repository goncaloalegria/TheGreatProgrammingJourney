package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta Herança (ID 0)
 * Esta ferramenta não cancela nenhum abismo diretamente.
 */
public class InheritanceTool extends Tool {

    public static final int ID = 0;
    private static final String NAME = "Herança";
    private static final String IMAGE_NAME = "inheritance.png";

    // Esta ferramenta não cancela abismos
    private static final Set<Integer> CANCELLABLE_ABYSSES = new HashSet<>();

    public InheritanceTool(int position) {
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