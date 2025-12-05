package pt.ulusofona.lp2.greatprogrammingjourney;

//O abismo Efeitos Secundários faz com que o programador recue para a posição onde estava há 2 movimentos atrás (duas posições atrás no histórico).

public class SecondaryEffects extends Abyss{
    private static final int ID = 6;
    private static final String NAME = "Efeitos Secundários";
    private static final String IMAGE_NAME = "secondary-effects.png";

    public SecondaryEffects(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {
        if (programmer == null) {
            return;
        }

        int targetPos = programmer.getPositionTwoMovesAgo();

        if (targetPos < 1) {
            targetPos = 1;
        }

        // Aqui usamos setPosition para NÃO mexer no histórico,
        // porque isto é um efeito de abismo, não um movimento normal.
        programmer.setPosition(targetPos);



    }

    @Override
    public String getImageName() {
        return IMAGE_NAME;
    }
}
