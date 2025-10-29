package tetris.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import tetris.domain.BlockKind;
import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.BlockShape;
import tetris.domain.model.Block;

public class GamePanel extends JPanel {

    private static final int BOARD_COLS = Board.W;
    private static final int BOARD_ROWS = Board.H;
    private static final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private static final Color GRID_COLOR = new Color(48, 48, 48, 180);
    private static final Color[] STANDARD_PALETTE = {
        new Color(30, 30, 30),     // empty
        new Color(0x00, 0xF0, 0xF0), // I
        new Color(0x00, 0x00, 0xF0), // J
        new Color(0xF0, 0xA0, 0x00), // L
        new Color(0xF0, 0xF0, 0x00), // O
        new Color(0x00, 0xF0, 0x00), // S
        new Color(0xA0, 0x00, 0xF0), // T
        new Color(0xF0, 0x00, 0x00)  // Z
    };
    private static final Color[] COLORBLIND_PALETTE = {
        STANDARD_PALETTE[0],
        new Color(0x56, 0xB4, 0xE9), // I
        new Color(0x00, 0x72, 0xB2), // J
        new Color(0xE6, 0x9F, 0x00), // L
        new Color(0xF0, 0xE4, 0x42), // O
        new Color(0x00, 0x9E, 0x73), // S
        new Color(0xCC, 0x79, 0xA7), // T
        new Color(0xD5, 0x5E, 0x00)  // Z
    };

    private GameModel gameModel;
    private Color[] activePalette = STANDARD_PALETTE;
    private boolean colorBlindMode;

    public GamePanel() {
        setSize(TetrisFrame.FRAME_SIZE);
        setBackground(BACKGROUND_COLOR);
        setOpaque(true);
        setVisible(false);
    }

    public void bindGameModel(GameModel model) {
        this.gameModel = model;
        repaint();
    }

    public void setColorBlindMode(boolean enabled) {
        if (this.colorBlindMode == enabled) {
            return;
        }
        this.colorBlindMode = enabled;
        this.activePalette = enabled ? COLORBLIND_PALETTE : STANDARD_PALETTE;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cellSize = Math.min(getWidth() / BOARD_COLS, getHeight() / BOARD_ROWS);
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

        if (gameModel != null) {
            drawLockedBlocks(g2, cellSize, originX, originY);
            drawActiveBlock(g2, cellSize, originX, originY);
        }

        drawGridLines(g2, cellSize, originX, originY, boardWidthPx, boardHeightPx);
        g2.dispose();
    }

    private void drawLockedBlocks(Graphics2D g2, int cellSize, int originX, int originY) {
        int[][] grid = gameModel.getBoard().gridView();
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                int value = grid[y][x];
                if (value <= 0) {
                    continue;
                }
                g2.setColor(colorForBlockId(value));
                int px = originX + x * cellSize;
                int py = originY + y * cellSize;
                g2.fillRect(px, py, cellSize, cellSize);
            }
        }
    }

    private void drawActiveBlock(Graphics2D g2, int cellSize, int originX, int originY) {
        Block active = gameModel.getActiveBlock();
        if (active == null) {
            return;
        }

        BlockShape shape = active.getShape();
        g2.setColor(colorForBlockKind(shape.kind()));

        for (int sy = 0; sy < shape.height(); sy++) {
            for (int sx = 0; sx < shape.width(); sx++) {
                if (!shape.filled(sx, sy)) {
                    continue;
                }
                int boardX = active.getX() + sx;
                int boardY = active.getY() + sy;
                if (boardX < 0 || boardX >= BOARD_COLS || boardY < 0 || boardY >= BOARD_ROWS) {
                    continue;
                }
                int px = originX + boardX * cellSize;
                int py = originY + boardY * cellSize;
                g2.fillRect(px, py, cellSize, cellSize);
            }
        }
    }

    private void drawGridLines(Graphics2D g2, int cellSize, int originX, int originY, int boardWidthPx, int boardHeightPx) {
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

    private Color colorForBlockId(int value) {
        if (value <= 0) {
            return activePalette[0];
        }
        int idx = value;
        if (idx >= activePalette.length) {
            idx = activePalette.length - 1;
        }
        return activePalette[idx];
    }

    private Color colorForBlockKind(BlockKind kind) {
        if (kind == null) {
            return activePalette[0];
        }
        return colorForBlockId(kind.ordinal() + 1);
    }
}
