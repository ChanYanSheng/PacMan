import examples.StarterGhostComm.MCTSGhost;
import algorithm.DFS.DFS;
import examples.StarterGhostComm.Blinky;
import examples.StarterGhostComm.Inky;
import examples.StarterGhostComm.Pinky;
import examples.StarterGhostComm.Sue;
import pacman.Executor;
import pacman.controllers.IndividualGhostController;
import pacman.controllers.MASController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants.*;
import pacman.game.internal.POType;

import java.util.EnumMap;

import algorithm.AStar.AStarSearch;
import algorithm.MonteCarlo.MonteCarloTreeSearch;

/**
 * Created by pwillic on 06/05/2016.
 */
public class Main {

    public static void main(String[] args) {

        int sightRadius = 10; // 5000 is maximum

        Executor executor = new Executor.Builder()
                .setVisual(true)
                .setPacmanPO(false)
                .setTickLimit(200000)
                .setScaleFactor(3) // Increase game visual size
                .setPOType(POType.RADIUS) // pacman sense objects around it in a radius wide fashion instead of straight
                                          // line sights
                .setSightLimit(sightRadius) // The sight radius limit, set to maximum
                .build();

        EnumMap<GHOST, IndividualGhostController> controllers = new EnumMap<>(GHOST.class);

        controllers.put(GHOST.INKY, new Inky());
        controllers.put(GHOST.BLINKY, new Blinky());
        controllers.put(GHOST.PINKY, new Pinky());
        controllers.put(GHOST.SUE, new Sue());

        int speed = 1; // smaller number will run faster

        MASController ghosts = new POCommGhosts(50);
        // executor.runGameTimed(new MonteCarlo(), new MASController(controllers));
//         executor.runGame(new MonteCarloTreeSearch(), ghosts, speed);
//        executor.runGame(new AStarSearch(ghosts, 15, 15, 20), ghosts, speed);
//         executor.runGame(new TreeSearchPacMan(), ghosts, speed);
//        executor.runGame(new GeneticPacman2(new Genome(473, 3.1854737562458, 688, 9768, -8513, 3055, 521, 79, 4)),ghosts,speed);

        executor.runGame(new DFS(),ghosts,speed);
        //executor.runGame(new MonteCarloTreeSearch(),ghosts,speed);
        //executor.runGame(new AStarSearch(ghosts, 15, 15, 20), ghosts, speed);
        
    }
}
