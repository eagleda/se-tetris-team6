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

    private static final char BORDER_CHAR = 'X';
    private static final char BLOCK_CHAR = 'O';
    private static final char EMPTY_CHAR = ' ';
    private static final char ITEM_CHAR = 'I';

    public JTextPane pane;

    public Block block;
    private int x = 3, y = 0;

    private int[][] board;
    private Color[][] colorBoard; // 각 셀의 색상 정보 저장
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
        colorBoard = new Color[HEIGHT][WIDTH]; // 색상 배열 초기화
        block = null;
    }

    public void start(Block activeBlock, int offsetX, int offsetY) {
        fixBlock();
        x = offsetX;
        y = offsetY;
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

    // 이동 전 블록 지우기 (임시 배치된 것만 지움)
    public void eraseBlock() {
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                int bx = x + i;
                int by = y + j;
                if (by >= 0 && by < HEIGHT && bx >= 0 && bx < WIDTH) {
                    // 현재 움직이는 블록만 지우고, 고정된 블록은 유지
                    if (board[by][bx] == 1 && colorBoard[by][bx] == null) {
                        board[by][bx] = 0;
                    }
                }
            }
        }
    }

    // 현재 블록 그리기 (임시 배치, 색상은 저장 안 함)
    public void placeBlock() {
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                int bx = x + i;
                int by = y + j;
                if (by >= 0 && by < HEIGHT && bx >= 0 && bx < WIDTH) {
                    if (block.getShape(i, j) == 1) {
                        board[by][bx] = 1;
                        // 임시 배치는 색상 저장 안 함 (null 유지)
                    }
                }
            }
        }
    }

    // 블록을 보드에 고정 (색상 저장)
    public void fixBlock() {
        if (block == null)
            return;
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                int bx = x + i;
                int by = y + j;
                if (by >= 0 && by < HEIGHT && bx >= 0 && bx < WIDTH) {
                    if (block.getShape(i, j) == 1) {
                        board[by][bx] = 1;
                        colorBoard[by][bx] = block.getColor(); // 색상 저장
                    }
                }
            }
        }
        block = null; // 블록 고정 후 null로 설정
        drawBoard();
    }

    public void deleteLine(int height) {
        if (height < 0 || height >= HEIGHT)
            return;

        // 보드 데이터 이동: 지정한 행을 지우고 위쪽을 아래로 당김
        for (int r = height; r > 0; r--) {
            System.arraycopy(board[r - 1], 0, board[r], 0, WIDTH);
            System.arraycopy(colorBoard[r - 1], 0, colorBoard[r], 0, WIDTH); // 색상도 이동
        }
        for (int c = 0; c < WIDTH; c++) {
            board[0][c] = 0;
            colorBoard[0][c] = null;
        }

        drawBoard();
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

        int rowStride = WIDTH + 3;

        // 1. 고정된 블록 색상 적용 (colorBoard에 색상 정보 있음)
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                if (board[r][c] == 1 && colorBoard[r][c] != null) {
                    int offset = (r + 1) * rowStride + 1 + c;
                    if (offset >= 0 && offset < doc.getLength()) {
                        SimpleAttributeSet fixedStyle = new SimpleAttributeSet();
                        StyleConstants.setForeground(fixedStyle, colorBoard[r][c]);
                        doc.setCharacterAttributes(offset, 1, fixedStyle, false);
                    }
                }
            }
        }

        // 2. 현재 움직이는 블록 색상 적용
        if (block != null) {
            SimpleAttributeSet blockStyles = new SimpleAttributeSet();
            StyleConstants.setForeground(blockStyles, block.getColor());
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
