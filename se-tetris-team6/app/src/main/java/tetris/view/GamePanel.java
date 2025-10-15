package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import tetris.model.Block;
import tetris.view.GameComponents.Board;
import tetris.view.GameComponents.Scoreboard;
import tetris.view.GameComponents.Upcoming;

public class GamePanel extends JPanel implements KeyListener {
    public Board board;
    public Upcoming upcoming;
    public Scoreboard scoreboard;

    public GamePanel() {
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

        addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> board.moveBlock(0, -1);
            case KeyEvent.VK_DOWN -> board.moveBlock(0, 1);
            case KeyEvent.VK_LEFT -> board.moveBlock(-1, 0);
            case KeyEvent.VK_RIGHT -> board.moveBlock(1, 0);
            case KeyEvent.VK_SPACE -> board.start(new Block());
            case KeyEvent.VK_ENTER -> upcoming.drawBoard(new Block());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
