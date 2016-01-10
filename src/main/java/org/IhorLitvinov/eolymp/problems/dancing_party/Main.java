package org.IhorLitvinov.eolymp.problems.dancing_party;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.IntPredicate;

public class Main {
    private Scanner scanner;
    private PrintWriter printWriter;
    private Party party;
    private int maxRounds;
    private int pairsNumber;

    public Main(Scanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    private void scanData() {
        pairsNumber = scanner.nextInt();
        maxRounds = pairsNumber * pairsNumber;
        int unfavoredPairsNumber = scanner.nextInt();
        char[][] preferences = new char[pairsNumber][pairsNumber];
        for (int boyIndex = 0; boyIndex < pairsNumber; boyIndex++) {
            String likesString = scanner.next();
            preferences[boyIndex] = likesString.toCharArray();
        }
        party = new Party(preferences, unfavoredPairsNumber);
    }

    private int binarySearch(
            int maxLimit, int minLimit, IntPredicate needToReduce) {
        int median;
        int currentMax = maxLimit;
        int currentMin = minLimit;
        while (currentMin < currentMax) {
            median = (currentMax + currentMin) / 2;
            if (needToReduce.test(median)) {
                currentMax = median;
            } else {
                currentMin = median + 1;
            }
        }
        return currentMin - 1;
    }

    private void solveProblem() {
        IntPredicate isNotEnoughPairs = possibleRoundsNumber -> {
            party.update(possibleRoundsNumber);
            int averageNumberOfRounds =
                    MaxFlowFinder.maxFlow(party) / pairsNumber;
            return averageNumberOfRounds < possibleRoundsNumber;
        };
        printWriter.println(
                binarySearch(maxRounds, 0, isNotEnoughPairs));
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(new File("input.txt"));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(scanner, printWriter);
        main.scanData();
        main.solveProblem();
        printWriter.close();
    }
}

interface Followable {
    int getSourceNode();

    int getDestinationNode();
}

interface MatrixGraph {
    int[][] adjacencyMatrix();

    int getNodesNumber();
}

interface Marked {
    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    void cleanMarks();
}

class MaxFlowFinder {
    public static <T extends MatrixGraph
            & Marked & Followable> int maxFlow(T graph) {
        int maxFlow = 0;
        graph.cleanMarks();
        while (hasWay(graph)) {
            int minCapacity = minCapacity(graph);
            maxFlow += minCapacity;
            reduceCapacity(graph, minCapacity);
            graph.cleanMarks();
        }
        return maxFlow;
    }

    private static <T extends MatrixGraph
            & Marked & Followable> int minCapacity(T graph) {
        int[][] adjacencyMatrix = graph.adjacencyMatrix();
        int currentNode = graph.getDestinationNode();
        int parent = graph.getMark(currentNode) - 1;
        int minCapacity = adjacencyMatrix[parent][currentNode];
        currentNode = parent;
        parent = graph.getMark(currentNode) - 1;
        while (parent >= 0) {
            int currentCapacity = adjacencyMatrix[parent][currentNode];
            if (currentCapacity < minCapacity) {
                minCapacity = currentCapacity;
            }
            currentNode = parent;
            parent = graph.getMark(currentNode) - 1;
        }
        return minCapacity;
    }

    private static <T extends MatrixGraph & Marked
            & Followable> void reduceCapacity(T graph, int minCapacity) {
        int[][] adjacencyMatrix = graph.adjacencyMatrix();
        int currentNode = graph.getDestinationNode();
        int parent = graph.getMark(currentNode) - 1;
        while (parent >= 0) {
            adjacencyMatrix[parent][currentNode] -= minCapacity;
            adjacencyMatrix[currentNode][parent] += minCapacity;
            currentNode = parent;
            parent = graph.getMark(currentNode) - 1;
        }
    }

