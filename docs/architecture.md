# Roadio 아키텍처

## 1. 전체 흐름

```text
[Location]  ──▶  RoutePath.project()  ──▶  RouteProjection
(FusedLocation / Simulation)                 │ distanceAlongM, lateralM, progress
                                             ▼
                                   DocentPlanner.next()
                                   ├─ lead / trail window
                                   ├─ one story at a time, quiet gap after it
                                   ├─ once per drive
                                   └─ priority, then the window that ends first
                                             │ DocentStory?
                                             ▼
                                   DocentViewModel.tell()
                                             │
                              ┌──────────────┴──────────────┐
                              ▼                             ▼
                   FocusedDocentSpeaker               Compose UI
                   (audio focus)                 (story, next, progress)
                              │
                   PleosDocentSpeaker ── 없으면 ──▶ AndroidDocentSpeaker
                   (Pleos TTS SDK)                  (Android TTS)
                              │ onDone
                              ▼
                   DocentPlanner.storyEnded()
```

핵심 원칙: **위치 소스와 음성 엔진을 인터페이스로 분리**한다. 음성은 Pleos TTS SDK를 쓰고, 위치는 지금 시뮬레이션 주행이며 Pleos FusedLocation/NaviHelper로 교체한다.

## 2. 모듈

| 파일 | 책임 | 테스트 |
|---|---|---|
| `core/Geo.kt` | 하버사인 거리, 세그먼트 정투영 | `GeoTest` |
| `core/RoutePath.kt` | 경로 세그먼트, 누적 거리, `pointAt`, `project` | `GeoTest` |
| `core/DocentStory.kt` | 이야기 데이터 모델 | - |
| `core/DocentPlanner.kt` | 트리거 구간, 한 번에 하나, 조용한 구간, 한 주행에 한 번, 우선순위, 다음 이야기 | `DocentPlannerTest` |
| `drive/SimulatedRouteDriver.kt` | 일정 속도로 경로 이동 | `SimulatedRouteDriverTest` |
| `audio/PleosDocentSpeaker.kt` | Pleos TextToSpeech SDK(온디바이스), 없으면 Android TTS로 넘김 | 에뮬레이터 |
| `audio/DocentSpeaker.kt` | 음성 인터페이스, 공통 AudioAttributes, Android TTS | 수동 |
| `audio/FocusedDocentSpeaker.kt` | 이야기마다 오디오 포커스, 중단·재개·종료, 끝 알림 시간 제한 | 에뮬레이터 |
| `car/DrivingRestrictions.kt` | 주행 제한(CarUxRestrictions) 구독, 본문 표시 여부 | 에뮬레이터 |
| `DocentViewModel.kt` | 250ms 틱, 화면 상태, 이야기 멈춤/다시 듣기/재시작, 다시 들려줄지 판단 | 수동 |
| `ui/RoadioScreen.kt` | 단일 화면(진행률, 이야기, 다음 이야기, 버튼) | 수동 |
| `data/DemoRoute.kt` | 광화문→시청→서울역→남산 데모 경로/7개 이야기(장소 좌표: OpenStreetMap) | `DemoRouteTest` |

## 3. 경로 투영

`RoutePath.project(position)`:

1. 각 세그먼트에 대해 점을 선분에 정투영한다.
2. 투영 파라미터 `t ∈ [0, 1]`로 누적 거리 `distanceAlongM`을 구한다.
3. 직선 거리 `lateralM`이 가장 작은 세그먼트를 선택한다.
4. `progress = distanceAlongM / totalDistanceM`을 계산한다.

로컬 평면 근사(위도 보정)로 계산하므로 도심 경로에서 충분한 정확도를 가진다.

## 4. 트리거 정책

```kotlin
data class TriggerConfig(
    val leadM: Double = 400.0,
    val minGapM: Double = 100.0,   // 앞 이야기가 끝난 뒤 조용히 달리는 거리
    val maxLateralM: Double = 150.0,
)
```

조건:

