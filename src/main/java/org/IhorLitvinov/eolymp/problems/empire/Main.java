package org.IhorLitvinov.eolymp.problems.empire;

import java.io.*;
import java.util.*;

public class Main {
    private Empire empire;
    private FastScanner scanner;
    private PrintWriter printWriter;

    public Main(FastScanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    private void scanData() {
        int planetsNumber = scanner.nextInt();
        int teleportsNumber = scanner.nextInt();
        List<Integer>[] adjacencyList =
                new ArrayList[planetsNumber];
        List<Integer>[] transposedList =
                new ArrayList[planetsNumber];
        for (int teleportIndex = 0; teleportIndex < teleportsNumber;
             teleportIndex++) {
            int basePlanet = scanner.nextInt();
            int destinationPlanet = scanner.nextInt();
            List<Integer> neighbors = adjacencyList[basePlanet - 1];
            if (neighbors == null) {
                neighbors = new ArrayList<>();
                adjacencyList[basePlanet - 1] = neighbors;
            }
            neighbors.add(destinationPlanet);

            neighbors = transposedList[destinationPlanet - 1];
            if (neighbors == null) {
                neighbors = new ArrayList<>();
                transposedList[destinationPlanet - 1] = neighbors;
            }
            neighbors.add(basePlanet);
        }
        empire = new Empire(planetsNumber, adjacencyList, transposedList);
    }

    private void solveProblem() {
        printWriter.println(Empire.getNeededTeleports(empire));
    }

    public static void main(String[] args) throws IOException {
        try {
            FastScanner scanner = new FastScanner(new File("input.txt"));
            PrintWriter printWriter = new PrintWriter(new File("output.txt"));
            Main main = new Main(scanner, printWriter);
            main.scanData();
            main.solveProblem();
            printWriter.close();
        } catch (IOException e) {
            System.out.println("All is very bad:(");
        }
    }
}

class GraphUtil {

    public static void sortByTimeNodes(
            List<Integer> timeOutSortedNodes,
            OrientedGraph graph, int startNode) {
        if (graph.getMark(startNode) != 0) {
            return;
        }
        graph.mark(startNode, 1);
        List<Integer> children = graph.getChildren(startNode);
        for (int child : children) {
            sortByTimeNodes(timeOutSortedNodes, graph, child);
        }
        timeOutSortedNodes.add(startNode);
    }

    public static boolean findIndependentComponents(
            OrientedGraph graph, int startNode, int mark) {
        if (graph.getMark(startNode) == 0) {
            boolean isIndependent = true;
            graph.mark(startNode, mark);
            for (int parent : graph.getParents(startNode)) {
                if (!findIndependentComponents(graph, parent, mark)) {
                    isIndependent = false;
                }
            }
            return isIndependent;
        } else return graph.getMark(startNode) == mark;

    }
}

class Empire implements OrientedGraph {
    private List<Integer>[] adjacencyList;
    private List<Integer>[] transposedList;
    private int planetsNumber;
    private int[] marks;

    public Empire(int planetsNumber, List<Integer>[] adjacencyList, List<Integer>[] transposedList) {
        this.planetsNumber = planetsNumber;
        this.adjacencyList = adjacencyList;
        this.transposedList = transposedList;
        marks = new int[planetsNumber];
    }

    public static int getNeededTeleports(OrientedGraph graph) {
        List<Integer> timeOutSortedNodes = new ArrayList<>();
        for (int nodeIndex = 1;
             nodeIndex <= graph.getNodesNumber(); nodeIndex++) {
            GraphUtil.sortByTimeNodes(timeOutSortedNodes, graph, nodeIndex);
        }
        graph.cleanMarks();
        int numberOfTeleports = 0;
        ListIterator<Integer> iterator =
                timeOutSortedNodes.listIterator(timeOutSortedNodes.size());
        while (iterator.hasPrevious()) {
            int nodeIndex = iterator.previous();
            if (GraphUtil.findIndependentComponents(graph, nodeIndex, nodeIndex)
                    && nodeIndex != 1) {
                numberOfTeleports++;
            }
        }
        return numberOfTeleports;
    }

    @Override
    public List<Integer> getChildren(int nodeIndex) {
        if (adjacencyList[nodeIndex - 1] == null) {
            return Collections.emptyList();
        }
        return adjacencyList[nodeIndex - 1];
    }

    @Override
    public List<Integer> getParents(int nodeIndex) {
        if (transposedList[nodeIndex - 1] == null) {
            return Collections.emptyList();
        }
        return transposedList[nodeIndex - 1];
    }

    @Override
    public void mark(int nodeIndex, int mark) {
        marks[nodeIndex - 1] = mark;
    }

    @Override
    public int getMark(int nodeIndex) {
        return marks[nodeIndex - 1];
    }

    @Override
    public void cleanMarks() {
        marks = new int[planetsNumber];
    }

    @Override
    public int getNodesNumber() {
        return planetsNumber;
    }
}

interface OrientedGraph {
    List<Integer> getChildren(int nodeIndex);

    List<Integer> getParents(int nodeIndex);

    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    void cleanMarks();

    int getNodesNumber();
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