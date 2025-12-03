package tetris.view.GameComponent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.BlockShape;
import tetris.domain.GameModel.ActiveItemInfo;
import tetris.domain.model.Block;
import tetris.view.palette.ColorPaletteProvider;

public class GamePanel extends JPanel {
    // 플래시 중인 행 및 타이머
    private List<Integer> flashingLines = new ArrayList<>();
    private Timer flashTimer;

    private static final int BOARD_COLS = Board.W;
    private static final int BOARD_ROWS = Board.H;
    private static final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private static final Color GRID_COLOR = new Color(48, 48, 48, 180);
    private static final Color ATTACK_LINE_COLOR = new Color(180, 180, 180); // 공격 라인 색상 (회색)
    private GameModel gameModel;
    private static final Color ITEM_OUTLINE_COLOR = new Color(255, 215, 0, 210);
    private static final Color ITEM_LABEL_BACKGROUND = new Color(0, 0, 0, 180);
    private static final Color ITEM_LABEL_COLOR = Color.WHITE;
    private static final Font ITEM_LABEL_FONT = new Font("SansSerif", Font.BOLD, 14);

    public GamePanel() {
        setBackground(BACKGROUND_COLOR);
        setOpaque(true);
        // 영역 경계선 표시
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
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

        drawLockedBlocks(g2, cellSize, originX, originY);
        highlightLines(g2, cellSize, originX, originY);
        drawActiveBlock(g2, cellSize, originX, originY);
        drawGridLines(g2, cellSize, originX, originY, boardWidthPx, boardHeightPx);
        g2.dispose();
    }

