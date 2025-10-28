package tetris.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.model.Block;
import tetris.domain.BlockShape;

public class GamePanel extends JPanel {

    private static final int BOARD_COLS = Board.W;
    private static final int BOARD_ROWS = Board.H;
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
                g2.setColor(colorFor(value));
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
        int colorIndex = shape.kind().ordinal() + 1;
        g2.setColor(colorFor(colorIndex));

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
        if (value <= 0) {
            return BLOCK_COLORS[0];
        }
        int idx = value % BLOCK_COLORS.length;
        if (idx == 0) {
            idx = BLOCK_COLORS.length - 1;
        }
        return BLOCK_COLORS[idx];
    }
}
