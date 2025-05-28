package com.snu.project;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.*;

public class ShamirStringUtil {

    // ===== DES Encryption Helpers =====

    private static SecretKey getKeyFromPassword(String password) throws Exception {
        byte[] keyBytes = password.getBytes("UTF-8");
        byte[] keyPadded = new byte[8];
        System.arraycopy(keyBytes, 0, keyPadded, 0, Math.min(keyBytes.length, 8));
        DESKeySpec keySpec = new DESKeySpec(keyPadded);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        return keyFactory.generateSecret(keySpec);
    }

    private static byte[] encryptBytes(byte[] data, String password) throws Exception {
        SecretKey key = getKeyFromPassword(password);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private static byte[] decryptBytes(byte[] encryptedData, String password) throws Exception {
        SecretKey key = getKeyFromPassword(password);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    // ===== Shamir Secret Sharing over Base64 String =====

    public static Map<Integer, String> splitEncryptedData(byte[] originalBytes, int n, int k, String password) throws Exception {
        byte[] encrypted = encryptBytes(originalBytes, password);
        String base64 = Base64.getEncoder().encodeToString(encrypted);
        int len = base64.length();

        Map<Integer, StringBuilder> shares = new HashMap<>();
        SecureRandom rand = new SecureRandom();

        for (int i = 0; i < len; i++) {
            int secret = base64.charAt(i);
            int[] coeffs = new int[k - 1];
            for (int j = 0; j < k - 1; j++) {
                coeffs[j] = rand.nextInt(256);
            }

            for (int x = 1; x <= n; x++) {
                int y = secret;
                for (int j = 0; j < k - 1; j++) {
                    y ^= multiply(coeffs[j], (int) Math.pow(x, j + 1));
                }
                shares.computeIfAbsent(x, key -> new StringBuilder()).append((char) y);
            }
        }

        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<Integer, StringBuilder> entry : shares.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString());
        }

        return result;
    }

    public static byte[] reconstructEncryptedData(Map<Integer, String> selectedShares, String password) throws Exception {
        // Ensure all shares are same length
        int len = selectedShares.values().iterator().next().length();
        for (String share : selectedShares.values()) {
            if (share.length() != len) {
                throw new IllegalArgumentException("❌ One or more share files are incomplete or corrupted. All shares must have equal length.");
            }
        }

        StringBuilder base64 = new StringBuilder();

        for (int i = 0; i < len; i++) {
            int[] x = new int[selectedShares.size()];
            int[] y = new int[selectedShares.size()];
            int idx = 0;
            for (Map.Entry<Integer, String> entry : selectedShares.entrySet()) {
                x[idx] = entry.getKey();
                y[idx] = entry.getValue().charAt(i);
                idx++;
            }
            int val = lagrangeInterpolation(0, x, y);
            base64.append((char) val);
        }

        try {
            byte[] encrypted = Base64.getDecoder().decode(base64.toString());
            return decryptBytes(encrypted, password);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("❌ Base64 decode failed. Possibly due to wrong shares or password.");
        }
    }

    // ===== Math Helpers =====

    private static int multiply(int a, int b) {
        return (a * b) % 257;
    }

    private static int modInverse(int a, int p) {
        for (int i = 1; i < p; i++) {
            if ((a * i) % p == 1) return i;
        }
        return 1;
    }

    private static int lagrangeInterpolation(int x, int[] xi, int[] yi) {
        int result = 0;
        int p = 257;

        for (int i = 0; i < xi.length; i++) {
            int num = 1, den = 1;
            for (int j = 0; j < xi.length; j++) {
                if (i != j) {
                    num = multiply(num, (x - xi[j] + p) % p);
                    den = multiply(den, (xi[i] - xi[j] + p) % p);
                }
            }
            int term = multiply(yi[i], multiply(num, modInverse(den, p)));
            result = (result + term) % p;
        }

        return result;
    }

    // ===== File I/O =====

    public static void saveShareToFile(String share, int index, String directory) throws IOException {
        File file = new File(directory + "/share" + index + ".txt");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(share);
        }
    }

    public static String loadShareFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine().trim(); // remove any newline
        }
    }
}

