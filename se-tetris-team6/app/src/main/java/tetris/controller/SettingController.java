package tetris.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;

import tetris.domain.GameDifficulty;

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

        // Back to main menu
        panel.backToMainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frame != null) {
                    frame.showMainPanel();
                }
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

        // Difficulty combo -> update immediately on change
        if (panel.difficultyCombo != null) {
            panel.difficultyCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GameDifficulty difficulty = (GameDifficulty) panel.difficultyCombo.getSelectedItem();
                    service.setDifficulty(difficulty);
                    if (gameController != null && difficulty != null) {
                        gameController.applyDifficulty(difficulty);
                    }
                }
            });
        }
    }

    private void loadToPanel() {
        Setting s = service.getSettings();
        panel.sizeCombo.setSelectedItem(s.getScreenSize());
        GameDifficulty difficulty = s.getDifficulty() != null ? s.getDifficulty() : GameDifficulty.NORMAL;
        if (panel.difficultyCombo != null) {
            panel.difficultyCombo.setSelectedItem(difficulty);
        }
        panel.colorBlindCheckbox.setSelected(s.isColorBlindMode());
        // single-player bindings: read from settings; defaults are provided by Setting.defaults()
        Integer ml = s.getKeyBinding("MOVE_LEFT");
        Integer mr = s.getKeyBinding("MOVE_RIGHT");
        Integer rot = s.getKeyBinding("ROTATE_CW");
        Integer sd = s.getKeyBinding("SOFT_DROP");
        Integer hd = s.getKeyBinding("HARD_DROP");
        panel.keyMoveLeftField.setText(ml == null ? "" : KeyMapper.keyCodeToName(ml));
        panel.keyMoveRightField.setText(mr == null ? "" : KeyMapper.keyCodeToName(mr));
        panel.keyRotateField.setText(rot == null ? "" : KeyMapper.keyCodeToName(rot));
        panel.keySoftDropField.setText(sd == null ? "" : KeyMapper.keyCodeToName(sd));
        panel.keyHardDropField.setText(hd == null ? "" : KeyMapper.keyCodeToName(hd));

        // Player1 bindings: read from settings; defaults are provided by Setting.defaults()
        Integer p1_ml = s.getKeyBinding("P1_MOVE_LEFT");
        Integer p1_mr = s.getKeyBinding("P1_MOVE_RIGHT");
        Integer p1_rot = s.getKeyBinding("P1_ROTATE_CW");
        Integer p1_sd = s.getKeyBinding("P1_SOFT_DROP");
        Integer p1_hd = s.getKeyBinding("P1_HARD_DROP");
        panel.p1KeyMoveLeftField.setText((p1_ml == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P1_MOVE_LEFT")) : KeyMapper.keyCodeToName(p1_ml)));
        panel.p1KeyMoveRightField.setText((p1_mr == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P1_MOVE_RIGHT")) : KeyMapper.keyCodeToName(p1_mr)));
        panel.p1KeyRotateField.setText((p1_rot == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P1_ROTATE_CW")) : KeyMapper.keyCodeToName(p1_rot)));
        panel.p1KeySoftDropField.setText((p1_sd == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P1_SOFT_DROP")) : KeyMapper.keyCodeToName(p1_sd)));
        panel.p1KeyHardDropField.setText((p1_hd == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P1_HARD_DROP")) : KeyMapper.keyCodeToName(p1_hd)));

        // Player2 bindings: read from settings; use defaults from Setting.defaults() when missing
        Integer p2_ml = s.getKeyBinding("P2_MOVE_LEFT");
        Integer p2_mr = s.getKeyBinding("P2_MOVE_RIGHT");
        Integer p2_rot = s.getKeyBinding("P2_ROTATE_CW");
        Integer p2_sd = s.getKeyBinding("P2_SOFT_DROP");
        Integer p2_hd = s.getKeyBinding("P2_HARD_DROP");
        panel.p2KeyMoveLeftField.setText((p2_ml == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P2_MOVE_LEFT")) : KeyMapper.keyCodeToName(p2_ml)));
        panel.p2KeyMoveRightField.setText((p2_mr == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P2_MOVE_RIGHT")) : KeyMapper.keyCodeToName(p2_mr)));
        panel.p2KeyRotateField.setText((p2_rot == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P2_ROTATE_CW")) : KeyMapper.keyCodeToName(p2_rot)));
        panel.p2KeySoftDropField.setText((p2_sd == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P2_SOFT_DROP")) : KeyMapper.keyCodeToName(p2_sd)));
        panel.p2KeyHardDropField.setText((p2_hd == null ? KeyMapper.keyCodeToName(Setting.defaults().getKeyBinding("P2_HARD_DROP")) : KeyMapper.keyCodeToName(p2_hd)));
        if (gameController != null) {
            gameController.applyDifficulty(difficulty);
        }
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
    if (code > 0) kb.put("ROTATE_CW", code);
    code = KeyMapper.nameToKeyCode(panel.keySoftDropField.getText().trim());
    if (code > 0) kb.put("SOFT_DROP", code);
    code = KeyMapper.nameToKeyCode(panel.keyHardDropField.getText().trim());
    if (code > 0) kb.put("HARD_DROP", code);
    // Player1 bindings
    code = KeyMapper.nameToKeyCode(panel.p1KeyMoveLeftField.getText().trim());
    if (code > 0) kb.put("P1_MOVE_LEFT", code);
    code = KeyMapper.nameToKeyCode(panel.p1KeyMoveRightField.getText().trim());
    if (code > 0) kb.put("P1_MOVE_RIGHT", code);
    code = KeyMapper.nameToKeyCode(panel.p1KeyRotateField.getText().trim());
    if (code > 0) kb.put("P1_ROTATE_CW", code);
    code = KeyMapper.nameToKeyCode(panel.p1KeySoftDropField.getText().trim());
    if (code > 0) kb.put("P1_SOFT_DROP", code);
    code = KeyMapper.nameToKeyCode(panel.p1KeyHardDropField.getText().trim());
    if (code > 0) kb.put("P1_HARD_DROP", code);
    // Player2 bindings
    code = KeyMapper.nameToKeyCode(panel.p2KeyMoveLeftField.getText().trim());
    if (code > 0) kb.put("P2_MOVE_LEFT", code);
    code = KeyMapper.nameToKeyCode(panel.p2KeyMoveRightField.getText().trim());
    if (code > 0) kb.put("P2_MOVE_RIGHT", code);
    code = KeyMapper.nameToKeyCode(panel.p2KeyRotateField.getText().trim());
    if (code > 0) kb.put("P2_ROTATE_CW", code);
    code = KeyMapper.nameToKeyCode(panel.p2KeySoftDropField.getText().trim());
    if (code > 0) kb.put("P2_SOFT_DROP", code);
    code = KeyMapper.nameToKeyCode(panel.p2KeyHardDropField.getText().trim());
    if (code > 0) kb.put("P2_HARD_DROP", code);
    // other actions can be added similarly

    service.setKeyBindings(kb);
        service.setColorBlindMode(panel.colorBlindCheckbox.isSelected());
        Object sel = panel.sizeCombo.getSelectedItem();
        if (sel instanceof Setting.ScreenSize) {
            service.setScreenSize((Setting.ScreenSize) sel);
        }
        Object diffSel = panel.difficultyCombo != null ? panel.difficultyCombo.getSelectedItem() : null;
        GameDifficulty difficulty = diffSel instanceof GameDifficulty
            ? (GameDifficulty) diffSel : GameDifficulty.NORMAL;
        service.setDifficulty(difficulty);
        service.save();

        // Apply keybindings to runtime GameController
        if (gameController != null) {
            // service.getSettings().getKeyBindings() already contains int codes
            java.util.Map<String, Integer> mapped = new java.util.HashMap<>(service.getSettings().getKeyBindings());
            gameController.applyKeyBindings(mapped);
            gameController.applyDifficulty(difficulty);
        }
    }
}
