package tetris.view.GameComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tetris.domain.score.Score;

/** Simple Game Over overlay panel. Controller should update via show(...) */
public class GameOverPanel extends JPanel {

    private final JLabel title = new JLabel("Game Over");
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JTextField nameField = new JTextField(12);
    private final JButton saveButton = new JButton("Save");
    private final JButton skipButton = new JButton("Skip");
    private final JButton backToMenu = new JButton("Back to Menu");

    public interface Listener {
        void onSave(String name);
        void onSkip();
        void onBackToMenu();
    }

    private Listener listener;

    public GameOverPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(0, 0, 0, 200));
        this.setOpaque(true);

        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridy = 0; center.add(title, gbc);
        gbc.gridy = 1; center.add(scoreLabel, gbc);
        gbc.gridy = 2; center.add(nameField, gbc);
        gbc.gridy = 3; center.add(saveButton, gbc);
        gbc.gridy = 4; center.add(skipButton, gbc);
        gbc.gridy = 5; center.add(backToMenu, gbc);

        saveButton.addActionListener((ActionEvent e) -> {
            if (listener != null) listener.onSave(nameField.getText().trim());
        });

        skipButton.addActionListener((ActionEvent e) -> {
            if (listener != null) listener.onSkip();
        });

        backToMenu.addActionListener((ActionEvent e) -> {
            if (listener != null) listener.onBackToMenu();
        });

        this.add(center, BorderLayout.CENTER);
        this.setVisible(false);
    }

    public void setListener(Listener l) { this.listener = l; }

    public void show(Score score, boolean allowNameEntry) {
        Objects.requireNonNull(score, "score");
        scoreLabel.setText(String.format("Score: %d | Lines: %d | Level: %d",
            score.getPoints(), score.getClearedLines(), score.getLevel()));
        nameField.setVisible(allowNameEntry);
        saveButton.setVisible(allowNameEntry);
        skipButton.setVisible(allowNameEntry);
        this.setVisible(true);
        this.requestFocusInWindow();
    }

    public void hidePanel() {
        this.setVisible(false);
    }
}
