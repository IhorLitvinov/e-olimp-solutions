package org.IhorLitvinov.eolymp.problems.trees_in_the_garden;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;


public class Main {
    private Scanner scanner;
    private PrintWriter printWriter;
    private TreesGarden treesGarden;
    private int maxLength = 0;

    public Main(Scanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    public void scanData() {
        int treesNumber = scanner.nextInt();
        int [][] treesFootpaths = new int[treesNumber - 1][treesNumber];
        for (int treeIndex = 0; treeIndex < treesNumber - 1; treeIndex++) {
            for (int treeNeighborIndex = treeIndex + 1;
                 treeNeighborIndex < treesNumber; treeNeighborIndex++) {
                int nextFootpath = scanner.nextInt();
                treesFootpaths[treeIndex][treeNeighborIndex] = nextFootpath;
                if (nextFootpath > maxLength) {
                    maxLength = nextFootpath;
                }
            }
        }
        treesGarden = new TreesGarden(treesFootpaths);
    }

    private int searchAnswer(int start, int end) {
        int middle = (start + end) / 2;
        while (start != end) {
            if (treesGarden.maySplitWith(middle)) {
                System.out.println("treesGarden.maySplitWith(middle) " + middle);
                end = middle;
            } else {
                System.out.println("NO !(middle) " + middle);
                start = middle + 1;
            }
            middle = (start + end) / 2;
        }
        return start;
    }

    public void solveProblem() {
        printWriter.println(searchAnswer(0, maxLength));
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(new File("input.txt"));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(scanner, printWriter);
        main.scanData();
        main.solveProblem();
        scanner.close();
        printWriter.close();
    }
}

class TreesGarden implements MarkableGraph {
    private int[][] footpathsMatrix;
    private int[] treesMarks;
    private int minInverseLength = 0;

    public TreesGarden(int[][] footpathsMatrix) {
        this.footpathsMatrix = footpathsMatrix;
    }

    public boolean maySplitWith(int biggestLength) {
        minInverseLength = biggestLength;
        treesMarks = new int[getNodesNumber()];
        return GraphUtil.isDicotyledonous(this, 1, 2);
    }

    private boolean isDistanceMoreMinLength(int firstTree, int secondTree) {
        if (firstTree < secondTree) {
            return footpathsMatrix[firstTree][secondTree] > minInverseLength;
        }
        return secondTree > firstTree
                && footpathsMatrix[secondTree][firstTree] > minInverseLength;
    }

    @Override
    public List<Integer> getNeighbors(int nodeIndex) {
        List<Integer> neighbors = new LinkedList<>();
        for (int neighborIndex = 0; neighborIndex < getNodesNumber();
             neighborIndex++) {
            if (isDistanceMoreMinLength(nodeIndex, neighborIndex)) {
                neighbors.add(neighborIndex);
            }
        }
        return neighbors;
    }

    @Override
    public void mark(int nodeIndex, int mark) {
        treesMarks[nodeIndex] = mark;
    }

    @Override
    public int getMark(int nodeIndex) {
        return treesMarks[nodeIndex];
    }

    @Override
    public int getNodesNumber() {
        return footpathsMatrix.length + 1;
    }

    @Override
    public NodeChecker getNodeChecker() {
        return nodeIndex -> treesMarks[nodeIndex] == 0;
    }
}

interface MarkableGraph {
    List<Integer> getNeighbors(int nodeIndex);

    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    int getNodesNumber();

    NodeChecker getNodeChecker();
}

class GraphUtil {

    public static boolean isDicotyledonous(
            MarkableGraph graph, int firstMark, int secondMark) {
        NodeChecker checker = graph.getNodeChecker();
        Queue<Integer> nodesQueue = new LinkedList<>();
        nodesQueue.add(0);
        int parentMark;
        graph.mark(0, firstMark);

        while (!nodesQueue.isEmpty()) {
            int currentNodeIndex = nodesQueue.poll();
            parentMark = graph.getMark(currentNodeIndex);
            List<Integer> neighbors = graph.getNeighbors(currentNodeIndex);
            for (int neighbor : neighbors) {
                if (graph.getMark(neighbor) == parentMark) {
                    return false;
                } else if (checker.nodeValid(neighbor)) {
                    nodesQueue.add(neighbor);
                    int newMark;
                    if (parentMark == firstMark) {
                        newMark = secondMark;
                    } else {
                        newMark = firstMark;
                    }
                    graph.mark(neighbor, newMark);
                }
            }
        }
        return true;
    }
}

interface NodeChecker {
    boolean nodeValid(int nodeIndex);
}