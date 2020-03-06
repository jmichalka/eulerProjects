package problems;

import com.sun.tools.javac.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


public class Pb96 {
    static int[][] sectorMap = new int[][]{{0,1,2},{3,4,5},{6,7,8}};
    static int[] sectorRow = new int[]{0,0,0,3,3,3,6,6,6};
    static int[] sectorColumn = new int[]{0,3,6,0,3,6,0,3,6};


    public static List<Sudoku> loadSudokus() throws Exception{
        File file = new File(Main.class.getClassLoader().getResource("p096_sudoku.txt").getPath());
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
        }
        return toReturn;
    }

    public static void main(String[] args) throws Exception {
        int sum = 0;
        int unsolved = 0;
        List<Sudoku> sudokus = loadSudokus();
        for (int i = 0; i < 50; i++) {
            Sudoku currentSudoku = sudokus.get(i);
            int maxLoops = 25;
            //currentSudoku.printMatrix();
            while (!isPuzzleSolved(currentSudoku.getMatrix()) && maxLoops > 0){

                int[][] currentMatrix = currentSudoku.getMatrix();
                boolean setAtLeastOneValue = false;
                PossibleValues[][] allPossibilities = new PossibleValues[9][9];
                for (int row = 0; row < 9; row++) {
                    for (int column = 0; column < 9; column++) {
                        allPossibilities[row][column] = new PossibleValues(determinePossibleValuesAtThisIndex(currentMatrix, row, column));
                    }
                }
                for (int grid = 0; grid < 9; grid++) {
                    List<Integer> unsetIntegers = determineUnsetIntegersInThisSquare(grid, currentMatrix);
                    for (int unsetIndex = 0; unsetIndex < unsetIntegers.size(); unsetIndex++) {
//Next deduction, if can narrow down a number to one row/column in a square, can go in other squares in (row/column)
//and remove that number from the possibilities
// for instance, in sudoku 5, when it gets stuck, in block 3, both 5/4 must be in top row, so in grid 2, must be bottom
// row...
                        Integer testInteger = unsetIntegers.get(unsetIndex);
                        int possibleRow = onlyOneRowContainsThisNumber(grid, testInteger, currentMatrix, allPossibilities);
                        if (possibleRow != -1) {
                            removeIntegerFromPossibleValuesInOtherGridsAlongThisRow(grid, testInteger, possibleRow, allPossibilities);
                        }
                        int possibleColumn = onlyOneColumnContainsThisNumber(grid, testInteger, currentMatrix, allPossibilities);
                        if (possibleColumn != -1) {
                            removeIntegerFromPossibleValuesInOtherGridsAlongThisColumn(grid, testInteger, possibleColumn, allPossibilities);
                        }
                    }
                }
                for (int row = 0; row < 9; row++) {
                    for (int column = 0; column < 9; column++) {
                        List<Integer> possibility = allPossibilities[row][column].getValues();
                        if (possibility.size() == 1) {
                            currentSudoku.setFixedPosition(row, column, possibility.get(0));
                            setAtLeastOneValue = true;
                        }
                    }
                }

                for (int row = 0; row < 9; row++) {
                    for (int column = 0; column < 9; column++) {
                        List<Integer> possibility = allPossibilities[row][column].getValues();
                        for (int j = 0; j < possibility.size(); j++) {
                            if (checkIntersection(row, column, possibility.get(j), allPossibilities)) {
                                currentSudoku.setFixedPosition(row, column, possibility.get(j));
                                setAtLeastOneValue = true;
                            }
                        }
                    }
                }


                if (!setAtLeastOneValue) {
                    //System.out.println(determineUnsetIntegersInThisSquare(0, currentMatrix));
                    currentSudoku = attemptBruteForceSolve(currentSudoku, allPossibilities);
                    if (!validateCurrentBoard(currentSudoku.getMatrix())) {
                        System.out.println("Something went wrong.");
                    }
                }
                maxLoops--;
            }
            System.out.println("Loops left: " + maxLoops);
            currentSudoku.printMatrix();
            sum += currentSudoku.getSumForEuler();

        }
        System.out.println("Unsolved: " + unsolved);
        System.out.println("Sum" + sum);
    }


    public static Sudoku attemptBruteForceSolve(Sudoku currentSudoku, PossibleValues[][] allPossibilities) {
        recursiveSolve(0, 81, currentSudoku, allPossibilities);
        return currentSudoku;
    }

    public static boolean recursiveSolve(int gridIndex, int emptySpacesLeft, Sudoku currentSudoku, PossibleValues[][] allPossibilities) {
        if (emptySpacesLeft == 0) {
            if (validateCurrentBoard(currentSudoku.getMatrix())) {
                currentSudoku.setFixedMatrix(currentSudoku.combineMatrices());
                return true;
            }
            return false;
        }
        int row = determineRowFromListPosition(gridIndex);
        int column = determineColumnFromListPosition(gridIndex);

        List<Integer> possibleValues = allPossibilities[row][column].getValues();
        if (possibleValues.size() > 0) {
            int[][] floatingMatrix = currentSudoku.getFloatingMatrix();
            for (int i = 0; i < possibleValues.size(); i++) {
                floatingMatrix[row][column] = possibleValues.get(i);
                currentSudoku.setFloatingMatrix(floatingMatrix);
                if (validateCurrentBoard(currentSudoku.getMatrix())) {
                    if (recursiveSolve(gridIndex + 1, emptySpacesLeft - 1, currentSudoku, allPossibilities)) {
                        return true;
                    }
                }
            }
            floatingMatrix[row][column] = 0;
            currentSudoku.setFloatingMatrix(floatingMatrix);
            return false;
        } else {
            return recursiveSolve(gridIndex + 1, emptySpacesLeft - 1, currentSudoku, allPossibilities);
        }
    }

    public static List<Integer> determineUnsetIntegersInThisSquare(int sector, int[][] currentMatrix) {
        Set<Integer> toReturn = new HashSet<>();
        int sectorRow = determineSectorRow(sector);
        int sectorColumn = determineSectorColumn(sector);
        for (int i = sectorRow; i < sectorRow + 3; i++) {
            for (int j = sectorColumn; j < sectorColumn + 3; j++) {
                toReturn.add(currentMatrix[i][j]);
            }
        }
        return new ArrayList<>(complementarySet(toReturn));
    }

    public static int onlyOneRowContainsThisNumber(int sector, Integer testInteger, int[][] currentMatrix, PossibleValues[][] allPossibilities) {
        int sectorRow = determineSectorRow(sector);
        int sectorColumn = determineSectorColumn(sector);
        boolean notSet = true;
        int keyRow = -1;
        for (int i = sectorRow; i < sectorRow + 3; i++) {
            for (int j = sectorColumn; j < sectorColumn + 3; j++) {
                if (currentMatrix[i][j] == 0) {
                    if (allPossibilities[i][j].getValues().contains(testInteger)) {
                        if (notSet) {
                            keyRow = i;
                            notSet = false;
                        } else {
                            if (keyRow != i) {
                                return -1;
                            }
                        }
                    }
                }
            }
        }
        return keyRow;
    }

    public static int onlyOneColumnContainsThisNumber(int sector, Integer testInteger, int[][] currentMatrix, PossibleValues[][] allPossibilities) {
        int sectorRow = determineSectorRow(sector);
        int sectorColumn = determineSectorColumn(sector);
        boolean notSet = true;
        int keyColumn = -1;
        for (int i = sectorRow; i < sectorRow + 3; i++) {
            for (int j = sectorColumn; j < sectorColumn + 3; j++) {
                if (currentMatrix[i][j] == 0) {
                    if (allPossibilities[i][j].getValues().contains(testInteger)) {
                        if (notSet) {
                            keyColumn = j;
                            notSet = false;
                        } else {
                            if (keyColumn != j) {
                                return -1;
                            }
                        }
                    }
                }
            }
        }
        return keyColumn;
    }

    public static PossibleValues[][] removeIntegerFromPossibleValuesInOtherGridsAlongThisRow(int sector, Integer valueToRemove, int row, PossibleValues[][] allPossibilities) {
        // 0 -> 1,2
        // 1 -> 2,0
        // 2 -> 0,1
        int otherSectorA = (sector + 1) % 3;
        int sectorAColumn = determineSectorColumn(otherSectorA);
        int otherSectorB = (sector + 2) % 3;
        int sectorBColumn = determineSectorColumn(otherSectorB);
        for (int column = sectorAColumn; column < sectorAColumn + 3; column++) {
            List<Integer> values = allPossibilities[row][column].getValues();
            if (values.contains(valueToRemove)) {
                values.remove(valueToRemove);
                allPossibilities[row][column].setValues(values);
            }
        }
        for (int column = sectorBColumn; column < sectorBColumn + 3; column++) {
            List<Integer> values = allPossibilities[row][column].getValues();
            if (values.contains(valueToRemove)) {
                values.remove(valueToRemove);
                allPossibilities[row][column].setValues(values);
            }
        }

        return allPossibilities;
    }

    public static PossibleValues[][] removeIntegerFromPossibleValuesInOtherGridsAlongThisColumn(int sector, Integer valueToRemove, int column, PossibleValues[][] allPossibilities) {
        // 0 -> 3,6
        // 3 -> 6,0
        // 6 -> 0,3
        int otherSectorA = (sector + 3) % 9;
        int sectorARow = determineSectorRow(otherSectorA);
        int otherSectorB = (sector + 6) % 9;
        int sectorBRow = determineSectorRow(otherSectorB);
        for (int row = sectorARow; row < sectorARow + 3; row++) {
            List<Integer> values = allPossibilities[row][column].getValues();
            if (values.contains(valueToRemove)) {
                values.remove(valueToRemove);
                allPossibilities[row][column].setValues(values);
            }
        }
        for (int row = sectorBRow; row < sectorBRow + 3; row++) {
            List<Integer> values = allPossibilities[row][column].getValues();
            if (values.contains(valueToRemove)) {
                values.remove(valueToRemove);
                allPossibilities[row][column].setValues(values);
            }
        }

        return allPossibilities;
    }


    public static int determineListPositionFromRowAndColumn(int row, int column) {
        return row*9+column;
    }

    public static int determineRowFromListPosition(int position) {
        return position/9;
    }

    public static int determineColumnFromListPosition(int position) {
        return position%9;
    }

    public static boolean onlyValueInRow(int row, int column, int value, PossibleValues[][] possibleValues) {
        for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
            if (column != columnIndex) {
                PossibleValues currentSpot = possibleValues[row][columnIndex];
                if (currentSpot.getValues().contains(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean onlyValueInColumn(int row, int column, int value, PossibleValues[][] possibleValues) {
        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            if (row != rowIndex) {
                PossibleValues currentSpot = possibleValues[rowIndex][column];
                if (currentSpot.getValues().contains(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean onlyValueInSquare(int row, int column, int value, PossibleValues[][] possibleValues) {
        int squareIndex = determineSector(row, column);

        int offSetY = (squareIndex % 3)*3;
        int offSetX = (squareIndex/3)*3;

        for (int i = offSetX; i < offSetX+3; i++) {
            for (int j = offSetY; j < offSetY+3; j++) {
                if (row == i && column == j) {
                    //skip
                } else {
                    PossibleValues currentSpot = possibleValues[i][j];
                    if (currentSpot.getValues().contains(value)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkIntersection(int row, int column, int value, PossibleValues[][] possibleValues) {
        return onlyValueInRow(row, column, value, possibleValues) ||
                onlyValueInColumn(row, column, value, possibleValues) ||
                onlyValueInSquare(row, column, value, possibleValues);
    }

    public static boolean isPuzzleSolved(int[][] sudoku) {
        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
                if (sudoku[rowIndex][columnIndex] == 0) {
                    return false;
                }
            }
        }
        return true;
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
        int offSetX = (squareIndex/3)*3;
        int offSetY = (squareIndex % 3)*3;

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

    public static List<Integer> determinePossibleValuesAtThisIndex(int[][] sudoku, int row, int column) {
        Set<Integer> toReturn = new HashSet<>();

        if (sudoku[row][column] != 0) {
            return new ArrayList<>(toReturn);
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

        return new ArrayList<>(complementarySet(toReturn));
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
