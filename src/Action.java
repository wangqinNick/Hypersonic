public class Action {
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
