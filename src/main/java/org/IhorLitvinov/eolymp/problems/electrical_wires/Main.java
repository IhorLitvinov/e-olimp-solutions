package org.IhorLitvinov.eolymp.problems.electrical_wires;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private Scanner scanner;
    private PrintWriter printWriter;
    private ElectricalNodes electricalNodes;
    private int[] outletNodes;
    private int startNumberOfWires;

    public Main(Scanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    public void scanData() {
        startNumberOfWires = 0;
        int nodesNumber = scanner.nextInt();
        ArrayList<Integer>[] neighborsOfNode = new ArrayList[nodesNumber];
        for (int nodeIndex = 0; nodeIndex < nodesNumber; nodeIndex++) {
            neighborsOfNode[nodeIndex] = new ArrayList<>(nodesNumber);
            char[] neighborsArray = scanner.next().toCharArray();
            for (int neighborIndex = 0;
                 neighborIndex < nodesNumber; neighborIndex++) {
                char connectionWithNeighbor = neighborsArray[neighborIndex];
                if (connectionWithNeighbor == '1') {
                    neighborsOfNode[nodeIndex].add(neighborIndex);
                    startNumberOfWires++;
                }
            }
        }
        startNumberOfWires /= 2;
        int outletNodesNumber = scanner.nextInt();
        outletNodes = new int[outletNodesNumber];
        for (int outletNodeIndex = 0;
             outletNodeIndex < outletNodesNumber; outletNodeIndex++) {
            outletNodes[outletNodeIndex] = scanner.nextInt();
        }
        electricalNodes = new ElectricalNodes(neighborsOfNode);
    }

    public boolean hasNextTest() {
        return scanner.hasNextInt();
    }

    public void solveProblem() {
        int[] componentsSizes = electricalNodes.componentSizeFinder(outletNodes);
        int allEdges = electricalNodes.findAllWires(componentsSizes);
        printWriter.println(allEdges - startNumberOfWires);
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

class ElectricalNodes implements Graph {
    private ArrayList<Integer>[] verticesNeighbors;
    private int[] nodesMarks;

    public ElectricalNodes(ArrayList<Integer>[] verticesNeighbors) {
        this.verticesNeighbors = verticesNeighbors;
        nodesMarks = new int[verticesNeighbors.length];

    }

    public int[] componentSizeFinder(int[] startNodes) {
        int componentNumbers = startNodes.length;
        int[] componentSizes = new int[componentNumbers];
        for (int componentIndex = 0; componentIndex < startNodes.length;
             componentIndex++) {
            int nodesNumber =
                    GraphUtil.findComponentNodes(
                            startNodes[componentIndex], this, componentIndex + 1);
            componentSizes[componentIndex] = nodesNumber;
        }
        return componentSizes;
    }

    public int findAllWires(int[] componentsSizes) {
        int componentSizesSum = 0;
        int maxSize = 0;
        int maxComponentIndex = 0;
        int componentNumbers = componentsSizes.length;
        for (int componentIndex = 0;
             componentIndex < componentNumbers; componentIndex++) {
            int currentSize = componentsSizes[componentIndex];
            componentSizesSum += currentSize;
            if (currentSize > maxSize) {
                maxSize = currentSize;
                maxComponentIndex = componentIndex;
            }
        }
        int numberOfUnmarkedNodes =
                verticesNeighbors.length - componentSizesSum;
        componentsSizes[maxComponentIndex] += numberOfUnmarkedNodes;
        int allWiresNumber = 0;
        for (int componentSize : componentsSizes) {
            allWiresNumber += componentSize * (componentSize - 1) / 2;
        }
        return allWiresNumber;
    }

    @Override
    public List<Integer> getNeighbors(int nodeIndex) {
        return verticesNeighbors[nodeIndex];
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
        nodesMarks = new int[verticesNeighbors.length];
    }

    @Override
    public int getNodesNumber() {
        return verticesNeighbors.length;
    }

}

class GraphUtil {

    public static int findComponentNodes(int start, Graph graph, int mark) {
        int numberOfNodes = 1;
        graph.mark(start, mark);
        List<Integer> neighbors = graph.getNeighbors(start);
        for (int neighbor : neighbors) {
            if (graph.getMark(neighbor) == 0) {
                numberOfNodes += findComponentNodes(neighbor, graph, mark);
            }
        }
        return numberOfNodes;
    }
}

interface Graph {
    List<Integer> getNeighbors(int nodeIndex);

    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    void cleanMarks();

    int getNodesNumber();
}