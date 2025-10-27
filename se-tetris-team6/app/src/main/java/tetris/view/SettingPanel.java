package tetris.view;

import java.awt.Color;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import tetris.view.SettingComponent.ResolutionPanel;

public class SettingPanel extends JPanel {
    public SettingPanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.LIGHT_GRAY);
        this.setOpaque(true);
        this.setVisible(false);

        // 수직 BoxLayout 설정 (여러 패널을 위아래로 배치)
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(new EmptyBorder(20, 20, 20, 20)); // 여백 추가

        // 1. ResolutionPanel 추가
        ResolutionPanel resolutionPanel = new ResolutionPanel();
        resolutionPanel.setAlignmentX(LEFT_ALIGNMENT); // 좌측 정렬
        this.add(resolutionPanel);

        // 패널 간 간격
        this.add(Box.createVerticalStrut(20));

        // 2. 추가 설정 패널들을 여기에 추가 가능
        // this.add(new AudioSettingPanel());
        // this.add(Box.createVerticalStrut(20));
        // this.add(new KeyBindingPanel());

        // 하단 여백 (나머지 공간 채우기)
        this.add(Box.createVerticalGlue());
    }
}
