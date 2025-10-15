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

public class Board extends JPanel {
    private static final int WIDTH = 10;
    private static final int HEIGHT = 20;
    public static final char BORDER_CHAR = 'X';
    public static final char BLOCK_CHAR = 'O';
    public static final char EMPTY_CHAR = ' ';

    public JTextPane pane;

    public Block block;
    private int x = 3, y = 0;

    private int[][] board;
    private SimpleAttributeSet styleSet;

    public Board() {

        pane = new JTextPane();
        pane.setEditable(false);
        pane.setPreferredSize(new Dimension(230, 450));
        pane.setBackground(Color.BLACK);
        pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 10),
                BorderFactory.createLineBorder(Color.DARK_GRAY, 5)));

        this.setLayout(new GridBagLayout());
        this.setOpaque(false);
        this.add(pane);

        // Document default style.
        styleSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(styleSet, 18);
        StyleConstants.setFontFamily(styleSet, "Monospaced");
        StyleConstants.setLineSpacing(styleSet, -0.35f);
        StyleConstants.setBold(styleSet, true);
        StyleConstants.setForeground(styleSet, Color.WHITE);
        StyleConstants.setAlignment(styleSet, StyleConstants.ALIGN_CENTER);

        board = new int[HEIGHT][WIDTH];
        block = null;

        drawBoard();
    }

    public void start(Block activeBlock) {
        x = 3;
        y = 0;
        block = activeBlock;
        placeBlock();
        drawBoard();
    }

    // 블록 움직임 입력
    public void moveBlock(int dx, int dy) {
        if (block == null) {
            return;
        }

        int nx = x + dx;
        int ny = y + dy;
        if (nx < 0 || nx + block.width() > WIDTH || ny < 0 || ny + block.height() > HEIGHT) {
            return;
        }
        eraseBlock();
        x = nx;
        y = ny;
        placeBlock();
        drawBoard();
    }

    // 이동 전 블록 지우기
    public void eraseBlock() {
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                int bx = x + i;
                int by = y + j;
                if (by >= 0 && by < HEIGHT && bx >= 0 && bx < WIDTH) {
                    board[by][bx] = 0;
                }
            }
        }
    }

    // 현재 블록 그리기
    public void placeBlock() {
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                int bx = x + i;
                int by = y + j;
                if (by >= 0 && by < HEIGHT && bx >= 0 && bx < WIDTH) {
                    board[by][bx] = block.getShape(i, j);
                }
            }
        }
    }

    // 보드 그리기
    public void drawBoard() {
        StringBuilder sb = new StringBuilder();
        for (int t = 0; t < WIDTH + 2; t++)
            sb.append(BORDER_CHAR);
        sb.append("\n");
        for (int[] board1 : board) {
            sb.append(BORDER_CHAR);
            for (int j = 0; j < board1.length; j++) {
                if (board1[j] == 1) {
                    sb.append(BLOCK_CHAR);
                } else {
                    sb.append(EMPTY_CHAR);
                }
            }
            sb.append(BORDER_CHAR);
            sb.append("\n");
        }
        for (int t = 0; t < WIDTH + 2; t++)
            sb.append(BORDER_CHAR);
        pane.setText(sb.toString());
        StyledDocument doc = pane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), styleSet, false);

        if (block != null) {
            SimpleAttributeSet blockStyles = new SimpleAttributeSet();
            StyleConstants.setForeground(blockStyles, block.getColor());
            int rowStride = WIDTH + 3;
            for (int j = 0; j < block.height(); j++) {
                for (int i = 0; i < block.width(); i++) {
                    if (block.getShape(i, j) == 1) {
                        int r = y + j;
                        int c = x + i;
                        if (r < 0 || r >= HEIGHT || c < 0 || c >= WIDTH)
                            continue;
                        int offset = (r + 1) * rowStride + 1 + c;
                        if (offset >= 0 && offset < doc.getLength()) {
                            doc.setCharacterAttributes(offset, 1, blockStyles, false);
                        }
                    }
                }
            }
        }
        pane.setStyledDocument(doc);
    }
}
