import java.util.ArrayList;

public class Box extends Point {
    public Box(int x, int y) {
        super(x, y);
    }

    public Box(Box box) {
        super(box.x, box.y);
    }

    public boolean alreadyHasBomb(ArrayList<Bomb> bombs) {
        return bombs.stream().anyMatch(bomb -> bomb.isAdj(new Box(this.x, this.y)));
    }
}
