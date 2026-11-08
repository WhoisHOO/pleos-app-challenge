# Docentrip 아키텍처

## 1. 전체 흐름

```text
[Position]  ──▶  RoutePath.project()  ──▶  RouteProjection
(VehicleLocation = Pleos FusedLocation,      │ distanceAlongM, lateralM, progress
 or SimulatedRouteDriver)                    │ (200m 넘게 벗어나면 "경로 밖")
                                             ▼
                                   DocentPlanner.next()
                                   ├─ lead / trail window
                                   ├─ one story at a time, quiet gap after it
                                   ├─ once per drive
                                   └─ priority, then the window that ends first
                                             │ DocentStory?
                                             ▼
                                   DocentEngine.tell()   (앱 전체에 하나)
                                             │
                    ┌────────────────────────┼─────────────────────────┐
                    ▼                        ▼                         ▼
          FocusedDocentSpeaker          Compose UI              DocentPlayer (Media3)
          (audio focus)            (story, next, progress)   DocentripMediaService: 미디어 세션,
                    │                                         포그라운드 서비스, 미디어 화면·휠 키
                              │
                   PleosDocentSpeaker ── 없으면 ──▶ AndroidDocentSpeaker
                   (Pleos TTS SDK)                  (Android TTS)
                              │ onDone
                              ▼
                   DocentPlanner.storyEnded()
```

핵심 원칙: **위치 소스와 음성 엔진을 코어에서 분리**한다. 음성은 Pleos TTS SDK, 위치는 Pleos FusedLocation(차량 위치 모드) 또는 시뮬레이션이다. 주행·이야기 상태는 프로세스에 하나인 `DocentEngine`이 갖고, 화면과 미디어 세션은 그것을 보여 주고 조작할 뿐이다.

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
| `audio/FocusedDocentSpeaker.kt` | 이야기마다 오디오 포커스, 중단·재개·종료, 끝 알림 시간 제한, 음성 명령을 듣는 동안 이야기 붙잡기(hold/resume) | 에뮬레이터 |
| `car/DrivingRestrictions.kt` | 주행 제한(CarUxRestrictions) 구독: 본문 표시, 설정 변경 가능 여부. 기어(GEAR_SELECTION) 구독 | 에뮬레이터 |
| `car/VehicleTrip.kt` | Pleos Vehicle SDK로 차의 트립 미터(m)와 주행 거리계(km)를 한 번 읽음(3초 제한) | 에뮬레이터 |
| `core/TripSummary.kt` | 도착 요약: 차의 미터가 움직였으면 그 거리, 아니면 경로 기준 거리, 주행 시간(분), 들은 이야기 | `TripSummaryTest` |
| `core/Gear.kt` | Android VehicleGear 값을 P/R/N/D로 | `GearTest` |
| `audio/PleosVoiceListener.kt` | Pleos SpeechToText로 한 번 듣기(온디바이스, ko-KR): 마이크가 열릴 때까지 6초, 열린 뒤 7초 제한, 끝 신호 뒤 마지막 글자 기다리기 | 에뮬레이터 |
| `core/VoiceCommand.kt` | 들은 글에서 명령(다음, 다시, 그만, 켜 줘) 고르기 | `VoiceCommandTest` |
| `debug/HearRecordingReceiver.kt` | debug 빌드만: 마이크 대신 녹음한 명령을 듣게 함(`tools/say-command.ps1`) | 에뮬레이터 |
| `core/SpeedEstimator.kt` | 위치 연속값으로 속도 계산(FusedLocation에는 속도가 없음) | `SpeedEstimatorTest` |
| `location/VehicleLocation.kt` | Pleos FusedLocation 시작·중지, 오류 코드 해석 | 에뮬레이터 |
| `DocentEngine.kt` | 앱 전체에 하나: 모드(시뮬레이션/차량 위치), 위치 동의·권한 상태, 250ms 시뮬레이션 틱, 이야기, 다시 들려줄지 판단, 진행 상태 저장·복원, 트립 요약, 후진 중 이야기 쉬기 | 에뮬레이터 |
| `core/PlannerSnapshot.kt` | 들은 이야기별 시작 거리와 마지막 이야기 뒤 조용한 구간을 텍스트로 저장 | `PlannerSnapshotTest` |
| `media/DocentripMediaService.kt` | Media3 `MediaLibraryService`, 포그라운드 서비스(mediaPlayback) | 에뮬레이터 |
| `media/DocentPlayer.kt` | 엔진을 미디어 플레이어로 보여 줌: 재생=주행 켜기, 일시정지=끄기, 다음=넘기기, 이전=다시 듣기 | 에뮬레이터 |
| `media/StoryLibrary.kt` | 미디어 화면의 이야기 목록·검색, 고른 이야기 바로 들려주기 | 에뮬레이터 |
| `media/MediaKeyClaim.kt` | 휠 미디어 키를 받으려고 주행·이야기 시작 때 0.2초 무음 재생 | 에뮬레이터 |
| `navigation/NavigationLink.kt` | Pleos NaviHelper: 경유지와 목적지로 안내 요청, 안내 중 다음 회전 거리(바뀔 때 + 2초마다 조회) | 에뮬레이터 |
| `core/TurnGuard.kt` | 회전이 150m 안으로 다가오는 동안 새 이야기를 미룸(회전마다 최대 20초) | `TurnGuardTest` |
| `ui/DocentripScreen.kt` | 단일 화면(진행률, 이야기, 다음 이야기, 버튼) | 수동 |
| `data/DocentripRoute.kt` | `assets/route.json`을 읽어 경로·이야기·내비 경유지를 만든다(장소는 좌표 또는 경로 비율) | `RouteContentTest` |

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

