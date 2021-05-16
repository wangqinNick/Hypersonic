public class Node {
    public Node parent = null;
    public int x;
    public int y;
    public int index;

    public Node(Node parent, int x, int y) {
        this.x = x;
        this.y = y;
        this.index = Grid.cordToIndex(x, y);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}