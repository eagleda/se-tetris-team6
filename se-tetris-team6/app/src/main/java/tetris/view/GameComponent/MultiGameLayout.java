package tetris.view.GameComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import tetris.domain.GameModel;
import tetris.view.EmptySpace;
import tetris.view.GameComponent.GamePanel;

public class MultiGameLayout extends JPanel {
    // 플레이어 1
    private GamePanel gamePanel_1;
    private NextBlockPanel nextBlockPanel_1;
    private ScorePanel scoreboard_1;
    private AttackQueuePanel attackQueuePanel_1;

    // 플레이어 2
    private GamePanel gamePanel_2;
    private NextBlockPanel nextBlockPanel_2;
    private ScorePanel scoreboard_2;
    private AttackQueuePanel attackQueuePanel_2;

    public MultiGameLayout() {
        super(new GridBagLayout());
        setOpaque(true);
        setVisible(false);

        // Left column (player 1)
        JPanel leftColumn = new JPanel(new GridBagLayout());
        leftColumn.setOpaque(false);

        gamePanel_1 = new GamePanel();
        nextBlockPanel_1 = new NextBlockPanel();
        scoreboard_1 = new ScorePanel();
        attackQueuePanel_1 = new AttackQueuePanel();

        // arrange similar to SingleGameLayout: game area on left, controls to the right
        addToRegionToParent(leftColumn, gamePanel_1, 0, 0, 4, 4, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        addToRegionToParent(leftColumn, nextBlockPanel_1, 4, 0, 1, 1, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER);
        addToRegionToParent(leftColumn, scoreboard_1, 4, 1, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.NORTH);
        addToRegionToParent(leftColumn, new EmptySpace(), 4, 2, 1, 1, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER);
        // attack queue below the scoreboard
        addToRegionToParent(leftColumn, attackQueuePanel_1, 4, 3, 1, 1, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER);

        gamePanel_1.setVisible(true);
        nextBlockPanel_1.setVisible(true);
        scoreboard_1.setVisible(true);
        attackQueuePanel_1.setVisible(true);

        // Right column (player 2) - mirrored horizontally by setting RIGHT_TO_LEFT
        // orientation
        JPanel rightColumn = new JPanel(new GridBagLayout());
        rightColumn.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        rightColumn.setOpaque(false);

        gamePanel_2 = new GamePanel();
        nextBlockPanel_2 = new NextBlockPanel();
        scoreboard_2 = new ScorePanel();
        attackQueuePanel_2 = new AttackQueuePanel();

        addToRegionToParent(rightColumn, gamePanel_2, 0, 0, 4, 4, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        addToRegionToParent(rightColumn, nextBlockPanel_2, 4, 0, 1, 1, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER);
        addToRegionToParent(rightColumn, scoreboard_2, 4, 1, 1, 1, GridBagConstraints.HORIZONTAL,
                GridBagConstraints.NORTH);
        addToRegionToParent(rightColumn, new EmptySpace(), 4, 2, 1, 1, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER);
        addToRegionToParent(rightColumn, attackQueuePanel_2, 4, 3, 1, 1, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER);

        gamePanel_2.setVisible(true);
        nextBlockPanel_2.setVisible(true);
        scoreboard_2.setVisible(true);
        attackQueuePanel_2.setVisible(true);

        // Add left and right columns to this panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        // increase right inset to add horizontal gap between columns
        gbc.insets = new Insets(4, 4, 4, 24);
        this.add(leftColumn, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        // increase left inset to match the gap
        gbc.insets = new Insets(4, 24, 4, 4);
        this.add(rightColumn, gbc);

        revalidate();
        repaint();
    }

    private void addToRegionToParent(JPanel parent, Component comp, int x, int y, int w, int h, int fill,
            int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;

        gbc.weightx = gbc.gridwidth;
        gbc.weighty = gbc.gridheight;

        gbc.fill = fill;
        gbc.anchor = anchor;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.ipadx = 5;
        gbc.ipady = 5;

        parent.add(comp, gbc);
    }

    public void bindGameModel(GameModel model) {
        gamePanel_1.bindGameModel(model);
        gamePanel_2.bindGameModel(model);
        nextBlockPanel_1.bindGameModel(model);
        nextBlockPanel_2.bindGameModel(model);
        scoreboard_1.bindGameModel(model);
        scoreboard_2.bindGameModel(model);
        attackQueuePanel_1.bindGameModel(model);
        attackQueuePanel_2.bindGameModel(model);
        repaint();
    }
}