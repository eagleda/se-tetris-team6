package tetris.view.GameComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.GameModel;
import tetris.domain.model.Block;
import tetris.view.palette.ColorPaletteProvider;

public class NextBlockPanel extends JPanel {
    private static final int BOARD_COLS = 5;
    private static final int BOARD_ROWS = 5;
    private static final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private static final Color GRID_COLOR = new Color(48, 48, 48, 180);
    private static final Color[] BLOCK_COLORS = {
            new Color(30, 30, 30), // 0: 빈 칸
            new Color(0, 240, 240), // I
            new Color(0, 0, 240), // J
            new Color(240, 160, 0), // L
            new Color(240, 240, 0), // O
            new Color(0, 240, 0), // S
            new Color(160, 0, 240), // T
            new Color(240, 0, 0) // Z
    };

    private GameModel gameModel;
    private static final Color ITEM_BADGE_BG = new Color(255, 215, 0, 220);
    private static final Color ITEM_BADGE_TEXT = Color.BLACK;
    private static final Font ITEM_BADGE_FONT = new Font("SansSerif", Font.BOLD, 12);

    public NextBlockPanel() {
        // 패널 레이아웃 및 외형 설정
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.BLACK);
    // 테두리
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

    JTextPane text = new JTextPane();
    text.setText("Next");
    text.setEditable(false);
    text.setOpaque(false);
    text.setForeground(Color.WHITE);
    text.setFont(new Font("SansSerif", Font.BOLD, 14));
    text.setFocusable(false);
    add(text, BorderLayout.NORTH);
    }

    public void bindGameModel(GameModel model) {
        this.gameModel = model;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (gameModel == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cellSize = Math.min(getWidth() / BOARD_COLS, getHeight() / BOARD_ROWS);
        cellSize = (int) (0.9 * cellSize);
        if (cellSize <= 0) {
            g2.dispose();
            return;
        }

        int boardWidthPx = cellSize * BOARD_COLS;
        int boardHeightPx = cellSize * BOARD_ROWS;
        int originX = (getWidth() - boardWidthPx) / 2;
        int originY = (getHeight() - boardHeightPx) / 2;

        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(originX, originY, boardWidthPx, boardHeightPx);

        BlockKind kind = gameModel.getNextBlockKind();
        if (kind == null) {
            g2.dispose();
            return;
        }

        Block preview = Block.spawn(kind, 2, 2);
        if (preview == null) {
            g2.dispose();
            return;
        }

        BlockShape shape = preview.getShape();
        int colorIndex = shape.kind().ordinal() + 1;
        g2.setColor(colorFor(colorIndex));

        int offsetX = (BOARD_COLS - shape.width()) / 2;
        int offsetY = (BOARD_ROWS - shape.height()) / 2;
        for (int sy = 0; sy < shape.height(); sy++) {
            for (int sx = 0; sx < shape.width(); sx++) {
                if (!shape.filled(sx, sy))
                    continue;
                int boardX = offsetX + sx;
                int boardY = offsetY + sy;
                if (boardX < 0 || boardX >= BOARD_COLS || boardY < 0 || boardY >= BOARD_ROWS)
                    continue;
                int px = originX + boardX * cellSize;
                int py = originY + boardY * cellSize;
                g2.fillRect(px, py, cellSize, cellSize);
            }
        }

        if (gameModel.isItemMode() && gameModel.isNextBlockItem()) {
            drawItemBadge(g2, originX, originY, boardWidthPx, boardHeightPx);
        }

        drawGridLines(g2, cellSize, originX, originY, boardWidthPx, boardHeightPx);
        g2.dispose();
    }

    private void drawGridLines(Graphics2D g2, int cellSize, int originX, int originY, int boardWidthPx,
            int boardHeightPx) {
        g2.setColor(GRID_COLOR);
        for (int x = 0; x <= BOARD_COLS; x++) {
            int px = originX + x * cellSize;
            g2.drawLine(px, originY, px, originY + boardHeightPx);
        }
        for (int y = 0; y <= BOARD_ROWS; y++) {
            int py = originY + y * cellSize;
            g2.drawLine(originX, py, originX + boardWidthPx, py);
        }
    }

    

    private Color colorFor(int value) {
        Color[] palette = ColorPaletteProvider.palette(gameModel != null && gameModel.isColorBlindMode());
        if (value <= 0) {
            return palette[0];
        }
        int idx = value % palette.length;
        if (idx == 0) {
            idx = palette.length - 1;
        }
        return palette[idx];
    }

    private void drawItemBadge(Graphics2D g2, int originX, int originY, int width, int height) {
        Graphics2D badge = (Graphics2D) g2.create();
        badge.setFont(ITEM_BADGE_FONT);
        String label = "ITEM";
        int textWidth = badge.getFontMetrics().stringWidth(label);
        int textHeight = badge.getFontMetrics().getAscent();
        int padding = 6;
        int boxWidth = textWidth + padding * 2;
        int boxHeight = textHeight + padding;
        int x = originX + width - boxWidth - 6;
        int y = originY + 6;
        badge.setColor(ITEM_BADGE_BG);
        badge.fillRoundRect(x, y, boxWidth, boxHeight, 12, 12);
        badge.setColor(ITEM_BADGE_TEXT);
        badge.drawString(label, x + padding, y + textHeight);
        badge.dispose();
    }
}
