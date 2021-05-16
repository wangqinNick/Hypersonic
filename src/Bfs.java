import java.util.ArrayList;
import java.util.LinkedList;

public class Bfs {
    protected int sr, sc;  // start row, start col
    protected LinkedList<Integer> rq, cq;  // row queue, col queue

    protected int moveCount;  // initialized to 0
    protected int nodesLeftInLayer;  // 1
    protected int nodesInNextLayer;  // 0

    protected boolean reachedEnd;  // false

    protected char[][] maze;
    protected boolean[][] visited;

    public Bfs(int sr, int sc, char[][] maze) {
        this.sr = sr;  // start row: y
        this.sc = sc;  // start col: x
        this.rq = new LinkedList<>();
        this.cq = new LinkedList<>();
        this.moveCount = 0;
        this.nodesLeftInLayer = 1;
        this.nodesInNextLayer = 0;
        this.reachedEnd = false;
        this.maze = maze;
        this.visited = new boolean[ConstantField.HEIGHT][ConstantField.WIDTH];

    }

    public Point search() {
        int r, c;
        rq.addLast(sr);
        cq.addLast(sc);
        visited[sr][sc] = true;
        while (!rq.isEmpty()) {  // or cq
            r = rq.pollFirst();
            c = cq.pollFirst();
            char next = maze[r][c];
            if (!(next == ConstantField.WALL) && !(next == ConstantField.EMPTY)) {  // not a wall nor empty, is a box
                reachedEnd = true;
                return new Point(c, r);
            }
            exploreNeighbours(r, c);
            nodesLeftInLayer --;
            if (nodesLeftInLayer == 0) {
                nodesLeftInLayer = nodesInNextLayer;
                nodesInNextLayer = 0;
                moveCount ++;
            }
        }
        return null;
    }

    public void exploreNeighbours(int r, int c) {
        for (int i = 0; i < 4; i++) {
            int rr = r + ConstantField.DR[i];
            int cc = c + ConstantField.DC[i];

            /* skip out of bounds locations */
            if (rr < 0 || cc < 0) continue;
            if (rr >= ConstantField.HEIGHT || cc >= ConstantField.WIDTH) continue;

            /* skip visited locations or blocked cells */
            if (visited[rr][cc]) continue;
            if (maze[rr][cc] != ConstantField.EMPTY) continue;

            rq.addLast(rr);
            cq.addLast(cc);
            visited[rr][cc] = true;
            nodesInNextLayer ++;
        }
    }
}
