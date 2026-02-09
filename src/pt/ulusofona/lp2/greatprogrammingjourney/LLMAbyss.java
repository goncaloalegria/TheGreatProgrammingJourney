package pt.ulusofona.lp2.greatprogrammingjourney;

/**
 * Abismo LLM (ID 20)
 *
 * Efeito:
 * - Só pode ser anulado pela ferramenta "Ajuda Do Professor" (ID 5)
 * - Se é a 1ª, 2ª ou 3ª jogada E tem ferramenta: fica no sítio (ferramenta consumida)
 * - Se é a 1ª, 2ª ou 3ª jogada E não tem ferramenta: volta para a posição anterior
 * - Se é a 4ª jogada ou posterior: AVANÇA mais uma vez o mesmo número de casas (independentemente de ter ferramenta)
 *
 * Nota: A lógica especial é tratada no GameManager.
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

        // O jogador volta para a posição anterior
        // (Este método só é chamado quando o jogador NÃO tem ferramenta
        // nas primeiras 3 jogadas - a lógica da 4ª jogada+ é tratada no GameManager)
        programmer.setPosition(previousPosition);
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