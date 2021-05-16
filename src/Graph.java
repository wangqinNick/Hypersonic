import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class Graph {
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
