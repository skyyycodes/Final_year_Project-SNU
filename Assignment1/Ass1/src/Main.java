import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int[][][] array = new int[3][3][3];
        int[] linearArray = new int[27];

        System.out.println("Choose the input method:" );
        System.out.println("1: Row-wise measure");
        System.out.println("2: Column-wise measure");
        System.out.println("3: Page-wise measure");
        int choice = scanner.nextInt();

        System.out.println("Enter 27 elements:");

        switch (choice) {
            case 1:
                inputRowWise(array, scanner, linearArray);
                break;
            case 2:
                inputColumnWise(array, scanner, linearArray);
                break;
            case 3:
                inputPageWise(array, scanner, linearArray);
                break;
            default:
                System.out.println("Invalid choice!");
                scanner.close();
                return;
        }

        System.out.println("\nThe 3x3x3 array is:");
        printArray(array);

        System.out.println("\nEnter Page (i), Row (j), and Column (k) indices (0-based) to retrieve the value:");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
        int page = scanner.nextInt();

        if (row >= 0 && row < 3 && col >= 0 && col < 3 && page >= 0 && page < 3) {
            int value = array[row][col][page];
            System.out.println("Value at (" + row + ", " + col + ", " + page + "): " + value);

            int position = findLinearPosition(linearArray, value);
            if (position != -1) {
                System.out.println("linear index: " + position) ;
            } else {
                System.out.println("Value not found in linear array!");
            }
        } else {
            System.out.println("Invalid indices!");
        }

        scanner.close();
    }

    private static int findLinearPosition(int[] linearArray, int value) {
        for (int i = 0; i < linearArray.length; i++) {
            if (linearArray[i] == value) {
                return i + 1; // 1-based index
            }
        }
        return -1;
    }

    private static void inputRowWise(int[][][] array, Scanner scanner, int[] linearArray) {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    array[i][j][k] = scanner.nextInt();
                    linearArray[index++] = array[i][j][k];
                }
            }
        }
    }

    private static void inputColumnWise(int[][][] array, Scanner scanner, int[] linearArray) {
        int index = 0;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                for (int k = 0; k < 3; k++) {
                    array[i][j][k] = scanner.nextInt();
                    linearArray[index++] = array[i][j][k];
                }
            }
        }
    }

    private static void inputPageWise(int[][][] array, Scanner scanner, int[] linearArray) {
        int index = 0;
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    array[i][j][k] = scanner.nextInt();
                    linearArray[index++] = array[i][j][k];
                }
            }
        }
    }

    private static void printArray(int[][][] array) {
        for (int i = 0; i < 3; i++) {
            System.out.println("Page i = " + i + ":");
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    System.out.print(array[i][j][k] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }
}
