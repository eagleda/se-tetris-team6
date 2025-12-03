Test Coverage Backlog (Uncovered Main Classes)
==============================================

Grouping by priority batches (10 items each). Classes already covered by tests are excluded. UI/controller are listed later; Jacoco currently excludes controller/view packages unless the Gradle excludes are removed.

1차 (핵심 도메인/로직)
1) tetris.domain.Board
2) tetris.domain.GameModel
3) tetris.domain.engine.GameplayEngine
4) tetris.domain.score.ScoreRuleEngine
5) tetris.domain.RandomBlockGenerator
6) tetris.domain.score.Score
7) tetris.domain.BlockGenerator
8) tetris.domain.BlockKind
9) tetris.domain.ShapeView
10) tetris.domain.block.BlockLike

2차 (설정/서비스/리포지토리)
11) tetris.domain.setting.SettingService
12) tetris.data.setting.PreferencesSettingRepository
13) tetris.data.leaderboard.PreferencesLeaderboardRepository
14) tetris.data.score.InMemoryScoreRepository
15) tetris.domain.leaderboard.LeaderboardEntry
16) tetris.domain.leaderboard.LeaderboardRepository
17) tetris.domain.leaderboard.LeaderboardResult
18) tetris.domain.setting.SettingRepository
19) tetris.domain.score.ScoreRepository
20) tetris.infrastructure.GameModelFactory

3차 (도메인 핸들러/게임 흐름)
21) tetris.domain.handler.AbstractGameHandler
22) tetris.domain.handler.GameHandler
23) tetris.domain.handler.GameOverHandler
24) tetris.domain.handler.GamePlayHandler
25) tetris.domain.handler.MenuHandler
26) tetris.domain.handler.NameInputHandler
27) tetris.domain.handler.PausedHandler
28) tetris.domain.handler.ScoreboardHandler
29) tetris.domain.handler.SettingsHandler
30) tetris.domain.GameMode

4차 (아이템/행동)
31) tetris.domain.item.ItemBehavior
32) tetris.domain.item.ItemContext
33) tetris.domain.item.ItemContextImpl
34) tetris.domain.item.ItemType
35) tetris.domain.item.behavior.BombBehavior
36) tetris.domain.item.behavior.DoubleScoreBehavior
37) tetris.domain.item.behavior.LineClearBehavior
38) tetris.domain.item.behavior.TimeSlowBehavior
39) tetris.domain.item.behavior.WeightBehavior
40) tetris.domain.item.ItemManager

5차 (모델/입력 상태)
41) tetris.domain.model.Block
42) tetris.domain.model.GameClock
43) tetris.domain.model.GameState
44) tetris.domain.model.InputState
45) tetris.domain.GameDifficulty
46) tetris.domain.GameModel.UiBridge
47) tetris.domain.GameModel.MultiplayerHook
48) tetris.domain.BlockShape
49) tetris.domain.setting.Setting
50) tetris.domain.item.model.ItemBlockModel

6차 (멀티플레이 모델)
51) tetris.multiplayer.model.AttackLine
52) tetris.multiplayer.model.Cell
53) tetris.multiplayer.model.LockedPieceSnapshot
54) tetris.multiplayer.model.MultiPlayerGame
55) tetris.multiplayer.model.PendingAttackBuffer
56) tetris.multiplayer.model.PlayerState
57) tetris.multiplayer.model.VersusRules
58) tetris.multiplayer.session.LocalMultiplayerSession
59) tetris.multiplayer.session.MultiplayerSessionFactory
60) tetris.multiplayer.session.NetworkMultiplayerSession

7차 (멀티플레이 컨트롤러/핸들러)
61) tetris.multiplayer.controller.LocalMultiPlayerController
62) tetris.multiplayer.controller.NetworkMultiPlayerController
63) tetris.multiplayer.handler.LocalMultiplayerHandler
64) tetris.multiplayer.handler.MultiplayerHandler
65) tetris.multiplayer.handler.NetworkedMultiplayerHandler
66) tetris.multiplayer.controller (기타)
67) tetris.multiplayer.handler (기타)
68) tetris.multiplayer.session (기타)
69) tetris.multiplayer.model (중복 제외 기타)
70) tetris.infrastructure (기타)

8차 (네트워크 프로토콜)
71) tetris.network.protocol.AttackLine
72) tetris.network.protocol.GameMessage
73) tetris.network.protocol.GameSnapshot
74) tetris.network.protocol.InputType
75) tetris.network.protocol.MessageType
76) tetris.network.protocol.NetworkProtocol
77) tetris.network.protocol.PlayerInput
78) tetris.network.protocol.GameMessage 직렬화/동등성
79) tetris.network.protocol.AttackLine 직렬화
80) tetris.network.protocol (기타 보조 타입)

9차 (네트워크/컨커런시 인프라)
81) tetris.network.NetworkManager
82) tetris.network.NetworkMode
83) tetris.network.GameDataListener
84) tetris.network.GameEventListener
85) tetris.network.INetworkThreadCallback
86) tetris.network.NetworkEventListener
87) tetris.concurrent.GameEvent
88) tetris.concurrent.GameEventListener
89) tetris.concurrent.GameThread
90) tetris.concurrent.NetworkThread

10차 (네트워크 서버/클라이언트)
91) tetris.network.client.GameClient
92) tetris.network.client.ClientHandler
93) tetris.network.client.GameStateListener
94) tetris.network.server.GameServer
95) tetris.network.server.ServerHandler
96) tetris.network.server.ServerStatus
97) tetris.concurrent.NetworkManager
98) tetris.concurrent.NetworkStats
99) tetris.concurrent.NetworkThread (동기 메서드)
100) tetris.network (기타 동기 메서드 대상)

11차 (컨트롤러/UI 로직 부분 — Jacoco 기본 exclude)
101) tetris.controller.GameController
102) tetris.controller.GameOverController
103) tetris.controller.ScoreController
104) tetris.controller.SettingController
105) tetris.controller (기타)
106) tetris.view.ScoreboardPanel
107) tetris.view.SettingPanel
108) tetris.view.NetworkStatusOverlay
109) tetris.view.ScoreView
110) tetris.view.palette.ColorPaletteProvider

12차 (나머지 UI/엔트리 — 필요 시)
111) tetris.App
112) tetris.view.MainPanel
113) tetris.view.GameComponent.AttackQueuePanel
114) tetris.view.GameComponent.GameOverPanel
115) tetris.view.GameComponent.GamePanel
116) tetris.view.GameComponent.GamePanelImpl
117) tetris.view.GameComponent.LocalPlayerPanel
118) tetris.view.GameComponent.MultiGameLayout
119) tetris.view.GameComponent.NetworkMultiGameLayout
120) tetris.view.GameComponent.RemotePlayerPanel