    private void drawLockedBlocks(Graphics2D g2, int cellSize, int originX, int originY) {
        if (gameModel == null)
            return;

        int[][] grid = gameModel.getBoard().gridView();
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                int value = grid[y][x];
                if (value <= 0)
                    continue;
                g2.setColor(colorFor(value));
                int px = originX + x * cellSize;
                int py = originY + y * cellSize;
                g2.fillRect(px, py, cellSize, cellSize);
            }
        }
    }

    private void drawActiveBlock(Graphics2D g2, int cellSize, int originX, int originY) {
        if (gameModel == null)
            return;
        Block active = gameModel.getActiveBlock();
        if (active == null)
            return;

        BlockShape shape = active.getShape();
        int colorIndex = shape.kind().ordinal() + 1;
        g2.setColor(colorFor(colorIndex));

        ActiveItemInfo itemInfo = gameModel.getActiveItemInfo();
        boolean highlightItem = itemInfo != null && (itemInfo.block() == null || itemInfo.block() == active);
        int blockMinPx = originX + active.getX() * cellSize;
        int blockMinPy = originY + active.getY() * cellSize;
        int blockWidthPx = cellSize * shape.width();
        int blockHeightPx = cellSize * shape.height();

        for (int sy = 0; sy < shape.height(); sy++) {
            for (int sx = 0; sx < shape.width(); sx++) {
                if (!shape.filled(sx, sy))
                    continue;
                int boardX = active.getX() + sx;
                int boardY = active.getY() + sy;
                if (boardX < 0 || boardX >= BOARD_COLS || boardY < 0 || boardY >= BOARD_ROWS)
                    continue;
                int px = originX + boardX * cellSize;
                int py = originY + boardY * cellSize;
                
                // 먼저 기본 블록 색상으로 칸 채우기
                g2.fillRect(px, py, cellSize, cellSize);
                
                // Bomb 아이템인 경우 (아이템 칸이 없고 블록 자체가 폭탄)
                if (highlightItem && "bomb".equals(itemInfo.label()) && !itemInfo.hasItemCell()) {
                    Graphics2D textG = (Graphics2D) g2.create();
                    textG.setColor(Color.WHITE);
                    Font cellFont = new Font("SansSerif", Font.BOLD, (int)(cellSize * 0.6f));
                    textG.setFont(cellFont);
                    int textWidth = textG.getFontMetrics().stringWidth("B");
                    int textHeight = textG.getFontMetrics().getAscent();
                    int textX = px + (cellSize - textWidth) / 2;
                    int textY = py + (cellSize + textHeight) / 2 - textG.getFontMetrics().getDescent();
                    textG.drawString("B", textX, textY);
                    textG.dispose();
                }
                
                // 아이템 칸인 경우 표시
                if (highlightItem && itemInfo.hasItemCell() && sx == itemInfo.itemCellX() && sy == itemInfo.itemCellY()) {
                    String itemId = itemInfo.label();
                    Graphics2D textG = (Graphics2D) g2.create();
                    textG.setColor(Color.WHITE);
                    
                    String displayText = "";
                    float fontSize = cellSize * 0.6f;
                    
                    if ("line_clear".equals(itemId)) {
                        displayText = "L";
                    } else if ("slow".equals(itemId)) {
                        displayText = "T";
                    } else if ("double_score".equals(itemId)) {
                        displayText = "2x";
                        fontSize = cellSize * 0.5f; // 2x는 두 글자이므로 좀 더 작게
                    } else {
                        // 기본: 흰색으로 칸 채우기
                        textG.dispose();
                        g2.setColor(Color.WHITE);
                        g2.fillRect(px, py, cellSize, cellSize);
                        g2.setColor(colorFor(colorIndex)); // 원래 색으로 복원
                        continue;
                    }
                    
                    // 글자 표시
                    Font cellFont = new Font("SansSerif", Font.BOLD, (int)fontSize);
                    textG.setFont(cellFont);
                    int textWidth = textG.getFontMetrics().stringWidth(displayText);
                    int textHeight = textG.getFontMetrics().getAscent();
                    int textX = px + (cellSize - textWidth) / 2;
                    int textY = py + (cellSize + textHeight) / 2 - textG.getFontMetrics().getDescent();
                    textG.drawString(displayText, textX, textY);
                    textG.dispose();
                }
            }
        }

        if (highlightItem) {
            Graphics2D overlay = (Graphics2D) g2.create();
            overlay.setStroke(new BasicStroke(Math.max(2f, cellSize * 0.1f)));
            overlay.setColor(ITEM_OUTLINE_COLOR);
            overlay.drawRect(blockMinPx, blockMinPy, blockWidthPx, blockHeightPx);

            String label = itemLabel(itemInfo.label());
            if (label != null && !label.isEmpty()) {
                Font font = ITEM_LABEL_FONT.deriveFont(Math.max(10f, cellSize * 0.45f));
                overlay.setFont(font);
                int textWidth = overlay.getFontMetrics().stringWidth(label);
                int textHeight = overlay.getFontMetrics().getAscent();
                int labelX = blockMinPx + (blockWidthPx - textWidth) / 2;
                int labelY = blockMinPy + textHeight - 20;
                overlay.setColor(ITEM_LABEL_BACKGROUND);
                overlay.fillRoundRect(labelX - 4, labelY - textHeight, textWidth + 8, textHeight + 4, 8, 8);
                overlay.setColor(ITEM_LABEL_COLOR);
                overlay.drawString(label, labelX, labelY);
            }
            overlay.dispose();
        }
    }

    private void highlightLines(Graphics2D g2, int cellSize, int originX, int originY) {
        if (gameModel == null)
            return;

        // 플래시가 아직 동작중이지 않다면 실제 삭제된 줄 목록을 확인해 플래시 시작
        if (flashingLines.isEmpty()) {
            List<Integer> cleared = gameModel.getLastClearedLines();
            if (cleared == null || cleared.isEmpty()) {
                return;
            }
            flashingLines = new ArrayList<>(cleared);
            if (flashTimer != null && flashTimer.isRunning())
                flashTimer.stop();
            flashTimer = new Timer(200, e -> {
                flashingLines.clear();
                if (gameModel != null) {
                     gameModel.clearLastClearedLines();
                 }
                repaint();
                ((Timer) e.getSource()).stop();
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
        }

        // 플래시 중인 행을 흰색으로 그림
        if (!flashingLines.isEmpty()) {
            g2.setColor(Color.WHITE);
            for (int highlightedLine : flashingLines) {
                int y = originY + highlightedLine * cellSize;
                for (int x = 0; x < BOARD_COLS; x++) {
                    int px = originX + x * cellSize;
                    g2.fillRect(px, y, cellSize, cellSize);
                }
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
        // 공격 라인(value=8)은 회색으로 표시
        if (value == 8) {
            return ATTACK_LINE_COLOR;
        }
        
        Color[] palette = ColorPaletteProvider.palette(gameModel != null && gameModel.isColorBlindMode());
        if (value <= 0)
            return palette[0];
        int idx = value % palette.length;
        if (idx == 0)
            idx = palette.length - 1;
        return palette[idx];
    }

    private String itemLabel(String id) {
        if (id == null || id.isEmpty()) {
            return "ITEM";
        }
        switch (id) {
            case "double_score":
                return "2x";
            case "slow":
                return "SLOW";
            case "bomb":
                return "BOMB";
            case "line_clear":
                return "LINE";
            case "weight":
                return "WEIGHT";
            default:
                return id.toUpperCase();
        }
    }
}
