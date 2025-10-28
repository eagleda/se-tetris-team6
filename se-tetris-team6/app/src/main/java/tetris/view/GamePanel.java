package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import tetris.domain.model.Block;
import tetris.view.GameComponents.Board;
import tetris.view.GameComponents.Scoreboard;
import tetris.view.GameComponents.Upcoming;

public class GamePanel extends JPanel implements KeyListener {
    public static GamePanel instance;

    public Board board;
    public Upcoming upcoming;
    public Scoreboard scoreboard;

    public GamePanel() {
        instance = this;

        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.LIGHT_GRAY);
        this.setOpaque(true);
        this.setVisible(false);

        this.setLayout(new BorderLayout());

        board = new Board();
        upcoming = new Upcoming();
        scoreboard = new Scoreboard();

        this.add(board, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel() {
            {
                setLayout(new BorderLayout());
                setOpaque(false);
                upcoming.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                scoreboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                add(upcoming, BorderLayout.NORTH);
                add(scoreboard, BorderLayout.CENTER);
            }
        };
        this.add(rightPanel, BorderLayout.EAST);

        board.drawBoard();

        addKeyListener(this);
    }

    /* 아래 메서드 호출로 블록 이동을 화면에 표시 */

    // 화면에 블록 배치
    public void setActiveBlock(Block activeBlock, int offsetX, int offsetY) {
        board.generateBlock(activeBlock, offsetX, offsetY);
    }

    // 다음 블록 화면에 출력
    public void showNextBlock(Block nextBlock) {
        upcoming.drawBoard(nextBlock);
    }

    // 현재 활성화 블록 고정
    public void fixActiveBlock() {
        board.fixBlock();
    }

    // 블록 한 칸 아래로 이동
    public void moveBlockDown() {
        board.moveBlock(0, 1);
    }

    // 블록 한 칸 좌측 이동
    public void moveBlockLeft() {
        board.moveBlock(-1, 0);
    }

    // 블록 한 칸 우측 이동
    public void moveBlockRight() {
        board.moveBlock(1, 0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Controller.handleKeyPress(e.getKeyCode());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> TetrisFrame.instance.togglePausePanel();
        }

        // 디버깅 (나중에 제거)
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> board.moveBlock(0, -1);
            case KeyEvent.VK_DOWN -> board.moveBlock(0, 1);
            case KeyEvent.VK_LEFT -> board.moveBlock(-1, 0);
            case KeyEvent.VK_RIGHT -> board.moveBlock(1, 0);
            case KeyEvent.VK_SPACE -> board.generateBlock(new Block(), 3, 0);
            case KeyEvent.VK_ENTER -> upcoming.drawBoard(new Block());
        }
        int idx = e.getKeyChar() - '0';
        if (1 <= idx && idx < 10) {
            board.deleteLine(20 - idx);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
