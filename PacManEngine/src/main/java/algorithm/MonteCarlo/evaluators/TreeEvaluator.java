package algorithm.MonteCarlo.evaluators;

import algorithm.MonteCarlo.MonteCarloTree;

public interface TreeEvaluator {
    void evaluateTree(MonteCarloTree simulator);
}