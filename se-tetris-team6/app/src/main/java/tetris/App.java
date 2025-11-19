package tetris;

import tetris.domain.GameModel;
import tetris.infrastructure.GameModelFactory;
import tetris.view.TetrisFrame;

public class App {

    public static void main(String[] args) {
        GameModel gameModel = GameModelFactory.createDefault();
        new TetrisFrame(gameModel);
    }
}
