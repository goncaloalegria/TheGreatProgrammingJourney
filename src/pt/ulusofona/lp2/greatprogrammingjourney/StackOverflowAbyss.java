package pt.ulusofona.lp2.greatprogrammingjourney;

/**
 * Abismo Stack Overflow (ID 10) - ABISMO DE CRIATIVIDADE
 *
 * Conceito: Quando um programador cai neste abismo, ele fica "sobrecarregado"
 * com demasiadas chamadas recursivas e perde TODAS as suas ferramentas!
 * Além disso, recua um número de casas igual ao número de ferramentas que tinha.
 *
 * Efeito:
 * - O jogador perde TODAS as ferramentas que possui
 * - Recua N casas, onde N = número de ferramentas que tinha
 * - Se não tinha ferramentas, apenas recua 1 casa (o "stack" mínimo)
 * - Nunca recua para posição inferior a 1
 *
 * Justificação temática:
 * O Stack Overflow acontece quando há demasiadas chamadas na pilha de execução.
 * Quanto mais "ferramentas" (funções/recursos) o programador acumulou,
 * maior é o impacto do overflow - ele perde tudo e recua proporcionalmente.
 * É uma lição sobre não acumular complexidade desnecessária no código!
 *
 * Estratégia:
 * - Jogadores com muitas ferramentas devem evitar este abismo a todo custo
 * - Jogadores sem ferramentas sofrem impacto mínimo (apenas 1 casa)
 * - Incentiva decisões estratégicas sobre quando usar ferramentas
 */
public class StackOverflowAbyss extends Abyss {

    public static final int ID = 10;
    private static final String NAME = "Stack Overflow";
    private static final String IMAGE_NAME = "unknownPiece.png";
    private static final int MINIMUM_RETREAT = 1;

    public StackOverflowAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        // Contar quantas ferramentas o jogador tem
        int toolCount = programmer.getTools().size();

        // Calcular recuo: número de ferramentas ou mínimo de 1
        int retreat = (toolCount > 0) ? toolCount : MINIMUM_RETREAT;

        // Remover TODAS as ferramentas (o stack "explodiu")
        programmer.getTools().clear();

        // Calcular nova posição
        int currentPosition = programmer.getPosition();
        int newPosition = currentPosition - retreat;

        // Nunca recuar para posição inferior a 1
        if (newPosition < 1) {
            newPosition = 1;
        }

        programmer.setPosition(newPosition);
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