`next()`가 이야기를 고르면 `telling`이 되고, 음성이 끝나면 `DocentEngine`이 `storyEnded(projection)`을 부른다. 다시 듣기는 `storyStarted()`로 같은 규칙을 따른다. 음성 엔진이 끝을 알려 주지 않으면 `FocusedDocentSpeaker`가 시간 제한(15초 + 글자당 0.2초, 다시 들려줄 때는 글자당 0.2초, 말하는 동안만 셈) 뒤에 끝난 것으로 처리한다.

상태는 플래너 내부에 있고, `PlannerSnapshot`(들은 이야기별 시작 거리, 마지막 이야기 뒤 조용한 구간)으로 SharedPreferences에 저장한다. 50m마다, 그리고 이야기 시작·끝·일시정지 때 저장하며, 새 프로세스의 `DocentEngine`이 복원한다. 시뮬레이션은 저장한 주행 거리에서 이어가고, 차량 위치 모드는 지금 위치를 다시 받으므로 들은 이야기와 조용한 구간만 이어받는다. 이야기가 나오던 중이었으면 다시 시작하지 않는다. 마지막으로 들려준 이야기의 id도 저장해, 다시 열면 카드에 그 이야기가 보이고 "다시 듣기"를 누를 수 있다(새 주행처럼 "아직 들려준 이야기가 없어요"가 나오지 않게). `reset()`은 저장도 지운다.

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

### 5.3 기어와 트립 요약

