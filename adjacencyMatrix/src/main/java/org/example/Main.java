package org.example;

public class Main {
    private static int[][] matris = {
            {9, 4},
            {6, 3},
            {5, 8}
    };

    public static void main(String[] args) {
        // Matris yazdırma
        for (int i = 0; i < matris.length; i++) {
            for (int j = 0; j < matris[i].length; j++) {
                System.out.print(matris[i][j] + " ");
            }
            System.out.println();
        }

        // Komşuluk matrisi oluştur
        int[][] adjacencyMatrix = buildAdjacencyMatrix(matris);

        // Başlık
        System.out.println("9 4 6 3 5 8");
        System.out.println("____________");

        // Komşuluk matrisi yazdırma
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                System.out.print(adjacencyMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Komşuluk matrisi üretir
    private static int[][] buildAdjacencyMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        int n = rows * cols; // toplam eleman sayısı
        int[][] adjacencyMatrix = new int[n][n];

        // Her hücre için komşularına bak
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int index1 = i * cols + j; // düğüm index

                // Yukarı
                if (i - 1 >= 0) {
                    int index2 = (i - 1) * cols + j;
                    adjacencyMatrix[index1][index2] = 1;
                }
                // Aşağı
                if (i + 1 < rows) {
                    int index2 = (i + 1) * cols + j;
                    adjacencyMatrix[index1][index2] = 1;
                }
                // Sol
                if (j - 1 >= 0) {
                    int index2 = i * cols + (j - 1);
                    adjacencyMatrix[index1][index2] = 1;
                }
                // Sağ
                if (j + 1 < cols) {
                    int index2 = i * cols + (j + 1);
                    adjacencyMatrix[index1][index2] = 1;
                }
            }
        }

        return adjacencyMatrix;
    }
}