```text
playable =
    !telling                                   // 이야기가 나오는 중이 아님 (다시 듣기 포함)
    && current - endedAt(last story) >= minGapM
    && current >= storyAlong - leadM
    && current <= storyAlong + story.triggerRadiusM
    && story not played in this drive
    && story.lateralM <= maxLateralM
```

선택: `priority` 내림차순 → 구간 끝(`storyAlong + triggerRadiusM`)이 가까운 순 → 현재 위치와의 거리 순. 구간이 먼저 끝나는 이야기를 먼저 들려줘야 뒤 이야기 때문에 앞 이야기를 놓치지 않는다.

`next()`가 이야기를 고르면 `telling`이 되고, 음성이 끝나면 ViewModel이 `storyEnded(projection)`을 부른다. 다시 듣기는 `storyStarted()`로 같은 규칙을 따른다. 음성 엔진이 끝을 알려 주지 않으면 `FocusedDocentSpeaker`가 시간 제한(15초 + 글자당 0.2초, 다시 들려줄 때는 글자당 0.2초, 말하는 동안만 셈) 뒤에 끝난 것으로 처리한다.

상태는 플래너 내부에만 존재한다. 위치가 뒤로 가거나 경로가 바뀌면 `reset()`으로 초기화한다.

## 5. 재생 상태

| 상태 | 조건 | UI |
|---|---|---|
| 대기 | 주행 전 | "대기 중", 시뮬레이션 주행 시작 |
| 주행 | 틱 진행 중 | "주행 중", 주행 정지 |
| 재생 | 음성 시작~완료 | "재생 중", 이야기 멈춤 |
| 도착 | 경로 끝 | "도착", 처음부터 다시 주행, 들은 이야기 수(예: 7/7) |

주행 정지는 가상 차량만 세우고, 나오던 이야기는 끝까지 들려준다.

### 5.1 오디오

- 공통 `AudioAttributes`: `USAGE_MEDIA` + `CONTENT_TYPE_SPEECH`. 이야기는 경로 안내가 아니라 콘텐츠이므로 차량의 MUSIC 컨텍스트에 둔다(Pleos 컨텍스트 표: MUSIC = MEDIA 등, NAVIGATION = ASSISTANCE_NAVIGATION_GUIDANCE).
- `FocusedDocentSpeaker`가 이야기마다 `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`을 요청하고 끝나면 반납한다. Pleos 음성 서비스는 재생할 때 스스로 포커스를 잡지 않는다(에뮬레이터 `dumpsys car_service`로 확인).

| 포커스 변화 | 동작 |
|---|---|
| 요청 거절 | 말하지 않고 화면에만 표시, 바로 끝난 것으로 처리 |
| `LOSS_TRANSIENT` (통화, Gleo) | 즉시 멈춤, 이야기 보관 |
| `GAIN` | 그 이야기가 아직 맞는 곳이면(또는 다시 듣기면) 처음부터 다시, 지났으면 끝냄. 맞는 곳: 장소 앞, 장소를 지나서 시작한 이야기(출발지)는 그 구간 끝까지 |
| `LOSS_TRANSIENT_CAN_DUCK` | 그대로 둠 — 두 음성 엔진 모두 재생 중 음량을 바꿀 수 없음 |
| `LOSS` | 멈추고 그 이야기를 끝냄 |

- 경로 안내(NAVIGATION)와 이야기(MUSIC)는 차량 오디오에서 동시 재생된다. AAOS 14는 이 경우 앱에 `CAN_DUCK`을 보내지 않고, 줄이는 것은 차량 오디오 정책이 맡는다. 에뮬레이터에서는 확인하지 못했다.
- 시간 제한: 말하는 동안만 센다(`LOSS_TRANSIENT`에서 멈추고, 다시 들려줄 때 새로 시작). 에뮬레이터에서 Gleo 직후 다시 들려준 이야기는 음성 서비스의 AudioTrack이 끊겼다 복구된 뒤 끝 알림이 오지 않았고, 시간 제한이 다음 이야기로 넘겼다.
- 선행 묵음: Pleos 음성 안내 가이드는 음성 앞 150~200ms 묵음을 권장하지만, Pleos TTS SDK는 재생을 차량 음성 서비스가 맡고 묵음 설정을 제공하지 않아 적용하지 못했다. Android TTS 대체 경로에도 적용하지 않았다(`playSilentUtterance`는 소리 데이터 없이 기다리기만 해서 가이드의 목적과 다르다).

