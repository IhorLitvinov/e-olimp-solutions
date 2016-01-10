package org.IhorLitvinov.eolymp.problems.kings_tour;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private Scanner scanner;
    private PrintWriter printWriter;
    private Chessboard chessboard;
    private int kingIndex;
    private int aPawnIndex;

    public Main(Scanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    public void scanData() {
        String data = scanner.next();
        char kingLetter = data.charAt(0);
        char kingNumber = data.charAt(1);
        kingIndex = Chessboard.getFigureIndex(kingLetter, kingNumber);
        data = scanner.next();
        char aPawnLetter = data.charAt(0);
        char aPawnNumber = data.charAt(1);
        aPawnIndex = Chessboard.getFigureIndex(aPawnLetter, aPawnNumber);
        data = scanner.next();
        char bPawnLetter = data.charAt(0);
        char bPawnNumber = data.charAt(1);
        int bPawnIndex = Chessboard.getFigureIndex(bPawnLetter, bPawnNumber);
        chessboard = new Chessboard(kingIndex, aPawnIndex, bPawnIndex);
    }

    public void solveProblem() {
        int pathLength = chessboard.findFastestPath();
        printWriter.println(pathLength);
    }

    public boolean hasNextTest() {
        return scanner.hasNext();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(new File("input.txt"));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(scanner, printWriter);
        while (main.hasNextTest()) {
            main.scanData();
            main.solveProblem();
        }
        scanner.close();
        printWriter.close();
    }
}

interface NodeChecker {
    boolean nodeValid(int nodeIndex);
}

interface Graph {
    List<Integer> getNeighbors(int nodeIndex);

    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    int getNodesNumber();

    NodeChecker getNodeChecker();
}

class GraphUtil {

    public static int lowestPathSolver(
            int start, Graph graph, int end) {
        NodeChecker checker = graph.getNodeChecker();
        Queue<Integer> nodesQueue = new LinkedList<>();
        nodesQueue.add(start);
        int pathLength = 1;
        graph.mark(start, pathLength);

        while (!nodesQueue.isEmpty()) {
            int currentNodeIndex = nodesQueue.poll();
            pathLength = graph.getMark(currentNodeIndex);
            List<Integer> neighbors = graph.getNeighbors(currentNodeIndex);
            for (int neighbor : neighbors) {
                if (checker.nodeValid(neighbor)) {
                    if (neighbor == end) {
                        return pathLength;
                    }
                    nodesQueue.add(neighbor);
                    graph.mark(neighbor, pathLength + 1);
                }
            }
        }
        return -1;
    }
}

class Chessboard implements Graph {
    static final int A_PAWN_ATTACK_MARK = -1;
    static final int B_PAWN_ATTACK_MARK = -2;
    private static final char DESK_LETTER_START = 'a';
    private static final char DESK_NUMBER_START = '1';
    private static final int DESK_LENGTH = 8;
    private static final int MAX_NEIGHBORS_NUMBER = 4;
    private int kingIndex;
    private int aPawnIndex;
    private int bPawnIndex;
    private int[] cellMarks = new int[DESK_LENGTH * DESK_LENGTH];

    public Chessboard(int kingIndex, int aPawnIndex, int bPawnIndex) {
        this.kingIndex = kingIndex;
        this.aPawnIndex = aPawnIndex;
        this.bPawnIndex = bPawnIndex;
        markPawnsAttacks(aPawnIndex, A_PAWN_ATTACK_MARK);
        markPawnsAttacks(bPawnIndex, B_PAWN_ATTACK_MARK);
    }

    private void markPawnsAttacks(int pawnIndex, int mark) {
        if (hasFront(pawnIndex)) {
            if (hasRight(pawnIndex)) {
                cellMarks[pawnIndex + DESK_LENGTH + 1] = mark;
            }
            if (hasLeft(pawnIndex)) {
                cellMarks[pawnIndex + DESK_LENGTH - 1] = mark;
            }
        }
    }

    public static int getFigureIndex(char letter, char number) {
        int columnIndex = letter - DESK_LETTER_START;
        int lineIndex = number - DESK_NUMBER_START;
        return columnIndex + DESK_LENGTH * lineIndex;
    }

    public int getBPawnIndex() {
        return bPawnIndex;
    }

    public int findFastestPath() {
        if (getMark(aPawnIndex) == B_PAWN_ATTACK_MARK) {
            return GraphUtil.lowestPathSolver(
                    kingIndex, this, bPawnIndex) + 1;
        } else {
            return GraphUtil.lowestPathSolver(
                    kingIndex, this, aPawnIndex);
        }
    }

    @Override
    public List<Integer> getNeighbors(int nodeIndex) {
        List<Integer> neighbors = new ArrayList<>(MAX_NEIGHBORS_NUMBER);
        if (hasFront(nodeIndex)) {
            neighbors.add(nodeIndex + DESK_LENGTH);
            if (hasRight(nodeIndex)) {
                neighbors.add(nodeIndex + DESK_LENGTH + 1);
            }
            if (hasLeft(nodeIndex)) {
                neighbors.add(nodeIndex + DESK_LENGTH - 1);
            }
        }
        if (hasRear(nodeIndex)) {
            neighbors.add(nodeIndex - DESK_LENGTH);
            if (hasRight(nodeIndex)) {
                neighbors.add(nodeIndex - DESK_LENGTH + 1);
            }
            if (hasLeft(nodeIndex)) {
                neighbors.add(nodeIndex - DESK_LENGTH - 1);
            }
        }
        if (hasLeft(nodeIndex)) {
            neighbors.add(nodeIndex - 1);
        }
        if (hasRight(nodeIndex)) {
            neighbors.add(nodeIndex + 1);
        }
        return neighbors;
    }

    private boolean hasLeft(int index) {
        return index % DESK_LENGTH != 0;
    }

    private boolean hasRight(int index) {
        return index % DESK_LENGTH != (DESK_LENGTH - 1);
    }

    private boolean hasRear(int index) {
        return (index - DESK_LENGTH) >= 0;
    }

    private boolean hasFront(int index) {
        return (index + DESK_LENGTH) < getNodesNumber();
    }

    @Override
    public void mark(int nodeIndex, int mark) {
        if (nodeIndex == bPawnIndex) {
            markPawnsAttacks(bPawnIndex, 0);
        }
        cellMarks[nodeIndex] = mark;
    }

    @Override
    public int getMark(int nodeIndex) {
        return cellMarks[nodeIndex];
    }

    @Override
    public int getNodesNumber() {
        return DESK_LENGTH * DESK_LENGTH;
    }

    @Override
    public NodeChecker getNodeChecker() {
        return new CellChecker(this);
    }
}

class CellChecker implements NodeChecker {
    private Chessboard chessboard;

    public CellChecker(Chessboard chessboard) {
        this.chessboard = chessboard;
    }

    @Override
    public boolean nodeValid(int nodeIndex) {
        int nodeMark = chessboard.getMark(nodeIndex);
        return nodeMark == 0;
    }
}