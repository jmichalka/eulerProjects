import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


public class Pb96 {
    static int[][] sectorMap = new int[][]{{0,1,2},{3,4,5},{6,7,8}};
    static int[] sectorRow = new int[]{0,0,0,3,3,3,6,6,6};
    static int[] sectorColumn = new int[]{0,3,6,0,3,6,0,3,6};


    public static List<Sudoku> loadSudokus() throws Exception{
        File file = new File("/Users/jmichalka/Desktop/5_funProgramming/java/euler/java/main/resources/p096_sudoku.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        List<Sudoku> toReturn = new ArrayList<>();
        Sudoku temp;
        while(br.ready()) {
            String line = br.readLine();
            if (line.contains("Grid")) {
                temp = new Sudoku();
                int[][] toFill = temp.getMatrix();
                for (int i = 0; i < 9; i++) {
                    String numberLine = br.readLine();
                    for (int j = 0; j < numberLine.length(); j++) {
                        toFill[i][j] = Integer.parseInt(numberLine.substring(j, j+1));
                    }
                }
                temp.setFixedMatrix(toFill);
                toReturn.add(temp);
            }
            break;
        }
        return toReturn;
    }

    public static void main(String[] args) throws Exception {
        List<Sudoku> sudokus = loadSudokus();
        //sudokus.get(0).printMatrix();
        sudokus.get(0).printMatrix();
        for (int i = 0; i < 1; i++) {
//            while (!validateCurrentBoard(sudoku.getMatrix())) {
//
//            }
            for (int row = 0; row < 9; row++) {
                for (int column = 0; column < 9; column++) {
                    System.out.println(determinePossibleValuesAtThisIndex(sudokus.get(i).getMatrix(), row, column));
                }
            }


            break;
        }
    }


    public static boolean validateCurrentBoard(int[][] sudoku) {
        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            if (!verifyRow(sudoku, rowIndex)) {
                return false;
            }
        }
        for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
            if (!verifyColumn(sudoku, columnIndex)) {
                return false;
            }
        }
        for (int squareIndex = 0; squareIndex < 9; squareIndex++) {
            if (!verifyBlock(sudoku, squareIndex)) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyRow(int[][] sudoku, int rowIndex) {
        int[] checklist = new int[10];
        for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
            checklist[sudoku[rowIndex][columnIndex]]++;
        }
        for (int i = 1; i < checklist.length; i++) {
            if (checklist[i] > 1 ) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyColumn(int[][] sudoku, int columnIndex) {
        int[] checklist = new int[10];
        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            checklist[sudoku[rowIndex][columnIndex]]++;
        }
        for (int i = 1; i < checklist.length; i++) {
            if (checklist[i] > 1 ) {
                return false;
            }
        }
        return true;
    }

    // 0   1   2
    // 3   4   5
    // 6   7   8
    public static boolean verifyBlock(int[][] sudoku, int squareIndex) {
        int offSetX = (squareIndex % 3)*3;
        int offSetY = (squareIndex/3)*3;

        int[] checklist = new int[10];
        for (int i = offSetX; i < offSetX+3; i++) {
            for (int j = offSetY; j < offSetY+3; j++) {
                checklist[sudoku[i][j]]++;
            }
        }
        for (int i = 1; i < checklist.length; i++) {
            if (checklist[i] > 1 ) {
                return false;
            }
        }
        return true;
    }


    //TODO Generate a List<Integer>[][]
    //TODO generate a List<Integer> of available values at a cell

    public static Set<Integer> determinePossibleValuesAtThisIndex(int[][] sudoku, int row, int column) {
        Set<Integer> toReturn = new HashSet<>();
        if (sudoku[row][column] != 0) {
            toReturn.add(sudoku[row][column]);
            return toReturn;
        } else {
            // Numbers that already exist in these rows/columns
            for (int i = 0; i < 9; i++) {
                toReturn.add(sudoku[row][i]);
                toReturn.add(sudoku[i][column]);
            }
            int sector = determineSector(row, column);
            int sectorRow = determineSectorRow(sector);
            int sectorColumn = determineSectorColumn(sector);
            for (int i = sectorRow; i < sectorRow + 3; i++) {
                for (int j = sectorColumn; j < sectorColumn + 3; j++) {
                    toReturn.add(sudoku[i][j]);
                }
            }
        }

        return complementarySet(toReturn);
    }

    public static Set<Integer> complementarySet(Set<Integer> negativeSet) {
        Set<Integer> positiveSet = new HashSet<>();
        positiveSet.addAll(List.of(1,2,3,4,5,6,7,8,9));
        for (Iterator<Integer> iterator = negativeSet.iterator(); iterator.hasNext(); ) {
            Integer next =  iterator.next();
            positiveSet.remove(next);
        }
        positiveSet.remove(0);
        return positiveSet;
    }

    public static int determineSector(int row, int column) {
        return sectorMap[row/3][column/3];
    }

    public static int determineSectorRow(int sector) {
        return sectorRow[sector];
    }

    public static int determineSectorColumn(int sector) {
        return sectorColumn[sector];
    }
}
