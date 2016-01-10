package org.IhorLitvinov.eolymp.problems.flight_planning;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    private Scanner scanner;
    private PrintWriter printWriter;
    private Tree tree;

    public Main(Scanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    public void scanData() {
        int citiesNumber = scanner.nextInt();
        List<Integer>[] adjacencyList = new List[citiesNumber];
        for (int flightIndex = 0; flightIndex < citiesNumber - 1; flightIndex++) {
            int nodeFrom = scanner.nextInt() - 1;
            int nodeTo = scanner.nextInt() - 1;
            if (adjacencyList[nodeFrom] == null) {
                adjacencyList[nodeFrom] = new ArrayList<>();
            }
            adjacencyList[nodeFrom].add(nodeTo);
            if (adjacencyList[nodeTo] == null) {
                adjacencyList[nodeTo] = new ArrayList<>();
            }
            adjacencyList[nodeTo].add(nodeFrom);
        }
        tree = new Tree(adjacencyList);
    }

    public void solveProblem() {
        DiameterNodes diameterNodes = new DiameterNodes(tree);
        Answer answer = diameterNodes.getSplit();
        printWriter.print(answer);
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

class Answer {
    private int diameter;
    private int secondNode;
    private int firstNode;
    private int newSecondNode;
    private int newFirstNode;


    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }


    public void setSecondNode(int secondNode) {
        this.secondNode = secondNode;
    }


    public void setFirstNode(int firstNode) {
        this.firstNode = firstNode;
    }


    public void setNewSecondNode(int newSecondNode) {
        this.newSecondNode = newSecondNode;
    }

    public void setNewFirstNode(int newFirstNode) {
        this.newFirstNode = newFirstNode;
    }

    @Override
    public String toString() {
        return diameter + "\n"
                + (firstNode + 1) + " " + (secondNode + 1) + "\n"
                + (newFirstNode + 1) + " " + (newSecondNode + 1) + "\n";
    }
}

class DiameterNodes {
    private List<DiameterNode> diameterNodes;
    private int diameter;
    private int[] diametersFirstTree;
    private int[] diametersSecondTree;


    private class DiameterNode {
        private int nodeIndex;
        List<DiameterNode> secondMaxWay;

        public DiameterNode(int nodeIndex, List<DiameterNode> secondMaxWay) {
            this.nodeIndex = nodeIndex;
            this.secondMaxWay = secondMaxWay;
        }
    }

    public DiameterNodes(Graph graph) {
        int mostDistantPoint = findBiggestPath(graph, 0)
                .get(0)
                .nodeIndex;
        graph.cleanMarks();
        diameterNodes = findBiggestPath(graph, mostDistantPoint);
        diameter = diameterNodes.size();
    }

    private List<DiameterNode> findBiggestPath(Graph graph, int startNode) {
        List<DiameterNode> secondMaxWay = Collections.emptyList();
        List<DiameterNode> maxWay = Collections.emptyList();
        graph.mark(startNode, 1);
        for (int child : graph.getNeighbors(startNode)) {
            if (graph.getMark(child) == 0) {
                List<DiameterNode> currentWay = findBiggestPath(graph, child);
                if (currentWay.size() > maxWay.size()) {
                    secondMaxWay = maxWay;
                    maxWay = currentWay;
                } else if (currentWay.size() > secondMaxWay.size()) {
                    secondMaxWay = currentWay;
                }
            }
        }
        if (maxWay.size() == 0) {
            maxWay = new ArrayList<>();
        }
        maxWay.add(new DiameterNode(startNode, secondMaxWay));
        return maxWay;
    }

    public Answer getSplit() {
        solveDiametersSplitTrees();
        return getSplitPair();
    }

    private void solveDiametersSplitTrees() {
        int oldMaxLength = 0;
        diametersFirstTree = new int[diameter - 1];
        for (int splitPoint = 0; splitPoint < diameter - 1; splitPoint++) {
            DiameterNode diameterNode = diameterNodes.get(splitPoint);
            List<DiameterNode> secondMaxWay = diameterNode.secondMaxWay;
            int secondMaxWayLength = secondMaxWay.size();
            int newProbablyDiameter = splitPoint + secondMaxWayLength;
            if (oldMaxLength < newProbablyDiameter) {
                oldMaxLength = newProbablyDiameter;
            }
            diametersFirstTree[splitPoint] = oldMaxLength;
        }
        oldMaxLength = 0;
        diametersSecondTree = new int[diameter - 1];
        for (int splitPoint = diameter - 2; splitPoint >= 0; splitPoint--) {
            DiameterNode diameterNode = diameterNodes.get(splitPoint + 1);
            List<DiameterNode> secondMaxWay = diameterNode.secondMaxWay;
            int secondMaxWayLength = secondMaxWay.size();
            int newProbablyDiameter = diameter - 2 - splitPoint
                    + secondMaxWayLength;
            if (oldMaxLength < newProbablyDiameter) {
                oldMaxLength = newProbablyDiameter;
            }
            diametersSecondTree[splitPoint] = oldMaxLength;
        }
    }

    private Answer getSplitPair() {
        int minimalMergeDiameter = maxLength(diametersFirstTree[0], diametersSecondTree[0]);
        int finalSplit = 0;
        for (int splitPoint = 1; splitPoint < diameter - 1; splitPoint++) {
            int currentMergeDiameter = maxLength(
                    diametersFirstTree[splitPoint], diametersSecondTree[splitPoint]);
            if (currentMergeDiameter < minimalMergeDiameter) {
                finalSplit = splitPoint;
                minimalMergeDiameter = currentMergeDiameter;
            }
        }
        Answer answer = new Answer();
        answer.setDiameter(minimalMergeDiameter);
        answer.setFirstNode(diameterNodes.get(finalSplit).nodeIndex);
        answer.setSecondNode(diameterNodes.get(finalSplit + 1).nodeIndex);
        answer.setNewFirstNode(diameterNodes.get(centerOfFirstTree(finalSplit)).nodeIndex);
        answer.setNewSecondNode(diameterNodes.get(centerOfSecondTree(finalSplit)).nodeIndex);
        return answer;
    }

    private int centerOfFirstTree(int finalSplit) {
        int firstTreeDiameter = diametersFirstTree[finalSplit];
        return firstTreeDiameter / 2;
    }

    private int centerOfSecondTree(int finalSplit) {
        int secondTreeDiameter = diametersSecondTree[finalSplit];
        return diameter - 1 - secondTreeDiameter / 2;
    }

    private int maxLength(int firstLength, int secondLength) {
        int half = (int) Math.ceil(0.5 * firstLength)
                + (int) Math.ceil(0.5 * secondLength) + 1;
        if (firstLength > secondLength) {
            if (firstLength > half) {
                return firstLength;
            }
            return half;
        }
        if (secondLength > half) {
            return secondLength;
        }
        return half;
    }
}

interface Graph {
    List<Integer> getNeighbors(int nodeIndex);

    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    void cleanMarks();

    int getNodesNumber();
}

class Tree implements Graph {
    private List<Integer>[] adjacencyList;
    private int[] nodesMarks;

    public Tree(List<Integer>[] adjacencyList) {
        this.adjacencyList = adjacencyList;
        nodesMarks = new int[adjacencyList.length];
    }

    @Override
    public List<Integer> getNeighbors(int nodeIndex) {
        if (adjacencyList[nodeIndex] != null) {
            return adjacencyList[nodeIndex];
        }
        return Collections.emptyList();
    }

    @Override
    public void mark(int nodeIndex, int mark) {
        nodesMarks[nodeIndex] = mark;
    }

    @Override
    public int getMark(int nodeIndex) {
        return nodesMarks[nodeIndex];
    }

    @Override
    public void cleanMarks() {
        nodesMarks = new int[adjacencyList.length];
    }

    @Override
    public int getNodesNumber() {
        return adjacencyList.length;
    }
}