package tetris.domain.leaderboard;

import java.util.List;

/**
 * 리더보드 업데이트 결과: 정렬된 엔트리 목록과 새 기록의 강조 인덱스를 함께 반환한다.
 */
public record LeaderboardResult(List<LeaderboardEntry> entries, int highlightIndex) {
}
