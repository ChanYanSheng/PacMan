package examples.StarterGhostComm;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.comms.BasicMessage;
import pacman.game.comms.Message;
import pacman.game.comms.Messenger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by pwillic on 25/02/2016.
 */

public class GeneticGhost extends IndividualGhostController {
    private final static float CONSISTENCY = 0.9f; // attack Ms Pac-Man with this probability
    private final static int PILL_PROXIMITY = 15; // if Ms Pac-Man is this close to a power pill, back away
    Random rnd = new Random();
    private int TICK_THRESHOLD;
    private int lastPacmanIndex = -1;
    private int tickSeen = -1;
    private int CLOSE_BOUNDARY = 2;
    private int[] values = new int[] { 1, 5, 5, 5, 5, 6, 7, 9, 9, 10 };
    int BLUE_PACMAN_WEIGHT;

    int GHOST_COST;
    int BLUE_BONUS;

    int BLUE_BLUE_COST;
    int BLUE_GHOST_BONUS;

    int SCALE_GC;
    int SCALE_BB;
    int SCALE_BBC;
    int SCALE_BGB;

    public GeneticGhost(Constants.GHOST ghost) {
        this(ghost, 5);
        this.BLUE_PACMAN_WEIGHT = 100;

        this.SCALE_GC = values[4];
        this.SCALE_BB = values[5];
        this.SCALE_BBC = values[6];
        this.SCALE_BGB = values[7];

    }

    public GeneticGhost(Constants.GHOST ghost, int TICK_THRESHOLD) {
        super(ghost);
        this.TICK_THRESHOLD = TICK_THRESHOLD;
    }

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        // Housekeeping - throw out old info
        int currentTick = game.getCurrentLevelTime();
        if (currentTick <= 2 || currentTick - tickSeen >= TICK_THRESHOLD) {
            lastPacmanIndex = -1;
            tickSeen = -1;
        }

        // Can we see PacMan? If so tell people and update our info
        int pacmanIndex = game.getPacmanCurrentNodeIndex();
        int currentIndex = game.getGhostCurrentNodeIndex(ghost);
        Messenger messenger = game.getMessenger();
        if (pacmanIndex != -1) {
            lastPacmanIndex = pacmanIndex;
            tickSeen = game.getCurrentLevelTime();
            if (messenger != null) {
                messenger.addMessage(new BasicMessage(ghost, null, BasicMessage.MessageType.PACMAN_SEEN, pacmanIndex,
                        game.getCurrentLevelTime()));
            }
        }

        this.GHOST_COST = (int) (values[0] * scale(game, SCALE_GC));
        this.BLUE_BONUS = (int) (values[1] * scale(game, SCALE_BB));

        this.BLUE_BLUE_COST = (int) (values[2] * scale(game, SCALE_BBC));
        this.BLUE_GHOST_BONUS = (int) (values[3] * scale(game, SCALE_BGB));

        // Has anybody else seen PacMan if we haven't?
        if (pacmanIndex == -1 && game.getMessenger() != null) {
            for (Message message : messenger.getMessages(ghost)) {
                if (message.getType() == BasicMessage.MessageType.PACMAN_SEEN) {
                    if (message.getTick() > tickSeen && message.getTick() < currentTick) { // Only if it is newer
                                                                                           // information
                        lastPacmanIndex = message.getData();
                        tickSeen = message.getTick();
                    }
                }
            }
        }
        if (pacmanIndex == -1) {
            pacmanIndex = lastPacmanIndex;
        }