- 기어: `DrivingRestrictions`가 `CarPropertyManager.registerCallback(GEAR_SELECTION, ON_CHANGE)`로 기어를 받는다. 권한은 `android.car.permission.CAR_POWERTRAIN`(normal, 설치 때 허용). Pleos Vehicle SDK에는 기어·속도 API가 없다. `registerCallback`은 먼저 차량 서비스에 속성 설정을 묻고 기다리는데, 메인 스레드에서 부르면 에뮬레이터에서 이 대기가 서비스 시작 제한(20초)을 넘겨 ANR이 났다(`DocentripMediaService`). 그래서 전용 스레드에서 구독하고, 값은 메인 스레드로 넘긴다. `subscribePropertyEvents`는 API 35 스텁에는 있지만 Pleos Connect(Android 14) 차량 서비스에는 없어 쓰지 않는다.
- 후진: 차량 위치 모드에서 R이 되면 듣던 이야기를 넘기고(다시 듣기 가능), R인 동안은 새 이야기를 미룬다(회전 직전 미루기와 같은 자리). 시뮬레이션은 시연용이라 기어를 따르지 않는다. 에뮬레이터에서 R이면 후방 카메라(EVS)가 화면을 덮는다.
- 트립 요약: `VehicleTrip`이 Pleos Vehicle SDK 2.3.2(`ai.pleos.playground:Vehicle`)의 `Odometer`로 `getTripCurrentDrivingDistance`(m)와 `getCurrentDistanceDriven`(km)를 읽는다. 권한 `pleos.car.permission.CAR_DRIVE`(normal). `initialize()`에는 완료 알림이 없어 앱이 시작할 때 미리 연결하고, 읽기마다 3초 제한을 둔다. 차량 위치 모드의 첫 주행 구간 시작과 도착 때 읽고, 도착하면 먼저 경로 기준 요약을 보여 준 뒤 차의 값이 오면 바꾼다. 거리는 트립 미터가 앞으로 움직였고 주행 거리계 차이와 1km 안에서 맞으면(주행 거리계가 km 단위일 수 있음) 트립 미터 차이, 아니면 주행 거리계 차이(주행 중 리셋된 트립 미터), 둘 다 움직이지 않았으면 경로 기준이다. 리셋 뒤 늦게 온 값은 버린다. 시간은 주행이 켜져 있던 시간의 합(분, 반올림, 최소 1분)이다. 주행 시간, 출발 위치, 요약은 진행 상태와 함께 저장한다. 저장된 요약은 도착했다는 표시라서, 차량 위치 모드에서도 다시 열면 도착 상태와 요약이 돌아오고 다음 켜기는 새 주행이 된다. 도착 뒤에는 위치 대기·경로 밖 안내나 시작하지 못한 마지막 이야기보다 요약을 먼저 보여 준다.
- SDK의 `vehicle-data`/`vehicle-aidl` POM이 `hilt-android`를 끌어와 `com.google.dagger`를 뺐다(Docentrip은 Hilt를 쓰지 않음). SDK의 `VehicleException`은 배포 jar에서 이름이 바뀌어 있어 메시지만 읽는다.
- 에뮬레이터: 트립 미터는 `ERROR_NOT_SUPPORTED_PROPERTY`, 주행 거리계는 Virtual Console 값(`PUT /vehicle/odometer?traveled_distance_km=N`)을 돌려준다. 주행해도 저절로 늘지 않는다.
- Pleos 문서는 Vehicle SDK 권한에 App Market 사전 승인이 필요하다고 안내한다. 에뮬레이터에서는 승인 없이 허용됐다. 실차 배포 전에 확인한다.

### 5.4 음성 명령 (Pleos SpeechToText)

- `ai.pleos.playground:SpeechToText:3.3.1`, `Mode.ON_DEVICE`, `Locale.KOREA`. 권한 `pleos.car.permission.STT_SERVICE`(normal). 앱 등록·네트워크가 필요 없다. 마이크는 Speech Service(`ai.pleos.playground.caas` 4.2.10)가 열어서 `RECORD_AUDIO`는 없다. SDK는 권한이 없으면 `SecurityException`을 바로 던지므로 모든 호출을 감싼다. Speech Service가 없는 기기는 초기화가 실패하거나 답이 없어 버튼이 보이지 않는다.
- 흐름: "말로 하기" → 듣던 이야기를 붙잡음(`hold`: 목소리만 멈추고 포커스와 이야기는 유지) → `request()` → 마이크가 열리면 "듣고 있어요"(처음에는 2.6초 걸렸다) → 마지막 글자(`onUpdated(..., completed = true)`) → `VoiceCommand.of`로 명령 → 실행. 명령이 아니거나 아무 말이 없으면 붙잡았던 이야기를 그 장소에 아직 맞을 때 처음부터 다시 들려준다(`resume`, 통화 뒤 다시 들려주기와 같은 규칙). 듣는 동안은 새 이야기를 시작하지 않는다. 다시 누르면 듣기를 그만둔다.
- 명령: 그만·멈춰·조용히·꺼 줘 → 도슨트 끄기(이야기도 멈춤), 다음·넘겨 → 넘기기, 켜·시작 → 켜기, 다시·한 번 더 → 다시 듣기. 이 순서로 고른다("그만 다시"는 끄기, "다음 이야기 시작"은 넘기기, "다시 켜 줘"는 켜기). 꺼는 동사일 때만 끄기다("다음 꺼"는 다음 것). "끄지 마"처럼 하지 말라는 말은 아무것도 하지 않는다. 띄어쓰기와 문장부호는 무시한다.
- 에뮬레이터에서 본 것: Speech Service는 끝 신호(`onEndedRecognition`)를 먼저, 마지막 글자를 약 65ms 뒤에 보낸다. 그래서 끝 신호 뒤 1.5초 동안 마지막 글자를 기다린다(오지 않으면 중간 글자를 쓴다). 듣는 동안 Speech Service는 오디오 포커스를 잡지 않는다(포커스 스택에 Docentrip만). 그래서 이야기를 멈추는 것은 Docentrip이 한다.
- 개인정보: Docentrip은 들은 말을 명령을 고르는 데만 쓰고 저장·전송하지 않으며, 화면과 Docentrip 로그에는 고른 명령만 남긴다(주행 중 음성 받아쓰기 표시 제한과도 맞음). Pleos SDK(Docentrip 프로세스 안)와 Speech Service는 들은 글을 시스템 로그(logcat)에 남기며, Docentrip이 막을 수 없다. 그래서 화면 문구는 "Docentrip은 … 저장하지 않아요"로 Docentrip이 하는 일만 말한다. 설정 화면(위치 안내)에서는 들은 결과를 보여 줄 자리가 없어 "말로 하기"를 숨긴다.
- 에뮬레이터 마이크: `-allow-host-audio`가 있어야 PC 마이크가 들어간다(`tools/start-emulator.ps1 -HostMic`). 개발 PC는 Windows에서 녹음해도 무음이라, debug 빌드의 `HearRecordingReceiver`가 녹음한 PCM(16kHz, 16bit, mono)을 `sendAudio()`로 같은 온디바이스 엔진에 넣는다. release APK에는 이 수신기가 없다.

