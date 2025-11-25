package tetris.controller;

import java.util.Objects;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import java.util.List;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.view.GameComponent.GameOverPanel;
import tetris.view.TetrisFrame;

/** Controller for Game Over flow: shows overlay and persists name to leaderboard. */
public final class GameOverController {

    private final ScoreRepository scoreRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final GameOverPanel panel;
    private final TetrisFrame frame;

    public GameOverController(ScoreRepository scoreRepository,
            LeaderboardRepository leaderboardRepository,
            GameOverPanel panel,
            TetrisFrame frame) {
        this.scoreRepository = Objects.requireNonNull(scoreRepository);
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
        this.panel = Objects.requireNonNull(panel);
        this.frame = Objects.requireNonNull(frame);

        panel.setListener(new GameOverPanel.Listener() {
            @Override
            public void onSave(String name) {
                submitName(name);
            }

            @Override
            public void onSkip() {
                hideAndShowScoreboard();
            }

            @Override
            public void onBackToMenu() {
                panel.hidePanel();
                frame.showMainPanel();
            }
        });
    }

    public void show(Score score, boolean allowNameEntry) {
        GameMode mode = frame.getGameModel().getLastMode();
        List<LeaderboardEntry> entries = leaderboardRepository.loadTop(10, mode);
        panel.renderLeaderboard(mode, entries);
        panel.show(score, allowNameEntry);
    }

    private void submitName(String name) {
        if (name == null || name.isBlank()) {
            // ignore empty names; fallback to skip
            hideAndShowScoreboard();
            return;
        }
        var score = scoreRepository.load();
        GameMode mode = frame.getGameModel().getLastMode();
        var entry = new LeaderboardEntry(name.trim(), score.getPoints(), mode);
        leaderboardRepository.saveEntry(entry);
        // after saving, update only the GameOverPanel's left leaderboard (do not change overlay)
        var entries = leaderboardRepository.loadTop(10, mode);
        panel.updateLeaderboardModel(mode, entries);
        LeaderboardResult result = leaderboardRepository.saveAndHighlight(entry);
        frame.setPendingLeaderboard(mode, result);
        hideAndShowScoreboard();
    }

    private void hideAndShowScoreboard() {
        panel.hidePanel();
        // instruct frame to show scoreboard panel so player can see updated leaderboard
        frame.showScoreboardPanel();
    }
}
