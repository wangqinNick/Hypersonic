import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Agent {
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