## 6. 차량 위치 (Pleos FusedLocation)

- `ai.pleos.playground:FusedLocation:1.1.3`, 권한 `pleos.car.permission.FUSED_LOCATION`(dangerous, 런타임 요청). Android 위치 권한은 필요 없다. SDK가 `uses-feature automotive required=true`를 합치므로 `tools:replace`로 `false`를 유지한다(휴대폰에서도 시뮬레이션은 동작).
- 흐름: 차량 위치로 → 안내·동의 화면(동의 버튼만 동의로 침) → 권한 요청 → `initialize()` → `registerFusedLocationCallback` → 약 1초마다 위치. 거부하면 다시 묻지 않고, 설정에서 허용한 뒤 화면으로 돌아오면 다시 연결한다. 주행 중(NO_SETUP)에는 모드 변경·동의를 막는다.
- `LocationData`에는 위도·경도·고도·방향·정확도·시각만 있고 속도가 없다. 속도는 `SpeedEstimator`가 위치 변화로 계산한다(3m 미만 움직임은 정지, 5초 넘는 공백은 새로 시작). 에뮬레이터에서는 방향이 늘 0이다.
- 오류는 `LocationException`이 아니라 메시지에 코드 숫자가 든 `Exception`으로 온다(예: `"800002"` = 위치 꺼짐). 코드를 해석해 권한 없음/위치 꺼짐/서비스 오류로 나누고, 위치가 꺼졌으면 화면(Activity)에서 `showLocationAccessBlockedPopup()`을 띄운다.
- 화면이 보일 때와 주행이 켜져 있을 때만 위치를 받는다. 화면이 꺼진 채 주행이 끝나면(휠·위젯 일시정지, 도착) 위치도 멈춘다. 미디어 화면이 부팅 때 서비스를 깨워도 위치는 켜지지 않는다.
- 거부한 권한은 다시 묻지 않는다(거부를 저장). 설정에서 허용하거나 "차량 위치로"를 다시 고르면 다시 묻는다. 권한 창도 설정이므로 주행 중에는 정차 뒤로 미룬다. 위치가 꺼져 있거나 권한이 없으면 차에서 켠 주행도 끈다.
- 에뮬레이터: Virtual Console에는 GPS가 없다. `tools/drive-demo-route.ps1`이 `adb emu geo fix`로 데모 경로를 1초마다 따라가고, 에뮬레이터의 위치 서비스가 그 값을 FusedLocation으로 넘긴다.

## 7. 미디어 세션과 백그라운드 (Media3 1.9.4)

