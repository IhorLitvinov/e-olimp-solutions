package org.IhorLitvinov.eolymp.problems.graph_of_operations;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

public class Main {
    private static final String XOR = "XOR";
    private static final String OR = "OR";
    private static final String AND = "AND";
    private int[][] adjacencyMatrix;
    private TwoSatGraph graph;

    private FastScanner scanner;
    private PrintWriter printWriter;

    public Main(FastScanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    private void parseData(int firstVertex, int secondVertex, int value, String operator) {
        switch (operator) {
            case AND:
                if (value == 1) {
                    joinFalseTrue(firstVertex, secondVertex);
                    joinTrue(firstVertex, secondVertex);
                    joinFalse(firstVertex, secondVertex);
                } else {
                    joinTrueFalse(firstVertex, secondVertex);
                }
                break;
            case OR:
                if (value == 1) {
                    joinFalseTrue(firstVertex, secondVertex);
                } else {
                    joinTrueFalse(firstVertex, secondVertex);
                    joinTrue(firstVertex, secondVertex);
                    joinFalse(firstVertex, secondVertex);
                }
                break;
            case XOR:
                if (value == 1) {
                    joinTrueFalse(firstVertex, secondVertex);
                    joinFalseTrue(firstVertex, secondVertex);
                } else {
                    joinTrue(firstVertex, secondVertex);
                    joinFalse(firstVertex, secondVertex);
                }
                break;
        }
    }

    private int falseNodeIndex(int node) {
        return node * 2;
    }

    private int trueNodeIndex(int node) {
        return node * 2 + 1;
    }

    private void joinFalse(int vertexOne, int vertexTwo) {
        join(falseNodeIndex(vertexOne), falseNodeIndex(vertexTwo));
        join(falseNodeIndex(vertexTwo), falseNodeIndex(vertexOne));
    }

    private void joinTrue(int vertexOne, int vertexTwo) {
        join(trueNodeIndex(vertexOne), trueNodeIndex(vertexTwo));
        join(trueNodeIndex(vertexTwo), trueNodeIndex(vertexOne));
    }

    private void joinTrueFalse(int vertexOne, int vertexTwo) {
        join(trueNodeIndex(vertexOne), falseNodeIndex(vertexTwo));
        join(trueNodeIndex(vertexTwo), falseNodeIndex(vertexOne));
    }

    private void joinFalseTrue(int vertexOne, int vertexTwo) {
        join(falseNodeIndex(vertexOne), trueNodeIndex(vertexTwo));
        join(falseNodeIndex(vertexTwo), trueNodeIndex(vertexOne));
    }

    private void join(int nodeFrom, int nodeTo) {
        adjacencyMatrix[nodeFrom][nodeTo] = 1;
    }

    private void scanData() {
        int nodesNumber = scanner.nextInt();
        int edgesNumber = scanner.nextInt();
        adjacencyMatrix = new int[nodesNumber * 2][nodesNumber * 2];
        for (int edgeIndex = 0; edgeIndex < edgesNumber; edgeIndex++) {
            int startNode = scanner.nextInt();
            int endNode = scanner.nextInt();
            int value = scanner.nextInt();
            String operator = scanner.next();
            parseData(startNode - 1, endNode - 1, value, operator);
        }
        graph = new TwoSatGraph(adjacencyMatrix);
    }

    private void solveProblem() {
        if (TwoSatTester.isSatisfiable(graph)) {
            printWriter.println("YES");
        } else {
            printWriter.println("NO");
        }
    }

    public static void main(String[] args) throws IOException {
        FastScanner scanner = new FastScanner(new File("input.txt"));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(scanner, printWriter);
        main.scanData();
        main.solveProblem();
        printWriter.close();
    }
}

interface OrientedGraph {
    List<Integer> getChildren(int nodeIndex);

    List<Integer> getParents(int nodeIndex);

    int[][] getAdjacencyMatrix();

    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    void cleanMarks();

    int getNodesNumber();
}

class TwoSatGraph implements OrientedGraph {
    private int[][] adjacencyMatrix;
    private int[] marks;
    int nodesNumber;

