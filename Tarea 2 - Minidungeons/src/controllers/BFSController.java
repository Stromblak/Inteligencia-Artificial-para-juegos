package controllers;

import dungeon.play.GameCharacter;
import dungeon.play.PlayMap;
import util.math2d.Point2D;

import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;
import java.util.Set;

public class BFSController extends Controller {
    private Queue<Integer> directionQueue;
    private Set<Point2D> visitedPositions;

    public BFSController(PlayMap map, GameCharacter controllingChar) {
        super(map, controllingChar, "BFSController");
        directionQueue = new LinkedList<>();
        visitedPositions = new HashSet<>();
    }

    public BFSController(PlayMap map, GameCharacter controllingChar, String label) {
        super(map, controllingChar, label);
        directionQueue = new LinkedList<>();
        visitedPositions = new HashSet<>();
    }

    @Override
    public void reset() {
        if (directionQueue != null && !directionQueue.isEmpty())
            directionQueue.clear();

        if (visitedPositions != null && !visitedPositions.isEmpty())
            visitedPositions.clear();
    }

    @Override
    public int getNextAction() {
        int nextAction = getBFSAction();
        return nextAction;
    }

    private int getBFSAction() {
        Point2D currentPosition = controllingChar.getPosition();

        for (int i = 0; i < 4; i++) {
            Point2D nextPos = controllingChar.getNextPosition(i);
            if (map.isValidMove(nextPos) && !visitedPositions.contains(nextPos)) {
                directionQueue.add(i);
                visitedPositions.add(nextPos);
            }
        }

        if (!directionQueue.isEmpty()) {
            int nextDirection = directionQueue.poll();
            return nextDirection;
        }

        return PlayMap.IDLE;
    }
}
