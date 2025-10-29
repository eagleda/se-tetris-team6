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
        Integer ml = s.getKeyBinding("MOVE_LEFT");
        Integer mr = s.getKeyBinding("MOVE_RIGHT");
        Integer rot = s.getKeyBinding("ROTATE");
        Integer sd = s.getKeyBinding("SOFT_DROP");
        panel.keyMoveLeftField.setText(ml == null ? "" : KeyMapper.keyCodeToName(ml));
        panel.keyMoveRightField.setText(mr == null ? "" : KeyMapper.keyCodeToName(mr));
        panel.keyRotateField.setText(rot == null ? "" : KeyMapper.keyCodeToName(rot));
        panel.keySoftDropField.setText(sd == null ? "" : KeyMapper.keyCodeToName(sd));
    }

    private void persistFromPanel() {
    // collect key bindings as key codes
    Map<String, Integer> kb = new HashMap<>();
    int code;
    code = KeyMapper.nameToKeyCode(panel.keyMoveLeftField.getText().trim());
    if (code > 0) kb.put("MOVE_LEFT", code);
    code = KeyMapper.nameToKeyCode(panel.keyMoveRightField.getText().trim());
    if (code > 0) kb.put("MOVE_RIGHT", code);
    code = KeyMapper.nameToKeyCode(panel.keyRotateField.getText().trim());
    if (code > 0) kb.put("ROTATE", code);
    code = KeyMapper.nameToKeyCode(panel.keySoftDropField.getText().trim());
    if (code > 0) kb.put("SOFT_DROP", code);
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
            // service.getSettings().getKeyBindings() already contains int codes
            java.util.Map<String, Integer> mapped = new java.util.HashMap<>(service.getSettings().getKeyBindings());
            gameController.applyKeyBindings(mapped);
        }
    }
}
