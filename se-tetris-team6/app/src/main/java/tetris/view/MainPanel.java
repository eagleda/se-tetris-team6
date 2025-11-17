package tetris.view;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainPanel extends JPanel {
    private final static Color NORMAL_COLOR = Color.white;
    private final static Color HIGHLIGHT_COLOR = Color.gray;

    private List<JButton> buttons;
    private int currentFocusIndex = 0;

    public MainPanel() {
        this.setBackground(Color.black);
        this.setOpaque(true);
        this.setVisible(false);
        this.setLayout(new GridBagLayout());

        // GridBagLayout 설정
        GridBagConstraints gbc = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                weightx = 1.0;
                weighty = 1.0;
                anchor = GridBagConstraints.CENTER;
                fill = GridBagConstraints.BOTH;
                insets = new Insets(10, 200, 10, 200);
            }
        };

        // 게임 타이틀
        JLabel titleText = new JLabel("TETRIS", SwingConstants.CENTER);
        titleText.setFont(new Font("SansSerif", Font.BOLD, 48));
        titleText.setForeground(Color.BLUE);
        titleText.setHorizontalAlignment(SwingConstants.CENTER);

        // 싱글 플레이 버튼 설정
        JButton singlePlayButton = new JButton("Single Play");
        attachSinglePlayDialog(singlePlayButton);
        // 멀티 플레이 버튼 설정
        JButton multiPlayButton = new JButton("Multi Play");
        attachMultiPlayDialog(multiPlayButton);
        // 설정 버튼 설정
        JButton settingButton = new JButton("Setting");
        settingButton.addActionListener(e -> {
            onSettingClicked();
        });
        // 스코어보드 버튼 설정
        JButton scoreboardButton = new JButton("Scoreboard");
        scoreboardButton.addActionListener(e -> {
            onScoreboardClicked();
        });
        // 종료 버튼 설정
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            onExitClicked();
        });

        // Add button to buttons
        buttons = new ArrayList<>();
        buttons.add(singlePlayButton);
        buttons.add(multiPlayButton);
        buttons.add(settingButton);
        buttons.add(scoreboardButton);
        buttons.add(exitButton);

        // Add Components to GridBagLayout
        addComponentVertical(new EmptySpace(), gbc);
        addComponentVertical(titleText, gbc);
        for (JButton button : buttons) {
            addComponentVertical(button, gbc);
        }
        addComponentVertical(new EmptySpace(), gbc);
    }

    private void addComponentVertical(Component component, GridBagConstraints gbc) {
        if (component instanceof JButton button) {
            button.setFont(new Font("SansSerif", Font.BOLD, 18));
            Dimension fixed = new Dimension(240, 44);
            button.setPreferredSize(fixed);
            button.setMaximumSize(fixed);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(button);
            this.add(wrapper, gbc);
        } else {
            this.add(component, gbc);
        }
        gbc.gridy++;
    }

    /**
     * Attach a simple modal dialog to the Single Play menu button. The dialog
     * contains a single Mode radio group (Normal / Item) and a Confirm button.
     * When Confirm is pressed this method calls `onSinglePlayConfirmed(mode)`.
     */
    private void attachSinglePlayDialog(JButton trigger) {
        if (trigger == null)
            return;

        trigger.addActionListener(e -> {
            java.awt.Window win = SwingUtilities.getWindowAncestor(this);
            final JDialog dlg = new JDialog(win, ModalityType.APPLICATION_MODAL);
            dlg.setTitle("Single Play Options");

            JPanel root = new JPanel();
            root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

            JPanel modeRow = new JPanel();
            modeRow.setLayout(new BoxLayout(modeRow, BoxLayout.X_AXIS));
            modeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JRadioButton modeNormal = new JRadioButton("Normal");
            JRadioButton modeItem = new JRadioButton("Item");
            ButtonGroup modeGroup = new ButtonGroup();
            modeGroup.add(modeNormal);
            modeGroup.add(modeItem);
            modeNormal.setSelected(true);
            modeRow.add(modeNormal);
            modeRow.add(Box.createHorizontalStrut(8));
            modeRow.add(modeItem);
            root.add(modeRow);
            root.add(Box.createVerticalStrut(14));

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            JButton confirm = new JButton("Confirm");
            confirm.addActionListener(ae -> {
                String mode = modeNormal.isSelected() ? "NORMAL" : "ITEM";
                onSinglePlayConfirmed(mode);
                dlg.dispose();
            });
            btnRow.add(confirm);
            root.add(btnRow);

            dlg.getContentPane().add(root);
            dlg.pack();
            dlg.setResizable(false);
            dlg.setLocationRelativeTo(win);
            dlg.setVisible(true);
        });
    }

    /**
     * Override-only hook invoked when a Single Play option is chosen.
     *
     * Subclasses (or anonymous subclasses) should override this method to
     * implement behavior for the selected single-player mode. This base
     * implementation intentionally does nothing.
     *
     * @param mode one of "NORMAL", "ITEM"
     */
    protected void onSinglePlayConfirmed(String mode) {
        // no-op: override in subclass to handle single-play selection
    }

    /**
     * Attach a specialized dialog to the Multi Play menu button. The dialog
     * contains three horizontal radio groups arranged vertically:
     * 1) Mode: Normal / Item / Time Limit
     * 2) Scope: Local / Online
     * 3) Role: Server / Client (only visible when Online is selected)
     *
     * A final Confirm button collects the selections and calls
     * `onMultiPlayConfirmed(mode, isOnline, isServer)` which is intentionally
     * left empty so higher-level code can decide what to do.
     */
    private void attachMultiPlayDialog(JButton trigger) {
        if (trigger == null)
            return;

        trigger.addActionListener(e -> {
            java.awt.Window win = SwingUtilities.getWindowAncestor(this);
            final JDialog dlg = new JDialog(win, ModalityType.APPLICATION_MODAL);
            dlg.setTitle("Multi Play Options");

            JPanel root = new JPanel();
            root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

            // 1) Mode selection
            JPanel modeRow = new JPanel();
            modeRow.setLayout(new BoxLayout(modeRow, BoxLayout.X_AXIS));
            // align mode row to the left to match scope/role alignment
            modeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JRadioButton modeNormal = new JRadioButton("Normal");
            JRadioButton modeItem = new JRadioButton("Item");
            JRadioButton modeTime = new JRadioButton("Time Limit");
            ButtonGroup modeGroup = new ButtonGroup();
            modeGroup.add(modeNormal);
            modeGroup.add(modeItem);
            modeGroup.add(modeTime);
            modeNormal.setSelected(true);
            modeRow.add(modeNormal);
            modeRow.add(Box.createHorizontalStrut(8));
            modeRow.add(modeItem);
            modeRow.add(Box.createHorizontalStrut(8));
            modeRow.add(modeTime);
            root.add(modeRow);
            root.add(Box.createVerticalStrut(10));

            // 2) Scope selection (Local / Online)
            JPanel scopeRow = new JPanel();
            scopeRow.setLayout(new BoxLayout(scopeRow, BoxLayout.X_AXIS));
            // align the scope row to the left within the vertical BoxLayout
            scopeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JRadioButton scopeLocal = new JRadioButton("Local");
            JRadioButton scopeOnline = new JRadioButton("Online");
            ButtonGroup scopeGroup = new ButtonGroup();
            scopeGroup.add(scopeLocal);
            scopeGroup.add(scopeOnline);
            scopeLocal.setSelected(true);
            scopeRow.add(scopeLocal);
            scopeRow.add(Box.createHorizontalStrut(8));
            scopeRow.add(scopeOnline);
            root.add(scopeRow);
            root.add(Box.createVerticalStrut(10));

            // 3) Role selection (Server / Client) - hidden unless Online selected
            JPanel roleRow = new JPanel();
            roleRow.setLayout(new BoxLayout(roleRow, BoxLayout.X_AXIS));
            JRadioButton roleServer = new JRadioButton("Server");
            JRadioButton roleClient = new JRadioButton("Client");
            ButtonGroup roleGroup = new ButtonGroup();
            roleGroup.add(roleServer);
            roleGroup.add(roleClient);
            roleServer.setSelected(true);
            roleRow.add(roleServer);
            roleRow.add(Box.createHorizontalStrut(8));
            roleRow.add(roleClient);

            // Wrap roleRow into a fixed-size container so the dialog reserves
            // the same vertical space even when roleRow's contents are hidden.
            JPanel roleContainer = new JPanel(new BorderLayout());
            // align container to the left so its reserved space lines up
            roleContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
            // set preferred size from the roleRow's preferred height so the
            // container reserves that vertical space when the radios are hidden
            roleContainer.add(roleRow, BorderLayout.CENTER);
            // ensure the roleRow itself is left-aligned within the container
            roleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            // capture preferred size while the roleRow is still laid out
            java.awt.Dimension rolePref = roleRow.getPreferredSize();
            roleContainer.setPreferredSize(rolePref);
            // hide the actual radio buttons initially but keep the container
            // so the reserved space remains
            roleRow.setVisible(false);
            root.add(roleContainer);
            root.add(Box.createVerticalStrut(14));

            // Show/hide roleRow depending on scope selection. We toggle the
            // roleRow visibility but keep roleContainer present so space is
            // preserved.
            scopeOnline.addActionListener(ae -> roleRow.setVisible(true));
            scopeLocal.addActionListener(ae -> roleRow.setVisible(false));

            // Confirm button
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            JButton confirm = new JButton("Confirm");
            confirm.addActionListener(ae -> {
                String mode = modeNormal.isSelected() ? "NORMAL"
                        : modeItem.isSelected() ? "ITEM" : "TIME_LIMIT";
                boolean isOnline = scopeOnline.isSelected();
                boolean isServer = roleServer.isSelected();
                // Delegate to handler method (intentionally empty)
                onMultiPlayConfirmed(mode, isOnline, isServer);
                dlg.dispose();
            });
            btnRow.add(confirm);
            root.add(btnRow);

            dlg.getContentPane().add(root);
            dlg.pack();
            dlg.setResizable(false);
            dlg.setLocationRelativeTo(win);
            dlg.setVisible(true);
        });
    }

    /**
     * Override-only hook invoked when the Multi Play dialog Confirm button
     * is pressed.
     * <p>
     * Subclasses (or anonymous subclasses) should override this method to
     * implement behavior for the current selection. This base implementation
     * intentionally does nothing.
     *
     * @param mode     one of "NORMAL", "ITEM", "TIME_LIMIT"
     * @param isOnline true if Online was chosen (otherwise Local)
     * @param isServer true if Server role is chosen (meaningful only when
     *                 isOnline==true)
     */
    protected void onMultiPlayConfirmed(String mode, boolean isOnline, boolean isServer) {
        // no-op: override in subclass to handle selection
    }

    protected void onSettingClicked() {
        // no-op: override in subclass to handle selection
    }

    protected void onScoreboardClicked() {
        // no-op: override in subclass to handle selection
    }

    protected void onExitClicked() {
        // no-op: override in subclass to handle selection
    }

    public void focusButton(int direction) {
        buttons.get(currentFocusIndex).setBackground(NORMAL_COLOR);
        currentFocusIndex = (currentFocusIndex + direction + buttons.size()) % buttons.size();
        buttons.get(currentFocusIndex).setBackground(HIGHLIGHT_COLOR);
    }

    public void clickFocusButton() {
        buttons.get(currentFocusIndex).doClick();
    }
}
