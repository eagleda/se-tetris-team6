package tetris.view.SettingComponent;

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
    private static final Dimension[] RESOLUTIONS = {
            new Dimension(700, 900), // 소형
            new Dimension(900, 1200), // 중형
            new Dimension(1100, 1500) // 대형
    };

    private static final String[] RESOLUTION_LABELS = {
            "700 x 900",
            "900 x 1200",
            "1100 x 1500"
    };

    private JRadioButton[] buttons;
    private ButtonGroup buttonGroup;

    public ResolutionPanel() {
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
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        gbc.gridwidth = 2;
        this.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // 각 해상도 옵션 생성
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            final int index = i;

            // 라디오 버튼
            buttons[i] = new JRadioButton();
            buttons[i].addActionListener(e -> changeResolution(RESOLUTIONS[index]));
            buttonGroup.add(buttons[i]);

            gbc.gridx = 0;
            this.add(buttons[i], gbc);

            // 해상도 텍스트
            JLabel resolutionLabel = new JLabel(RESOLUTION_LABELS[i]);
            gbc.gridx = 1;
            this.add(resolutionLabel, gbc);

            gbc.gridy++;
        }

        // 기본 선택 (첫 번째)
        buttons[0].setSelected(true);
    }

    private void changeResolution(Dimension newSize) {
        TetrisFrame frame = TetrisFrame.instance;
        if (frame != null) {
            // TetrisFrame의 FRAME_SIZE 변경
            TetrisFrame.FRAME_SIZE = newSize;

            // 프레임 크기 조정
            frame.setSize(newSize);
            frame.setLocationRelativeTo(null); // 화면 중앙 재배치

            // 모든 패널 크기 업데이트
            if (TetrisFrame.mainPanel != null)
                TetrisFrame.mainPanel.setSize(newSize);
            if (TetrisFrame.gamePanel != null)
                TetrisFrame.gamePanel.setSize(newSize);
            if (TetrisFrame.settingPanel != null)
                TetrisFrame.settingPanel.setSize(newSize);
            if (TetrisFrame.scoreboardPanel != null)
                TetrisFrame.scoreboardPanel.setSize(newSize);
            if (TetrisFrame.pausePanel != null)
                TetrisFrame.pausePanel.setSize(newSize);

            // 레이아웃 갱신
            frame.revalidate();
            frame.repaint();
        }
    }

    // 현재 선택된 해상도 반환
    public Dimension getSelectedResolution() {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isSelected()) {
                return RESOLUTIONS[i];
            }
        }
        return RESOLUTIONS[0]; // 기본값
    }

    // 프로그래밍 방식으로 해상도 설정
    public void setResolution(Dimension size) {
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            if (RESOLUTIONS[i].equals(size)) {
                buttons[i].setSelected(true);
                changeResolution(size);
                break;
            }
        }
    }
}
