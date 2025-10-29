package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JList;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.score.Score;

public class ScoreboardPanel extends JPanel implements ScoreView {

    private final DefaultListModel<String> standardModel = new DefaultListModel<>();
    private final DefaultListModel<String> itemModel = new DefaultListModel<>();
    private final JList<String> standardList = new JList<>(standardModel);
    private final JList<String> itemList = new JList<>(itemModel);
    private final JButton backButton = new JButton("Back to Menu");

    public ScoreboardPanel() {
        setSize(TetrisFrame.FRAME_SIZE);
        setBackground(Color.darkGray);
        setOpaque(true);
        setVisible(false);
        setLayout(new BorderLayout(12, 12));

        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;

        center.add(createListPanel("STANDARD", standardList), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(20, 0, 20, 0);
        center.add(createSeparatorPanel(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(12, 12, 12, 12);
        center.add(createListPanel("ITEM", itemList), gbc);

        add(center, BorderLayout.CENTER);

    }

    private JPanel createListPanel(String title, JList<String> list) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(label, BorderLayout.NORTH);

        list.setOpaque(false);
        list.setBackground(new Color(0, 0, 0, 0));
        list.setForeground(Color.WHITE);
        list.setFont(new Font("SansSerif", Font.PLAIN, 16));
        list.setFocusable(false);

        list.setSelectionBackground(new Color(255, 255, 255, 50));
        list.setSelectionForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(list);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        left.setOpaque(false);
        styleBackButton(backButton);
        left.add(backButton);
        wrapper.add(left, BorderLayout.WEST);
        return wrapper;
    }

    private JPanel createSeparatorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setForeground(Color.LIGHT_GRAY);
        panel.add(separator, BorderLayout.CENTER);
        return panel;
    }

    private void styleBackButton(JButton button) {
        button.setText("Back to Menu");
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(64, 64, 64));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    public void setBackAction(ActionListener listener) {
        for (ActionListener l : backButton.getActionListeners()) {
            backButton.removeActionListener(l);
        }
        backButton.addActionListener(listener);
    }

    @Override
    public void renderScore(Score score) {
        // unused
    }

    public void renderLeaderboard(GameMode mode, List<LeaderboardEntry> entries) {
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
