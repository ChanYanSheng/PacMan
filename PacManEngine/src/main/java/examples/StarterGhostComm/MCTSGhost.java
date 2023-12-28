/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.StarterGhostComm;

/**
 *
 * @author asus
 */

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.comms.BasicMessage;
import pacman.game.comms.Message;
import pacman.game.comms.Messenger;

import java.util.Random;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MCTSGhost extends IndividualGhostController {
    private final static int SIMULATION_COUNT = 100;
    private final static int MAX_SIMULATION_MOVES = 100;
    private Node root;
    private Random rnd = new Random();
    private int TICK_THRESHOLD;
    public MCTSGhost(Constants.GHOST ghost) {
        this(ghost, 5);
    }

    public MCTSGhost(Constants.GHOST ghost, int TICK_THRESHOLD) {
        super(ghost);
        this.TICK_THRESHOLD = TICK_THRESHOLD;
    }

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        root = new Node(null, null, game);

        for (int i = 0; i < SIMULATION_COUNT; i++) {
            Node node = root;
            Game clonedGame = game.copy();

            // Selection and expansion
            while (!node.isTerminal() && node.isFullyExpanded()) {
                node = node.selectChild();
                Map<Constants.GHOST, Constants.MOVE> ghostMoves = new EnumMap<>(Constants.GHOST.class);
                ghostMoves.put(ghost, node.move);
                clonedGame.advanceGame(node.move, ghostMoves);
            }

            if (!node.isTerminal()) {
                Constants.MOVE[] untriedMoves = clonedGame.getPossibleMoves(clonedGame.getGhostCurrentNodeIndex(ghost), clonedGame.getGhostLastMoveMade(ghost));
                Constants.MOVE randomMove = untriedMoves[rnd.nextInt(untriedMoves.length)];
                Map<Constants.GHOST, Constants.MOVE> ghostMoves = new EnumMap<>(Constants.GHOST.class);
                ghostMoves.put(ghost, randomMove);
                clonedGame.advanceGame(randomMove, ghostMoves);
                node = node.addChild(randomMove, clonedGame);
            }

            // Simulation
            int score = performSimulation(clonedGame);

            // Backpropagation
            while (node != null) {
                node.update(score);
                node = node.parent;
            }
        }

        return root.getBestMove();
    }


    private int performSimulation(Game game) {
        Game clonedGame = game.copy();
        int simulationMoves = 0;

        while (simulationMoves < MAX_SIMULATION_MOVES) {
            Constants.MOVE[] possibleMoves = clonedGame.getPossibleMoves(clonedGame.getGhostCurrentNodeIndex(ghost), clonedGame.getGhostLastMoveMade(ghost));
            Constants.MOVE randomMove = possibleMoves[rnd.nextInt(possibleMoves.length)];
            Map<Constants.GHOST, Constants.MOVE> ghostMoves = new EnumMap<>(Constants.GHOST.class);
            ghostMoves.put(ghost, randomMove);
            clonedGame.advanceGame(randomMove, ghostMoves);

            // Check for terminal states
            if (clonedGame.gameOver()) {
                if (clonedGame.wasPacManEaten()) {
                    // Pac-Man was eaten
                    return 0; // Score can be adjusted based on your scoring mechanism
                } else {
                    // Game was won
                    return clonedGame.getScore(); // Adjust score accordingly
                }
            }
            
            simulationMoves++;
        }
        return clonedGame.getScore(); // Return current score after simulation moves
    }

    private static class Node {
        private Node parent;
        private final Constants.MOVE move;
        private final Game state;
        private final List<Node> children;
        private int visitCount;
        private int score;
        private static final double C = Math.sqrt(2);
        
        public Node(Node parent, Constants.MOVE move, Game state) {
            this.parent = parent;
            this.move = move;
            this.state = state;
            this.children = new ArrayList<>();
            this.visitCount = 0;
            this.score = 0;
            
        }

        public boolean isTerminal() {
            return state.gameOver();
        }

        public boolean isFullyExpanded() {
            Constants.GHOST ghostType = Constants.GHOST.BLINKY; // Replace BLINKY with the appropriate ghost type
            Constants.MOVE[] possibleMoves = state.getPossibleMoves(state.getGhostCurrentNodeIndex(ghostType), state.getGhostLastMoveMade(ghostType));
            return children.size() == possibleMoves.length;
        }

        public Node selectChild() {
            double maxUCT = Double.NEGATIVE_INFINITY;
        Node selectedNode = null;

        for (Node child : children) {
            double uctValue = (child.score / (double) child.visitCount) + C * Math.sqrt(Math.log(visitCount) / (double) child.visitCount);

            if (uctValue > maxUCT) {
                maxUCT = uctValue;
                selectedNode = child;
            }
        }
        return selectedNode;
        }

        public Node addChild(Constants.MOVE move, Game state) {
            Node child = new Node(this, move, state);
            children.add(child);
            return child;
        }

        public void update(int score) {
            visitCount++;
            this.score += score;
        }

        public Constants.MOVE getBestMove() {
            double maxScore = Double.NEGATIVE_INFINITY;
        Node bestChild = null;

        for (Node child : children) {
            if (child.visitCount > 0) {
                double averageScore = child.score / (double) child.visitCount;
                if (averageScore > maxScore) {
                    maxScore = averageScore;
                    bestChild = child;
                }
            }
        }

        return (bestChild != null) ? bestChild.move : null;
         
        }
    }
}
