import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class TextSteganography {

    // Zero-width Space (U+200B) = bit 0  |  Zero-width Non-Joiner (U+200C) = bit 1
    // These characters are completely invisible in all text editors and viewers
    private static final char ZW_ZERO = '​';
    private static final char ZW_ONE  = '‌';

    public static void hideText(File input, File output, String message, String key,
                                java.util.function.Consumer<String> log) throws Exception {

        log.accept("=== TEXT EMBEDDING PROCESS (Zero-Width Steganography) ===");

        String content = new String(Files.readAllBytes(input.toPath()), StandardCharsets.UTF_8);
        log.accept("Step 1 - Original file loaded (" + content.length() + " chars)");

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH/MM/ss/dd/MM/yyyy"));

        String fullMessage = message + "#" + timestamp + "#@";
        log.accept("Step 2 - Full message: \"" + fullMessage + "\"");

        String encrypted = xorEncrypt(fullMessage, key);
        log.accept("Step 3 - XOR encryption complete (" + encrypted.length() + " chars)");

        // Convert each char to 8 zero-width bits
        StringBuilder zwBits = new StringBuilder();
        for (char c : encrypted.toCharArray()) {
            int val = c & 0xFF;
            for (int i = 7; i >= 0; i--) {
                zwBits.append(((val >> i) & 1) == 0 ? ZW_ZERO : ZW_ONE);
            }
        }
        log.accept("Step 4 - Encoded as " + zwBits.length() + " invisible zero-width characters");

        // Insert the invisible bits right after the first space in the text
        int insertPos = content.indexOf(' ');
        if (insertPos == -1) insertPos = content.length();
        else insertPos++;

        String stegoContent = content.substring(0, insertPos)
                            + zwBits
                            + content.substring(insertPos);

        Files.write(output.toPath(), stegoContent.getBytes(StandardCharsets.UTF_8));

        log.accept("Step 5 - Watermark inserted invisibly at char position " + insertPos);
        log.accept("=== EMBEDDING COMPLETED ===");
        log.accept("Output file looks IDENTICAL to original - watermark is truly invisible.");
    }

    public static String extractText(File input, String key,
                                     java.util.function.Consumer<String> log) throws Exception {

        log.accept("=== TEXT EXTRACTION PROCESS (Zero-Width Steganography) ===");

        String content = new String(Files.readAllBytes(input.toPath()), StandardCharsets.UTF_8);

        // Collect all zero-width bits
        StringBuilder bits = new StringBuilder();
        for (char c : content.toCharArray()) {
            if      (c == ZW_ZERO) bits.append('0');
            else if (c == ZW_ONE)  bits.append('1');
        }

        if (bits.length() == 0) {
            log.accept("ERROR: No zero-width watermark found in this file.");
            return null;
        }
        if (bits.length() % 8 != 0) {
            log.accept("ERROR: Corrupted watermark (bit count not multiple of 8).");
            return null;
        }

        log.accept("Step 1 - Found " + bits.length() + " hidden bits (" + (bits.length() / 8) + " chars)");

        // Reconstruct encrypted string from bits
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i + 8 <= bits.length(); i += 8) {
            encrypted.append((char) Integer.parseInt(bits.substring(i, i + 8), 2));
        }

        String decrypted = xorEncrypt(encrypted.toString(), key);
        log.accept("Step 2 - XOR decryption complete");

        int endIdx = decrypted.indexOf("#@");
        if (endIdx == -1) {
            log.accept("ERROR: End marker '#@' not found — wrong password or corrupted file.");
            return null;
        }

        String result = decrypted.substring(0, endIdx + 2);
        log.accept("Step 3 - End marker found. Extracted: \"" + result + "\"");
        log.accept("=== EXTRACTION COMPLETED ===");
        return result;
    }

    private static String xorEncrypt(String text, String key) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            out.append((char) (text.charAt(i) ^ key.charAt(i % key.length())));
        }
        return out.toString();
    }
}
