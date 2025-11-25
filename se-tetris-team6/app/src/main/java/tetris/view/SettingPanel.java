package tetris.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tetris.domain.setting.Setting;
import tetris.domain.GameDifficulty;

public class SettingPanel extends JPanel {
    // Exposed UI components for controller wiring
    public JComboBox<Setting.ScreenSize> sizeCombo;
    public JCheckBox colorBlindCheckbox;

    // Single player key fields
    public JTextField keyMoveLeftField;
    public JTextField keyMoveRightField;
    public JTextField keyRotateField;
    public JTextField keySoftDropField;
    public JTextField keyHardDropField;

    public JButton captureMoveLeftButton;
    public JButton captureMoveRightButton;
    public JButton captureRotateButton;
    public JButton captureSoftDropButton;
    public JButton captureHardDropButton;

    // Multiplayer - Player 1 fields
    public JTextField p1KeyMoveLeftField;
    public JTextField p1KeyMoveRightField;
    public JTextField p1KeyRotateField;
    public JTextField p1KeySoftDropField;
    public JTextField p1KeyHardDropField;

    public JButton captureP1MoveLeftButton;
    public JButton captureP1MoveRightButton;
    public JButton captureP1RotateButton;
    public JButton captureP1SoftDropButton;
    public JButton captureP1HardDropButton;

    // Multiplayer - Player 2 fields
    public JTextField p2KeyMoveLeftField;
    public JTextField p2KeyMoveRightField;
    public JTextField p2KeyRotateField;
    public JTextField p2KeySoftDropField;
    public JTextField p2KeyHardDropField;

    public JButton captureP2MoveLeftButton;
    public JButton captureP2MoveRightButton;
    public JButton captureP2RotateButton;
    public JButton captureP2SoftDropButton;
    public JButton captureP2HardDropButton;

    public JButton resetScoresButton;
    public JButton resetDefaultsButton;
    public JButton saveButton;
    public JButton backToMainButton;
    public JComboBox<GameDifficulty> difficultyCombo;
    private KeyEventDispatcher activeCaptureDispatcher;
    private JButton activeCaptureButton;

