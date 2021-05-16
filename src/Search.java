import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Search {
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
