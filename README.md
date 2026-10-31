# Roadio — 차창 밖 이야기

> 지나가는 길이 곧 콘텐츠가 되는, Pleos Connect 차량용 오디오 도슨트.

운전자는 화면을 보지 않습니다. 경로를 따라가면 지나가는 지역과 명소의 이야기가 그 장소에 닿기 전에 자동으로 흘러나옵니다. 이야기는 한 번에 하나씩, 장소마다 한 번만 나옵니다. 운전자가 할 일은 없고, 필요하면 이야기를 멈추거나 다시 들을 수 있습니다.

- 공모전: 현대자동차그룹 Pleos App Market 차량용 앱 개발 공모전
- 팀: SOLOMON
- 저장소: https://github.com/WhoisHOO/pleos-app-challenge
- **제출 APK: [`apk/SOLOMON_Roadio_release.apk`](apk/SOLOMON_Roadio_release.apk)** (release 서명)
- 앱 카테고리(예정): Music/Audio — 백그라운드 재생 서비스(MediaBrowserService)를 갖춘 뒤 신청

## 현재 상태

| 구분 | 상태 |
|---|---|
| 코어 엔진(경로 투영, 트리거, 한 번에 하나씩 재생) | 구현 완료 |
| 단일 화면 UI + 시뮬레이션 주행 | 구현 완료 |
| 음성: Pleos TextToSpeech SDK(차량 온디바이스 엔진) | 연동 완료, Pleos 에뮬레이터에서 재생 확인 |
| 오디오 포커스(통화·음성 명령·경로 안내와의 관계) | 구현 완료, Gleo 음성 명령으로 중단·재개 확인 |
| 주행 중 사용(Distraction Optimized + 주행 제한 CarUxRestrictions) | 구현 완료, Virtual Console 주행 상태에서 확인 |
| 단위 테스트 | 22개 통과 |
| 실제 위치/경로(FusedLocation, NaviHelper) | 미연동 — 데모 경로 시뮬레이션으로 대체 |
| 백그라운드 재생(앱이 화면에서 내려갈 때) | 미구현 |

지금은 **가상 주행 모드**로 앱 전체 흐름을 확인할 수 있습니다. 위치 소스만 Pleos FusedLocation/NaviHelper로 바꾸면 같은 코어가 실제 경로에서 동작하는 구조입니다.

## 왜 차량 앱인가

- 내비게이션은 "목적지까지"만 알려주고, 지나가는 장소는 알려주지 않습니다.
- 운전 중에는 검색도, 화면 읽기도 어렵습니다. 오디오만이 안전합니다.
- 가족 드라이브, 초행길, 관광객에게 "이동 시간"을 "경험"으로 바꿔 줍니다.
- 단순한 화면과 달리, 경로·위치·콘텐츠·오디오를 실시간으로 맞추는 코어는 쉽게 따라 하기 어렵습니다.

## 실행 방법

### 테스트 환경

| 항목 | 값 |
|---|---|
| 에뮬레이터 | Pleos Connect Emulator v2.1.0 (시스템 이미지 `system-images;android-34;pleos_car_emulator;x86_64`, 기기 `Pleos_Connect_v2_0`, 2560×1440) |
| AVD 설정 | RAM 4GB, Cold boot |
| 호스트 | Windows 11, x86_64 (WHPX 가속) |
| 위치 권한 / 네트워크 / 계정 | 필요 없음 (음성은 차량 온디바이스 엔진) |

