import java.util.ArrayList;
public class State {
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
