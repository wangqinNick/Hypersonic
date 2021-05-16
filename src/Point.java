import java.util.ArrayList;
import java.util.Objects;

public class Point {
    protected int x;
    protected int y;
    protected int index;


    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.index = Grid.cordToIndex(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", index=" + index +
                '}';
    }

    public int manhattanDistance(Point other) {
        return Math.abs(other.x - this.x) + Math.abs(other.y - this.y);
    }

    public boolean isAdj(Point other) {
        return manhattanDistance(other) == 1;
    }

    /* check if this unit is in same line with another unit other */
    public boolean isSameLine(Point other) {
        return this.x == other.x || this.y == other.y;
    }

    /* check if this unit is blocked from a wall / a box from a unit */
    public boolean isBlocked(Point point, ArrayList<Wall> walls, ArrayList<Box> boxes) {
        assert isSameLine(point): "not in same line";

        if (this.x == point.x) {  // unit and bomb are in same row
            /* check walls first */
            return (walls.stream().anyMatch(u -> u.x == this.x && ((this.y < u.y && u.y < point.y) || (point.y < u.y && u.y < this.y)))) || (boxes.stream().anyMatch(u -> u.x == this.x && ((this.y < u.y && u.y < point.y) || (point.y < u.y && u.y < this.y))));
        } else {
            return (walls.stream().anyMatch(u -> u.y == this.y && ((this.x < u.x && u.x < point.x) || (point.x < u.x && u.x < this.x)))) || (boxes.stream().anyMatch(u -> u.y == this.y && ((this.x < u.x && u.x < point.x) || (point.x < u.x && u.x < this.x))));
        }
    }

    public boolean isSafe(ArrayList<Bomb> bombs, ArrayList<Wall> walls, ArrayList<Box> boxes) {
        Point point = new Point(this.x, this.y);
        for (Bomb bomb: bombs) {
            if (bomb.isSameLine(point)) {
                if (!bomb.isBlocked(point, walls, boxes)) {
                    if (bomb.explosionRange >= bomb.manhattanDistance(point)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isAdjToBox(ArrayList<Box> boxes) {
        for (Box box: boxes) {
            if (box.isAdj(new Point(this.x, this.y))) return true;
        }
        return false;
    }
}
