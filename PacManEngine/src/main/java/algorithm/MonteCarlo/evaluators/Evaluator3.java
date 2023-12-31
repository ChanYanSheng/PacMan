package algorithm.MonteCarlo.evaluators;

import java.util.Collection;

import algorithm.MonteCarlo.MonteCarloTree;
import algorithm.MonteCarlo.MonteCarloTreeNode;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

public class Evaluator3 implements TreeEvaluator {

    private int penalty;
    private static final int DEFAULT_PENALTY = 900;

    public Evaluator3(int penalty) {
        this.penalty = penalty;
    }

    public Evaluator3() {
        this(DEFAULT_PENALTY);
    }

    @Override
    public void evaluateTree(MonteCarloTree simulator) {
        Collection<MonteCarloTreeNode> children = simulator.getPacManChildren();

        if (children == null)
            return;

        Game game = simulator.getGameState();

        if (isPowerPillActive(game)) {
            for (MonteCarloTreeNode child : children) {
                if (child.isMoveEatsPowerPill()) {
                    child.addScoreBonus(-penalty);
                }
            }
        }
    }

    private boolean isPowerPillActive(Game game) {
        int edibleTime = game.getGhostEdibleTime(GHOST.BLINKY)
                + game.getGhostEdibleTime(GHOST.INKY)
                + game.getGhostEdibleTime(GHOST.PINKY)
                + game.getGhostEdibleTime(GHOST.SUE);

        return edibleTime > 0;
    }
}