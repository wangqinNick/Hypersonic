import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.ArrayList;
enum UnitType {
    Bomber(0),
    Bomb(1),
    ITEM(2);

    protected int value;

    UnitType(int value) {
        this.value = value;
    }
}

class Agent {
    protected State state;
    protected Action bestAction;
    protected Scanner in;
    protected Timeout timeout;

    public Agent() {
        this.in = new Scanner(System.in);
        timeout = new Timeout();
    }

    public void readGeneralInformation() {
        in.nextInt();
        in.nextInt();
        int myId = in.nextInt();

        state = new State(myId);
    }

    public void readTurn() {

        timeout.start();
        state = new State(state);
        state.clear();
        /* read map */
        for (int i = 0; i < ConstantField.HEIGHT; i++) {
            String row = in.next();
            for (int j = 0; j < row.length(); j++) {
                char c = row.charAt(j);
                state.grid.data[i][j] = c;
                switch (c) {
                    case 'X':  // walls
                        state.walls.add(new Wall(j, i));
                        break;
                    case '.':  // empty
                        break;
                    default:  // boxes
                        state.boxes.add(new Box(j, i));
                }
            }
        }

        /* add edges */
        final long startTime = System.nanoTime();
        int source, destination;
        for (int r = 0; r < ConstantField.HEIGHT; r++) {
            for (int c = 0; c < ConstantField.WIDTH; c++) {
                if (state.grid.data[r][c] != ConstantField.EMPTY) continue;
                source = Grid.cordToIndex(c, r);
                for (int i = 0; i < 4; i++) {
                    int rr = r + ConstantField.DR[i];
                    int cc = c + ConstantField.DC[i];

                    /* skip out of bounds locations */
                    if (rr < 0 || cc < 0) continue;
                    if (rr >= ConstantField.HEIGHT || cc >= ConstantField.WIDTH) continue;

                    /* skip visited locations or blocked cells */
                    if (state.grid.data[rr][cc] != ConstantField.EMPTY) continue;

                    /* connect edges */
                    destination = Grid.cordToIndex(cc, rr);
                    state.graph.addEdge(source, destination);
                }
            }
        }
        final long duration = System.nanoTime() - startTime;
        Log.log(String.format("Construct graph using: %d", duration));
        // state.graph.printGraph();

        /* read units */
        int units = in.nextInt();
        for (int i = 0; i < units; i++) {
            int entityType = in.nextInt();
            int owner = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            int param1 = in.nextInt();
            int param2 = in.nextInt();
            switch (entityType) {
                case 0:  // bomber
                    state.bombers.add(new Bomber(x, y, owner, param1, param2));
                    break;
                case 1:  // bomb
                    state.bombs.add(new Bomb(x, y, owner, param1, param2));
                    break;
                case 2:
                    switch (param1) {
                        case ConstantField.EXTRA_RANGE:
                            state.itemsExtraRange.add(new Item(x, y, ConstantField.EXTRA_RANGE));
                            break;
                        case ConstantField.EXTRA_BOMB:
                            state.itemsExtraBomb.add(new Item(x, y, ConstantField.EXTRA_BOMB));
                            break;
                        default:
                            Log.log("Unknown item type");
                    }
                    break;
                default:
                    Log.log("Unknown point type");
            }
        }
    }

    public void print() {
        bestAction.print();
    }

    public ArrayList<Point> getPath() {
        Bomber myBomber = state.bombers.get(state.myId);

        return state.search.bfs(new Node(null, myBomber.x, myBomber.y), state.boxes);
    }

    public Item getClosestExtraBomb() {
        Bomber myBomber = state.bombers.get(state.myId);
        Item closestExtraBomb = state.itemsExtraBomb.get(0);
        double minDistance = Double.MAX_VALUE;
        double distance;
        for (Item item: state.itemsExtraBomb) {
            distance = myBomber.manhattanDistance(item);
            if (distance < minDistance) {
                closestExtraBomb = item;
                minDistance = distance;
            }
        }
        return closestExtraBomb;
    }

    public static Action getRandomAction(State stateCopy) {
        ArrayList<Action> actions = stateCopy.getAllLegalActions(stateCopy);
        if (actions.isEmpty()) return null;

        int actionIndex = new Random().nextInt(actions.size());
        return actions.get(actionIndex);
    }

    public static double evaluateState(State state) {
        // if my bomber is dead
        Bomber myBomber = state.bombers.get(state.myId);
        Bomber opponentBomber = state.bombers.get(1-state.myId);
        if (myBomber.isDead) return Double.NEGATIVE_INFINITY;
        Box closestBox = state.getClosestBox();
        double distanceToBox = myBomber.manhattanDistance(closestBox);
        return myBomber.boxDestroyed - distanceToBox + myBomber.explosionRange * 2;
    }

