package com.snu.project;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class MainGUI {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("Watermark Protection Tool");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton btnSignature = new JButton("Embed/View Digital Signature");
        JButton btnShamir = new JButton("Image Sharing (Shamir's)");

        btnSignature.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Feature 1 already implemented.");
        });

        // Feature 2: Shamir's Secret Sharing using ShamirStringUtil
        btnShamir.addActionListener(e -> {
            String[] options = {"Split Image into Shares", "Reconstruct Image from Shares"};
            int choice = JOptionPane.showOptionDialog(
                    frame, "What would you like to do?",
                    "Shamir's Secret Sharing",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) { // Split
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select image to split");
                if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                File inputImage = fileChooser.getSelectedFile();

                try {
                    byte[] imageBytes = new FileInputStream(inputImage).readAllBytes();

                    JPasswordField pf1 = new JPasswordField();
                    JPasswordField pf2 = new JPasswordField();
                    if (JOptionPane.showConfirmDialog(frame, pf1, "Enter Password", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                    if (JOptionPane.showConfirmDialog(frame, pf2, "Confirm Password", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                    String pass1 = new String(pf1.getPassword());
                    String pass2 = new String(pf2.getPassword());
                    if (!pass1.equals(pass2)) {
                        JOptionPane.showMessageDialog(frame, "Passwords do not match.");
                        return;
                    }

                    int n = Integer.parseInt(JOptionPane.showInputDialog("Enter total number of shares (n):"));
                    int k = Integer.parseInt(JOptionPane.showInputDialog("Enter minimum shares to reconstruct (k):"));
                    if (k > n || k < 2) {
                        JOptionPane.showMessageDialog(frame, "Invalid values: k must be <= n and >= 2");
                        return;
                    }

                    JFileChooser dirChooser = new JFileChooser();
                    dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    dirChooser.setDialogTitle("Select folder to save shares");
                    if (dirChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                    String outputDir = dirChooser.getSelectedFile().getAbsolutePath();

                    Map<Integer, String> shares = ShamirStringUtil.splitEncryptedData(imageBytes, n, k, pass1);
                    for (Map.Entry<Integer, String> entry : shares.entrySet()) {
                        ShamirStringUtil.saveShareToFile(entry.getValue(), entry.getKey(), outputDir);
                    }

                    JOptionPane.showMessageDialog(frame, "Image split into " + n + " shares.");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error during splitting: " + ex.getMessage());
                }

            } else if (choice == 1) { // Reconstruct
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select share files");
                fileChooser.setMultiSelectionEnabled(true);
                if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                File[] selectedFiles = fileChooser.getSelectedFiles();
                if (selectedFiles.length < 2) {
                    JOptionPane.showMessageDialog(frame, "Select at least 2 share files.");
                    return;
                }

                JPasswordField pf = new JPasswordField();
                if (JOptionPane.showConfirmDialog(frame, pf, "Enter Password", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                String password = new String(pf.getPassword());

                try {
                    Map<Integer, String> selectedShares = new HashMap<>();
                    for (File f : selectedFiles) {
                        String name = f.getName().replaceAll("[^0-9]", "");
                        int index = Integer.parseInt(name);
                        selectedShares.put(index, ShamirStringUtil.loadShareFromFile(f));
                    }

                    byte[] originalBytes = ShamirStringUtil.reconstructEncryptedData(selectedShares, password);

                    JFileChooser saveChooser = new JFileChooser();
                    saveChooser.setDialogTitle("Save reconstructed image");
                    if (saveChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                    File outputFile = saveChooser.getSelectedFile();
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        fos.write(originalBytes);
                    }

                    JOptionPane.showMessageDialog(frame, "Image successfully reconstructed!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Reconstruction failed: " + ex.getMessage());
                }
            }
        });

        panel.add(btnSignature);
        panel.add(btnShamir);
        frame.add(panel);
        frame.setVisible(true);
    }
}


