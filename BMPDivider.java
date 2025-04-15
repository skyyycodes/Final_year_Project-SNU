
import java.io.*;
import java.util.Scanner;

public class BMPDivider {
    private static final int HEADER_SIZE = 54;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("BMP Tool - Choose an option:");
        System.out.println("1. Split BMP into header and pixel data");
        System.out.println("2. Recombine header and pixel data into BMP");
        System.out.print("Enter choice (1 or 2): ");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        switch (choice) {
            case 1:
                System.out.print("Enter BMP file path to split: ");
                String bmpPath = scanner.nextLine();
                splitBMP(bmpPath);
                break;

            case 2:
                System.out.print("Enter header file path: ");
                String headerPath = scanner.nextLine();
                System.out.print("Enter pixel data file path: ");
                String pixelPath = scanner.nextLine();
                System.out.print("Enter output BMP file name: ");
                String outputPath = scanner.nextLine();
                recombineBMP(headerPath, pixelPath, outputPath);
                break;

            default:
                System.out.println("Invalid choice.");
        }
        scanner.close();
    }

    private static void splitBMP(String bmpPath) {
        try (FileInputStream fis = new FileInputStream(bmpPath)) {
            byte[] fileData = fis.readAllBytes();

            if (fileData.length < HEADER_SIZE) {
                System.out.println("File too small to be a valid BMP.");
                return;
            }

            byte[] header = new byte[HEADER_SIZE];
            byte[] pixelData = new byte[fileData.length - HEADER_SIZE];

            System.arraycopy(fileData, 0, header, 0, HEADER_SIZE);
            System.arraycopy(fileData, HEADER_SIZE, pixelData, 0, pixelData.length);

            try (FileOutputStream headerOut = new FileOutputStream("header.bin");
                 FileOutputStream pixelOut = new FileOutputStream("pixels.bin")) {
                headerOut.write(header);
                pixelOut.write(pixelData);
            }

            System.out.println("Split complete. Files saved as 'header.bin' and 'pixels.bin'.");
        } catch (IOException e) {
            System.err.println("Error during split: " + e.getMessage());
        }
    }

    private static void recombineBMP(String headerPath, String pixelPath, String outputPath) {
        try (FileInputStream headerIn = new FileInputStream(headerPath);
             FileInputStream pixelIn = new FileInputStream(pixelPath);
             FileOutputStream bmpOut = new FileOutputStream(outputPath)) {

            byte[] header = headerIn.readAllBytes();
            byte[] pixels = pixelIn.readAllBytes();

            if (header.length != HEADER_SIZE) {
                System.out.println("Invalid header size. Must be exactly 54 bytes.");
                return;
            }

            bmpOut.write(header);
            bmpOut.write(pixels);

            System.out.println("BMP recombined and saved as: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error during recombination: " + e.getMessage());
        }
    }
}
