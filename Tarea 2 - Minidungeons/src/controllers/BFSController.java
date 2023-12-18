package controllers;

import dungeon.play.GameCharacter;
import dungeon.play.PlayMap;
import java.util.LinkedList;
import java.util.Queue;
import util.math2d.Point2D;

public class BFSController extends Controller {
    Point2D[][] parent = new Point2D[map.getMapSizeX()][map.getMapSizeY()];
    LinkedList<Point2D> path = new LinkedList<>();

    public BFSController(PlayMap map, GameCharacter controllingChar) {
        super(map, controllingChar, "BFSController");
    }

    public BFSController(PlayMap map, GameCharacter controllingChar, String label) {
        super(map, controllingChar, label);
    }

    public int getNextAction() {
        Point2D currentPosition = controllingChar.getPosition();

        if (path.isEmpty()) {
            Point2D exit = bfs();
            reconstructPath(exit);
        }

        int a = -1;
        if (!path.isEmpty())
            a = moveAlongPath(currentPosition);
        return a;
    }

    private int moveAlongPath(Point2D currentPosition) {
        Point2D nextPosition = path.poll();
        if (currentPosition.y + 1 == nextPosition.y)
            return 2;
        else if (currentPosition.x + 1 == nextPosition.x)
            return 1;
        else if (currentPosition.y - 1 == nextPosition.y)
            return 0;
        else if (currentPosition.x - 1 == nextPosition.x)
            return 3;

        return -1;
    }

    public Point2D bfs() {
        Point2D root = controllingChar.getPosition();
        int rootX = (int) root.x;
        int rootY = (int) root.y;

        Queue<Point2D> queue = new LinkedList<>();
        // vida en cada pixel del mapa
        int[][] vida = new int[map.getMapSizeX()][map.getMapSizeY()];

        // vida inicial
        vida[rootX][rootY] = controllingChar.getHitpoints();
        queue.add(root);

        while (!queue.isEmpty()) {
            Point2D v = queue.poll();
            // vida actual en mi posicion
            int currentvida = vida[(int) v.x][(int) v.y];
            if (map.isExit((int) v.x, (int) v.y)) {
                reconstructPath(v);
                return v;
            }

            // explorar todas las acciones
            for (int a = 0; a < 4; a++) {
                // proxima pos, despues de ejecutar la accion
                Point2D nextPosition = new Point2D(v.x, v.y);
                if (a == 0)
                    nextPosition.y += 1;
                else if (a == 1)
                    nextPosition.x += 1;
                else if (a == 2)
                    nextPosition.y -= 1;
                else if (a == 3)
                    nextPosition.x -= 1;

                int nextX = (int) nextPosition.x;
                int nextY = (int) nextPosition.y;

                // si el movimiento es valido
                if (map.isValidMove(nextPosition)) {
                    int nextvida = currentvida;
                    // si hay un mounstruo, perdera vida
                    if (map.isMonster(nextX, nextY))
                        nextvida -= map.getMonsterChar(map.getMonsterIndex(nextX, nextY)).getDamage();

                    // ir a la pos que da mayor vida
                    if (nextvida > vida[nextX][nextY]) {
                        vida[nextX][nextY] = nextvida;
                        queue.add(nextPosition);
                        parent[nextX][nextY] = v;
                    }
                }
            }
        }
        return null;
    }

    private void reconstructPath(Point2D exit) {
        path.clear();

        if (exit == null) {
            return;
        }

        Point2D current = exit;
        while (current != null) {
            path.addFirst(current);
            current = parent[(int) current.x][(int) current.y];
        }
        path.poll();
    }
}
