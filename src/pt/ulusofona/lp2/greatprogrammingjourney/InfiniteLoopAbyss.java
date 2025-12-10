package pt.ulusofona.lp2.greatprogrammingjourney;

/**
 * Abismo Ciclo Infinito (ID 8)
 * Efeito: O jogador fica "Preso" na casa.
 * - Enquanto preso, o turno do jogador é saltado
 * - Só se liberta se tiver a ferramenta "Ajuda do Professor" ao cair
 * - Se outro jogador sem ferramenta cair na mesma casa, também fica preso
 */
public class InfiniteLoopAbyss extends Abyss {

    public static final int ID = 8;
    private static final String NAME = "Ciclo Infinito";
    private static final String IMAGE_NAME = "infinite-loop.png";

    public InfiniteLoopAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        // O jogador fica preso no Ciclo Infinito
        programmer.setState("Preso");
    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }

    @Override
    public boolean forcesRepeatTurn() {
        // Não repete turno - o jogador fica preso e os turnos são saltados
        return false;
    }
}