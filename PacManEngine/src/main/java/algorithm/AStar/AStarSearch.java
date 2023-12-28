package algorithm.AStar;

import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.DM;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.PriorityQueue;

public class AStarSearch extends Controller<MOVE> {
    final int MIN_DISTANCE_FROM_GHOST; // minimum distance away from ghost, run away if less than minimum distance
    final int MAX_DISTANCE_FROM_EDIBLE_GHOST; // maximum distance away from edible ghost, get closer if less than
                                              // maximum distance
    final int SEARCH_DEPTH; // search depth

    ArrayList<Node> trackedNodes = new ArrayList<>();

    Controller<EnumMap<GHOST, MOVE>> ghostMoves;

    public AStarSearch(Controller<EnumMap<GHOST, MOVE>> ghostMoves, int minDistanceFromGhost,
            int maxDistanceFromEdibleGhost, int searchDepth) {
        this.ghostMoves = ghostMoves;
        this.MIN_DISTANCE_FROM_GHOST = minDistanceFromGhost;
        this.MAX_DISTANCE_FROM_EDIBLE_GHOST = maxDistanceFromEdibleGhost;
        this.SEARCH_DEPTH = searchDepth;
    }

    public MOVE getMove(Game game, long timeDue) {
        int pacmanCurrentNodeIndex = game.getPacmanCurrentNodeIndex();

        // calculate the shortest distance from pacman to the nearest ghost
        int shortestDistanceFromGhost = Integer.MAX_VALUE;
        GHOST nearestGhost = GHOST.values()[0];

        for (GHOST ghost : GHOST.values()) {
            int distanceFromGhost = game.getManhattanDistance(pacmanCurrentNodeIndex,
                    game.getGhostCurrentNodeIndex(ghost));
            if (distanceFromGhost < shortestDistanceFromGhost) {
                shortestDistanceFromGhost = distanceFromGhost;
                nearestGhost = ghost;
            }
        }

        // if the nearest ghost is edible and near enough, chase it
        if (game.getGhostEdibleTime(nearestGhost) > 0) {
            if (shortestDistanceFromGhost <= MAX_DISTANCE_FROM_EDIBLE_GHOST) {
                return game.getNextMoveTowardsTarget(pacmanCurrentNodeIndex,
                        game.getGhostCurrentNodeIndex(nearestGhost),
                        DM.MANHATTAN);
            }
        } else {
            // if the nearest ghost is not edible and near enough, run away from it
            if (shortestDistanceFromGhost < MIN_DISTANCE_FROM_GHOST) {
                return game.getNextMoveAwayFromTarget(pacmanCurrentNodeIndex,
                        game.getGhostCurrentNodeIndex(nearestGhost),
                        DM.MANHATTAN);
            }
        }

        return searchAndReturnBestMove(game);
    }

    // implements the A* search logic to find the best move for Pac Man
    // starts from the current game state and explores different possible moves,
    // evaluating each move's cost using the getCost function
    // maintains maintains a priority queue to select the node with the best score
    // (lowest cost)
    private MOVE searchAndReturnBestMove(Game game) {
        trackedNodes.add(new Node(game.copy(), null, null, 0, 0));
        PriorityQueue<Node> sortedBestNodes = new PriorityQueue<>(); // use priority queue to sort the best nodes,
                                                                     // lowest
                                                                     // cost node will be at the top

        for (int i = 0; i < SEARCH_DEPTH; i++) {
            Node n = trackedNodes.get(i);

            for (MOVE move : MOVE.values()) {
                Game _game = n.game.copy();

                for (int j = 0; j < 4; j++) {
                    _game.advanceGame(move, ghostMoves.getMove(_game.copy(), -1));
                }

                Node nextNode = new Node(_game, move, n, n.depth + 1, 0);

                double score = getCost(nextNode);
                nextNode.score = (int) score;

                sortedBestNodes.add(nextNode);
                trackedNodes.add(nextNode);
            }
        }

        Node bestNode = sortedBestNodes.remove();
        while (bestNode.depth != 1) {
            bestNode = bestNode.parent;
        }

        sortedBestNodes.clear();
        return bestNode.move;
    }

    // calculate the cost by combining various factors such as distance from active
    // pills, distance from active power pills, distance from ghosts, and cost
    private double getCost(Node node) {
        int index = node.game.getPacmanCurrentNodeIndex();

        double shortestDistanceFromActivePill = Integer.MAX_VALUE;
        int[] activePillsIndices = node.game.getActivePillsIndices();
        for (int activePillsIndice : activePillsIndices) {
            double dist = node.game.getEuclideanDistance(index, activePillsIndice);
            if (dist < shortestDistanceFromActivePill) {
                shortestDistanceFromActivePill = dist;
            }
        }

        double shortestDistanceFromActivePowerPill = Integer.MAX_VALUE;
        int[] activePowerPillsIndices = node.game.getActivePowerPillsIndices();
        for (int activePowerPillsIndice : activePowerPillsIndices) {
            double dist = node.game.getEuclideanDistance(index, activePowerPillsIndice);
            if (dist < shortestDistanceFromActivePowerPill) {
                shortestDistanceFromActivePowerPill = dist;
            }
        }

        double shortestDistanceFromGhost = Integer.MAX_VALUE;
        for (GHOST currentGhost : GHOST.values()) {
            double dist = node.game.getEuclideanDistance(index, node.game.getGhostCurrentNodeIndex(currentGhost));
            if (dist < shortestDistanceFromGhost) {
                shortestDistanceFromGhost = dist;
            }
        }

        double score = node.game.getScore();

        double cost = shortestDistanceFromActivePill + shortestDistanceFromActivePowerPill - shortestDistanceFromGhost
                - score;
        // System.out.println("shortestDistanceFromActivePill: " +
        // shortestDistanceFromActivePill);
        // System.out.println("shortestDistanceFromActivePowerPill: " +
        // shortestDistanceFromActivePowerPill);
        // System.out.println("shortestDistanceFromGhost: " +
        // shortestDistanceFromGhost);
        // System.out.println("node.game.getScore(): " + score);
        // System.out.println("cost: " + cost);

        return cost;
    }
}

class Node implements Comparable<Node> {
    public Game game;
    public MOVE move;
    public Node parent;
    public int depth;
    public int score;

    public Node(Game game, MOVE move, Node parent, int depth, int score) {
        this.game = game;
        this.move = move;
        this.parent = parent;
        this.depth = depth;
        this.score = score;
    }

    public int compareTo(Node n) {
        return Integer.compare(n.score, this.score);
    }
}