package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta Ajuda Do Professor (ID 5)
 * Anula os abismos:
 * - Abismo 20 (LLM) - mas apenas quando o dado é < 4
 */
public class AjudaProfessorTool extends Tool {

    public static final int ID = 5;
    private static final String NAME = "Ajuda Do Professor";
    private static final String IMAGE_NAME = "ajuda-professor.png";

    // IDs dos abismos que esta ferramenta pode anular
    private static final Set<Integer> CANCELLABLE_ABYSSES =
            new HashSet<>(Arrays.asList(20));

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