public class Condition {

    public String leftTable;
    public int leftCol;

    public String rightTable; // null si filtre
    public Integer rightValue;
    
    public int rightCol;

    public boolean isJoin() {
        return rightTable != null;
    }
}