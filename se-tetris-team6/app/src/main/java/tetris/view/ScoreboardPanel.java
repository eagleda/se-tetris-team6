package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JList;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.score.Score;
import tetris.view.ScoreboardComponent.StandardModePanel;
import tetris.view.ScoreboardComponent.ItemModePanel;

public class ScoreboardPanel extends JPanel implements ScoreView {

    private final DefaultListModel<String> standardModel = new DefaultListModel<>();
    private final DefaultListModel<String> itemModel = new DefaultListModel<>();
    private final JList<String> standardList = new JList<>(standardModel);
    private final JList<String> itemList = new JList<>(itemModel);
    private final JButton backButton = new JButton("Back to Menu");
    private final JButton resetButton = new JButton("Reset Scores");
    private int standardHighlight = -1;
    private int itemHighlight = -1;

    public ScoreboardPanel() {
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

        center.add(new StandardModePanel(standardList), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(20, 0, 20, 0);
        center.add(createSeparatorPanel(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(12, 12, 12, 12);
        center.add(new ItemModePanel(itemList), gbc);

        add(center, BorderLayout.CENTER);

        // 커스텀 하이라이트 렌더러 및 선택 방지 설정
        installNoSelection(standardList);
        installNoSelection(itemList);
        installHighlightRenderer(standardList, false);
        installHighlightRenderer(itemList, true);

    }

    private void installHighlightRenderer(JList<String> list, boolean isItemList) {
        list.setOpaque(true);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> jList, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(jList, value, index, false, false);
                int highlight = isItemList ? itemHighlight : standardHighlight;
                boolean isHighlight = index == highlight;
                if (isHighlight) {
                    c.setBackground(new Color(220, 0, 0, 160));
                } else {
                    c.setBackground(list.getBackground());
                }
                c.setForeground(Color.WHITE);
                if (c instanceof javax.swing.JComponent jc) {
                    jc.setOpaque(true);
                }
                return c;
            }
        });
    }

    private void installNoSelection(JList<String> list) {
        list.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                // no-op
            }
        });
    }

    private JPanel createHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        left.setOpaque(false);
        styleBackButton(backButton);
        left.add(backButton);
        wrapper.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        right.setOpaque(false);
        styleResetButton(resetButton);
        right.add(resetButton);
        wrapper.add(right, BorderLayout.EAST);
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

    private void styleResetButton(JButton button) {
        button.setText("Reset Scores");
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(120, 0, 0));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    public void setBackAction(ActionListener listener) {
        for (ActionListener l : backButton.getActionListeners()) {
            backButton.removeActionListener(l);
        }
        backButton.addActionListener(listener);
    }

    public void setResetAction(ActionListener listener) {
        for (ActionListener l : resetButton.getActionListeners()) {
            resetButton.removeActionListener(l);
        }
        resetButton.addActionListener(listener);
    }

    @Override
    public void renderScore(Score score) {
        // unused
    }

    public void renderLeaderboard(GameMode mode, List<LeaderboardEntry> entries) {
        renderLeaderboard(mode, entries, -1);
    }

    public void renderLeaderboard(GameMode mode, List<LeaderboardEntry> entries, int highlightIndex) {
        DefaultListModel<String> target = mode == GameMode.ITEM ? itemModel : standardModel;
        target.clear();
        if (mode == GameMode.ITEM) {
            itemHighlight = highlightIndex;
        } else {
            standardHighlight = highlightIndex;
        }
        System.out.printf("[UI] ScoreboardPanel.render mode=%s size=%d highlight=%d%n",
                mode, entries == null ? 0 : entries.size(), highlightIndex);
        if (entries == null || entries.isEmpty()) {
            target.addElement("No entries yet.");
            selectHighlight(mode);
            return;
        }
        int index = 1;
        for (LeaderboardEntry entry : entries) {
            target.addElement(String.format("%2d. %s — %d", index++, entry.getName(), entry.getPoints()));
        }
        selectHighlight(mode);
    }

    private void selectHighlight(GameMode mode) {
        if (mode == GameMode.ITEM) {
            if (itemHighlight >= 0 && itemHighlight < itemModel.size()) {
                itemList.ensureIndexIsVisible(itemHighlight);
            }
        } else {
            if (standardHighlight >= 0 && standardHighlight < standardModel.size()) {
                standardList.ensureIndexIsVisible(standardHighlight);
            }
        }
        itemList.repaint();
        standardList.repaint();
    }
}
