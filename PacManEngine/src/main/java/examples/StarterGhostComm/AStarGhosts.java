package examples.StarterGhostComm;

import java.util.EnumMap;

import com.fossgalaxy.object.annotations.ObjectDef;

import pacman.controllers.IndividualGhostController;
import pacman.controllers.MASController;
import pacman.game.Constants.GHOST;

public class AStarGhosts extends MASController {

    public AStarGhosts() {
        this(50);
    }

    @ObjectDef("ASG")
    public AStarGhosts(int TICK_THRESHOLD) {
        super(true, new EnumMap<GHOST, IndividualGhostController>(GHOST.class));
        controllers.put(GHOST.BLINKY, new AStarGhost(GHOST.BLINKY, TICK_THRESHOLD));
        controllers.put(GHOST.INKY, new AStarGhost(GHOST.INKY, TICK_THRESHOLD));
        controllers.put(GHOST.PINKY, new AStarGhost(GHOST.PINKY, TICK_THRESHOLD));
        controllers.put(GHOST.SUE, new AStarGhost(GHOST.SUE, TICK_THRESHOLD));
    }

    @Override
    public String getName() {
        return "ASG";
    }
}
