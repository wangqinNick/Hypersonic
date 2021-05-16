final class ConstantField {
    protected final static int WIDTH = 13;
    protected final static int HEIGHT = 11;
    protected final static int EXTRA_RANGE = 1;
    protected final static int EXTRA_BOMB = 2;

    protected final static char WALL = 'X';
    protected final static char EMPTY = '.';
    protected final static char EMPTY_BOX = '0';

    protected final static int[] DR = new int[]{-1, 1, 0, 0};  // up, down, left, right
    protected final static int[] DC = new int[]{0, 0, 1, -1};

    protected final static int MAX_ROUNDS_REMAIN = 8;

    protected final static double MAX_TIME_SPAN_FIRST_TURN = 800;
    protected final static double MAX_TIME_SPAN = 30;
}
