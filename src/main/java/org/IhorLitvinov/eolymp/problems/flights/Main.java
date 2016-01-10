package org.IhorLitvinov.eolymp.problems.flights;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Main {
    private static final String XOR = "XOR";
    private static final String OR = "OR";
    private static final String AND = "AND";
    private int[][] adjacencyMatrix;
    int maxPossibleCapacity;
    private CitiesFuelGraph graph;

    private FastScanner scanner;
    private PrintWriter printWriter;

    public Main(FastScanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    private void scanData() {
        int citiesNumber = scanner.nextInt();
        maxPossibleCapacity = 0;
        adjacencyMatrix = new int[citiesNumber][citiesNumber];
        for (int startCity = 0; startCity < citiesNumber; startCity++) {
            for (int destinationCity = 0; destinationCity < citiesNumber; destinationCity++) {
                int nextCapacity = scanner.nextInt();
                adjacencyMatrix[startCity][destinationCity] = nextCapacity;
                if (nextCapacity > maxPossibleCapacity) {
                    maxPossibleCapacity = nextCapacity;
                }
            }
        }
        graph = new CitiesFuelGraph(adjacencyMatrix);
    }

    private boolean enoughSize(int capacity) {
        graph.setMaxCapacity(capacity);
        graph.cleanMarks();
        List<Integer> sortedNodes = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < graph.getNodesNumber(); nodeIndex++) {
            ConnectednessUtil.topologicalSort(sortedNodes, graph, nodeIndex);
        }
        graph.cleanMarks();
        if (sortedNodes.isEmpty()) {
            return false;
        }
        int lastNode = sortedNodes.get(sortedNodes.size() - 1);
        int componentSize = ConnectednessUtil.markComponent(
                graph, lastNode, lastNode + 1, 0);
        return componentSize == graph.getNodesNumber();
    }

    private int binarySearch() {
        int start = 0;
        int end = maxPossibleCapacity;
        int median;
        while (start < end) {
            median = (start + end) / 2;
            if (enoughSize(median)) {
                end = median;
            } else {
                start = median + 1;
            }
        }
        return start;
    }

    private void solveProblem() {
        printWriter.println(binarySearch());
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

class CitiesFuelGraph implements OrientedGraph {
    private int[][] adjacencyMatrix;
    private int[] marks;
    private int nodesNumber;
    private int maxCapacity;

    public CitiesFuelGraph(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
        nodesNumber = adjacencyMatrix.length;
        marks = new int[nodesNumber];
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public List<Integer> getChildren(int nodeIndex) {
        List<Integer> children = new ArrayList<>();
        for (int possibleChild = 0; possibleChild < nodesNumber; possibleChild++) {
            if (adjacencyMatrix[nodeIndex][possibleChild] <= maxCapacity) {
                children.add(possibleChild);
            }
        }
        return children;
    }

    @Override
    public List<Integer> getParents(int nodeIndex) {
        List<Integer> parents = new ArrayList<>();
        for (int possibleParent = 0; possibleParent < nodesNumber; possibleParent++) {
            if (adjacencyMatrix[possibleParent][nodeIndex] <= maxCapacity) {
                parents.add(possibleParent);
            }
        }
        return parents;
    }

    @Override
    public int[][] getAdjacencyMatrix() {
        throw new UnsupportedOperationException();
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

class ConnectednessUtil {
    public static void topologicalSort(List<Integer> sortedNodes, OrientedGraph graph, int startNode) {
        if (graph.getMark(startNode) != 0) {
            return;
        }
        graph.mark(startNode, 1);
        for (int childIndex : graph.getChildren(startNode)) {
            topologicalSort(sortedNodes, graph, childIndex);
        }
        sortedNodes.add(startNode);
    }

    public static int markComponent(OrientedGraph graph, int startNode, int mark, int oldSize) {
        if (graph.getMark(startNode) != 0) {
            return oldSize;
        }
        int size = oldSize + 1;
        graph.mark(startNode, mark);
        for (int parentIndex : graph.getParents(startNode)) {
            size = markComponent(graph, parentIndex, mark, size);
        }
        return size;
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

interface OrientedGraph {
    List<Integer> getChildren(int nodeIndex);

    List<Integer> getParents(int nodeIndex);

    int[][] getAdjacencyMatrix();

    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    void cleanMarks();

    int getNodesNumber();
}