    public void think() {
        //bestAction = getRandomAction(state);
        ArrayList<Action> bestTurn = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        while (!timeout.isElapsed(ConstantField.MAX_TIME_SPAN)) {
            State newState = new State(state);
            ArrayList<Action> newTurn = new ArrayList<>();

            // simulation
            for (int i = 0; i < 9; i++) {
                Log.log("Depth: " + i);
                Action action = getRandomAction(newState);
                Log.log(action);
                newTurn.add(action);
                newState = newState.update(action);
            }

            // evaluation
            double score = evaluateState(newState);
            if (score > bestScore) {
                bestScore = score;
                bestTurn = newTurn;
            }
        }
        assert bestTurn != null;
        bestAction = bestTurn.get(0);
    }
}
class Grid {
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
enum ActionType {
    BOMB("BOMB"),
    MOVE("MOVE");

    protected String value;

    ActionType(String value) {
        this.value = value;
    }
}
class Log {
    public static void log(Object obj) {
        System.err.println(obj);
    }
}

class Player {

    public static void main(String[] args) {
        Agent agent = new Agent();
        // game loop
        agent.readGeneralInformation();
        while (true) {
            agent.readTurn();
            agent.think();
            agent.print();
        }
    }
}
class Action {
    protected ActionType actionType;
    protected int x;
    protected int y;

    public Action() {
    }

    public void bomb(int x, int y) {
        this.actionType = ActionType.BOMB;
        this.x = x;
        this.y = y;
    }

    public void move(int x, int y) {
        this.actionType = ActionType.MOVE;
        this.x = x;
        this.y = y;
    }

    public void bomb(Point p) {
        this.actionType = ActionType.BOMB;
        this.x = p.x;
        this.y = p.y;
    }

    public void move(Point p) {
        this.actionType = ActionType.MOVE;
        this.x = p.x;
        this.y = p.y;
    }

    public void print() {
        switch (actionType) {
            case BOMB:
                System.out.printf("BOMB %d %d\n", x, y);
                break;
            case MOVE:
                System.out.printf("MOVE %d %d\n", x, y);
            default:
        }
    }

    @Override
    public String toString() {
        return "Action{" +
                "actionType=" + actionType +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}

class Search {
    protected int n;
    protected Graph g;

    public Search(int n, Graph g) {
        this.n = n;
        this.g = g;
    }

    public ArrayList<Point> bfs(Node s, ArrayList<Box> boxes) {
        return solve(s, boxes);  // do a bfs staring at node s
    }

    public ArrayList<Point> solve(Node s, ArrayList<Box> boxes) {
        LinkedList<Node> q = new LinkedList<>();
        q.addLast(s);

        boolean[] visited = new boolean[n];  // track if node i has been visited
        Arrays.fill(visited, false);
        visited[s.index] = true;

        Node[] prev = new Node[n];  // to reconstruct from start node to end node
        Arrays.fill(prev, null);
        Node e = null;
        while(!q.isEmpty()) {
            Node node = q.pollFirst();

            if (new Point(node.x, node.y).isAdjToBox(boxes)) {
                e = new Node(null, node.x, node.y);
                break;
            }

            ArrayList<Node> neighbours = g.get(node);  // node's neighbours
            for (Node next: neighbours) {

                int nextIndex = next.index;
                if (!visited[nextIndex]) {
                    q.addLast(next);
                    visited[nextIndex] = true;
                    prev[nextIndex] = node;
                }
            }
        }
        ArrayList<Point> path = new ArrayList<>();
        for (Node at = e; at != null; at = prev[at.index]) {
            path.add(new Point(at.x, at.y));
        }

        Collections.reverse(path);

        if (path.isEmpty()) return null;
        /* if start node and end node are connected, return the path */
        if (path.get(0).equals(new Point(s.x, s.y))) {
            return path;
        }
        return null;  // return null if cannot reconstruct
    }

    public ArrayList<Point> bfs2(Node s, State state) {
        return solve2(s, state);  // do a bfs staring at node s
    }

