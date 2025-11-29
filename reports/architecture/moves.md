# 파일 이동 및 리네임 계획

## 목표 패키지 레이아웃
```
src/main/java/
  domain/
    model/
    item/
    setting/
  application/
    usecase/
    port/
  presentation/
    controller/
    view/
    viewmodel/
  infrastructure/
    persistence/
    preference/
    time/
```

## 세부 이동 계획

| 기존 파일 | 조치 | 대상 위치 | 후속 작업 |
|---|---|---|---|
| se-tetris-team6/app/src/main/java/tetris/domain/GameModel.java | `domain/model` 아래에 유지하되 저장소/뷰 연결 로직 제거 | domain/model/GameModel.java | `application.port.ScoreRepositoryPort`, `SettingPort` 등 새 포트를 통해 주입 |
| se-tetris-team6/app/src/main/java/tetris/domain/model/GameClock.java | `application.port.GameClockPort` 구현체로 `infrastructure/time/SwingGameClockAdapter.java`로 이동 | infrastructure/time/SwingGameClockAdapter.java | 도메인에서는 `GameClock` 인터페이스만 의존 |
| se-tetris-team6/app/src/main/java/tetris/domain/setting/Setting.java | 값 객체로 유지하고 `KeyEvent` 기본값은 `application/port/KeyBindingPort`로 이동 | domain/setting/Setting.java | 애플리케이션 계층이 주입 어댑터로 기본값 채움 |
| se-tetris-team6/app/src/main/java/tetris/controller/GameController.java | `presentation/controller/GameInputController`와 `application/usecase` 유스케이스로 분리 | presentation/controller/GameInputController.java | `HandleGameplayInput`, `PauseGame`, `NavigateMenu` 유스케이스 추가 |
| se-tetris-team6/app/src/main/java/tetris/view/** | `presentation/view/**`로 이동하고 `GameModel` 직접 의존 제거 | presentation/view/** | 뷰는 `presentation.viewmodel.*` 스냅샷만 구독 |
| se-tetris-team6/app/src/main/java/tetris/data/** | `infrastructure/persistence/**`로 이동해 애플리케이션 포트 구현 | infrastructure/persistence/** | 컴포지션 루트에서 어댑터 등록 |
| se-tetris-team6/app/src/main/java/tetris/App.java | 루트의 `Main.java`로 리네임 후 컴포지션 루트 역할 담당 | src/main/java/Main.java | 애플리케이션 서비스를 생성 후 컨트롤러→뷰 연결 |
| se-tetris-team6/app/src/main/java/tetris/util/** | 뷰 전용 유틸이면 제거하거나 프레젠테이션 인접 위치로 이동 | presentation/view/util/** | 도메인이 AWT 기반 util을 참조하지 않도록 함 |

## 예상 변경 흐름

1. `build.gradle.kts`의 source set을 새 패키지 경로로 갱신한다.
2. 각 자바 파일의 `package` 선언을 이동 경로와 일치시키고 import를 정리한다.
3. `application.port`에 `ScoreRepositoryPort`, `ClockPort` 등 인터페이스를 생성한다.
4. 기존 `new PreferencesSettingRepository()` 호출을 모두 컴포지션 루트에서 주입하도록 바꾼다.
5. 컨트롤러가 DTO/뷰모델을 생성해 뷰에 전달하도록 수정한다.
