package com.snu.project;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LSBSteganography {

    public static BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static void saveImage(BufferedImage image, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        ImageIO.write(image, "png", outputFile);
    }

    public static BufferedImage embedText(String text, BufferedImage image) {
        // Append end marker
        text += "#####";
        byte[] msgBytes = text.getBytes();
        int msgLength = msgBytes.length;
        int width = image.getWidth();
        int height = image.getHeight();
        int msgIndex = 0, bitIndex = 0;

        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (msgIndex >= msgLength) break outer;

                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;

                // Embed one bit of message byte into LSB of red
                int bit = (msgBytes[msgIndex] >> (7 - bitIndex)) & 1;
                red = (red & 0xFE) | bit;

                // Repack RGB with new red
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                int newRGB = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newRGB);

                bitIndex++;
                if (bitIndex == 8) {
                    bitIndex = 0;
                    msgIndex++;
                }
            }
        }
        return image;
    }

    public static String extractText(BufferedImage image) {
        StringBuilder builder = new StringBuilder();
        int width = image.getWidth();
        int height = image.getHeight();
        int bitCount = 0;
        byte currentByte = 0;

        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int lsb = red & 1;

                currentByte = (byte) ((currentByte << 1) | lsb);
                bitCount++;

                if (bitCount == 8) {
                    char character = (char) currentByte;
                    builder.append(character);
                    if (builder.length() >= 5 && builder.substring(builder.length() - 5).equals("#####")) break outer;
                    bitCount = 0;
                    currentByte = 0;
                }
            }
        }

        return builder.toString().replace("#####", ""); // remove end marker
    }
}


