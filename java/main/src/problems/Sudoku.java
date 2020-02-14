public class Sudoku {
    int[][] fixedMatrix;
    int[][] floatingMatrix;

    public Sudoku() {
        this.fixedMatrix = new int[9][9];
        this.floatingMatrix = new int[9][9];
    }

    public int[][] getMatrix() {
        return combineMatrices();
    }

    public int[][] getFixedMatrix() {
        return fixedMatrix;
    }

    public void setFixedMatrix(int[][] matrix) {
        this.fixedMatrix = matrix;
    }

    public void setFloatingMatrix(int[][] matrix) {
        this.floatingMatrix = matrix;
    }

    public int[][] combineMatrices() {
        int[][] combined = new int[9][9];
        for (int i = 0; i < fixedMatrix.length; i++) {
            for (int j = 0; j < fixedMatrix[0].length; j++) {
                combined[i][j] = fixedMatrix[i][j] + floatingMatrix[i][j];
            }
        }
        return combined;
    }

    public void printMatrix() {
        int[][] combinedMatrix = combineMatrices();
        System.out.println("-------------");
        for (int i = 0; i < 9; i++) {
            System.out.print("|");
            for (int j = 0; j < 9; j++) {
                if (j % 3 == 0 && j > 0) {
                    System.out.print("|");
                }
                System.out.print(combinedMatrix[i][j]);
            }
            System.out.print("|");
            if (i % 3 == 2 && i > 0) {
                System.out.println();
                System.out.println("-------------");
            } else {
                System.out.println();
            }
        }
    }
}
