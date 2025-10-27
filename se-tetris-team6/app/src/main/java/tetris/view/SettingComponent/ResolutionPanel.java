package tetris.view.SettingComponent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tetris.view.TetrisFrame;

// 3가지 이상의 화면 크기 조절
public class ResolutionPanel extends JPanel {
    public static final Dimension[] RESOLUTIONS = {
            new Dimension(600, 800), // 소형
            new Dimension(700, 900), // 중형
            new Dimension(800, 1000) // 대형
    };

    private static final String[] RESOLUTION_LABELS = {
            "700 x 900",
            "900 x 1200",
            "1100 x 1500"
    };

    private JRadioButton[] buttons;
    private ButtonGroup buttonGroup;

    public ResolutionPanel() {
        this.setPreferredSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.gray);
        this.setLayout(new GridBagLayout());

        buttonGroup = new ButtonGroup();
        buttons = new JRadioButton[RESOLUTIONS.length];

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);

        // 타이틀
        JLabel titleLabel = new JLabel("화면 크기 설정:");
        gbc.gridwidth = 2;
        this.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // 각 해상도 옵션 생성
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            Dimension newSize = RESOLUTIONS[i];

            // 라디오 버튼
            buttons[i] = new JRadioButton();
            buttons[i].addActionListener(e -> {
                TetrisFrame.instance.changeResolution(newSize);
            });
            buttonGroup.add(buttons[i]);

            gbc.gridx = 0;
            this.add(buttons[i], gbc);

            // 해상도 텍스트 라벨 추가
            JLabel resolutionLabel = new JLabel(RESOLUTION_LABELS[i]);
            resolutionLabel.setForeground(Color.WHITE); // 텍스트 색상 (배경이 어두우니 흰색)
            gbc.gridx = 1;
            this.add(resolutionLabel, gbc);

            gbc.gridy++;
        }

        // 기본 선택 (첫 번째)
        buttons[0].setSelected(true);
    }
}
