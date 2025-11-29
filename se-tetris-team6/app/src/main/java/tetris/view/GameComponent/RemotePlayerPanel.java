package tetris.view.GameComponent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Remote player panel: visual cue for the opponent's board.
 */
public class RemotePlayerPanel extends GamePanel {

    public RemotePlayerPanel() {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw a small "OPP" label at top-left corner
        try {
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.setColor(new Color(200, 0, 0, 200));
            g.drawString("OPP", 8, 16);
        } catch (Exception ignore) {}
    }
}
