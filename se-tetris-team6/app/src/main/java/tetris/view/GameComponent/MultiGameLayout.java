package tetris.view.GameComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import tetris.domain.GameModel;

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

        // 중앙 Timer
        private TimerPanel timerPanel;

        public MultiGameLayout() {
                super(new GridBagLayout());
                setOpaque(true);
                setVisible(false);

                // 각 요소 객체 생성 및 배치
                gamePanel_1 = new GamePanel();
                addToRegion(gamePanel_1, 0, 0, 6, 11, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
                nextBlockPanel_1 = new NextBlockPanel();
                addToRegion(nextBlockPanel_1, 6, 0, 2, 3, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
                scoreboard_1 = new ScorePanel();
                addToRegion(scoreboard_1, 6, 3, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);      
                attackQueuePanel_1 = new AttackQueuePanel();
                addToRegion(attackQueuePanel_1, 6, 5, 2, 6, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
                
                gamePanel_2 = new GamePanel();
                addToRegion(gamePanel_2, 11, 0, 6, 11, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
                nextBlockPanel_2 = new NextBlockPanel();        
                addToRegion(nextBlockPanel_2, 8, 0, 2, 3, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
                scoreboard_2 = new ScorePanel();
                addToRegion(scoreboard_2, 8, 3, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
                attackQueuePanel_2 = new AttackQueuePanel();
                addToRegion(attackQueuePanel_2, 8, 5, 2, 6, GridBagConstraints.BOTH, GridBagConstraints.CENTER);

                timerPanel = new TimerPanel();
                addToRegion(timerPanel, 6, 4, 4, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);

                revalidate();
                repaint();
        }

        /**
         * Show the central timer panel.
         */
        public void showTimer() {
                setTimerVisible(true);
        }

        /**
         * Hide the central timer panel.
         */
        public void hideTimer() {
                setTimerVisible(false);
        }

        /**
         * Set visibility of the central timer panel.
         */
        public void setTimerVisible(boolean visible) {
                if (timerPanel == null)
                        return;
                timerPanel.setVisible(visible);
                revalidate();
                repaint();
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
                timerPanel.bindGameModel(model);
                repaint();
        }

        public void addToRegion(Component comp, int x, int y, int w, int h, int fill, int anchor) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = x;
                gbc.gridy = y;
                gbc.gridwidth = w;
                gbc.gridheight = h;

                gbc.weightx = gbc.gridwidth;
                gbc.weighty = gbc.gridheight;

                gbc.fill = fill;
                gbc.anchor = anchor;
                gbc.insets = new Insets(12, 12, 12, 12);
                gbc.ipadx = 5;
                gbc.ipady = 5;

                comp.setVisible(true);
                this.add(comp, gbc);
        }
}