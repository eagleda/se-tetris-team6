package tetris.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;

import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;
import tetris.data.setting.PreferencesSettingRepository;
import tetris.domain.score.ScoreRepository;
import tetris.view.SettingPanel;
import tetris.controller.GameController;
import tetris.view.TetrisFrame;
import tetris.util.KeyMapper;

/**
 * Controller that wires SettingPanel and SettingService.
 */
public class SettingController {

    private final SettingService service;
    private final SettingPanel panel;

    private final GameController gameController;
    private final TetrisFrame frame;

    public SettingController(ScoreRepository scoreRepository, SettingPanel panel, GameController gameController, TetrisFrame frame) {
        SettingRepository repo = new PreferencesSettingRepository();
        this.service = new SettingService(repo, scoreRepository);
        this.panel = panel;
        this.gameController = gameController;
        this.frame = frame;
        bind();
        loadToPanel();
    }

    private void bind() {
        // Save button: read UI and persist
        panel.saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                persistFromPanel();
            }
        });

        // Reset defaults
        panel.resetDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                service.resetToDefaults();
                loadToPanel();
            }
        });

        // Reset scoreboard
        panel.resetScoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                service.resetScoreboard();
            }
        });

        // Screen size combo -> update immediately on change
        JComboBox<Setting.ScreenSize> combo = panel.sizeCombo;
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Setting.ScreenSize s = (Setting.ScreenSize) combo.getSelectedItem();
                service.setScreenSize(s);
                // Immediately apply to frame
                if (frame != null) {
                    frame.applyScreenSize(s);
                }
            }
        });
    }

    private void loadToPanel() {
        Setting s = service.getSettings();
        panel.sizeCombo.setSelectedItem(s.getScreenSize());
        panel.colorBlindCheckbox.setSelected(s.isColorBlindMode());
        // fill key fields
        panel.keyMoveLeftField.setText(s.getKeyBinding("MOVE_LEFT"));
        panel.keyMoveRightField.setText(s.getKeyBinding("MOVE_RIGHT"));
        panel.keyRotateField.setText(s.getKeyBinding("ROTATE"));
        panel.keySoftDropField.setText(s.getKeyBinding("SOFT_DROP"));
    }

    private void persistFromPanel() {
        // collect key bindings
        Map<String, String> kb = new HashMap<>();
        kb.put("MOVE_LEFT", panel.keyMoveLeftField.getText().trim());
        kb.put("MOVE_RIGHT", panel.keyMoveRightField.getText().trim());
        kb.put("ROTATE", panel.keyRotateField.getText().trim());
        kb.put("SOFT_DROP", panel.keySoftDropField.getText().trim());
        // other actions can be added similarly

        service.setKeyBindings(kb);
        service.setColorBlindMode(panel.colorBlindCheckbox.isSelected());
        Object sel = panel.sizeCombo.getSelectedItem();
        if (sel instanceof Setting.ScreenSize) {
            service.setScreenSize((Setting.ScreenSize) sel);
        }
        service.save();

        // Apply keybindings to runtime GameController
        if (gameController != null) {
            java.util.Map<String, Integer> mapped = new java.util.HashMap<>();
            for (java.util.Map.Entry<String, String> e : service.getSettings().getKeyBindings().entrySet()) {
                int code = KeyMapper.nameToKeyCode(e.getValue());
                if (code > 0) {
                    mapped.put(e.getKey(), code);
                }
            }
            gameController.applyKeyBindings(mapped);
        }
    }
}
