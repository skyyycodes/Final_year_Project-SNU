package com.snu.project;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;

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
        JButton btnWatermark = new JButton("Watermark Utility");

        // Signature placeholder
        btnSignature.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Feature 1 already implemented.");
        });

        // Watermark Utility Feature
        btnWatermark.addActionListener(e -> {
            String[] options = {"Add Watermark", "Fetch Watermark"};
            int choice = JOptionPane.showOptionDialog(
                    frame, "Choose an action:", "Watermark Options",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) {
                // Add Watermark
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select Image");
                if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                File imageFile = chooser.getSelectedFile();

                String userText = JOptionPane.showInputDialog(frame, "Enter watermark text:");
                if (userText == null || userText.trim().isEmpty()) return;

                String timeStamp = LocalDateTime.now().toString();
                String finalText = userText + " | " + timeStamp;

                JPasswordField passwordField = new JPasswordField();
                if (JOptionPane.showConfirmDialog(frame, passwordField, "Enter password for encryption", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
                    return;
                String password = new String(passwordField.getPassword());

                try {
                    BufferedImage img = LSBSteganography.loadImage(imageFile.getAbsolutePath());
                    String encrypted = DESUtil.encrypt(finalText, password);
                    BufferedImage watermarked = LSBSteganography.embedText(encrypted, img);

                    JFileChooser saveChooser = new JFileChooser();
                    saveChooser.setDialogTitle("Save watermarked image");
                    if (saveChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;

                    File saveFile = saveChooser.getSelectedFile();
                    String filePath = saveFile.getAbsolutePath();

                    // Ensure it ends with .png
                    if (!filePath.toLowerCase().endsWith(".png")) {
                        filePath += ".png";
                    }

                    LSBSteganography.saveImage(watermarked, filePath);

                    JOptionPane.showMessageDialog(frame, "Watermark embedded successfully!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }

            } else if (choice == 1) {
                // Fetch Watermark
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select Watermarked Image");
                if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                File imageFile = chooser.getSelectedFile();

                JPasswordField passwordField = new JPasswordField();
                if (JOptionPane.showConfirmDialog(frame, passwordField, "Enter password to decrypt", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
                    return;
                String password = new String(passwordField.getPassword());

                try {
                    BufferedImage img = LSBSteganography.loadImage(imageFile.getAbsolutePath());
                    String extracted = LSBSteganography.extractText(img);
                    String decrypted = DESUtil.decrypt(extracted, password);
                    JOptionPane.showMessageDialog(frame, "Watermark: " + decrypted);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Failed to extract watermark: " + ex.getMessage());
                }
            }
        });

        panel.add(btnSignature);
        panel.add(btnWatermark);

        frame.add(panel);
        frame.setVisible(true);
    }
}
