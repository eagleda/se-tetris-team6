package tetris.domain.model;

/**
 * 게임의 상위 상태를 표현하는 열거형.
 * 각 상태는 대응되는 {@code GameHandler}를 통해 로직이 실행됩니다.
 */
public enum GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    SETTINGS,
    SCOREBOARD,
    NAME_INPUT
}