    private static <T extends MatrixGraph
            & Marked & Followable> boolean hasWay(T graph) {
        int source = graph.getSourceNode();
        graph.mark(source, -1);
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        while (!queue.isEmpty()) {
            int currentNode = queue.poll();
            int[] capacities = graph.adjacencyMatrix()[currentNode];
            for (int neighborIndex = 0; neighborIndex < capacities.length;
                 neighborIndex++) {
                if (capacities[neighborIndex] > 0
                        && graph.getMark(neighborIndex) == 0) {
                    graph.mark(neighborIndex, currentNode + 1);
                    if (neighborIndex == graph.getDestinationNode()) {
                        return true;
                    }
                    queue.add(neighborIndex);
                }
            }
        }
        return false;
    }
}

class Party implements MatrixGraph, Marked, Followable {
    private static final int NUMBER_OF_LAYERS = 4;
    private static final int BOYS_UNLIKE_LAYER = 1;
    private static final int GIRLS_UNLIKE_LAYER = 2;
    private static final int GIRLS_LAYER = 3;
    private int[][] adjacencyMatrix;
    private int unfavoredPairsNumber;
    private int nodesNumber;
    private int pairsNumber;
    private int[] marks;
    private char[][] preferences;

    public Party(char[][] preferences, int unfavoredPairsNumber) {
        this.preferences = preferences;
        this.unfavoredPairsNumber = unfavoredPairsNumber;
        pairsNumber = preferences.length;
        nodesNumber = pairsNumber * NUMBER_OF_LAYERS + 2;
        marks = new int[nodesNumber];
    }

    public void update(int roundsNumber) {
        adjacencyMatrix = new int[nodesNumber][nodesNumber];
        for (int boyIndex = 0; boyIndex < pairsNumber; boyIndex++) {
            for (int girlIndex = 0; girlIndex < pairsNumber; girlIndex++) {
                if (preferences[boyIndex][girlIndex] == 'Y') {
                    uniteNodes(boyIndex, girlsNode(girlIndex), 1);
                } else {
                    uniteNodes(boyUnlikeNode(boyIndex),
                            girlsUnlikeNode(girlIndex), 1);
                }
            }
        }
        for (int boyIndex = 0; boyIndex < pairsNumber; boyIndex++) {
            uniteNodes(boyIndex,
                    boyUnlikeNode(boyIndex), unfavoredPairsNumber);
        }
        for (int girlIndex = 0; girlIndex < pairsNumber; girlIndex++) {
            uniteNodes(girlsUnlikeNode(girlIndex),
                    girlsNode(girlIndex), unfavoredPairsNumber);
        }
        int sourceNode = getSourceNode();
        for (int boyNode = 0; boyNode < pairsNumber; boyNode++) {
            uniteNodes(sourceNode, boyNode, roundsNumber);
        }
        int destinationNode = getDestinationNode();
        for (int girlIndex = 0; girlIndex < pairsNumber; girlIndex++) {
            uniteNodes(girlsNode(girlIndex),
                    destinationNode, roundsNumber);
        }
    }

    private int boyUnlikeNode(int boyIndex) {
        return boyIndex + pairsNumber * BOYS_UNLIKE_LAYER;
    }

    private int girlsUnlikeNode(int girlIndex) {
        return girlIndex + pairsNumber * GIRLS_UNLIKE_LAYER;
    }

    private int girlsNode(int girlIndex) {
        return girlIndex + pairsNumber * GIRLS_LAYER;
    }

    private void uniteNodes(int nodeFrom, int nodeTo, int capacity) {
        adjacencyMatrix[nodeFrom][nodeTo] = capacity;
    }

    @Override
    public int[][] adjacencyMatrix() {
        return adjacencyMatrix;
    }

    @Override
    public int getNodesNumber() {
        return nodesNumber;
    }

    @Override
    public void mark(int nodeIndex, int mark) {
        marks[nodeIndex] = mark;
    }

    @Override
    public int getMark(int nodeIndex) {
        return marks[nodeIndex];
    }

    @Override
    public void cleanMarks() {
        marks = new int[nodesNumber];
    }

    @Override
    public int getSourceNode() {
        return NUMBER_OF_LAYERS * pairsNumber;
    }

    @Override
    public int getDestinationNode() {
        return NUMBER_OF_LAYERS * pairsNumber + 1;
    }
}