### 5.2 주행 제한

`DrivingRestrictions`가 `CarUxRestrictionsManager`를 구독한다. 주행 최적화가 필요하고 `LIMIT_STRING_LENGTH` 또는 `LIMIT_CONTENT`가 걸리면 화면은 본문 대신 "주행 중에는 차량 규정에 따라 이야기를 글 대신 소리로만 들려 드려요."를 보여 준다. 본문은 평소에도 3줄까지만 보인다. Pleos 에뮬레이터는 주행 중 `0x50`(영상·설정 금지)만 걸어 본문이 그대로 보인다. 휴대폰에서는 Car API가 없어 제한하지 않는다.

## 6. Pleos SDK 연동 지점

| 현재 구현 | 교체 대상 | 비고 |
|---|---|---|
| `SimulatedRouteDriver.advance()` | FusedLocation 콜백 | 위치만 공급하면 코어는 그대로 |
| `DemoRoute.path` | NaviHelper 경로 폴리라인 | 경유지 변경 시 플래너 재생성 |
| `AndroidDocentSpeaker` | Pleos TTS SDK | 연동 완료(`PleosDocentSpeaker`), Android TTS는 대체 경로 |
| 없음 | Vehicle SDK | 주행/정차 상태로 재생 제어 |
| 없음 | Gleo AI | 음성 명령 라우팅 |

SDK 연동 시 `Vehicle.release()` 등 리소스 해제를 ViewModel `onCleared()`에 추가한다.

## 7. 빌드 환경

| 항목 | 값 |
|---|---|
| JDK | 17 (Android Studio 번들 JBR 25는 Gradle 8.10.2와 비호환) |
| Gradle | 8.10.2 (wrapper) |
| AGP | 8.8.2 |
| Kotlin | 2.0.0 |
| compileSdk / targetSdk | 35 |
| minSdk | 28 (차량 사이버 보안 요건) |
| Compose BOM | 2024.09.03 |
| 에뮬레이터 | Pleos Connect v2.1.0, API 34, RAM 4GB, Cold boot |

## 8. 검증

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

- 단위 테스트 22개: 거리/투영/보간/클램프, 트리거 구간, 한 번에 하나, 조용한 구간, 한 주행에 한 번, 다시 듣기, 우선순위, 구간 끝 순서, 측면 필터, 다음 이야기, 장소 통과 판정, 데모 경로 전체 주행(이야기 길이 5~30초에서 7개 모두 순서대로), 시뮬레이션 주행.
- 에뮬레이터 시나리오(Pleos Connect v2.1.0): 시작 → 7개 이야기 순차 재생 → 도착 7/7 → 처음부터 다시 주행. Virtual Console에서 기어 D·속도를 주어도 화면이 가려지지 않음. 이야기마다 오디오 포커스를 받고 반납(`USAGE_MEDIA`).
- Gleo 중단: `adb shell cmd car_service inject-key 231`로 Gleo를 부르면 이야기가 멈추고(`LOSS_TRANSIENT`), Gleo가 끝나면 처음부터 다시 나온다.

## 9. 알려진 한계

- 실차 위치/GPS 튐에 대한 필터링은 아직 없다(정지 판정, 스무딩 필요).
- 이야기 콘텐츠가 코드에 하드코딩되어 있다(JSON/캐시로 분리 예정).
- 백그라운드 재생 서비스가 없다: 앱이 화면에서 내려가면 주행 루프와 이야기가 멈출 수 있고, 프로세스가 끝나면 진행 상태가 사라진다.
- Pleos 음성 서비스와 Android TTS가 모두 없는 기기에서는 화면 자막만 진행된다.
- release 서명은 저장소 루트의 `keystore.properties`(git 제외)를 읽는다. 없으면 서명되지 않은 APK가 만들어지고 빌드가 경고한다.
