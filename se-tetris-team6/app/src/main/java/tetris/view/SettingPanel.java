package tetris.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tetris.domain.setting.Setting;

public class SettingPanel extends JPanel {

    // Exposed UI components for controller wiring
    public JComboBox<Setting.ScreenSize> sizeCombo;
    public JCheckBox colorBlindCheckbox;
    public JTextField keyMoveLeftField;
    public JTextField keyMoveRightField;
    public JTextField keyRotateField;
    public JTextField keySoftDropField;
    public JButton captureMoveLeftButton;
    public JButton captureMoveRightButton;
    public JButton captureRotateButton;
    public JButton captureSoftDropButton;
    public JButton resetScoresButton;
    public JButton resetDefaultsButton;
    public JButton saveButton;
    public JButton backToMainButton;

    public SettingPanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.DARK_GRAY);
        this.setOpaque(true);
        this.setVisible(false);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Screen size selector
        JPanel sizeRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizeRow.setOpaque(false);
        sizeRow.add(new JLabel("Screen size:"));
        sizeCombo = new JComboBox<>(Setting.ScreenSize.values());
        sizeCombo.setPreferredSize(new Dimension(200, 24));
        sizeRow.add(sizeCombo);
        this.add(sizeRow);

        // Colorblind mode
        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorRow.setOpaque(false);
        colorRow.add(new JLabel("Colorblind mode:"));
        colorBlindCheckbox = new JCheckBox();
        colorRow.add(colorBlindCheckbox);
        this.add(colorRow);

        // Key bindings
        JPanel keysRow = new JPanel();
        keysRow.setLayout(new BoxLayout(keysRow, BoxLayout.Y_AXIS));
        keysRow.setOpaque(false);
        keysRow.add(new JLabel("Key bindings (use key names, e.g. LEFT, RIGHT, UP, DOWN, SPACE):"));

    JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.setOpaque(false);
        row1.add(new JLabel("Move Left:"));
        keyMoveLeftField = new JTextField(8);
    row1.add(keyMoveLeftField);
    captureMoveLeftButton = new JButton("Capture");
    row1.add(captureMoveLeftButton);
        row1.add(new JLabel("Move Right:"));
        keyMoveRightField = new JTextField(8);
    row1.add(keyMoveRightField);
    captureMoveRightButton = new JButton("Capture");
    row1.add(captureMoveRightButton);
        keysRow.add(row1);

    JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.setOpaque(false);
        row2.add(new JLabel("Rotate:"));
        keyRotateField = new JTextField(8);
    row2.add(keyRotateField);
    captureRotateButton = new JButton("Capture");
    row2.add(captureRotateButton);
        row2.add(new JLabel("Soft Drop:"));
        keySoftDropField = new JTextField(8);
    row2.add(keySoftDropField);
    captureSoftDropButton = new JButton("Capture");
    row2.add(captureSoftDropButton);
        keysRow.add(row2);

        this.add(keysRow);

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
        this.add(actions);
        // install capture behavior: controller will attach listeners, but provide a default capturing helper
        installCaptureHandlers();
    }

    private void installCaptureHandlers() {
        // Generic capture installer
        installCapture(captureMoveLeftButton, keyMoveLeftField);
        installCapture(captureMoveRightButton, keyMoveRightField);
        installCapture(captureRotateButton, keyRotateField);
        installCapture(captureSoftDropButton, keySoftDropField);
    }

    private void installCapture(JButton button, JTextField targetField) {
        button.addActionListener(e -> {
            button.setText("Press a key...");
            button.setEnabled(false);

            java.awt.KeyEventDispatcher dispatcher = new java.awt.KeyEventDispatcher() {
                @Override
                    public boolean dispatchKeyEvent(java.awt.event.KeyEvent evt) {
                        if (evt.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
                            int code = evt.getKeyCode();
                            targetField.setText(tetris.util.KeyMapper.keyCodeToName(code));
                            // consume the event so no KEY_TYPED is delivered to other components
                            evt.consume();
                            // restore button
                            button.setText("Capture");
                            button.setEnabled(true);
                            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                            return true; // indicate consumed by dispatcher
                        }
                        return false;
                    }
            };

            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        });
    }
}