Pleos Connect 시스템 이미지는 Pleos 업데이트 사이트에서 받습니다. 자세한 절차는 공모전 개발 가이드(https://appdevchallenge.auto-hmg.io/guide/development)를 따릅니다.

```text
https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml
```

### 설치

```powershell
adb install -r apk\SOLOMON_Roadio_release.apk
```

### 사용

1. Pleos Connect 에뮬레이터를 Cold boot로 켭니다.
2. 앱 목록에서 `Roadio`를 엽니다.
3. **시뮬레이션 주행 시작**을 누르면 광화문 → 청계광장 → 서울시청 → 숭례문 → 서울역 → 남산 → N서울타워 경로(약 4km)를 시속 50km로 가상 주행하며 7개의 이야기가 차례로 재생됩니다(약 5분).

| 조작 | 동작 |
|---|---|
| 시뮬레이션 주행 시작 / 계속 주행 | 가상 차량 출발 |
| 주행 정지 | 가상 차량 정지. 듣던 이야기는 끝까지 나옵니다 |
| 이야기 멈춤 / 다시 듣기 | 지금 이야기를 멈추거나, 마지막 이야기를 처음부터 다시 듣기 |
| 처음부터 / 처음부터 다시 주행 | 경로와 이야기를 처음 상태로 되돌림 |

화면 아래 줄에는 다음 이야기와 남은 거리가, 경로 끝에서는 들은 이야기 수(예: 7/7)가 나옵니다.

### 주행 상태에서 확인 (Virtual Console)

에뮬레이터의 Virtual Console로 차량을 주행 상태로 바꿔도 Roadio가 가려지지 않고 이야기가 이어지는지 확인할 수 있습니다.

```powershell
adb forward tcp:8088 tcp:8088
# 브라우저에서 http://127.0.0.1:8088/ 을 열고 기어를 D, 속도를 30km/h 정도로 설정
```

### 소스에서 빌드

- JDK 17 (Android Studio 번들 JBR이 Java 25이면 Gradle 8.10.2가 실행되지 않습니다. `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`를 **17**로 지정하세요.)
- Android SDK: compileSdk / targetSdk 35, minSdk 28

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug        # app\build\outputs\apk\debug\app-debug.apk
.\gradlew.bat :app:assembleRelease      # 저장소 루트에 keystore.properties(서명 정보, git 제외)가 있어야 서명됩니다
```

## 음성과 오디오

- **음성 엔진**: Pleos Connect에는 Android 기본 TTS 엔진이 없어서, Pleos TextToSpeech SDK(`ai.pleos.playground:TextToSpeech`)로 차량의 Speech Service를 씁니다. 온디바이스 모드라 네트워크나 앱 등록이 필요 없습니다. Pleos 음성 서비스가 없는 기기(휴대폰, 일반 에뮬레이터)에서는 Android TTS로 넘어갑니다.
- **오디오 포커스**: 이야기는 미디어(말소리)로 재생되고, 이야기마다 오디오 포커스를 잡았다가 끝나면 돌려줍니다(Pleos 음성 서비스는 스스로 포커스를 잡지 않아 앱이 잡습니다). Pleos 오디오 포커스 가이드에 맞춰 다음처럼 동작합니다.
  - 통화, Gleo 음성 명령: 이야기를 바로 멈추고, 끝나면 그 이야기를 처음부터 다시 들려줍니다. 그사이 그 장소를 지나쳤으면(출발지 이야기처럼 장소를 지나서 시작한 이야기는 그 구간이 끝났으면) 다시 들려주지 않고 다음 이야기로 넘어갑니다. (에뮬레이터에서 Gleo로 확인)
  - 경로 안내: 이야기와 함께 나옵니다(차량 오디오에서 경로 안내와 미디어는 동시 재생). 이야기 소리를 줄이는 것은 차량 오디오 정책을 따르며, 에뮬레이터에서는 확인하지 못했습니다.
  - 다른 앱이 포커스를 가져가면 그 이야기는 끝냅니다.
  - 포커스를 받지 못하면(통화 중 등) 이야기는 화면에만 표시합니다.
- **끝 알림이 오지 않을 때**: 에뮬레이터에서 Gleo 직후 다시 들려준 이야기는 Pleos 음성 서비스가 끝까지 재생하고도 끝났다는 알림을 보내지 않았습니다. 그래서 이야기마다 시간 제한(15초 + 글자당 0.2초, 데모 이야기는 35~45초로 실제 길이의 두 배 이상)을 두고, 넘으면 다음 이야기로 넘어갑니다. 다시 들려줄 때는 글자당 0.2초만 둡니다.
- **선행 묵음**: Pleos 음성 안내 가이드는 음성 앞 150~200ms 묵음을 권장하지만, Pleos TTS SDK는 재생을 차량 음성 서비스가 맡고 묵음 설정을 제공하지 않아 적용하지 못했습니다.
- **주행 제한**: 차량이 주행 중 글자 수나 콘텐츠를 제한하면(CarUxRestrictions), 화면은 이야기 본문 대신 "소리로만 들려 드려요" 안내를 보여 줍니다. Pleos 에뮬레이터는 주행 중에도 글자 제한을 걸지 않아 본문이 그대로 보입니다.

## 테스트

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

검증 항목:

- 두 지점 간 거리, 경로 투영, 측면 이탈 거리
- 경로상 위치 보간 및 거리 클램프
- 트리거 구간(장소 400m 앞 ~ 통과 후 반경)
- 한 주행에 한 번만, 한 번에 하나씩, 이야기가 끝난 뒤 조용한 구간(100m)
- 우선순위, 구간이 먼저 끝나는 이야기 먼저, 경로에서 먼 이야기 제외
- 다음 이야기와 거리
- 데모 경로 전체 주행: 이야기 길이를 5~30초로 바꿔도 7개 이야기가 경로 순서대로, 각 장소에 닿기 전에 시작
- 장소를 지났는지 판정(통화가 끝난 뒤 다시 들려줄지)
- 시뮬레이션 주행 속도/종료/리셋

## 구조

```text
app/src/main/java/com/whohoo/roadio/
├── core/            # 순수 Kotlin 코어 (테스트 대상)
│   ├── Geo.kt           # 거리, 세그먼트 투영
│   ├── RoutePath.kt     # 경로, 진행률, pointAt
│   ├── DocentStory.kt   # 이야기 모델
│   └── DocentPlanner.kt # 트리거, 한 번에 하나, 우선순위
├── data/DemoRoute.kt    # 데모 경로 + 7개 이야기
├── drive/               # 시뮬레이션 주행
├── audio/               # Pleos TTS, Android TTS, 오디오 포커스
├── car/                 # 주행 제한(CarUxRestrictions)
├── ui/                  # 단일 화면 Compose UI
├── DocentViewModel.kt   # 주행 루프와 화면 상태
└── MainActivity.kt
```

자세한 설계는 [docs/architecture.md](docs/architecture.md), 기획 초안은 [docs/plan.md](docs/plan.md), 제출 요건은 [docs/submission-checklist.md](docs/submission-checklist.md)를 참고하세요.

## Pleos 연동 계획

| 현재 | 교체/연동 |
|---|---|
| `SimulatedRouteDriver` | FusedLocation SDK |
| `DemoRoute` 고정 경로 | NaviHelper SDK 경로/경유지/ETA |
| Pleos TextToSpeech SDK | 연동 완료 |
| 없음 | Vehicle SDK(주행/정차 상태) |
| 없음 | Gleo AI 음성 명령("이야기 꺼줘") |

## 로드맵

- [x] Pleos TTS 연동(온디바이스)
- [x] release 서명 및 `apk/` 제출본
- [ ] 백그라운드 재생(MediaBrowserService, 스티어링 휠 미디어 키)
- [ ] FusedLocation + NaviHelper 연동, 실경로 수신
- [ ] 이야기 콘텐츠 파이프라인(공공 관광 데이터 → LLM 초안 → 검수 → 오프라인 캐시)
- [ ] 다국어(영어/일본어/중국어) 및 아이 모드
- [ ] 시연 영상 촬영

## 라이선스 / 개인정보

- 코드 라이선스: 미정 (제출 전 결정)
- 데모 이야기는 저장소 내 창작 텍스트입니다. 실제 서비스에서는 공공데이터/라이선스를 확인한 콘텐츠만 사용합니다.
- 현재 버전은 위치 권한을 요청하지 않습니다. 위치를 쓰게 되면 온디바이스에서만 처리하는 것을 원칙으로 합니다.
