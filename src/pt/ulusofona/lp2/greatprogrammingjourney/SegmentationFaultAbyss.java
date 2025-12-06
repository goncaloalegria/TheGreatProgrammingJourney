package pt.ulusofona.lp2.greatprogrammingjourney;

// ABISMO 9: Segmentation Fault
public class SegmentationFaultAbyss extends Abyss {

    public static final int ID = 9;
    private static final String NAME = "Segmentation Fault";


    public SegmentationFaultAbyss(int position) {
        super(ID, NAME, position);
    }

    @Override
    public void applyEffect(Programmer programmer, int diceValue, int previousPosition) {

    }

    @Override
    public String getImageName() {
        return "";
    }


}