Test Coverage Backlog (Uncovered Main Classes)
==============================================

Grouping by priority batches (10 items each). Classes already covered by tests are excluded. UI/controller are listed later; Jacoco currently excludes controller/view packages unless the Gradle excludes are removed.

1차 (핵심 도메인/로직)
1) [o] tetris.domain.Board
2) [o] tetris.domain.GameModel
3) [o] tetris.domain.engine.GameplayEngine
4) [o] tetris.domain.score.ScoreRuleEngine
5) [o] tetris.domain.RandomBlockGenerator
6) [o] tetris.domain.score.Score
7) [o] tetris.domain.BlockGenerator
8) [o] tetris.domain.BlockKind
9) [o] tetris.domain.ShapeView
10) [o] tetris.domain.block.BlockLike

2차 (설정/서비스/리포지토리)
11) [o] tetris.domain.setting.SettingService
12) [o] tetris.data.setting.PreferencesSettingRepository
13) [o] tetris.data.leaderboard.PreferencesLeaderboardRepository
14) [o] tetris.data.score.InMemoryScoreRepository
15) [o] tetris.domain.leaderboard.LeaderboardEntry
16) [o] tetris.domain.leaderboard.LeaderboardRepository
17) [o] tetris.domain.leaderboard.LeaderboardResult
18) [o] tetris.domain.setting.SettingRepository
19) [o] tetris.domain.score.ScoreRepository
20) [o] tetris.infrastructure.GameModelFactory

3차 (도메인 핸들러/게임 흐름)
21) [o] tetris.domain.handler.AbstractGameHandler
22) [x] tetris.domain.handler.GameHandler
23) [o] tetris.domain.handler.GameOverHandler
24) [o] tetris.domain.handler.GamePlayHandler
25) [o] tetris.domain.handler.MenuHandler
26) [o] tetris.domain.handler.NameInputHandler
27) [o] tetris.domain.handler.PausedHandler
28) [o] tetris.domain.handler.ScoreboardHandler
29) [o] tetris.domain.handler.SettingsHandler
30) [x] tetris.domain.GameMode

4차 (아이템/행동)
31) [o] tetris.domain.item.ItemBehavior
32) [o] tetris.domain.item.ItemContext
33) [o] tetris.domain.item.ItemContextImpl
34) [o] tetris.domain.item.ItemType
35) [o] tetris.domain.item.behavior.BombBehavior
36) [o] tetris.domain.item.behavior.DoubleScoreBehavior
37) [o] tetris.domain.item.behavior.LineClearBehavior
38) [o] tetris.domain.item.behavior.TimeSlowBehavior
39) [o] tetris.domain.item.behavior.WeightBehavior
40) [o] tetris.domain.item.ItemManager

5차 (모델/입력 상태)
41) [o] tetris.domain.model.Block
42) [o] tetris.domain.model.GameClock
43) [o] tetris.domain.model.GameState
44) [o] tetris.domain.model.InputState
45) [o] tetris.domain.GameDifficulty
46) [o] tetris.domain.GameModel.UiBridge
47) [o] tetris.domain.GameModel.MultiplayerHook
48) [o] tetris.domain.BlockShape
49) [o] tetris.domain.setting.Setting
50) [o] tetris.domain.item.model.ItemBlockModel

6차 (멀티플레이 모델)
51) [x] tetris.multiplayer.model.AttackLine
52) [x] tetris.multiplayer.model.Cell
53) [x] tetris.multiplayer.model.LockedPieceSnapshot
54) [x] tetris.multiplayer.model.MultiPlayerGame
55) [x] tetris.multiplayer.model.PendingAttackBuffer
56) [x] tetris.multiplayer.model.PlayerState
57) [x] tetris.multiplayer.model.VersusRules
58) [x] tetris.multiplayer.session.LocalMultiplayerSession
59) [x] tetris.multiplayer.session.MultiplayerSessionFactory
60) [x] tetris.multiplayer.session.NetworkMultiplayerSession

7차 (멀티플레이 컨트롤러/핸들러)
61) [x] tetris.multiplayer.controller.LocalMultiPlayerController
62) [x] tetris.multiplayer.controller.NetworkMultiPlayerController
63) [x] tetris.multiplayer.handler.LocalMultiplayerHandler
64) [x] tetris.multiplayer.handler.MultiplayerHandler
65) [x] tetris.multiplayer.handler.NetworkedMultiplayerHandler
66) [x] tetris.multiplayer.controller (기타)
67) [x] tetris.multiplayer.handler (기타)
68) [x] tetris.multiplayer.session (기타)
69) [x] tetris.multiplayer.model (중복 제외 기타)
70) [x] tetris.infrastructure (기타)

8차 (네트워크 프로토콜)
71) [x] tetris.network.protocol.AttackLine
72) [o] tetris.network.protocol.GameMessage
73) [x] tetris.network.protocol.GameSnapshot
74) [x] tetris.network.protocol.InputType
75) [x] tetris.network.protocol.MessageType
76) [x] tetris.network.protocol.NetworkProtocol
77) [x] tetris.network.protocol.PlayerInput
78) [o] tetris.network.protocol.GameMessage 직렬화/동등성
79) [x] tetris.network.protocol.AttackLine 직렬화
80) [x] tetris.network.protocol (기타 보조 타입)

9차 (네트워크/컨커런시 인프라)
81) [x] tetris.network.NetworkManager
82) [x] tetris.network.NetworkMode
83) [x] tetris.network.GameDataListener
84) [x] tetris.network.GameEventListener
85) [x] tetris.network.INetworkThreadCallback
86) [x] tetris.network.NetworkEventListener
87) [x] tetris.concurrent.GameEvent
88) [x] tetris.concurrent.GameEventListener
89) [x] tetris.concurrent.GameThread
90) [x] tetris.concurrent.NetworkThread

10차 (네트워크 서버/클라이언트)
91) [x] tetris.network.client.GameClient
92) [x] tetris.network.client.ClientHandler
93) [x] tetris.network.client.GameStateListener
94) [x] tetris.network.server.GameServer
95) [x] tetris.network.server.ServerHandler
96) [x] tetris.network.server.ServerStatus
97) [x] tetris.concurrent.NetworkManager
98) [x] tetris.concurrent.NetworkStats
99) [x] tetris.concurrent.NetworkThread (동기 메서드)
100) [x] tetris.network (기타 동기 메서드 대상)

11차 (컨트롤러/UI 로직 부분 — Jacoco 기본 exclude)
101) [o] tetris.controller.GameController
102) [o] tetris.controller.GameOverController
103) [o] tetris.controller.ScoreController
104) [o] tetris.controller.SettingController
105) [x] tetris.controller (기타)
106) [o] tetris.view.ScoreboardPanel
107) [x] tetris.view.SettingPanel
108) [x] tetris.view.NetworkStatusOverlay
109) [x] tetris.view.ScoreView
110) [x] tetris.view.palette.ColorPaletteProvider

12차 (나머지 UI/엔트리 — 필요 시)
111) [o] tetris.App
112) [x] tetris.view.MainPanel
113) [x] tetris.view.GameComponent.AttackQueuePanel
114) [x] tetris.view.GameComponent.GameOverPanel
115) [x] tetris.view.GameComponent.GamePanel
116) [x] tetris.view.GameComponent.GamePanelImpl
117) [x] tetris.view.GameComponent.LocalPlayerPanel
118) [x] tetris.view.GameComponent.MultiGameLayout
119) [x] tetris.view.GameComponent.NetworkMultiGameLayout
120) [x] tetris.view.GameComponent.RemotePlayerPanel
