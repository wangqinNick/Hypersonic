public class Item extends Point {
    protected int itemType;

    public Item(int x, int y, int itemType) {
        super(x, y);
        this.itemType = itemType;
    }
}
