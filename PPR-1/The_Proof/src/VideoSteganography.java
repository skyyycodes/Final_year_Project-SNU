import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

/*
 * Frame-level video watermarking.
 *
 * Approach:
 *   1. Extract frame 0 from the input video as a BMP using FFmpeg.
 *   2. Apply the DWM2 row-based LSB watermark to that BMP.
 *   3. Use FFmpeg's overlay filter to substitute the watermarked frame back
 *      into the video, re-encoding losslessly with FFV1 into an MKV container.
 *
 * Output is .mkv (lossless FFV1), not .mp4 — H.264 would quantize away the LSBs.
 *
 * Requires the `ffmpeg` (and `ffprobe`) executable on PATH.
 */
public class VideoSteganography {

    public static void hideText(File input, File output, String message, String key,
                                Consumer<String> log) throws Exception {

        log.accept("=== VIDEO EMBEDDING PROCESS (FRAME-LEVEL LSB) ===");

        if (message == null || message.trim().isEmpty()) {
            throw new Exception("Watermark text is empty");
        }
        if (message.length() > 16) {
            throw new Exception("Watermark must be 16 characters or less");
        }
        if (key == null || key.isEmpty()) {
            throw new Exception("Password is empty");
        }

        ensureFfmpeg();

        Path tempDir = Files.createTempDirectory("dwm2_video_embed_");
        try {
            File frameFile = tempDir.resolve("frame0.bmp").toFile();

            log.accept("Step 1 - Extracting frame 0 from " + input.getName());
            runFfmpeg(log,
                    "-y",
                    "-i", input.getAbsolutePath(),
                    "-vf", "select=eq(n\\,0)",
                    "-vframes", "1",
                    "-pix_fmt", "bgr24",
                    frameFile.getAbsolutePath());

            if (!frameFile.exists()) {
                throw new Exception("FFmpeg did not produce a frame; is the input a valid video?");
            }

            BufferedImage frame = ImageIO.read(frameFile);
            if (frame == null) {
                throw new Exception("Could not read extracted frame");
            }
            log.accept("Step 2 - Loaded frame: " + frame.getWidth() + "x" + frame.getHeight());

            if (frame.getWidth() < 256 || frame.getHeight() < 256) {
                throw new Exception("Video frames must be at least 256x256 pixels (algorithm requires a 256-pixel row)");
            }

            log.accept("Step 3 - Applying DWM2 row-based LSB watermark to top-left 256x256 region");
            BufferedImage watermarked = embedWatermarkOnFrame(frame, message, key, log);

            ImageIO.write(watermarked, "BMP", frameFile);
            log.accept("Step 4 - Watermarked frame saved");

            log.accept("Step 5 - Re-encoding video with FFV1 (lossless), substituting frame 0...");
            runFfmpeg(log,
                    "-y",
                    "-i", input.getAbsolutePath(),
                    "-i", frameFile.getAbsolutePath(),
                    "-filter_complex",
                    "[0:v]format=bgr24[base];[base][1:v]overlay=enable='eq(n\\,0)'[v]",
                    "-map", "[v]",
                    "-map", "0:a?",
                    "-c:v", "ffv1",
                    "-level", "3",
                    "-pix_fmt", "bgr24",
                    "-c:a", "copy",
                    output.getAbsolutePath());

            log.accept("=== VIDEO EMBEDDING COMPLETED ===");
            log.accept("Output: " + output.getAbsolutePath() + " (" + (output.length() / 1024 / 1024) + " MB)");
        } finally {
            deleteRecursive(tempDir);
        }
    }

    public static String extractText(File video, String key, Consumer<String> log) throws Exception {

        log.accept("=== VIDEO EXTRACTION PROCESS (FRAME-LEVEL LSB) ===");

        if (key == null || key.isEmpty()) {
            throw new Exception("Password is empty");
        }

        ensureFfmpeg();

        Path tempDir = Files.createTempDirectory("dwm2_video_extract_");
        try {
            File frameFile = tempDir.resolve("frame0.bmp").toFile();

            log.accept("Step 1 - Extracting frame 0 from " + video.getName());
            runFfmpeg(log,
                    "-y",
                    "-i", video.getAbsolutePath(),
                    "-vf", "select=eq(n\\,0)",
                    "-vframes", "1",
                    "-pix_fmt", "bgr24",
                    frameFile.getAbsolutePath());

            BufferedImage frame = ImageIO.read(frameFile);
            if (frame == null) {
                throw new Exception("Could not read extracted frame");
            }
            log.accept("Step 2 - Loaded frame: " + frame.getWidth() + "x" + frame.getHeight());

            return extractWatermarkFromFrame(frame, key, log);
        } finally {
            deleteRecursive(tempDir);
        }
    }

    /* =========================================================
     * Row-based LSB algorithm — same logic as DWM2.embedWatermark
     * Operates on the top-left 256x256 region of the frame.
     * ========================================================= */

