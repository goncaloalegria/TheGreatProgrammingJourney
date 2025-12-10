package pt.ulusofona.lp2.greatprogrammingjourney;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Ferramenta Ajuda Do Professor (ID 5)
 * Anula os abismos relacionados a dificuldades de aprendizagem:
 * - Abismo 3 (FileNotFoundException)
 * - Abismo 8 (Ciclo Infinito)
 */
public class AjudaProfessorTool extends Tool {

    public static final int ID = 5;
    private static final String NAME = "Ajuda Do Professor";
    private static final String IMAGE_NAME = "ajuda-professor.png";

    // IDs dos abismos que esta ferramenta pode anular
    private static final Set<Integer> CANCELLABLE_ABYSSES =
            new HashSet<>(Arrays.asList(3, 8));

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