    public TwoSatGraph(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
        nodesNumber = adjacencyMatrix.length;
        marks = new int[nodesNumber];
    }

    @Override
    public List<Integer> getChildren(int nodeIndex) {
        List<Integer> children = new ArrayList<>();
        for (int adjacencyNodeIndex = 0;
             adjacencyNodeIndex < nodesNumber; adjacencyNodeIndex++) {
            if (adjacencyMatrix[nodeIndex][adjacencyNodeIndex] > 0) {
                children.add(adjacencyNodeIndex);
            }
        }
        return children;
    }

    @Override
    public List<Integer> getParents(int nodeIndex) {
        List<Integer> parents = new ArrayList<>();
        for (int adjacencyNodeIndex = 0;
             adjacencyNodeIndex < nodesNumber; adjacencyNodeIndex++) {
            if (adjacencyMatrix[adjacencyNodeIndex][nodeIndex] > 0) {
                parents.add(adjacencyNodeIndex);
            }
        }
        return parents;
    }

    @Override
    public int[][] getAdjacencyMatrix() {
        return adjacencyMatrix;
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
    public int getNodesNumber() {
        return nodesNumber;
    }
}

class TwoSatTester {
    public static boolean isSatisfiable(TwoSatGraph graph) {
        List<Integer> sortedNodes = new ArrayList<>(graph.getNodesNumber());
        for (int nodeIndex = 0; nodeIndex < graph.getNodesNumber(); nodeIndex++) {
            topologicalSort(sortedNodes, graph, nodeIndex);
        }
        graph.cleanMarks();
        ListIterator<Integer> iterator = sortedNodes.listIterator(sortedNodes.size());
        while (iterator.hasPrevious()) {
            int nodeIndex = iterator.previous();
            markComponent(graph, nodeIndex, nodeIndex + 1);
        }
        for (int vertexIndex = 0; vertexIndex < graph.getNodesNumber() / 2; vertexIndex++) {
            if (graph.getMark(vertexIndex * 2) == graph.getMark(vertexIndex * 2 + 1)) {
                return false;
            }
        }
        return true;
    }

    private static void topologicalSort(List<Integer> sortedNodes, OrientedGraph graph, int startNode) {
        if (graph.getMark(startNode) != 0) {
            return;
        }
        graph.mark(startNode, 1);
        int[][] adjacencyMatrix = graph.getAdjacencyMatrix();
        for (int childIndex = 0; childIndex < adjacencyMatrix.length; childIndex++) {
            if (adjacencyMatrix[startNode][childIndex] == 1) {
                topologicalSort(sortedNodes, graph, childIndex);
            }
        }
        sortedNodes.add(startNode);
    }

    private static void markComponent(OrientedGraph graph, int startNode, int mark) {
        if (graph.getMark(startNode) != 0) {
            return;
        }
        graph.mark(startNode, mark);
        int[][] adjacencyMatrix = graph.getAdjacencyMatrix();
        for (int parentIndex = 0; parentIndex < adjacencyMatrix.length; parentIndex++) {
            if (adjacencyMatrix[parentIndex][startNode] == 1) {
                markComponent(graph, parentIndex, mark);
            }
        }
    }
}

class FastScanner {
    BufferedReader br;
    StringTokenizer st;

    public FastScanner(File f) {
        try {
            br = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public FastScanner(InputStream f) {
        br = new BufferedReader(new InputStreamReader(f));
    }

    String next() {
        while (st == null || !st.hasMoreTokens()) {
            String s = null;
            try {
                s = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (s == null)
                return null;
            st = new StringTokenizer(s);
        }
        return st.nextToken();
    }

    boolean hasMoreTokens() {
        while (st == null || !st.hasMoreTokens()) {
            String s = null;
            try {
                s = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (s == null)
                return false;
            st = new StringTokenizer(s);
        }
        return true;
    }

    int nextInt() {
        return Integer.parseInt(next());
    }

    long nextLong() {
        return Long.parseLong(next());
    }
}