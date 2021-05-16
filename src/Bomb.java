import java.util.ArrayList;

public class Bomb extends Point {
    protected int owner;
    protected int roundsRemain;
    protected int explosionRange;
    public Bomb(int x, int y, int owner, int roundsRemain, int explosionRange) {
        super(x, y);
        this.owner = owner;
        this.roundsRemain = roundsRemain;
        this.explosionRange = explosionRange;
    }

    public Bomb(Bomb bomb) {
        super(bomb.x, bomb.y);
        this.owner = bomb.owner;
        this.roundsRemain = bomb.roundsRemain;
        this.explosionRange = bomb.explosionRange;
    }

    public boolean willAffect(Point other, ArrayList<Wall> walls, ArrayList<Box> boxes) {
        Point thisPoint = new Point(x, y);
        if (other.isSameLine(thisPoint)) {
            if (!isBlocked(thisPoint, walls, boxes)) {
                return explosionRange >= other.manhattanDistance(thisPoint);
            }
        }
        return false;
    }
}
