package tetris.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainPanel extends JPanel {
    public JButton singleNormalButton;
    public JButton singleItemButton;

    public JButton localMultiNormalButton;
    public JButton localMultiItemButton;
    public JButton localMultiTimeLimitButton;

    public JButton onlineMultiNormalButton;
    public JButton onlineMultiItemButton;
    public JButton onlineMultiTimeLimitButton;

    public JButton settingButton;
    public JButton scoreboardButton;
    public JButton exitButton;

    public Color NORMAL_COLOR = Color.white;
    public Color HIGHLIGHT_COLOR = Color.gray;
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
        JButton singlePlayButton = getMenuButton("Single Play");
        singleNormalButton = getOptionButton("Normal Mode");
        singleItemButton = getOptionButton("Item Mode");
        attachPopupToTrigger(singlePlayButton, "Single Play Options",
                Arrays.asList(singleNormalButton, singleItemButton));

        // 멀티 플레이 버튼 설정
        JButton multiPlayButton = getMenuButton("Multi Play");
        JButton localMultiButton = getOptionButton("Local");

        localMultiNormalButton = getOptionButton("Normal Mode");
        localMultiItemButton = getOptionButton("Item Mode");
        localMultiTimeLimitButton = getOptionButton("Time Limit Mode");
        attachPopupToTrigger(localMultiButton,
                "Local Multi Options",
                Arrays.asList(localMultiNormalButton, localMultiItemButton, localMultiTimeLimitButton));
        JButton onlineMultiButton = getOptionButton("Online");
        onlineMultiNormalButton = getOptionButton("Normal Mode");
        onlineMultiItemButton = getOptionButton("Item Mode");
        onlineMultiTimeLimitButton = getOptionButton("Time Limit Mode");
        attachPopupToTrigger(onlineMultiButton,
                "Online Multi Options",
                Arrays.asList(onlineMultiNormalButton, onlineMultiItemButton, onlineMultiTimeLimitButton));
        attachPopupToTrigger(multiPlayButton, "Multi Play Options", Arrays.asList(localMultiButton, onlineMultiButton));

        settingButton = getMenuButton("Setting");
        scoreboardButton = getMenuButton("Scoreboard");
        exitButton = getMenuButton("Exit");

        // Add Components to GridBagLayout
        for (int i = 0; i < 2; i++) {
            addComponentVertical(new EmptySpace(), gbc);
        }
        addComponentVertical(titleText, gbc);
        addComponentVertical(singlePlayButton, gbc);
        addComponentVertical(multiPlayButton, gbc);
        addComponentVertical(settingButton, gbc);
        addComponentVertical(scoreboardButton, gbc);
        addComponentVertical(exitButton, gbc);
        for (int i = 0; i < 8; i++) {
            addComponentVertical(new EmptySpace(), gbc);
        }

        // Add button to buttons
        buttons = new ArrayList<>();
        buttons.add(singlePlayButton);
        buttons.add(multiPlayButton);
        buttons.add(settingButton);
        buttons.add(scoreboardButton);
        buttons.add(exitButton);
    }

    private void addComponentVertical(Component component, GridBagConstraints gbc) {
        // If the component is a JButton, wrap it in a center-aligned panel and
        // fix its preferred size so all menu buttons keep the same horizontal
        // size regardless of the parent width.
        if (component instanceof JButton button) {
            // choose a fixed size for menu buttons
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

    private JButton getMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setPreferredSize(new Dimension(240, 44));
        // button.setMaximumSize(BUTTON_SIZE);
        return button;
    }

    private JButton getOptionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(160, 36));
        return button;
    }

    /**
     * Attach a popup to a trigger button. When the trigger is clicked a small
     * dialog
     * will appear showing the given option buttons (one per line). Clicking any
     * option will invoke the original option's action (via doClick()) and close
     * the popup.
     *
     * Notes:
     * - The popup creates new JButton instances copied from the provided options
     * to avoid adding listeners to the original buttons repeatedly.
     * - The popup is non-modal and positioned just below the trigger button.
     *
     * @param trigger the button that opens the popup when clicked
     * @param options list of buttons whose labels and fonts will be used for popup
     */
    public void attachPopupToTrigger(JButton trigger, String title, List<JButton> options) {
        if (trigger == null || options == null || options.isEmpty())
            return;

        trigger.addActionListener(e -> {
            java.awt.Window win = SwingUtilities.getWindowAncestor(this);
            final JDialog dlg = new JDialog(win, ModalityType.MODELESS);
            dlg.setTitle(title);

            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            // arrange option buttons horizontally (side-by-side)
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);

            for (JButton opt : options) {
                // create a lightweight copy so we don't mutate the caller's button
                JButton copy = new JButton(opt.getText());
                copy.setFont(opt.getFont());
                // center vertically within horizontal layout
                copy.setAlignmentY(Component.CENTER_ALIGNMENT);
                copy.addActionListener(ae -> {
                    // forward the click to the original button so existing listeners run
                    opt.doClick();
                    dlg.dispose();
                });
                panel.add(copy);
                // horizontal spacing between option buttons
                panel.add(Box.createHorizontalStrut(6));
            }

            dlg.getContentPane().add(panel);
            dlg.pack();
            dlg.setResizable(false);

            try {
                // center the dialog inside the parent window when possible
                if (win != null) {
                    Rectangle wb = win.getBounds();
                    int x = wb.x + (wb.width - dlg.getWidth()) / 2;
                    int y = wb.y + (wb.height - dlg.getHeight()) / 2;
                    dlg.setLocation(x, y);
                } else {
                    // fallback to positioning below the trigger button
                    Point p = trigger.getLocationOnScreen();
                    dlg.setLocation(p.x, p.y + trigger.getHeight());
                }
            } catch (IllegalComponentStateException ex) {
                dlg.setLocationRelativeTo(trigger);
            }

            dlg.setVisible(true);
        });
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
