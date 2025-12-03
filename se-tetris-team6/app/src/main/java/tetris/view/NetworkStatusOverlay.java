package tetris.view;

import javax.swing.*;
import java.awt.*;

/**
 * 온라인 멀티플레이어 게임 중 네트워크 상태를 표시하는 투명 오버레이 패널
 * 우측 상단에 색상 인디케이터와 핑 수치를 표시
 */
public class NetworkStatusOverlay extends JPanel {
    private JLabel statusIndicator;     // 색상 인디케이터 (●)
    private JLabel pingLabel;           // 핑 수치 레이블
    private long currentPing = -1;      // 현재 핑 값 (ms)
    
    private static final Color GOOD_COLOR = new Color(0, 255, 0);      // 녹색: 0-100ms
    private static final Color MODERATE_COLOR = new Color(255, 255, 0); // 노란색: 100-300ms
    private static final Color BAD_COLOR = new Color(255, 0, 0);        // 빨간색: 300ms+
    private static final Color DISCONNECTED_COLOR = new Color(128, 128, 128); // 회색: 연결 끊김
    
    public NetworkStatusOverlay() {
        setOpaque(false); // 투명 배경
        setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        // 색상 인디케이터 (원형)
        statusIndicator = new JLabel("●");
        statusIndicator.setFont(new Font("Arial", Font.BOLD, 24));
        statusIndicator.setForeground(DISCONNECTED_COLOR);
        
        // 핑 레이블
        pingLabel = new JLabel("-- ms");
        pingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pingLabel.setForeground(Color.WHITE);
        
        add(statusIndicator);
        add(pingLabel);
        
        // 초기 상태 업데이트
        updateStatus(-1);
    }
    
    /**
     * 네트워크 상태 업데이트
     * @param ping 현재 핑 값 (ms), -1이면 측정 중 또는 연결 안됨
     */
    public void updateStatus(long ping) {
        this.currentPing = ping;
        
        SwingUtilities.invokeLater(() -> {
            if (ping < 0) {
                // 연결 안됨 또는 측정 중
                statusIndicator.setForeground(DISCONNECTED_COLOR);
                pingLabel.setText("-- ms");
            } else if (ping <= 100) {
                // 양호
                statusIndicator.setForeground(GOOD_COLOR);
                pingLabel.setText(ping + " ms");
            } else if (ping <= 300) {
                // 보통
                statusIndicator.setForeground(MODERATE_COLOR);
                pingLabel.setText(ping + " ms");
            } else {
                // 나쁨
                statusIndicator.setForeground(BAD_COLOR);
                pingLabel.setText(ping + " ms");
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // 투명도를 위해 배경을 그리지 않음
        super.paintComponent(g);
    }
}
