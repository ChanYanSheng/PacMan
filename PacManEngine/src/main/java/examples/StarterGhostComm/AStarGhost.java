package examples.StarterGhostComm;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashSet;

public class AStarGhost extends IndividualGhostController {
    private int TICK_THRESHOLD;

    public AStarGhost(GHOST ghost, int TICK_THRESHOLD) {
        super(ghost);
        this.TICK_THRESHOLD = TICK_THRESHOLD;
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        int ghostIndex = game.getGhostCurrentNodeIndex(ghost);
        int pacmanIndex = game.getPacmanCurrentNodeIndex();

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingDouble(node -> node.fCost));
        HashSet<Integer> visited = new HashSet<>();

        int initialDistance = game.getManhattanDistance(ghostIndex, pacmanIndex);
        Node initialNode = new Node(ghostIndex, null, 0.0, initialDistance);
        frontier.add(initialNode);

        while (!frontier.isEmpty()) {
            Node currentNode = frontier.poll();

            if (currentNode.index == pacmanIndex) {
                return reconstructPath(currentNode, ghostIndex, game);
            }

            visited.add(currentNode.index);

            for (int neighbor : game.getNeighbouringNodes(currentNode.index)) {
                if (!visited.contains(neighbor)) {
                    double neighborCost = currentNode.gCost + 1.0; // Assuming uniform cost
                    double heuristic = game.getManhattanDistance(neighbor, pacmanIndex);
                    double fCost = neighborCost + heuristic;
                    frontier.add(new Node(neighbor, currentNode, neighborCost, fCost));
                }
            }
        }

        return MOVE.NEUTRAL; // No path found or other conditions, return a default move
    }

    private MOVE reconstructPath(Node goalNode, int ghostIndex, Game game) {
        Node currentNode = goalNode;
        while (currentNode.parent != null && currentNode.parent.index != ghostIndex) {
            currentNode = currentNode.parent;
        }

        // If the current node's parent is the ghost's current position
        if (currentNode.parent != null && currentNode.parent.index == ghostIndex) {
            return game.getNextMoveTowardsTarget(ghostIndex, currentNode.index, DM.PATH);
        }

        // If no valid path found or other conditions, return a default move
        return MOVE.NEUTRAL;
    }

    class Node {
        int index;
        Node parent;
        double gCost; // Cost from start node to current node
        double fCost; // fCost = gCost + heuristic cost

        public Node(int index, Node parent, double gCost, double fCost) {
            this.index = index;
            this.parent = parent;
            this.gCost = gCost;
            this.fCost = fCost;
        }
    }

}
