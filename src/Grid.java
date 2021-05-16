public class Grid {
    protected char[][] data;  // . an empty cell. 0 a box.

    public Grid() {
        this.data = new char[ConstantField.HEIGHT][ConstantField.WIDTH];
    }

    public static int cordToIndex(int x, int y) {
        /*
        x: -- >
         */
        return y * ConstantField.WIDTH + x;
    }

    public boolean isBox(int x, int y) {
        return data[y][x] != ConstantField.EMPTY && data[y][x] != ConstantField.WALL;
    }
}