    private static BufferedImage embedWatermarkOnFrame(BufferedImage src, String watermark, String password,
                                                       Consumer<String> log) throws Exception {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH/MM/ss/dd/MM/yyyy"));
        String fullWatermark = watermark + "#" + timestamp + "#@";
        log.accept("  Full watermark: \"" + fullWatermark + "\"");

        String md5 = md5Hex(password);
        int rowNumber = xorFirst12Bytes(md5);
        int startColumn = xorLast4Bytes(md5);
        log.accept("  Password MD5: " + md5);
        log.accept("  Row " + rowNumber + ", start column " + startColumn);

        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();

        // Row occupation check (column 0 of selected row)
        if (readCharAt(out, 0, rowNumber) == 36) {
            throw new Exception("Row " + rowNumber + " already occupied. Use a different password.");
        }

        // Mark row occupied with '$'
        embedCharAt(out, 0, rowNumber, '$');

        char[] payload = ("*" + fullWatermark).toCharArray();
        if (payload.length > 256) {
            throw new Exception("Watermark too long: " + payload.length + " characters (max 256)");
        }

        for (int i = 0; i < payload.length; i++) {
            int x = (startColumn + i) % 256;
            embedCharAt(out, x, rowNumber, payload[i]);
        }
        log.accept("  Embedded " + payload.length + " chars at row " + rowNumber);
        return out;
    }

    private static String extractWatermarkFromFrame(BufferedImage frame, String password,
                                                    Consumer<String> log) throws Exception {
        String md5 = md5Hex(password);
        int rowNumber = xorFirst12Bytes(md5);
        int startColumn = xorLast4Bytes(md5);
        log.accept("Step 3 - Reading row " + rowNumber + " starting at column " + startColumn);

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            int x = (startColumn + i) % 256;
            out.append((char) readCharAt(frame, x, rowNumber));
            if (out.length() >= 2 && out.substring(out.length() - 2).equals("#@")) break;
            if (i >= 100) break; // matches DWM2 safety limit
        }

        String full = out.toString();
        if (!full.endsWith("#@")) {
            throw new Exception("End marker '#@' not found — wrong password or no watermark");
        }
        String content = full.substring(0, full.length() - 2);
        if (!content.isEmpty() && content.charAt(0) == '*') content = content.substring(1);

        String[] parts = content.split("#", 2);
        if (parts.length < 2) {
            throw new Exception("Delimiter '#' not found in extracted content");
        }
        log.accept("  Watermark: \"" + parts[0] + "\"");
        log.accept("  Timestamp: \"" + parts[1] + "\"");
        return parts[0] + " (timestamp: " + parts[1] + ")";
    }

    private static void embedCharAt(BufferedImage img, int x, int y, char c) {
        int rgb = img.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int ascii = (int) c;
        int newR = (r & 0xF8) | ((ascii >> 5) & 0x07);
        int newG = (g & 0xF8) | ((ascii >> 2) & 0x07);
        int newB = (b & 0xFC) | (ascii & 0x03);
        img.setRGB(x, y, (newR << 16) | (newG << 8) | newB);
    }

    private static int readCharAt(BufferedImage img, int x, int y) {
        int rgb = img.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return ((r & 0x07) << 5) | ((g & 0x07) << 2) | (b & 0x03);
    }

    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 unavailable", e);
        }
    }

    private static int xorFirst12Bytes(String hash) {
        int r = Integer.parseInt(hash.substring(0, 2), 16);
        for (int i = 2; i < 24; i += 2) {
            r ^= Integer.parseInt(hash.substring(i, i + 2), 16);
        }
        return r;
    }

    private static int xorLast4Bytes(String hash) {
        int r = Integer.parseInt(hash.substring(24, 26), 16);
        for (int i = 26; i < hash.length(); i += 2) {
            r ^= Integer.parseInt(hash.substring(i, i + 2), 16);
        }
        return r;
    }

    /* =========================================================
     * FFmpeg helpers
     * ========================================================= */

    private static void ensureFfmpeg() throws Exception {
        try {
            Process p = new ProcessBuilder("ffmpeg", "-version")
                    .redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            int rc = p.waitFor();
            if (rc != 0) {
                throw new Exception("ffmpeg returned exit code " + rc);
            }
        } catch (IOException e) {
            throw new Exception(
                    "FFmpeg not found on PATH. Install FFmpeg (e.g. `brew install ffmpeg` on macOS) and retry.");
        }
    }

    private static void runFfmpeg(Consumer<String> log, String... args) throws Exception {
        String[] cmd = new String[args.length + 1];
        cmd[0] = "ffmpeg";
        System.arraycopy(args, 0, cmd, 1, args.length);

        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line).append('\n');
            }
        }
        int rc = p.waitFor();
        if (rc != 0) {
            log.accept("FFmpeg failed:\n" + out);
            throw new Exception("FFmpeg failed (exit " + rc + "). See log for details.");
        }
    }

    private static void deleteRecursive(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
