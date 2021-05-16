public class Wall extends Point {
    public Wall(int x, int y) {
        super(x, y);
    }

    public Wall(Wall wall) {
        super(wall.x, wall.y);
    }
}
