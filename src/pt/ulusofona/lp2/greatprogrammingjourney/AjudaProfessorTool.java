package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta Ajuda Do Professor (ID 5)
 * Esta ferramenta não cancela nenhum abismo diretamente.
 */
public class AjudaProfessorTool extends Tool {

    public static final int ID = 5;
    private static final String NAME = "Ajuda Do Professor";
    private static final String IMAGE_NAME = "ajuda-professor.png";

    // Esta ferramenta não cancela abismos
    private static final Set<Integer> CANCELLABLE_ABYSSES = new HashSet<>();

    public AjudaProfessorTool(int position) {
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