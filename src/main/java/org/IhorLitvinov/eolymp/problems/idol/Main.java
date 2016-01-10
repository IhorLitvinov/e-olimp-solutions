package org.IhorLitvinov.eolymp.problems.idol;

import java.io.*;
import java.util.*;

public class Main {
    private List<Integer>[] adjacencyList;
    private List<Integer>[] transposedAdjacencyList;
    private TwoSatGraph graph;
    int participantsNumber;

    private FastScanner scanner;
    private PrintWriter printWriter;

    public Main(FastScanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    private void scanData() {
        participantsNumber = scanner.nextInt();
        int judgesNumber = scanner.nextInt();
        adjacencyList = new ArrayList[participantsNumber * 2];
        transposedAdjacencyList = new ArrayList[participantsNumber * 2];
        for (int judgeIndex = 0; judgeIndex < judgesNumber; judgeIndex++) {
            int firstVote = scanner.nextInt();
            int secondVote = scanner.nextInt();
            parseData(firstVote, secondVote);
        }
        parseData(1, 2);
        parseData(1, -2);
        graph = new TwoSatGraph(adjacencyList, transposedAdjacencyList);
    }


    private void parseData(int firstVote, int secondVote) {
        int firstNodeIndex = node(firstVote);
        int secondNodeIndex = node(secondVote);
        int firstAntiNodeIndex = antiNode(firstVote);
        int secondAntiNodeIndex = antiNode(secondVote);
        join(firstAntiNodeIndex, secondNodeIndex);
        join(secondAntiNodeIndex, firstNodeIndex);
    }

    private int antiNode(int vote) {
        int node = participantsNumber - vote;
        if (node > participantsNumber) {
            return node - 1;
        }
        return node;
    }

    private int node(int vote) {
        int node = participantsNumber + vote;
        if (node > participantsNumber) {
            return node - 1;
        }
        return node;
    }

    private void join(int nodeFrom, int nodeTo) {
        if (adjacencyList[nodeFrom] == null) {
            adjacencyList[nodeFrom] = new ArrayList<>();
        }
        adjacencyList[nodeFrom].add(nodeTo);
        if (transposedAdjacencyList[nodeTo] == null) {
            transposedAdjacencyList[nodeTo] = new ArrayList<>();
        }
        transposedAdjacencyList[nodeTo].add(nodeFrom);
    }


    private void solveProblem() {
        if (TwoSatTester.isSatisfiable(graph)) {
            printWriter.println("yes");
        } else {
            printWriter.println("no");
        }
    }

    public static void main(String[] args) throws IOException {
        FastScanner scanner = new FastScanner(new File("input.txt"));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(scanner, printWriter);
        while (scanner.hasMoreTokens()) {
            main.scanData();
            main.solveProblem();
        }
        printWriter.close();
    }
}

interface OrientedGraph {
    List<Integer> getChildren(int nodeIndex);

    List<Integer> getParents(int nodeIndex);

    int[][] getAdjacencyList();

    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    void cleanMarks();

    int getNodesNumber();
}

class TwoSatGraph implements OrientedGraph {
    private List<Integer>[] adjacencyList;
    private List<Integer>[] transposedAdjacencyList;
    private int[] marks;
    int nodesNumber;

    public TwoSatGraph(List<Integer>[] adjacencyList, List<Integer>[] transposedAdjacencyList) {
        this.adjacencyList = adjacencyList;
        this.transposedAdjacencyList = transposedAdjacencyList;
        nodesNumber = adjacencyList.length;
        marks = new int[nodesNumber];
    }

    public int getAntiNode(int node) {
        return nodesNumber - node - 1;
    }

    @Override
    public List<Integer> getChildren(int nodeIndex) {
        if (adjacencyList[nodeIndex] == null) {
            return Collections.emptyList();
        }
        return adjacencyList[nodeIndex];
    }

    @Override
    public List<Integer> getParents(int nodeIndex) {
        if (transposedAdjacencyList[nodeIndex] == null) {
            return Collections.emptyList();
        }
        return transposedAdjacencyList[nodeIndex];
    }

    @Override
    public int[][] getAdjacencyList() {
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
            if (graph.getMark(vertexIndex)
                    == graph.getMark(graph.getAntiNode(vertexIndex))) {
                return false;
            }
        }
        return true;
    }

    private static void topologicalSort(
            List<Integer> sortedNodes, OrientedGraph graph, int startNode) {
        if (graph.getMark(startNode) != 0) {
            return;
        }
        graph.mark(startNode, 1);
        for (int children : graph.getChildren(startNode)) {
            topologicalSort(sortedNodes, graph, children);
        }
        sortedNodes.add(startNode);
    }

    private static void markComponent(OrientedGraph graph, int startNode, int mark) {
        if (graph.getMark(startNode) != 0) {
            return;
        }
        graph.mark(startNode, mark);
        for (int parent : graph.getParents(startNode)) {
            markComponent(graph, parent, mark);
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