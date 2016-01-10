package org.IhorLitvinov.eolymp.problems.remakes;

import java.io.*;
import java.util.StringTokenizer;

public class Main {
    private FastScanner scanner;
    private PrintWriter printWriter;
    private int[] pattern;
    private int[] composition;


    public Main(FastScanner scanner, PrintWriter printWriter) {
        this.scanner = scanner;
        this.printWriter = printWriter;
    }

    public static void main(String[] args) throws IOException {
        FastScanner scanner = new FastScanner(new File("input.txt"));
        PrintWriter printWriter = new PrintWriter(new File("output.txt"));
        Main main = new Main(scanner, printWriter);
        int testNumber = scanner.nextInt();
        for (int testIndex = 0; testIndex < testNumber; testIndex++) {
            main.scanData();
            main.solveProblem();
        }
        printWriter.close();
    }

    private void scanData() {
        int patternSize = scanner.nextInt();
        pattern = new int[patternSize];
        for (int noteIndex = 0; noteIndex < patternSize; noteIndex++) {
            pattern[noteIndex] = scanner.nextInt();
        }
        int compositionSize = scanner.nextInt();
        composition = new int[compositionSize];
        for (int noteIndex = 0; noteIndex < compositionSize; noteIndex++) {
            composition[noteIndex] = scanner.nextInt();
        }
    }

    private void solveProblem() {
        if (ArrayPatternFinder.contains(pattern, composition)) {
            printWriter.println(1);
        } else {
            printWriter.println(0);
        }
    }

    static class ArrayPatternFinder {
        public static boolean contains(int[] pattern,
                                       int[] text) {
            if (pattern.length > text.length) {
                return false;
            }
            if (pattern.length == 1) {
                return true;
            }
            int dataSize = pattern.length + text.length;
            int[] data = new int[dataSize];
            System.arraycopy(pattern, 0, data, 0, pattern.length);
            System.arraycopy(text, 0, data, pattern.length, text.length);
            int[] prefixFunctions = new int[dataSize];
            prefixFunctions[0] = 0;
            int delta;
            for (int symbolIndex = 1; symbolIndex < dataSize; symbolIndex++) {
                int lastPrefixSize = prefixFunctions[symbolIndex - 1];
                int nextSymbol = data[symbolIndex];
                int nextPrefixSymbol = data[lastPrefixSize];
                for (; ; ) {
                    if (lastPrefixSize > 0) {
                        delta = data[symbolIndex - 1] - data[lastPrefixSize - 1];
                    } else {
                        delta = nextSymbol - nextPrefixSymbol;
                    }
                    if (delta != nextSymbol - nextPrefixSymbol) {
                        lastPrefixSize = prefixFunctions[lastPrefixSize - 1];
                        nextPrefixSymbol = data[lastPrefixSize];
                    } else {
                        lastPrefixSize++;
                        break;
                    }
                }
                if (lastPrefixSize >= pattern.length
                        && symbolIndex >= pattern.length * 2 - 1) {
                    return true;
                }
                prefixFunctions[symbolIndex] = lastPrefixSize;
            }
            return false;
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