    public ArrayList<Point> solve2(Node s, State state) {
        LinkedList<Node> q = new LinkedList<>();
        q.addLast(s);

        boolean[] visited = new boolean[n];  // track if node i has been visited
        Arrays.fill(visited, false);
        visited[s.index] = true;

        Node[] prev = new Node[n];  // to reconstruct from start node to end node
        Arrays.fill(prev, null);
        Node e = null;
        while(!q.isEmpty()) {
            Node node = q.pollFirst();

            if (new Point(node.x, node.y).isSafe(state.bombs, state.walls, state.boxes)) {
                e = new Node(null, node.x, node.y);
                break;
            }

            ArrayList<Node> neighbours = g.get(node);  // node's neighbours
            for (Node next: neighbours) {

                int nextIndex = next.index;
                if (!visited[nextIndex]) {
                    q.addLast(next);
                    visited[nextIndex] = true;
                    prev[nextIndex] = node;
                }
            }
        }
        ArrayList<Point> path = new ArrayList<>();
        for (Node at = e; at != null; at = prev[at.index]) {
            path.add(new Point(at.x, at.y));
        }

        Collections.reverse(path);


        /* if start node and end node are connected, return the path */
        if (path.isEmpty()) return null;
        if (path.get(0).equals(new Point(s.x, s.y))) {
            return path;
        }
        return null;  // return null if cannot reconstruct
    }
}

class Point {
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

class Graph {
    protected static int n = ConstantField.WIDTH * ConstantField.HEIGHT;

    LinkedList<Integer> list[];

    public Graph() {
        list = new LinkedList[n];
        for (int i = 0; i < n ; i++) {
            list[i] = new LinkedList<>();
        }
    }

    public void addEdge(int source, int destination){

        //add edge
        list[source].addFirst(destination);

        //add back edge ((for undirected)
        // list[destination].addFirst(source);
    }

    public void printGraph(){
        for (int i = 0; i < n ; i++) {
            if(list[i].size()>0) {
                System.err.print("Point " + i + " is connected to: ");
//                for (int j = 0; j < list[i].size(); j++) {
//                    System.err.print(list[i].get(j) + " ");
//                }
                System.err.println(list[i]);
//                System.err.println();
            }
        }
    }

    /* get the neighbours of a given node */
    public ArrayList<Node> get(Node node) {
        ArrayList<Node> neighbours = new ArrayList<>();
        LinkedList<Integer> neighboursIndex = list[node.index];
        int x, y;
        for (int i: neighboursIndex) {  // i = 13
            x = i % ConstantField.WIDTH;  // x = 2, height = 11
            y = Math.floorDiv(i, ConstantField.WIDTH);  // y = 1
            // Log.log("index: " + i + "-> point: (" + x + ", " + y + ")");
            neighbours.add(new Node(node, x, y));
        }
        return neighbours;
    }

    /* get the neighbours of a given point */
    public ArrayList<Point> get(Point point) {
        ArrayList<Point> neighbours = new ArrayList<>();
        LinkedList<Integer> neighboursIndex = list[point.index];
        int x, y;
        for (int index: neighboursIndex) {  // i = 13
            x = index % ConstantField.WIDTH;  // x = 0, width = 13
            y = Math.floorDiv(index, ConstantField.WIDTH);  // y = 1
            // Log.log("index: " + i + "-> point: (" + x + ", " + y + ")");
            neighbours.add(new Point(x, y));
        }
        return neighbours;
    }
}
class Node {
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
class Wall extends Point {
    public Wall(int x, int y) {
        super(x, y);
    }

    public Wall(Wall wall) {
        super(wall.x, wall.y);
    }
}
enum Direction {

    UP(0),
    RIGHT(1),
    DOWN(2),
    LEFT(3);

    protected int value;

    Direction(int value) {
        this.value = value;
    }

}

class Bomb extends Point {
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

class Bfs {
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

class Box extends Point {
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
class Timeout {
    protected long startTime;

    public void start() {
        startTime = System.currentTimeMillis();
        // Log.log(String.format("%d", startTime));
    }

    public boolean isElapsed(double maxSpanSeconds) {
        long currentTime = System.currentTimeMillis();
        return currentTime - startTime >= maxSpanSeconds;
    }
}
class Item extends Point {
    protected int itemType;

    public Item(int x, int y, int itemType) {
        super(x, y);
        this.itemType = itemType;
    }
}
class State {
    protected Grid grid;
    protected ArrayList<Bomber> bombers;
    protected ArrayList<Bomb> bombs;
    protected ArrayList<Box> boxes;
    protected ArrayList<Wall> walls;
    protected ArrayList<Item> itemsExtraBomb;
    protected ArrayList<Item> itemsExtraRange;
    protected final int myId;

    protected Graph graph;
    protected Search search;
    public State(int myId) {
        this.grid = new Grid();
        this.bombers = new ArrayList<>();
        this.bombs = new ArrayList<>();
        this.boxes = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.itemsExtraBomb = new ArrayList<>();
        this.itemsExtraRange = new ArrayList<>();
        this.myId = myId;

        this.graph = new Graph();
        this.search = new Search(Graph.n, graph);

    }

