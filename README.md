# Roadio — 차창 밖 이야기

> 지나가는 길이 곧 콘텐츠가 되는, Pleos Connect 차량용 오디오 도슨트.

운전자는 화면을 보지 않습니다. 경로를 따라가면 지나가는 지역과 명소의 이야기가 자동으로 흘러나옵니다. 조작은 재생/정지 하나, 나머지는 차량 위치와 경로가 알아서 합니다.

- 공모전: 현대자동차그룹 Pleos App Market 차량용 앱 개발 공모전
- 저장소: https://github.com/WhoisHOO/pleos-app-challenge
- 앱 카테고리(예정): Music/Audio — 오디오 콘텐츠 중심

## 현재 상태 (WIP)

| 구분 | 상태 |
|---|---|
| 코어 엔진(경로 투영, 트리거, 재생 상태) | 구현 완료 |
| 단일 화면 UI + 시뮬레이션 주행 | 구현 완료 |
| 단위 테스트 | 14개 통과 |
| Debug APK 빌드 | 통과 |
| Pleos Connect SDK 연동 | 미연동 (SDK 사용 신청 후) |
| 실차/에뮬레이터 위치 연동 | 시뮬레이션으로 대체 중 |

현재는 **가상 주행 모드**로 앱 전체 흐름을 검증할 수 있습니다. Pleos SDK(FusedLocation, NaviHelper, TTS)를 붙이면 동일한 코어에 실제 위치/경로/음성만 교체하는 구조입니다.

## 왜 차량 앱인가

- 내비게이션은 "목적지까지"만 알려주고, 지나가는 장소는 알려주지 않습니다.
- 운전 중에는 검색도, 화면 읽기도 어렵습니다. 오디오만이 안전합니다.
- 가족 드라이브, 초행길, 관광객에게 "이동 시간"을 "경험"으로 바꿔 줍니다.
- 단순한 화면과 달리, 경로·위치·콘텐츠·오디오를 실시간으로 맞추는 코어는 쉽게 따라 하기 어렵습니다.

## 실행 방법

### 필요 환경

- JDK 17 (필수)
- Android Studio (최신)
- Android SDK: compileSdk / targetSdk 35, minSdk 28
- Pleos Connect Emulator 2.1.0 (AVD: `Pleos Connect v2.1.0`, API 34, RAM 4GB, Cold boot)

> Android Studio 번들 JBR이 Java 25인 경우 Gradle 8.10.2가 실행되지 않습니다.
> `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`를 **17**로 지정하세요.

### 빌드 및 설치

```powershell
# 저장소 루트에서
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

### 실행

1. Pleos Connect AVD(API 34)를 Cold boot로 실행합니다.
2. 앱을 설치하고 `Roadio`를 엽니다.
3. **시뮬레이션 주행 시작**을 누르면 광화문 → 시청 → 서울역 → 남산 경로를 가상으로 달리며 이야기가 재생됩니다.
4. **다시 듣기**, **처음부터**로 반복 검증할 수 있습니다.

현재 버전은 위치 권한이나 실차 없이 동작합니다. TTS는 Android 기본 한국어 음성을 사용하며, 에뮬레이터에 한국어 음성 데이터가 없으면 자막만 진행됩니다.

## 테스트

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

검증 항목:

- 두 지점 간 거리, 경로 투영, 측면 이탈 거리
- 경로상 위치 보간 및 거리 클램프
- 트리거 윈도우(앞 400m ~ 통과 후 반경)
- 재생 후 재트리거 방지(rearm), 최소 간격(min gap)
- 우선순위 경쟁, 경로에서 먼 이야기 제외
- 다음 이야기 거리 계산
- 시뮬레이션 주행 속도/종료/리셋

## 구조

```text
app/src/main/java/com/whohoo/roadio/
├── core/            # 순수 Kotlin 코어 (테스트 대상)
│   ├── Geo.kt           # 거리, 세그먼트 투영
│   ├── RoutePath.kt     # 경로, 진행률, pointAt
│   ├── DocentStory.kt   # 이야기 모델
│   └── DocentPlanner.kt # 트리거/쿨다운/우선순위
├── data/DemoRoute.kt    # 데모 경로 + 7개 이야기
├── drive/               # 시뮬레이션 주행
├── audio/               # TTS + 오디오 포커스(덕킹)
├── ui/                  # 단일 화면 Compose UI
├── DocentViewModel.kt   # 상태 머신
└── MainActivity.kt
```

자세한 설계는 [docs/architecture.md](docs/architecture.md), 기획 초안은 [docs/plan.md](docs/plan.md), 제출 요건은 [docs/submission-checklist.md](docs/submission-checklist.md)를 참고하세요.

## Pleos 연동 계획

| 현재 | 교체/연동 |
|---|---|
| `SimulatedRouteDriver` | FusedLocation SDK |
| `DemoRoute` 고정 경로 | NaviHelper SDK 경로/경유지/ETA |
| Android `TextToSpeech` | Pleos TTS SDK |
| 없음 | Vehicle SDK(주행 상태로 재생 일시정지) |
| 없음 | Gleo AI 음성 명령("이야기 꺼줘") |

## 로드맵

- [ ] Pleos Playground 프로젝트 생성 및 SDK 사용 신청
- [ ] FusedLocation + NaviHelper 연동, 실경로 수신
- [ ] Pleos TTS 전환 및 프리롤 실런스 적용
- [ ] 이야기 콘텐츠 파이프라인(공공 관광 데이터 → LLM 초안 → 검수 → 오프라인 캐시)
- [ ] 다국어(영어/일본어/중국어) 및 아이 모드
- [ ] 주행 제한(CarUxRestrictions) 대응 점검
- [ ] release 서명 및 `apk/` 제출본 생성
- [ ] 시연 영상 촬영

## 라이선스 / 개인정보

- 코드 라이선스: 미정 (제출 전 결정)
- 데모 이야기는 저장소 내 창작 텍스트입니다. 실제 서비스에서는 공공데이터/라이선스를 확인한 콘텐츠만 사용합니다.
- 위치 데이터는 온디바이스에서만 처리하는 것을 원칙으로 합니다.
