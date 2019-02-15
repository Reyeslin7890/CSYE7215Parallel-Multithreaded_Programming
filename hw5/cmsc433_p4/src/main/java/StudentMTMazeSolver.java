import javafx.geometry.Pos;

import java.util.*;
import java.util.concurrent.*;
// Author: Jin Lin

/**
 * This file needs to hold your solver to be tested.
 * You can alter the class to extend any class that extends MazeSolver.
 * It must have a constructor that takes in a Maze.
 * It must have a solve() method that returns the datatype List<Direction>
 * which will either be a reference to a list of steps to take or will
 * be null if the maze cannot be solved.
 */
public class StudentMTMazeSolver extends SkippingMazeSolver {
    private static final int nThread = Runtime.getRuntime().availableProcessors() + 1;
    private static volatile boolean flag = false;
    private final ExecutorService exec = Executors.newFixedThreadPool(nThread);

    public StudentMTMazeSolver(Maze maze) {
        super(maze);
    }

    public class SolutionNode {
        public SolutionNode parent;
        public Choice choice;

        public SolutionNode(SolutionNode parent, Choice choice) {
            this.parent = parent;
            this.choice = choice;
        }
    }


    public List<Direction> solve() {

        LinkedList<SPFA> tasks = new LinkedList<SPFA>();
        List<Direction> solution = null;
        List<Future<List<Direction>>> futures = new LinkedList<Future<List<Direction>>>();

        try {
            Choice start = firstChoice(maze.getStart());
            int size = start.choices.size();
            for (int i = 0; i < size; i++) {
                Choice newCh = follow(start.at, start.choices.pop());
                SolutionNode newNode = new SolutionNode(null, newCh);
                int size2 = newCh.choices.size();
                for (int j = 0; j < size2; j++) {
                    Choice Ch = follow(newCh.at, newCh.choices.pop());
                    SolutionNode SolNode = new SolutionNode(newNode, Ch);
                    tasks.add(new SPFA(SolNode));
                }
            }
            //System.out.println(nThread+" "+tasks.size());
        } catch (SolutionFound solutionFound) {
            solutionFound.printStackTrace();
        }
        try {
            futures = exec.invokeAll(tasks);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        while (!flag) continue;
        exec.shutdownNow();
        try {
            for (Future<List<Direction>> ans : futures)
                if (ans.get() != null) {
                    solution = ans.get();
                }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return solution;
    }

    private class SPFA implements Callable<List<Direction>> {

        SolutionNode sn;
        Direction exploring = null;

        public SPFA(SolutionNode sn) {
            this.sn = sn;
        }


        public List<SolutionNode> expand(SolutionNode node) throws SolutionFound {
            LinkedList<SolutionNode> result = new LinkedList<SolutionNode>();
            for (Direction dir : node.choice.choices) {
                exploring = dir;
                Choice newChoice = follow(node.choice.at, dir);
                result.add(new SolutionNode(node, newChoice));
            }
            return result;
        }

        @Override
        public List<Direction> call() throws Exception {
            LinkedList<SolutionNode> queue = new LinkedList<SolutionNode>();
            queue.push(sn);
            SolutionNode cur = null;
            try {
                while (queue.size() > 0) {
                    cur = queue.remove();
                    for (SolutionNode s : expand(cur))
                        if (!s.choice.isDeadend()) queue.add(s);
                }
                return null;

            } catch (SolutionFound e) {

                if (cur == null) {
                    return pathToFullPath(maze.getMoves(maze.getStart()));
                } else {
                    LinkedList<Direction> soln = new LinkedList<Direction>();
                    soln.push(exploring);
                    while (cur != null) {
                        try {
                            Choice mark = followMark(cur.choice.at, cur.choice.from, 1);
                            soln.push(mark.from);
                            cur = cur.parent;
                        } catch (SolutionFound e2) {
                            if (maze.getMoves(maze.getStart()).size() > 1) soln.push(e2.from);
                            return pathToFullPath(soln);
                        }
                    }
                    return pathToFullPath(soln);
                }
            } finally {
                flag = true;
            }
        }
    }
}
