package org.IhorLitvinov.eolymp.problems.needle_in_the_haystack;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private TextSource textSource;
    private char[] pattern;

    public Main(BufferedReader bufferedReader, PrintWriter printWriter) {
        this.printWriter = printWriter;
        this.bufferedReader = bufferedReader;
        textSource = new BufferedTextSource(bufferedReader);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(
                        new File("input.txt")));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(bufferedReader, printWriter);
        for (int testIndex = 0; main.hasMoreTests(); testIndex++) {
            main.printSeparator(testIndex);
            main.scanData();
            main.solveProblem();
        }
        bufferedReader.close();
        printWriter.close();
    }

    private void printSeparator(int testIndex) {
        if (testIndex > 0) {
            printWriter.println();
        }
    }

    private boolean hasMoreTests() throws IOException {
        String patternSize = bufferedReader.readLine();
        return patternSize != null;
    }

    private void scanData() throws IOException {
        pattern = bufferedReader.readLine().toCharArray();
    }

    private void solveProblem() {
        PatternsFinder patternsFinder = new PatternsFinder(textSource, pattern);
        List<Integer> patternPositions = patternsFinder
                .kMPAlgorithm();
        printAnswer(patternPositions);
    }

    private void printAnswer(List<Integer> patternPositions) {
        if (!patternPositions.isEmpty()) {
            patternPositions
                    .stream()
                    .forEach(printWriter::println);
        }
    }
}

class BufferedTextSource implements TextSource {
    private final char END_OF_LINE = '\n';
    private BufferedReader bufferedReader;
    private int nextChar;
    private boolean nextRead;

    public BufferedTextSource(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    @Override
    public boolean hasNext() {
        if (nextRead) {
            return true;
        }
        readNext();
        if (nextChar == -1 || nextChar == END_OF_LINE) {
            return false;
        } else {
            nextRead = true;
            return true;
        }
    }

    private void readNext() {
        try {
            nextChar = bufferedReader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int nextChar() {
        if (nextRead) {
            nextRead = false;
            return nextChar;
        }
        readNext();
        return nextChar;
    }
}

interface TextSource {
    boolean hasNext();

    int nextChar();
}

class PatternsFinder {
    private TextSource source;
    private char[] pattern;
    private int[] patternPrefixFunctions;
    private int numberOfMatchingSymbols = 0;
    private char nextElement;

    public PatternsFinder(TextSource source, char[] pattern) {
        this.source = source;
        this.pattern = pattern;
        patternPrefixFunctions = new int[pattern.length];
    }

    private void checkPatternSize() {
        if (pattern.length == 0) {
            throw new IllegalArgumentException("Pattern size is 0.");
        }
    }

    public List<Integer> kMPAlgorithm() {
        checkPatternSize();
        List<Integer> patternPositions = new ArrayList<>();
        for (int symbolIndex = 1; source.hasNext(); symbolIndex++) {
            if (symbolIndex < pattern.length) {
                nextElement = pattern[symbolIndex];
            } else {
                nextElement = (char) source.nextChar();
            }
            while (needToReduceNumberOfMatchingSymbols()) {
                numberOfMatchingSymbols =
                        patternPrefixFunctions[numberOfMatchingSymbols - 1];
            }
            if (nextElement == pattern[numberOfMatchingSymbols]) {
                numberOfMatchingSymbols++;
            }
            if (symbolIndex < pattern.length) {
                patternPrefixFunctions[symbolIndex] = numberOfMatchingSymbols;
            }
            if (numberOfMatchingSymbols == pattern.length) {
                addPatternPosition(patternPositions, symbolIndex);
            }
        }
        return patternPositions;
    }

    private boolean needToReduceNumberOfMatchingSymbols() {
        return numberOfMatchingSymbols == pattern.length
                || (numberOfMatchingSymbols > 0
                && nextElement != pattern[numberOfMatchingSymbols]);
    }

    private void addPatternPosition(List<Integer> patternPositions, int elementIndex) {
        int position = elementIndex - 2 * pattern.length + 1;
        if (position >= 0) {
            patternPositions.add(position);
        }
    }
}