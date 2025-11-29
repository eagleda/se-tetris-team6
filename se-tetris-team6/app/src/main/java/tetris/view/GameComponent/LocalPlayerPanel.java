package tetris.view.GameComponent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Local player panel: visual cue for the local player's board.
 */
public class LocalPlayerPanel extends GamePanel {

    public LocalPlayerPanel() {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw a small "You" label at top-left corner
        try {
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.setColor(new Color(0, 200, 0, 200));
            g.drawString("YOU", 8, 16);
        } catch (Exception ignore) {}
    }
}
