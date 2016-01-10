package org.IhorLitvinov.eolymp.problems.parking;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.IntPredicate;

public class Main {
    private char[][] parkingMap;
    private Scanner scanner;
    private PrintWriter printWriter;
    private ParkingMapParser parkingMapParser;
    private int columnsNumber;
    private int rowsNumber;

    public Main(Scanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    private void scanData() {
        rowsNumber = scanner.nextInt();
        columnsNumber = scanner.nextInt();
        parkingMap = new char[rowsNumber][columnsNumber];
        for (int rowIndex = 0; rowIndex < rowsNumber; rowIndex++) {
            String likesString = scanner.next();
            parkingMap[rowIndex] = likesString.toCharArray();
        }
        parkingMapParser = new ParkingMapParser(parkingMap);
    }

    private int binarySearch(int maxLimit,
                             int minLimit,
                             IntPredicate needToReduce) {
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
        if (currentMin == maxLimit && !needToReduce.test(maxLimit)) {
            return -1;
        }
        return currentMin;
    }

    private void solveProblem() {
        if (parkingMapParser.carsNumber() == 0) {
            printWriter.println(0);
        } else {
            IntPredicate carsCanFindPlace = maxDistance -> {
                ParkingGraph parkingGraph =
                        parkingMapParser.getGraph(maxDistance);
                int maxFlow = MaxFlowFinder.maxFlow(parkingGraph);
                return maxFlow == parkingMapParser.carsNumber();
            };
            int maxPossibleDistance =
                    parkingMapParser.getMaxPossibleDistance();
            int answer = binarySearch(maxPossibleDistance,
                    0, carsCanFindPlace);
            if (answer == 0) {
                answer = -1;
            }
            printWriter.println(answer);
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(new File("input.txt"));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(scanner, printWriter);
        while (scanner.hasNext()) {
            main.scanData();
            main.solveProblem();
        }
        printWriter.close();
    }
}

interface Flowable {
    int getSourceNode();

    int getDestinationNode();
}

interface Marked {
    void mark(int nodeIndex, int mark);

    int getMark(int nodeIndex);

    void cleanMarks();
}

interface MatrixGraph {
    int[][] adjacencyMatrix();

    int getNodesNumber();
}

class MaxFlowFinder {
    public static <T extends MatrixGraph
            & Marked & Flowable> int maxFlow(T graph) {
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
            & Marked & Flowable> int minCapacity(T graph) {
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
            & Flowable> void reduceCapacity(T graph, int minCapacity) {
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
            & Marked & Flowable> boolean hasWay(T graph) {
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

class ParkingGraph implements MatrixGraph, Marked, Flowable {
    private int[][] adjacencyMatrix;
    private int nodesNumber;
    private int[] marks;
    private int sourceNode;
    private int destinationNode;

    public ParkingGraph(int[][] adjacencyMatrix,
                        int sourceNode, int destinationNode) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        nodesNumber = adjacencyMatrix.length;
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
        return sourceNode;
    }

    @Override
    public int getDestinationNode() {
        return destinationNode;
    }
}

class ParkingMapParser {
    private char[][] parkingMap;
    private int mapSize;
    private int columnsNumber;
    private int carsNumber;
    private int placesNumber;
    private int[][] adjacencyMatrix;
    private int sourceNode;
    private int destinationNode;
    private int maxPossibleDistance = 0;

    public ParkingMapParser(char[][] parkingMap) {
        this.parkingMap = parkingMap;
        int rowsNumber = parkingMap.length;
        columnsNumber = parkingMap[0].length;
        mapSize = rowsNumber * columnsNumber;
        parseMap();
    }

    private void parseMap() {
        List<Integer> carPositions = new ArrayList<>();
        List<Integer> parkingPositions = new ArrayList<>();
        getCarsAndParkingPositions(parkingPositions, carPositions);
        int nodesNumber = carsNumber + placesNumber + 2;
        sourceNode = nodesNumber - 2;
        destinationNode = nodesNumber - 1;
        adjacencyMatrix = new int[nodesNumber][nodesNumber];
        for (int carNode = 0; carNode < carPositions.size(); carNode++) {
            int carCell = carPositions.get(carNode);
            findDistancesToParking(carCell, carNode, parkingPositions);
            connectSourceNodesAndCars();
            connectParkingNodesAndDestination();
        }
    }

    private void connectSourceNodesAndCars() {
        for (int carNode = 0; carNode < carsNumber; carNode++) {
            adjacencyMatrix[sourceNode][carNode] = 1;
        }
    }

    private void connectParkingNodesAndDestination() {
        for (int placeIndex = 0; placeIndex < placesNumber; placeIndex++) {
            int placeNode = parkingNode(placeIndex);
            adjacencyMatrix[placeNode][destinationNode] = 1;
        }
    }

    private void getCarsAndParkingPositions(List<Integer> parkingPositions,
                                            List<Integer> carPositions) {
        final char CAR_SYMBOL = 'C';
        final char PARKING_PLACE_SYMBOL = 'P';
        int cellIndex = 0;
        for (char[] row : parkingMap) {
            for (char mapCell : row) {
                if (mapCell == CAR_SYMBOL) {
                    carPositions.add(cellIndex);
                } else if (mapCell == PARKING_PLACE_SYMBOL) {
                    parkingPositions.add(cellIndex);
                }
                cellIndex++;
            }
        }
        carsNumber = carPositions.size();
        placesNumber = parkingPositions.size();
    }

    private void findDistancesToParking(int carCell, int carNode,
                                        List<Integer> parkingPositions) {
        final char WALL_SYMBOL = 'X';
        int[] mapMarks = new int[mapSize];
        markParkingPlaces(mapMarks, parkingPositions);
        int currentDistance = 1;
        mapMarks[carCell] = currentDistance;
        Queue<Integer> queue = new LinkedList<>();
        queue.add(carCell);
        while (!queue.isEmpty()) {
            int currentNode = queue.poll();
            currentDistance = mapMarks[currentNode];
            List<Integer> neighbors = mapCellNeighbors(currentNode);
            for (int neighborIndex : neighbors) {
                int mark = mapMarks[neighborIndex];
                char cell = getCell(neighborIndex);
                if (mark == 0 && cell != WALL_SYMBOL) {
                    mapMarks[neighborIndex] = currentDistance + 1;
                    queue.add(neighborIndex);
                } else if (mark < 0) {
                    mapMarks[neighborIndex] = currentDistance + 1;
                    queue.add(neighborIndex);
                    writeDistance(mark, carNode, currentDistance);
                }
            }
        }
    }

    private void writeDistance(int mark, int carNode, int currentDistance) {
        int parkingPositionIndex = -mark - 1;
        int parkingNode = parkingNode(parkingPositionIndex);
        adjacencyMatrix[carNode][parkingNode]
                = currentDistance;
        if (currentDistance > maxPossibleDistance) {
            maxPossibleDistance = currentDistance;
        }
    }

    private char getCell(int cellIndex) {
        int rowIndex = cellIndex / columnsNumber;
        int columnIndex = cellIndex % columnsNumber;
        return parkingMap[rowIndex][columnIndex];
    }

    private void markParkingPlaces(int[] mapMarks,
                                   List<Integer> parkingPositions) {
        for (int parkingIndex = 0; parkingIndex < parkingPositions.size();
             parkingIndex++) {
            int parkingPosition = parkingPositions.get(parkingIndex);
            mapMarks[parkingPosition] = -parkingIndex - 1;
        }
    }

    private List<Integer> mapCellNeighbors(int cellIndex) {
        final int MAX_NEIGHBORS_NUMBER = 4;
        List<Integer> mapNodeNeighbors =
                new ArrayList<>(MAX_NEIGHBORS_NUMBER);
        int up = cellIndex - columnsNumber;
        if (valid(up)) {
            mapNodeNeighbors.add(up);
        }
        int down = cellIndex + columnsNumber;
        if (valid(down)) {
            mapNodeNeighbors.add(down);
        }
        int left = cellIndex - 1;
        if (valid(left)
                && cellIndex % columnsNumber != 0) {
            mapNodeNeighbors.add(left);
        }
        int right = cellIndex + 1;
        if (valid(right)
                && cellIndex % columnsNumber != columnsNumber - 1) {
            mapNodeNeighbors.add(right);
        }
        return mapNodeNeighbors;
    }

    private boolean valid(int nodeIndex) {
        return nodeIndex >= 0
                && nodeIndex < mapSize;
    }

    private int parkingNode(int parkingPositionIndex) {
        return carsNumber + parkingPositionIndex;
    }

    public int carsNumber() {
        return carsNumber;
    }

    public int getMaxPossibleDistance() {
        return maxPossibleDistance;
    }

    public ParkingGraph getGraph(int maxDistance) {
        int[][] graphAdjacencyMatrix =
                new int[adjacencyMatrix.length][adjacencyMatrix.length];
        for (int rowIndex = 0;
             rowIndex < adjacencyMatrix.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < adjacencyMatrix.length;
                 columnIndex++) {
                int distance = adjacencyMatrix[rowIndex][columnIndex];
                if (distance > 0 && distance <= maxDistance) {
                    graphAdjacencyMatrix[rowIndex][columnIndex] = 1;
                }
            }
        }
        return new ParkingGraph(graphAdjacencyMatrix,
                sourceNode, destinationNode);
    }
}