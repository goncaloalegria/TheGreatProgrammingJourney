package pt.ulusofona.lp2.greatprogrammingjourney;

/**
 * Abismo LLM (ID 20)
 *
 * Efeito:
 * - Só pode ser anulado pela ferramenta "Ajuda Do Professor" (ID 5)
 * - Se o jogador tem a ferramenta E o dado foi < 4: fica no sítio (ferramenta consumida)
 * - Se o jogador não tem a ferramenta E o dado foi < 4: volta para a posição anterior
 * - Se o dado foi >= 4: avança mais uma vez o mesmo número de casas (independentemente de ter ferramenta)
 *
 * Nota: A lógica especial para dado >= 4 é tratada no GameManager porque
 * precisa de acesso ao lastDiceValue e ao boardSize para calcular bounce-back.
 */
public class LLMAbyss extends Abyss {

    public static final int ID = 20;
    private static final String NAME = "LLM";
    private static final String IMAGE_NAME = "llm.png";

    public LLMAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        // Se o dado foi < 4, o jogador volta para a posição anterior
        // (Este método só é chamado quando o jogador NÃO tem ferramenta
        // ou quando dado >= 4 - neste caso a lógica especial é tratada no GameManager)
        if (diceValue < 4) {
            programmer.setPosition(previousPosition);
        }
        // Se dado >= 4, a lógica é tratada no GameManager
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