    public State(State state) {
        this.grid = state.grid;
        this.walls = state.walls;
        this.bombers = new ArrayList<>();
        for(Bomber bomber: state.bombers) {
            Bomber copy = new Bomber(bomber);
            this.bombers.add(copy);
        }

        this.bombs = new ArrayList<>();
        for(Bomb bomb: state.bombs) {
            Bomb copy = new Bomb(bomb);
            this.bombs.add(copy);
        }

        this.boxes = new ArrayList<>();
        for(Box box: state.boxes) {
            Box copy = new Box(box);
            this.boxes.add(copy);
        }

        this.itemsExtraBomb = new ArrayList<>();
        this.itemsExtraRange = new ArrayList<>();
        this.myId = state.myId;
        this.graph = state.graph;
        this.search = state.search;

    }

    public State(Grid grid, ArrayList<Bomber> bombers, ArrayList<Bomb> bombs, ArrayList<Box> boxes, ArrayList<Wall> walls, ArrayList<Item> itemsExtraBomb, ArrayList<Item> itemsExtraRange, int myId, Graph graph, Search search) {
        this.grid = grid;
        this.bombers = bombers;
        this.bombs = bombs;
        this.boxes = boxes;
        this.walls = walls;
        this.itemsExtraBomb = itemsExtraBomb;
        this.itemsExtraRange = itemsExtraRange;
        this.myId = myId;
        this.graph = graph;
        this.search = search;
    }

    public void clear() {
        this.bombers.clear();
        this.boxes.clear();
        this.bombs.clear();
        this.graph = new Graph();
    }

    public ArrayList<Action> getAllLegalActions(State stateCopy) {
        ArrayList<Action> legalActions = new ArrayList<>();

        Bomber myBomber = stateCopy.bombers.get(stateCopy.myId);
        Action action;
        // get adj points
        ArrayList<Point> neighbourPoints = stateCopy.graph.get(myBomber);


        if (myBomber.hasBombs()) {  // can plant bomb
            action = new Action();
            action.bomb(myBomber);
            legalActions.add(action);
        }
        // moves
        for (Point point: neighbourPoints) {
            action = new Action();
            action.move(point);
            legalActions.add(action);
        }
        return legalActions;
    }

    public void explode(Bomb bomb) {
        //bombs.remove(bomb);
        bombers.get(bomb.owner).bombsRemain ++;

        /* check bomb */
        for (Bomb otherBomb: bombs) {
            if (bomb.willAffect(otherBomb, walls, boxes)) {
                // explode(otherBomb);  // recurse explodes bombs
            }
        }

        /* check bomber */
        for (Bomber bomber: bombers) {
            if (bomber.isDead) continue;  // ignore bombers who are dead in simulation
            if (bomb.willAffect(bomber, walls, boxes)) {
                bomber.isDead = true;  // set bomber as dead
            }
        }

        /* check box at last */
        ArrayList<Box> affectedBoxes = new ArrayList<>();
        for (Box box: boxes) {
            if (bomb.willAffect(box, walls, boxes)) {
                affectedBoxes.add(box);
                bombers.get(bomb.owner).boxDestroyed ++;  // add score
            }
        }
        for (Box box:affectedBoxes) {
            boxes.remove(box);
        }
    }

    public State update(Action action) {  // update one round
        Bomber myBomber = bombers.get(myId);
        // all bombs count down
        for (Bomb bomb: bombs) {
            bomb.roundsRemain --;
            if (bomb.roundsRemain == 0) {  // the bomb explodes
                explode(bomb);
            }
        }

        // my action
        switch (action.actionType) {
            case MOVE:
                myBomber.move(action.x, action.y);
                break;
            case BOMB:
                myBomber.bombsRemain --;
                Bomb newBomb = new Bomb(myBomber.x, myBomber.y, myId, ConstantField.MAX_ROUNDS_REMAIN, myBomber.explosionRange);
                bombs.add(newBomb);
                break;
            default:
        }
        return new State(grid, bombers, bombs, boxes, walls, itemsExtraBomb, itemsExtraRange, myId, graph, search);
    }

    public Box getClosestBox() {
        Bomber myBomber = bombers.get(myId);

        // get the closest box
        Box closestBox = boxes.get(0);
        double minDistance = Double.MAX_VALUE;
        double distance;
        for (Box box: boxes) {
            if (box.alreadyHasBomb(bombs)) continue;  // ignore the invalid boxes
            distance = myBomber.manhattanDistance(box);
            if (distance < minDistance) {
                closestBox = box;
                minDistance = distance;
            }
        }
        return closestBox;
    }
}

class Bomber extends Point {
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