    public SettingPanel() {
        super(new GridBagLayout());
        setBackground(Color.LIGHT_GRAY);
        setOpaque(true);
        setVisible(false);

        // Screen size selector
        JPanel sizeRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizeRow.setOpaque(false);
        sizeRow.add(new JLabel("Screen size:"));
        sizeCombo = new JComboBox<>(Setting.ScreenSize.values());
        sizeCombo.setPreferredSize(new Dimension(200, 24));
        sizeRow.add(sizeCombo);

        addToLayout(sizeRow, 0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST);

        // Difficulty selector
        JPanel difficultyRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        difficultyRow.setOpaque(false);
        difficultyRow.add(new JLabel("Difficulty:"));
        difficultyCombo = new JComboBox<>(GameDifficulty.values());
        difficultyCombo.setPreferredSize(new Dimension(200, 24));
        difficultyRow.add(difficultyCombo);

        addToLayout(difficultyRow, 0, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST);

        // Colorblind mode
        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorRow.setOpaque(false);
        colorRow.add(new JLabel("Colorblind mode:"));
        colorBlindCheckbox = new JCheckBox();
        colorRow.add(colorBlindCheckbox);

        addToLayout(colorRow, 0, 2, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST);

        // Key bindings
        JPanel keyBindingPanel = new JPanel();
        keyBindingPanel.setLayout(new BoxLayout(keyBindingPanel, BoxLayout.Y_AXIS));
        keyBindingPanel.setOpaque(false);

        // Single play key bindings
        {
            JLabel textLabel = new JLabel("Single Play Key bindings");
            keyBindingPanel.add(textLabel);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setOpaque(false);
            keyBindingPanel.add(row);

            row.add(new JLabel("Move Left:"));
            row.add(keyMoveLeftField = new JTextField(8));
            keyMoveLeftField.setFocusable(false);
            row.add(captureMoveLeftButton = new JButton("Capture"));

            row.add(new JLabel("Move Right:"));
            row.add(keyMoveRightField = new JTextField(8));
            keyMoveRightField.setFocusable(false);
            row.add(captureMoveRightButton = new JButton("Capture"));

            row.add(new JLabel("Rotate:"));
            row.add(keyRotateField = new JTextField(8));
            keyRotateField.setFocusable(false);
            row.add(captureRotateButton = new JButton("Capture"));

            row.add(new JLabel("Soft Drop:"));
            row.add(keySoftDropField = new JTextField(8));
            keySoftDropField.setFocusable(false);
            row.add(captureSoftDropButton = new JButton("Capture"));

            row.add(new JLabel("Hard Drop:"));
            row.add(keyHardDropField = new JTextField(8));
            keyHardDropField.setFocusable(false);
            row.add(captureHardDropButton = new JButton("Capture"));
        }

        // Multi play key bindings - Player 1
        {
            JLabel p1Label = new JLabel("Player 1 Key Bindings");
            keyBindingPanel.add(p1Label);

            JPanel rowP1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            rowP1.setOpaque(false);
            keyBindingPanel.add(rowP1);

            rowP1.add(new JLabel("P1 Move Left:"));
            rowP1.add(p1KeyMoveLeftField = new JTextField(8));
            p1KeyMoveLeftField.setFocusable(false);
            rowP1.add(captureP1MoveLeftButton = new JButton("Capture"));

            rowP1.add(new JLabel("P1 Move Right:"));
            rowP1.add(p1KeyMoveRightField = new JTextField(8));
            p1KeyMoveRightField.setFocusable(false);
            rowP1.add(captureP1MoveRightButton = new JButton("Capture"));

            rowP1.add(new JLabel("P1 Rotate:"));
            rowP1.add(p1KeyRotateField = new JTextField(8));
            p1KeyRotateField.setFocusable(false);
            rowP1.add(captureP1RotateButton = new JButton("Capture"));

            rowP1.add(new JLabel("P1 Soft Drop:"));
            rowP1.add(p1KeySoftDropField = new JTextField(8));
            p1KeySoftDropField.setFocusable(false);
            rowP1.add(captureP1SoftDropButton = new JButton("Capture"));

            rowP1.add(new JLabel("P1 Hard Drop:"));
            rowP1.add(p1KeyHardDropField = new JTextField(8));
            p1KeyHardDropField.setFocusable(false);
            rowP1.add(captureP1HardDropButton = new JButton("Capture"));
        }

        // Multi play key bindings - Player 2
        {
            JLabel p2Label = new JLabel("Player 2 Key Bindings");
            keyBindingPanel.add(p2Label);

            JPanel rowP2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            rowP2.setOpaque(false);
            keyBindingPanel.add(rowP2);

            rowP2.add(new JLabel("P2 Move Left:"));
            rowP2.add(p2KeyMoveLeftField = new JTextField(8));
            p2KeyMoveLeftField.setFocusable(false);
            rowP2.add(captureP2MoveLeftButton = new JButton("Capture"));

            rowP2.add(new JLabel("P2 Move Right:"));
            rowP2.add(p2KeyMoveRightField = new JTextField(8));
            p2KeyMoveRightField.setFocusable(false);
            rowP2.add(captureP2MoveRightButton = new JButton("Capture"));

            rowP2.add(new JLabel("P2 Rotate:"));
            rowP2.add(p2KeyRotateField = new JTextField(8));
            p2KeyRotateField.setFocusable(false);
            rowP2.add(captureP2RotateButton = new JButton("Capture"));

            rowP2.add(new JLabel("P2 Soft Drop:"));
            rowP2.add(p2KeySoftDropField = new JTextField(8));
            p2KeySoftDropField.setFocusable(false);
            rowP2.add(captureP2SoftDropButton = new JButton("Capture"));

            rowP2.add(new JLabel("P2 Hard Drop:"));
            rowP2.add(p2KeyHardDropField = new JTextField(8));
            p2KeyHardDropField.setFocusable(false);
            rowP2.add(captureP2HardDropButton = new JButton("Capture"));
        }
        

        addToLayout(keyBindingPanel, 0, 3, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        resetScoresButton = new JButton("Reset Scoreboard");
        resetDefaultsButton = new JButton("Reset to Defaults");
        saveButton = new JButton("Save Settings");
        backToMainButton = new JButton("Back to Menu");
        actions.add(resetScoresButton);
        actions.add(resetDefaultsButton);
        actions.add(saveButton);
        actions.add(backToMainButton);

        addToLayout(actions, 0, 4, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST);

        // install capture behavior: controller will attach listeners, but provide a
        // default capturing helper
        installCaptureHandlers();
    }

    private void addToLayout(Component comp, int x, int y, int w, int h, int fill, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;

        gbc.weightx = gbc.gridwidth;
        gbc.weighty = gbc.gridheight;

        gbc.fill = fill;
        gbc.anchor = anchor;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.ipadx = 5;
        gbc.ipady = 5;

        this.add(comp, gbc);
    }

    private void installCaptureHandlers() {
        // Generic capture installer
        installCapture(captureMoveLeftButton, keyMoveLeftField);
        installCapture(captureMoveRightButton, keyMoveRightField);
        installCapture(captureRotateButton, keyRotateField);
        installCapture(captureSoftDropButton, keySoftDropField);
        installCapture(captureHardDropButton, keyHardDropField);
        // Player1 captures
        installCapture(captureP1MoveLeftButton, p1KeyMoveLeftField);
        installCapture(captureP1MoveRightButton, p1KeyMoveRightField);
        installCapture(captureP1RotateButton, p1KeyRotateField);
        installCapture(captureP1SoftDropButton, p1KeySoftDropField);
        installCapture(captureP1HardDropButton, p1KeyHardDropField);
        // Player2 captures
        installCapture(captureP2MoveLeftButton, p2KeyMoveLeftField);
        installCapture(captureP2MoveRightButton, p2KeyMoveRightField);
        installCapture(captureP2RotateButton, p2KeyRotateField);
        installCapture(captureP2SoftDropButton, p2KeySoftDropField);
        installCapture(captureP2HardDropButton, p2KeyHardDropField);
    }

    private void installCapture(JButton captureKeyButton, JTextField targetField) {
        captureKeyButton.addActionListener(e -> {
            cancelActiveCapture();
            activeCaptureButton = captureKeyButton;
            captureKeyButton.setText("Press a key...");
            captureKeyButton.setEnabled(false);
            // prevent the text field from accepting KEY_TYPED events while capturing
            java.awt.KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent evt) {
                    // If a key was pressed, capture it and stop dispatching further key events.
                    if (evt.getID() == KeyEvent.KEY_PRESSED) {
                        int code = evt.getKeyCode();
                        // If ESC pressed, cancel capture without assigning
                        if (code != KeyEvent.VK_ESCAPE) {
                            targetField.setText(tetris.util.KeyMapper.keyCodeToName(code));
                        }
                        // consume and prevent subsequent KEY_TYPED being delivered
                        evt.consume();
                        // restore button and field state
                        captureKeyButton.setText("Capture");
                        captureKeyButton.setEnabled(true);
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                        if (activeCaptureDispatcher == this) {
                            activeCaptureDispatcher = null;
                            activeCaptureButton = null;
                        }
                        return true; // consumed
                    }
                    // Also consume any KEY_TYPED events while capturing to avoid stray input
                    if (evt.getID() == KeyEvent.KEY_TYPED) {
                        evt.consume();
                        return true;
                    }
                    return false;
                }
            };
            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
            activeCaptureDispatcher = dispatcher;
        });
    }

    public void cancelActiveCapture() {
        if (activeCaptureDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(activeCaptureDispatcher);
            activeCaptureDispatcher = null;
        }
        if (activeCaptureButton != null) {
            activeCaptureButton.setText("Capture");
            activeCaptureButton.setEnabled(true);
            activeCaptureButton = null;
        }
    }
}
