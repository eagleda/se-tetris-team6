package tetris.view.GameComponents;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import tetris.model.Block;

public class Upcoming extends JPanel {
    private static final int WIDTH = 5;
    private static final int HEIGHT = 5;
    private static final char BLOCK_CHAR = 'O';
    private static final char EMPTY_CHAR = ' ';
    private final int[][] board;
    private final SimpleAttributeSet styleSet;

    public JTextPane pane;

    public Upcoming() {
        pane = new JTextPane();
        pane.setEditable(false);
        pane.setPreferredSize(new Dimension(120, 120));
        pane.setBackground(Color.BLACK);
        pane.setOpaque(true);
        pane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        this.setLayout(new GridBagLayout());
        this.setOpaque(false);
        this.add(pane);

        styleSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(styleSet, 18);
        StyleConstants.setFontFamily(styleSet, "Monospaced");
        StyleConstants.setLineSpacing(styleSet, -0.35f);
        StyleConstants.setBold(styleSet, true);
        StyleConstants.setForeground(styleSet, Color.WHITE);
        StyleConstants.setAlignment(styleSet, StyleConstants.ALIGN_CENTER);

        board = new int[HEIGHT][WIDTH];
        clearBoard();
    }

    // board에 다음 블록 그리기
    public void drawBoard(Block upcomingBlock) {
        clearBoard();

        if (upcomingBlock != null) {
            int bw = upcomingBlock.width();
            int bh = upcomingBlock.height();
            int startX = (WIDTH - bw) / 2;
            int startY = (HEIGHT - bh) / 2;

            for (int j = 0; j < bh; j++) {
                for (int i = 0; i < bw; i++) {
                    if (upcomingBlock.getShape(i, j) == 1) {
                        int bx = startX + i;
                        int by = startY + j;
                        if (by >= 0 && by < HEIGHT && bx >= 0 && bx < WIDTH) {
                            board[by][bx] = 1;
                        }
                    }
                }
            }
        }

        // 텍스트로 변환
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                sb.append(board[r][c] == 1 ? BLOCK_CHAR : EMPTY_CHAR);
            }
            if (r < HEIGHT - 1)
                sb.append('\n');
        }
        pane.setText(sb.toString());

        // 스타일 적용
        StyledDocument doc = pane.getStyledDocument();
        doc.setCharacterAttributes(0, doc.getLength(), styleSet, true);

        if (upcomingBlock != null) {
            SimpleAttributeSet blockStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(blockStyle, upcomingBlock.getColor());

            int rowStride = WIDTH + 1; // 각 행 뒤의 '\n' 포함
            for (int r = 0; r < HEIGHT; r++) {
                for (int c = 0; c < WIDTH; c++) {
                    if (board[r][c] == 1) {
                        int offset = r * rowStride + c;
                        if (offset >= 0 && offset < doc.getLength()) {
                            doc.setCharacterAttributes(offset, 1, blockStyle, false);
                        }
                    }
                }
            }
        }

        pane.revalidate();
        pane.repaint();
    }

    // board 초기화
    private void clearBoard() {
        for (int r = 0; r < HEIGHT; r++)
            for (int c = 0; c < WIDTH; c++)
                board[r][c] = 0;
    }
}