- Media3 1.10부터는 compileSdk 36이 필요해 1.9.4를 쓴다. ExoPlayer는 쓰지 않는다(소리는 차량 음성 서비스가 낸다).
- `DocentPlayer`(`SimpleBasePlayer`)는 엔진 상태를 그대로 보여 준다. 재생 목록은 경로의 이야기 7개, 지금 항목은 들려주는/들려준 이야기 또는 다음 이야기. 주행이 켜져 있거나 이야기가 나오는 동안은 재생 중(READY + playWhenReady)으로 둔다(주행을 끈 채 요청한 이야기도 재생 중이라, 일시정지가 주행을 켜지 않고 그 이야기를 멈춘다). 주행 중에는 이야기 사이 조용한 구간도 재생 중이다. 그래야 Media3가 서비스를 포그라운드로 유지하고, Android 14가 앱을 얼리지 않는다. 주행 전에도 IDLE이 아니라 READY(일시정지)로 두어, 미디어 화면이 다음 이야기를 보여 주고 재생으로 주행을 켤 수 있게 한다.
- 스스로 재생을 시작하지 않는다. 주행은 재생 명령으로만 켜진다: 운전자의 재생, 또는 차가 꺼질 때 켜져 있던 주행을 차의 부팅 설정(CarMediaService 자동 재생)대로 다시 켜는 경우.
- 미디어 화면에서 이야기를 고르거나 음성 검색하면 `StoryLibrary.onSetMediaItems`가 그 이야기를 바로 들려주고 요청 자체는 거절한다. 그래서 뒤따르는 재생 명령이 주행 전체를 켜지 않는다.
- `MainActivity`는 화면이 보이는 동안 `MediaController`로 세션에 연결하고, 서비스는 `onCreate`에서 세션을 `addSession`한다. 그래서 화면에서 시작한 주행도 포그라운드 서비스가 된다.
- 최근 앱에서 Docentrip을 닫으면(`onTaskRemoved`) 주행을 끄고 서비스를 멈춘다.
- 휠 미디어 키: AOSP는 가장 최근에 소리를 낸 앱의 세션으로 키를 보낸다. 이야기는 차량 음성 서비스(다른 앱 ID)가 재생하므로, Docentrip이 주행·이야기 시작 때 0.2초 무음을 직접 재생한다(`MediaKeyClaim`).
- Docentrip은 Music/Audio 앱이라 주행 중에는 차량의 미디어 소스다. 켜면 음악이 멈추고, 음악을 틀면 Docentrip이 일시정지한다(AAOS CarMediaService 규칙).

## 8. Pleos SDK 연동 현황

| SDK | 상태 | 비고 |
|---|---|---|
| TextToSpeech 3.3.1 | 연동 완료 | 온디바이스, 없으면 Android TTS |
| FusedLocation 1.1.3 | 연동 완료 | 차량 위치 모드 |
| NaviHelper 3.0.5 | 연동 완료 | 경로 모양은 주지 않는다. "내비로 안내": `requestRouteWithWaypoints`(숭례문·문화역서울 284 경유, N서울타워 도착, POI ID 없이 좌표·이름·주소. 이름은 `route.json`의 `placeName` 그대로라 `문화역서울284`로 붙여 씀). 안내 중 `onTBTInfo`의 다음 회전 거리로 `TurnGuard`가 새 이야기를 미룸. 권한 `NAVI_ROUTE`, `NAVI_ROUTE_SEARCH`(normal) |
| Vehicle 2.3.2 | 연동 완료 | `Odometer`로 트립 요약(5.3). 권한 `CAR_DRIVE`(normal, App Market 사전 승인 필요로 안내됨). 기어·속도 API는 없어 기어는 표준 `CarPropertyManager` |
| SpeechToText 3.3.1 | 연동 완료 | "말로 하기" 음성 명령(5.4). 권한 `STT_SERVICE`(normal), 온디바이스 ko-KR |
| Gleo AI | 확장 | Media3 기반 음성 제어는 Pleos가 "향후 업데이트"로 안내, 지금은 Gleo Interface 스키마 등록 필요 |

## 9. 빌드 환경

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

## 10. 검증

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

