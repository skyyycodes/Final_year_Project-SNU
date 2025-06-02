import java.io.FileInputStream;
import java.io.IOException;

public class BMPWithHeaderAndRGBFlatArray {

    public static void main(String[] args) {
        String bmpFilePath = "C:\\Users\\Akash Chakraborty\\OneDrive\\Desktop\\Final_year_Project-SNU\\Mini Assignments\\Assignment4\\Ass4\\src\\IMG-20250527-WA0064_imresizer.bmp";  // Replace with your BMP image path

        final int WIDTH = 256;
        final int HEIGHT = 256;
        final int HEADER_SIZE = 54;
        final int PIXEL_COUNT = WIDTH * HEIGHT;
        final int RGB_DATA_SIZE = PIXEL_COUNT * 3;
        final int TOTAL_SIZE = RGB_DATA_SIZE + HEADER_SIZE;

        byte[] header = new byte[HEADER_SIZE];
        byte[] flatRGBArray = new byte[RGB_DATA_SIZE];

        try (FileInputStream fis = new FileInputStream(bmpFilePath)) {
            // Read header
            int headerRead = fis.read(header);
            if (headerRead != HEADER_SIZE) {
                throw new IOException("Could not read full BMP header.");
            }

            // Print header bytes
            System.out.println("=== BMP Header (54 bytes) ===");
            for (int i = 0; i < HEADER_SIZE; i++) {
                System.out.printf("0x%02X ", header[i]);
                if ((i + 1) % 8 == 0) System.out.println();
            }

            // Read and flatten pixel data
            int rowSize = (WIDTH * 3 + 3) & ~3;  // padded to 4-byte boundary
            byte[] rowData = new byte[rowSize];
            int index = 0;

            for (int row = HEIGHT - 1; row >= 0; row--) {
                int bytesRead = fis.read(rowData);
                if (bytesRead != rowSize) {
                    throw new IOException("Unexpected EOF at row " + row);
                }

                for (int col = 0; col < WIDTH; col++) {
                    int base = col * 3;

                    byte blue = rowData[base];
                    byte green = rowData[base + 1];
                    byte red = rowData[base + 2];

                    flatRGBArray[index++] = red;
                    flatRGBArray[index++] = green;
                    flatRGBArray[index++] = blue;
                }
            }

            // Print pixel data in [r: g: b:] format
            System.out.println("\n=== Pixel RGB Data ===");
            for (int i = 0; i < flatRGBArray.length; i += 3) {
                int r = Byte.toUnsignedInt(flatRGBArray[i]);
                int g = Byte.toUnsignedInt(flatRGBArray[i + 1]);
                int b = Byte.toUnsignedInt(flatRGBArray[i + 2]);

                System.out.print("[r:" + r + " g:" + g + " b:" + b + "]");
                if (i + 3 < flatRGBArray.length) {
                    System.out.print(", ");
                }

                if (((i / 3 + 1) % 10) == 0) System.out.println();
            }

            // Total count
            System.out.println("\nTotal header bytes: " + header.length);
            System.out.println("Total RGB data bytes: " + flatRGBArray.length);
            System.out.println("Total combined elements: " + TOTAL_SIZE);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
