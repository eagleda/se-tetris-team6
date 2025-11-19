package tetris.view.GameComponent;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JList;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.score.Score;
import tetris.view.ScoreboardComponent.StandardModePanel;
import tetris.view.ScoreboardComponent.ItemModePanel;

/** Game Over overlay panel showing leaderboard (left) and input (right). */
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

    // Leaderboard lists for left side
    private final DefaultListModel<String> standardModel = new DefaultListModel<>();
    private final DefaultListModel<String> itemModel = new DefaultListModel<>();
    private final JList<String> standardList = new JList<>(standardModel);
    private final JList<String> itemList = new JList<>(itemModel);
    private final JPanel leftCardPanel = new JPanel(new CardLayout());

    public GameOverPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(0, 0, 0, 200));
        this.setOpaque(true);

        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);

        // left scoreboard area as CardLayout (STANDARD / ITEM)
        StandardModePanel standardPanel = new StandardModePanel(standardList);
        ItemModePanel itemPanel = new ItemModePanel(itemList);
        leftCardPanel.add(standardPanel, "STANDARD");
        leftCardPanel.add(itemPanel, "ITEM");
        leftCardPanel.setOpaque(false);
        // Outer empty border provides margin outside the white line border,
        // inner empty border provides padding inside the white border.
        leftCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(8, 8, 8, 8),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            )
        ));
        leftCardPanel.setPreferredSize(new Dimension(260, 320));

        // right input area (existing controls)
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridy = 0; right.add(title, gbc);
        gbc.gridy = 1; right.add(scoreLabel, gbc);
        gbc.gridy = 2; right.add(nameField, gbc);
        gbc.gridy = 3; right.add(saveButton, gbc);
        gbc.gridy = 4; right.add(skipButton, gbc);
        gbc.gridy = 5; right.add(backToMenu, gbc);

        saveButton.addActionListener((ActionEvent e) -> {
            if (listener != null) listener.onSave(nameField.getText().trim());
        });

        skipButton.addActionListener((ActionEvent e) -> {
            if (listener != null) listener.onSkip();
        });

        backToMenu.addActionListener((ActionEvent e) -> {
            if (listener != null) listener.onBackToMenu();
        });

        // add left and right to main panel
        this.add(leftCardPanel, BorderLayout.WEST);
        this.add(right, BorderLayout.CENTER);

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

    public void renderLeaderboard(GameMode mode, List<LeaderboardEntry> entries) {
        DefaultListModel<String> target = mode == GameMode.ITEM ? itemModel : standardModel;
        target.clear();
        if (entries == null || entries.isEmpty()) {
            target.addElement("No entries yet.");
        } else {
            int index = 1;
            for (LeaderboardEntry entry : entries) {
                target.addElement(String.format("%2d. %s — %d", index++, entry.getName(), entry.getPoints()));
            }
        }
        CardLayout cl = (CardLayout) leftCardPanel.getLayout();
        cl.show(leftCardPanel, mode == GameMode.ITEM ? "ITEM" : "STANDARD");
    }

    /** Update only the underlying list model for the given mode without changing visible cards. */
    public void updateLeaderboardModel(GameMode mode, List<LeaderboardEntry> entries) {
        DefaultListModel<String> target = mode == GameMode.ITEM ? itemModel : standardModel;
        target.clear();
        if (entries == null || entries.isEmpty()) {
            target.addElement("No entries yet.");
            return;
        }
        int index = 1;
        for (LeaderboardEntry entry : entries) {
            target.addElement(String.format("%2d. %s — %d", index++, entry.getName(), entry.getPoints()));
        }
    }

}