- 단위 테스트 65개: 거리/투영/보간/클램프, 트리거 구간, 한 번에 하나, 조용한 구간, 한 주행에 한 번, 다시 듣기, 우선순위, 구간 끝 순서, 측면 필터, 다음 이야기, 장소 통과 판정, 데모 경로 전체 주행(이야기 길이 5~30초에서 7개 모두 순서대로), 위치에서 속도, 불가능한 GPS 점프 제거, 회전 직전 이야기 미루기, 진행 상태 저장·복원, JSON 경로 파싱·검증, 시뮬레이션 주행, 트립 요약(트립 미터·주행 거리계·경로 기준, 리셋된 트립 미터, 분 반올림), 기어 값, 음성 명령 낱말.
- 에뮬레이터 시나리오(Pleos Connect v2.1.0): 시작 → 7개 이야기 순차 재생 → 도착 7/7 → 처음부터 다시 주행. Virtual Console에서 기어 D·속도를 주어도 화면이 가려지지 않음. 이야기마다 오디오 포커스를 받고 반납(`USAGE_MEDIA`).
- Gleo 중단: `adb shell cmd car_service inject-key 231`로 Gleo를 부르면 이야기가 멈추고(`LOSS_TRANSIENT`), Gleo가 끝나면 처음부터 다시 나온다.
- 백그라운드: 주행 중 내비게이션을 앞에 띄워도 이야기가 이어진다. `dumpsys activity services`에서 `DocentripMediaService isForeground=true types=mediaPlayback`, `dumpsys media_session`에서 Docentrip 세션 `PLAYING`.
- 미디어 화면·키: 차량 왼쪽 미디어 위젯에 지금 이야기 제목과 "Docentrip"이 보이고, 위젯의 이전/다음/일시정지와 `cmd car_service inject-key 85/87`(휠 재생·다음)이 동작한다.
- 차량 위치: 동의 → 권한 허용 → `tools/drive-demo-route.ps1` → 도슨트 켜기. 속도는 위치에서 계산해 약 47~50km/h로 보이고 이야기가 위치를 따라 나온다.
- 음성 명령: 시뮬레이션 주행 중 `tools/say-command.ps1`로 "다음"(이야기 넘김), "다시 들려줘"(지금 이야기 다시), "음악 틀어 줘"("잘 듣지 못했어요", 붙잡았던 이야기를 처음부터 이어 감), "그만"(주행 멈춤), "도슨트 켜 줘"(주행 다시)를 확인했다. 차량 마이크(`request()`)로도 열리고 7초 뒤 닫히는 것, 그동안 포커스를 Docentrip만 가진 것을 확인했다. 실제 사람 목소리는 이 PC 마이크가 무음이라 확인하지 못했다.
- 후진: 차량 위치 모드로 주행 중 Virtual Console에서 기어를 R로 바꾸면 이야기가 멈추고(오디오 포커스 반납), D로 바꾸면 다음 이야기가 나온다.
- 트립 요약: 시뮬레이션 주행 끝에 "오늘 4.0km(경로 기준) · 5분 · 이야기 7/7"이 나오고, 강제 종료 뒤 다시 열어도 남는다. 차량 위치 모드(GPS 한 점씩, Virtual Console 주행 거리계 104 → 108km)에서는 "오늘 4.0km(차량 주행 거리)"로 바뀌고, 다시 열어도 도착 상태와 요약이 남는다. 트립 미터는 에뮬레이터가 지원하지 않아 단위 테스트로 확인했다.
- 내비게이션: "내비로 안내"를 누르면 차량 내비(Navi42)가 앞으로 나와 숭례문·문화역서울 284를 경유해 N서울타워로 안내를 시작하고, 회전 정보(`onTBTInfo`)가 온다. 에뮬레이터 내비는 GPS 문장(NMEA) 체크섬 오류로 위치를 따라가지 않아 판교에 머물고 회전 거리가 변하지 않는다. 그래서 회전 직전 미루기는 `TurnGuardTest`로 확인했다.

## 11. 알려진 한계

- 불가능한 GPS 점프는 `FixGate`가 버려 경로 투영과 이야기에 반영하지 않는다. 작은 흔들림은 아직 스무딩하지 않는다(속도 계산에서 3m 미만은 정지로 봄).
- 이야기 경로는 Docentrip의 데모 경로 하나다. 차량 위치 모드도 그 경로 위에서만 이야기가 나온다.
- 에뮬레이터: Docentrip 프로세스가 다시 시작되면(재설치 등) 차량 왼쪽 미디어 위젯이 이전 세션에 머물러 "제목 정보 없음"으로 보이고 위젯 버튼이 듣지 않았다. 이때 Docentrip 세션은 정상(`PLAYING`)이고 휠 키도 동작했으며, CarMediaService는 이전 재생 상태를 그대로 들고 있었다. 에뮬레이터를 다시 켜면 정상으로 돌아온다. 시연은 부팅 뒤 설치·실행한 상태에서 한다.
- Pleos 음성 서비스와 Android TTS가 모두 없는 기기에서는 화면 자막만 진행된다.
- release 서명은 저장소 루트의 `keystore.properties`(git 제외)를 읽는다. 없으면 서명되지 않은 APK가 만들어지고 빌드가 경고한다.
