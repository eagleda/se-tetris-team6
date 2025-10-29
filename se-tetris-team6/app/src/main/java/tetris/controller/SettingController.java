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

/**
 * Controller that wires SettingPanel and SettingService.
 */
public class SettingController {

    private final SettingService service;
    private final SettingPanel panel;

    public SettingController(ScoreRepository scoreRepository, SettingPanel panel) {
        SettingRepository repo = new PreferencesSettingRepository();
        this.service = new SettingService(repo, scoreRepository);
        this.panel = panel;
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
    }
}
