import java.util.Scanner;

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