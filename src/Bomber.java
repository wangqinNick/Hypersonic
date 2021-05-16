import java.util.ArrayList;

public class Bomber extends Point {
    protected int owner;
    protected int bombsRemain;
    protected int explosionRange;
    protected int boxDestroyed;
    protected boolean isDead;

    public Bomber(int x, int y, int owner, int bombsRemain, int explosionRange) {
        super(x, y);
        this.owner = owner;
        this.bombsRemain = bombsRemain;
        this.explosionRange = explosionRange;
        this.boxDestroyed = 0;
        this.isDead = false;
    }

    public Bomber(Bomber bomber) {
        super(bomber.x, bomber.y);
        this.owner = bomber.owner;
        this.bombsRemain = bomber.bombsRemain;
        this.explosionRange = bomber.explosionRange;
        this.boxDestroyed = bomber.boxDestroyed;
        this.isDead = bomber.isDead;
    }

    public boolean hasBombs() {
        return bombsRemain != 0;
    }

    public void move(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point getSafePoints(State state) {
        ArrayList<Point> safePoints = state.search.bfs2(new Node(null, x, y), state);
        return safePoints.get(1);
    }
}