        Boolean requiresAction = game.doesGhostRequireAction(ghost);
        if (requiresAction != null && requiresAction) // if ghost requires an action
        {
            if (pacmanIndex != -1) {
                if (game.getGhostEdibleTime(ghost) > 0 || closeToPower(game)) // retreat from Ms Pac-Man if edible or if
                                                                              // Ms Pac-Man is close to power pill
                {
                    try {
                        return game.getApproximateNextMoveAwayFromTarget(currentIndex,
                                game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), Constants.DM.PATH);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(e);
                        System.out.println(pacmanIndex + " : " + currentIndex);
                    }
                } else {
                    if (rnd.nextFloat() < CONSISTENCY) { // attack Ms Pac-Man otherwise (with certain probability)
                        try {
                            MOVE[] possibilities = game.getPossibleMoves(currentIndex,
                                    game.getGhostLastMoveMade(ghost));
                            boolean sameLoc = false;

                            for (GHOST ghostFriend : GHOST.values()) {
                                int distance = (game.getShortestPathDistance(currentIndex,
                                        game.getGhostCurrentNodeIndex(ghostFriend)));
                                if ((ghostFriend != ghost) && distance < this.CLOSE_BOUNDARY && distance >= 0) {
                                    if (game.getShortestPathDistance(currentIndex,
                                            game.getPacmanCurrentNodeIndex()) >= game.getShortestPathDistance(
                                                    game.getGhostCurrentNodeIndex(ghostFriend),
                                                    pacmanIndex)) {
                                        sameLoc = true;
                                    }
                                }
                            }

                            if (sameLoc) {
                                return possibilities[rnd.nextInt(possibilities.length)];
                            }

                            else if (game.getGhostEdibleTime(ghost) > 0) {
                                return getBlueMove(game, ghost, possibilities);
                            }

                            else {
                                return getColorfulMove(game, ghost, possibilities);
                            }
                            // Constants.MOVE move = game.getApproximateNextMoveTowardsTarget(
                            // game.getGhostCurrentNodeIndex(ghost),
                            // pacmanIndex, game.getGhostLastMoveMade(ghost), Constants.DM.PATH);
                            // return move;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println(e);
                            System.out.println(pacmanIndex + " : " + currentIndex);
                        }
                    }
                }
            } else {
                Constants.MOVE[] possibleMoves = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost),
                        game.getGhostLastMoveMade(ghost));
                return possibleMoves[rnd.nextInt(possibleMoves.length)];
            }
        }
        return null;
    }

    private float scale(Game game, int maxScore) {
        float score = game.getScore();
        if (score >= maxScore) {
            return 1;
        } else {
            return score / maxScore;
        }
    }

    private MOVE getBlueMove(Game game, GHOST ghost, MOVE[] possibilities) {
        Map<MOVE, Integer> weights = new HashMap<MOVE, Integer>(possibilities.length);
        int myLoc = game.getGhostCurrentNodeIndex(ghost);

        for (MOVE move : possibilities) {
            int neighbor = game.getNeighbour(myLoc, move);
            int pacLoc = game.getPacmanCurrentNodeIndex();
            int score = game.getShortestPathDistance(pacLoc, neighbor) * (BLUE_PACMAN_WEIGHT);
            for (GHOST ghostFriend : GHOST.values()) {
                int ghostFriendLoc = game.getGhostCurrentNodeIndex(ghostFriend);
                int distance = game.getShortestPathDistance(pacLoc, ghostFriendLoc);
                if (ghostFriend != ghost && distance > 0) {
                    int[] path = new int[distance];
                    path = game.getShortestPath(pacLoc, ghostFriendLoc);
                    for (int node : path) {
                        if (node == neighbor && game.getGhostEdibleTime(ghostFriend) > 0) {
                            score += BLUE_BLUE_COST;
                        } else if (node == neighbor) {
                            score += BLUE_GHOST_BONUS;
                        }
                    }
                }
            }
            weights.put(move, score);
        }

        int bestScore = Integer.MIN_VALUE;
        MOVE best = null;
        for (MOVE move : possibilities) {
            if (weights.get(move) > bestScore) {
                bestScore = weights.get(move);
                best = move;
            }
        }

        return best;

    }

    private MOVE getColorfulMove(Game game, GHOST ghost, MOVE[] possibilities) {

        Map<MOVE, Integer> weights = new HashMap<MOVE, Integer>(possibilities.length);
        int myLoc = game.getGhostCurrentNodeIndex(ghost);

        for (MOVE move : possibilities) {
            int neighbor = game.getNeighbour(myLoc, move);
            int[] path = game.getShortestPath(neighbor, game.getPacmanCurrentNodeIndex(), move);
            int score = path.length;
            for (GHOST ghostFriend : GHOST.values()) {
                if (ghostFriend != ghost) {
                    int ghostFriendLoc = game.getGhostCurrentNodeIndex(ghostFriend);
                    for (int node : path) {
                        if (node == ghostFriendLoc && game.getGhostEdibleTime(ghostFriend) > 0) {
                            score -= BLUE_BONUS;
                        } else if (node == ghostFriendLoc) {
                            score -= GHOST_COST;
                        }
                    }
                }
            }
            weights.put(move, score);
        }

        int bestScore = Integer.MAX_VALUE;
        MOVE best = null;
        for (MOVE move : possibilities) {
            if (weights.get(move) < bestScore) {
                bestScore = weights.get(move);
                best = move;
            }
        }

        return best;
    }

    // This helper function checks if Ms Pac-Man is close to an available power pill
    private boolean closeToPower(Game game) {
        int[] powerPills = game.getPowerPillIndices();

        for (int i = 0; i < powerPills.length; i++) {
            Boolean powerPillStillAvailable = game.isPowerPillStillAvailable(i);
            int pacmanNodeIndex = game.getPacmanCurrentNodeIndex();
            if (pacmanNodeIndex == -1) {
                pacmanNodeIndex = lastPacmanIndex;
            }
            if (powerPillStillAvailable == null || pacmanNodeIndex == -1) {
                return false;
            }
            if (powerPillStillAvailable
                    && game.getShortestPathDistance(powerPills[i], pacmanNodeIndex) < PILL_PROXIMITY) {
                return true;
            }
        }

        return false;
    }
}