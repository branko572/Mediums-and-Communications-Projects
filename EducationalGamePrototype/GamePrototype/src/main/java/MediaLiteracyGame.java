package main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MediaLiteracyGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameWindow();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Грешка при стартување на играта: " + e.getMessage(),
                        "Грешка", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}

class GameWindow extends JFrame {
    private JLabel imageLabel;
    private JButton safeButton, unsafeButton;
    private int score = 0;
    private JLabel scoreLabel;

    private List<ImageItem> allImages = new ArrayList<>();
    private Iterator<ImageItem> imageIterator;
    private ImageItem currentImage;

    public GameWindow() {
        setupWindow();
        loadImageLists();
        if (allImages.isEmpty()) {
            showError("Недостасуваат слики! Осигурајте се дека има слики во папките safeimages и unsafeimages.");
            System.exit(1);
        }
        setupUI();
        loadNextImage();
        setVisible(true);
    }

    private void setupWindow() {
        setTitle("Медиумска писменост - Безбеден интернет");
        setSize(1200, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void loadImageLists() {
        allImages.addAll(loadImagesFromFolder("safeimages/", true));
        allImages.addAll(loadImagesFromFolder("unsafeimages/", false));
        Collections.shuffle(allImages); // randomize the order
        imageIterator = allImages.iterator();
    }

    private List<ImageItem> loadImagesFromFolder(String folder, boolean isSafe) {
        List<ImageItem> images = new ArrayList<>();
        try {
            URL url = getClass().getClassLoader().getResource(folder);
            if (url == null) {
                showError("Папката не е пронајдена: " + folder);
                return images;
            }

            File dir = new File(url.toURI());
            File[] files = dir.listFiles((d, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
            if (files != null) {
                for (File f : files) {
                    images.add(new ImageItem(folder + f.getName(), isSafe));
                }
            }
        } catch (URISyntaxException e) {
            showError("Невалиден пат до папката: " + folder);
        } catch (Exception e) {
            showError("Грешка при вчитување слики од " + folder + ": " + e.getMessage());
        }
        return images;
    }

    private void setupUI() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(100, 150, 255));
        JLabel titleLabel = new JLabel("Помогни му на херојот да ги спречи сајбер-насилниците!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Score
        scoreLabel = new JLabel("Поени: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(scoreLabel, BorderLayout.PAGE_START);

        // Image area
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        add(imageLabel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        safeButton = createStyledButton("Безбедно", new Color(100, 200, 100));
        unsafeButton = createStyledButton("Небезбедно", new Color(200, 100, 100));

        safeButton.addActionListener(new AnswerListener(true));
        unsafeButton.addActionListener(new AnswerListener(false));

        buttonPanel.add(safeButton);
        buttonPanel.add(unsafeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        return button;
    }

    private void loadNextImage() {
        if (!imageIterator.hasNext()) {
            showFinalScore();
            return;
        }

        currentImage = imageIterator.next();

        try {
            URL imgURL = getClass().getClassLoader().getResource(currentImage.path);
            if (imgURL == null) {
                throw new RuntimeException("Недостасува слика: " + currentImage.path);
            }

            ImageIcon icon = new ImageIcon(imgURL);
            Image scaledImage = icon.getImage().getScaledInstance(1000, 800, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setText(""); // clear any previous error text
        } catch (Exception e) {
            showError("Грешка при вчитување на слика: " + e.getMessage());
            imageLabel.setIcon(null);
            imageLabel.setText("Сликата не може да се вчита");
        }
    }

    private void showError(String message) {
        System.err.println(message);
        JOptionPane.showMessageDialog(this, message, "Грешка", JOptionPane.ERROR_MESSAGE);
    }

    private void showFinalScore() {
        JOptionPane.showMessageDialog(this,
                "Играта заврши! Вкупен број на поени: " + score,
                "Крај на играта",
                JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private class AnswerListener implements ActionListener {
        private final boolean userChoiceIsSafe;

        public AnswerListener(boolean userChoiceIsSafe) {
            this.userChoiceIsSafe = userChoiceIsSafe;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (userChoiceIsSafe == currentImage.isSafe) {
                score++;
                scoreLabel.setText("Поени: " + score);
                showMessage("Точен одговор! Вкупно поени: " + score, "Браво!", false);
            } else {
                showMessage("Неточен одговор! Ова " + (currentImage.isSafe ? "е" : "не е") + " безбедна содржина.",
                        "Грешка", true);
            }
            loadNextImage();
        }

        private void showMessage(String message, String title, boolean isError) {
            JOptionPane.showMessageDialog(GameWindow.this, message, title,
                    isError ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static class ImageItem {
        String path;
        boolean isSafe;

        public ImageItem(String path, boolean isSafe) {
            this.path = path;
            this.isSafe = isSafe;
        }
    }
}
