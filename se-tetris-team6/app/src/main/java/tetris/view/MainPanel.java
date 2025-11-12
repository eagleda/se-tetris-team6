package tetris.view;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.util.Arrays;

import javax.swing.*;

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

        settingButton = new JButton() {
            {
                setText("Setting");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        scoreboardButton = new JButton() {
            {
                setText("Scoreboard");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        exitButton = new JButton() {
            {
                setText("Exit");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };

        // 싱글 플레이 버튼 설정
        JButton singlePlayButton = new JButton() {
            {
                setText("Single Play");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        singleNormalButton = new JButton() {
            {
                setText("Normal Mode");
                setFont(new Font("SansSerif", Font.BOLD, 14));
            }
        };
        singleItemButton = new JButton() {
            {
                setText("Item Mode");
                setFont(new Font("SansSerif", Font.BOLD, 14));
            }
        };
        attachPopupToTrigger(singlePlayButton, Arrays.asList(singleNormalButton, singleItemButton));

        // 멀티 플레이 버튼 설정
        JButton multiPlayButton = new JButton() {
            {
                setText("Multi Play");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        JButton localMultiButton = new JButton() {
            {
                setText("Local");
                setFont(new Font("SansSerif", Font.BOLD, 14));
            }
        };
        localMultiNormalButton = new JButton("Normal Mode");
        localMultiNormalButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        localMultiItemButton = new JButton("Item Mode");
        localMultiItemButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        localMultiTimeLimitButton = new JButton("Time Limit Mode");
        localMultiTimeLimitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        attachPopupToTrigger(localMultiButton,
                Arrays.asList(localMultiNormalButton, localMultiItemButton, localMultiTimeLimitButton));
        JButton onlineMultiButton = new JButton() {
            {
                setText("Online");
                setFont(new Font("SansSerif", Font.BOLD, 14));
            }
        };
        onlineMultiNormalButton = new JButton("Normal Mode");
        onlineMultiNormalButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        onlineMultiItemButton = new JButton("Item Mode");
        onlineMultiItemButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        onlineMultiTimeLimitButton = new JButton("Time Limit Mode");
        onlineMultiTimeLimitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        attachPopupToTrigger(onlineMultiButton,
                Arrays.asList(onlineMultiNormalButton, onlineMultiItemButton, onlineMultiTimeLimitButton));
        attachPopupToTrigger(multiPlayButton, Arrays.asList(localMultiButton, onlineMultiButton));

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
        this.add(component, gbc);
        gbc.gridy++;
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
    public void attachPopupToTrigger(JButton trigger, List<JButton> options) {
        if (trigger == null || options == null || options.isEmpty())
            return;

        trigger.addActionListener(e -> {
            java.awt.Window win = SwingUtilities.getWindowAncestor(this);
            final JDialog dlg = new JDialog(win, ModalityType.MODELESS);

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
