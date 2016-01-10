package org.IhorLitvinov.eolymp.problems.mecho;

import org.IhorLitvinov.eolymp.problems.mecho.Map.Cell;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private Scanner scanner;
    private PrintWriter printWriter;
    private Map map;
    private Queue<Cell> hives;
    private Map.Cell mecho;
    private Map.Cell home;


    public Main(Scanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    void scanMap() {
        int sideLength;
        sideLength = scanner.nextInt();
        int mechoSpeed = scanner.nextInt();
        hives = new LinkedList<>();
        char[][] cellTypes = new char[sideLength][sideLength];
        map = new Map(cellTypes, mechoSpeed);
        for (int lineIndex = 0; lineIndex < sideLength; lineIndex++) {
            String currentLine = scanner.next();
            char[] cellTypeArray = currentLine.toCharArray();
            for (int columnIndex = 0; columnIndex < sideLength; columnIndex++) {
                char cellType = cellTypeArray[columnIndex];
                cellTypes[columnIndex][lineIndex] = cellType;
                if (cellType == Map.HIVE) {
                    Map.Cell thisCell = map.new Cell(columnIndex, lineIndex);
                    hives.add(thisCell);
                } else if (cellType == Map.MECHO) {
                    mecho = map.new Cell(columnIndex, lineIndex);
                } else if (cellType == Map.HOME) {
                    home = map.new Cell(columnIndex, lineIndex);
                }
            }
        }
    }

    int findTheGreatestDelay(int start, int end) {
        int middle = (start + end) / 2;
        while (start != middle) {
            middle = (start + end) / 2;
            if (map.canBearGoToEnd(mecho, home, middle)) {
                start = middle;
            } else {
                end = middle;
            }
            middle = (start + end) / 2;
        }
        if (map.canBearGoToEnd(mecho, home, start)) {
            return start;
        }
        return -1;
    }

    void solveProblem() {
        int [][] beesTimes = map.solveBeesTime(hives);
        int maxTimeOfEating =
                beesTimes[mecho.getColumnIndex()][mecho.getLineIndex()];
        int minTimeOfEating = 0;
        int timeOfEating = findTheGreatestDelay(minTimeOfEating, maxTimeOfEating);
        printWriter.println(timeOfEating);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(new File("input.txt"));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(scanner, printWriter);
        main.scanMap();
        main.solveProblem();
        scanner.close();
        printWriter.close();
    }
}

class Map {
    static final char TREE = 'T';
    static final char HOME = 'D';
    static final char MECHO = 'M';
    static final char HIVE = 'H';
    static final char GRASS = 'G';
    static final int MAX_NEIGHBORS_NUMBER = 4;
    private int sideLength;
    private int bearSpeed;
    private char[][] cellTypes;
    private int[][] beesArriveTime;
    private int[][] distance;

    public Map(char[][] cellTypes, int bearSpeed) {
        this.bearSpeed = bearSpeed;
        this.cellTypes = cellTypes;
        sideLength = cellTypes.length;
        beesArriveTime = new int[sideLength][sideLength];
        distance = new int[sideLength][sideLength];
    }

    class Cell {
        private final int columnIndex;
        private final int lineIndex;

        public Cell(int columnIndex, int lineIndex) {
            this.columnIndex = columnIndex;
            this.lineIndex = lineIndex;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public int getLineIndex() {
            return lineIndex;
        }

        public List<Cell> getNeighbors() {
            List<Cell> neighbors = new ArrayList<>(MAX_NEIGHBORS_NUMBER);
            if (validIndex(columnIndex + 1)) {
                Cell right = new Cell(columnIndex + 1, lineIndex);
                neighbors.add(right);
            }
            if (validIndex(columnIndex - 1)) {
                Cell left = new Cell(columnIndex - 1, lineIndex);
                neighbors.add(left);
            }
            if (validIndex(lineIndex + 1)) {
                Cell upper = new Cell(columnIndex, lineIndex + 1);
                neighbors.add(upper);
            }
            if (validIndex(lineIndex - 1)) {
                Cell lower = new Cell(columnIndex, lineIndex - 1);
                neighbors.add(lower);
            }
            return neighbors;
        }

        private boolean validIndex(int index) {
            return (index >= 0) && (index < sideLength);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Cell cell = (Cell) o;

            return columnIndex == cell.columnIndex
                    && lineIndex == cell.lineIndex;
        }

        @Override
        public int hashCode() {
            int result = columnIndex;
            result = 800 * result + lineIndex;
            return result;
        }
    }

    public int[][] solveBeesTime(Queue<Cell> startCells) {
        while (!startCells.isEmpty()) {
            Cell currentCell = startCells.poll();
            int currentCellTime =
                    beesArriveTime[currentCell.columnIndex][currentCell.lineIndex];
            List<Cell> neighbors = currentCell.getNeighbors();
            for (Cell neighbor : neighbors) {
                if (typeValidForBees(neighbor)
                        && isBeesNotSolved(neighbor)) {
                    writeBeesTime(neighbor, currentCellTime + 1);
                    startCells.add(neighbor);
                }
            }
        }
        return beesArriveTime;
    }

    private boolean typeValidForBees(Cell cell) {
        char cellType = cellTypes[cell.columnIndex][cell.lineIndex];
        return (cellType == GRASS)
                || (cellType == MECHO);
    }

    private boolean isBeesNotSolved(Cell cell) {
        return beesArriveTime[cell.columnIndex][cell.lineIndex] == 0;
    }

    private void writeBeesTime(Cell cell, int time) {
        beesArriveTime[cell.columnIndex][cell.lineIndex] = time;
    }

    public boolean canBearGoToEnd(Cell start, Cell end, int delay) {
        distance = new int[sideLength][sideLength];
        Queue<Cell> cellQueue = new LinkedList<>();
        cellQueue.add(start);
        int currentDistance;
        while (!cellQueue.isEmpty()) {
            Cell currentCell = cellQueue.poll();
            currentDistance = getBearDistance(currentCell);
            List<Cell> neighbors = currentCell.getNeighbors();
            for (Cell neighbor : neighbors) {
                if (neighbor.equals(end)) {
                    return true;
                }
                if (typeValidForBees(neighbor)
                        && isBearNotSolved(neighbor) && !neighbor.equals(start)) {
                    writeBearDistance(neighbor, currentDistance + 1);
                    int solvedDelay = solveDelay(neighbor);
                    if (solvedDelay >= delay) {
                        cellQueue.add(neighbor);
                    }
                }
            }
        }
        return false;
    }

    private int getBearDistance(Cell cell) {
        return distance[cell.columnIndex][cell.lineIndex];
    }

    private int solveDelay(Cell cell) {
        int bearArriveTime =
                distance[cell.columnIndex][cell.lineIndex] / bearSpeed;

        return beesArriveTime[cell.columnIndex][cell.lineIndex]
                - bearArriveTime - 1;
    }

    private void writeBearDistance(Cell cell, int someDistance) {
        this.distance[cell.columnIndex][cell.lineIndex] =
                someDistance;
    }

    private boolean isBearNotSolved(Cell cell) {
        return  distance[cell.columnIndex][cell.lineIndex] == 0;
    }

}