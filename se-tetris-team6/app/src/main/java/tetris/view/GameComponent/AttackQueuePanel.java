package tetris.view.GameComponent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.lang.reflect.Method;
import java.util.function.IntSupplier;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import tetris.domain.GameModel;

/**
 * AttackQueuePanel
 * - 10x10 보드 상태를 시각화 하는 간단한 패널입니다.
 * - 외부에서 int[][] 형태의 그리드(행,열)를 전달받아 화면을 갱신합니다.
 * - GamePanel로부터 직접 상태를 받으려면 리플렉션을 통해
 *   getAttackQueueGrid() 같은 메서드를 호출하도록 시도합니다(존재하면 사용).
 *
 * 노트:
 * - 블록 관련 로직(회전/충돌 등)은 구현하지 않았습니다. 그리드 값은
 *   0 = 빈칸, >0 = 채워진 블록(색상 인덱스) 으로 가정합니다.
 */
public class AttackQueuePanel extends JPanel {
    private static final int BOARD_COLS = 10;
    private static final int BOARD_ROWS = 10;
    private static final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private static final Color GRID_COLOR = new Color(48, 48, 48, 180);
    // Use only gray colors for cells: empty and filled
    private static final Color EMPTY_COLOR = new Color(30, 30, 30);
    private static final Color FILLED_COLOR = new Color(180, 180, 180);

    // 내부 그리드(방어적 복사)
    private int[][] grid = new int[BOARD_ROWS][BOARD_COLS];

    private IntSupplier pendingLinesSupplier;
    private java.util.function.Supplier<java.util.List<tetris.multiplayer.model.AttackLine>> attackLinesSupplier;

    public AttackQueuePanel() {
        setBackground(BACKGROUND_COLOR);
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        setPreferredSize(new Dimension(160, 160));
    }

    public void bindGameModel(GameModel model) {
        this.pendingLinesSupplier = null;
        repaint();
    }

    /**
     * 로컬 멀티 UI에서 공격 대기 줄 수치를 직접 공급할 때 사용한다.
     */
    public void bindPendingLinesSupplier(IntSupplier supplier) {
        this.pendingLinesSupplier = supplier;
        this.attackLinesSupplier = null;
        repaint();
    }

    /**
     * 로컬 멀티 UI에서 공격 대기 줄의 실제 패턴을 공급할 때 사용한다.
     */
    public void bindAttackLinesSupplier(java.util.function.Supplier<java.util.List<tetris.multiplayer.model.AttackLine>> supplier) {
        this.attackLinesSupplier = supplier;
        this.pendingLinesSupplier = null;
        repaint();
    }

    /**
     * 외부에서 그리드 상태를 직접 설정합니다. 배열은 복사됩니다.
     * 기대 형식: grid.length == 10 && grid[0].length == 10
     */
    public void setGrid(int[][] newGrid) {
        if (newGrid == null) {
            clearGrid();
            return;
        }
        int rows = Math.min(BOARD_ROWS, newGrid.length);
        for (int r = 0; r < BOARD_ROWS; r++) {
            for (int c = 0; c < BOARD_COLS; c++) {
                if (r < rows && newGrid[r] != null && c < newGrid[r].length) {
                    grid[r][c] = newGrid[r][c];
                } else {
                    grid[r][c] = 0;
                }
            }
        }
        repaint();
    }

    public void clearGrid() {
        for (int r = 0; r < BOARD_ROWS; r++) {
            for (int c = 0; c < BOARD_COLS; c++)
                grid[r][c] = 0;
        }
        repaint();
    }

    /**
     * GamePanel과 같은 객체로부터 상태를 가져오려면 이 메서드를 호출하세요.
     * 이 메서드는 리플렉션을 사용해 호출 가능한 메서드 이름을 시도합니다:
     * - getAttackQueueGrid()
     * - getAttackGrid()
     * - getQueuedLinesGrid()
     *
     * 위 메서드들이 존재하면 int[][]를 반환한다고 가정하고 setGrid로 복사합니다.
     */
    public void updateFromGamePanel(Object gamePanel) {
        if (gamePanel == null)
            return;
        String[] candidates = { "getAttackQueueGrid", "getAttackGrid", "getQueuedLinesGrid" };
        for (String name : candidates) {
            try {
                Method m = gamePanel.getClass().getMethod(name);
                Object res = m.invoke(gamePanel);
                if (res instanceof int[][]) {
                    setGrid((int[][]) res);
                    return;
                }
            } catch (NoSuchMethodException nsme) {
                // ignore and try next
            } catch (Exception ex) {
                // 반사 호출 중 에러가 발생하면 로그 없이 무시(안정성 우선)
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cellSize = Math.min(getWidth() / BOARD_COLS, getHeight() / BOARD_ROWS);
        cellSize = Math.max(4, cellSize - 2); // 최소 크기 보장 및 약간의 패딩
        int boardWidthPx = cellSize * BOARD_COLS;
        int boardHeightPx = cellSize * BOARD_ROWS;
        int originX = (getWidth() - boardWidthPx) / 2;
        int originY = (getHeight() - boardHeightPx) / 2;

        // 배경
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(originX, originY, boardWidthPx, boardHeightPx);

        int pendingLines = pendingLinesSupplier == null ? -1 : Math.max(0, pendingLinesSupplier.getAsInt());
        java.util.List<tetris.multiplayer.model.AttackLine> attackLines = attackLinesSupplier == null ? null : attackLinesSupplier.get();

        // 블록 그리기
        for (int r = 0; r < BOARD_ROWS; r++) {
            for (int c = 0; c < BOARD_COLS; c++) {
                int v;
                if (attackLines != null && !attackLines.isEmpty()) {
                    // AttackLine 패턴을 사용하여 정확히 표시 (아래에서부터)
                    int fromBottom = BOARD_ROWS - 1 - r;
                    if (fromBottom < attackLines.size()) {
                        tetris.multiplayer.model.AttackLine line = attackLines.get(fromBottom);
                        // 구멍이면 빈칸, 아니면 채워진 블록
                        v = (c < line.width() && line.isHole(c)) ? 0 : 1;
                    } else {
                        v = 0; // 대기 중인 줄보다 위쪽은 비워둔다
                    }
                } else if (pendingLines >= 0) {
                    // 공급자가 있으면 간단히 "아래에서부터 몇 줄 차있는지"만 시각화한다.
                    int fromBottom = BOARD_ROWS - 1 - r;
                    v = fromBottom < pendingLines ? 1 : 0;
                } else {
                    v = grid[r][c];
                }
                g2.setColor(colorFor(v));
                int px = originX + c * cellSize;
                int py = originY + r * cellSize;
                g2.fillRect(px, py, cellSize, cellSize);
                // cell border
                g2.setColor(GRID_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(px, py, cellSize, cellSize);
            }
        }

        // 상단 라벨
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, cellSize / 2)));
        g2.setColor(Color.WHITE);
        int count = attackLines != null ? attackLines.size() : (pendingLines >= 0 ? pendingLines : 0);
        String label = count > 0 ? "Incoming (" + count + ")" : "Incoming";
        int tw = g2.getFontMetrics().stringWidth(label);
        g2.drawString(label, Math.max(4, (getWidth() - tw) / 2), Math.max(12, originY - 6));

        g2.dispose();
    }

    private Color colorFor(int value) {
        // Only use gray shades: empty -> dark gray, filled -> light gray
        return (value <= 0) ? EMPTY_COLOR : FILLED_COLOR;
    }
}
