# Roadio 아키텍처

## 1. 전체 흐름

```text
[Location]  ──▶  RoutePath.project()  ──▶  RouteProjection
(FusedLocation / Simulation)                 │ distanceAlongM, lateralM, progress
                                             ▼
                                   DocentPlanner.next()
                                   ├─ lead / trail window
                                   ├─ min gap
                                   ├─ rearm
                                   └─ priority
                                             │ DocentStory?
                                             ▼
                                   DocentViewModel.play()
                                             │
                              ┌──────────────┴──────────────┐
                              ▼                             ▼
                        DocentSpeaker                  Compose UI
                        (TTS + audio focus)       (story, next, progress)
```

핵심 원칙: **위치 소스와 음성 엔진을 인터페이스로 분리**한다. 현재는 시뮬레이션 주행과 Android TTS를 쓰고, 제출 버전에서는 Pleos FusedLocation/NaviHelper/TTS로 교체한다.

## 2. 모듈

| 파일 | 책임 | 테스트 |
|---|---|---|
| `core/Geo.kt` | 하버사인 거리, 세그먼트 정투영 | `GeoTest` |
| `core/RoutePath.kt` | 경로 세그먼트, 누적 거리, `pointAt`, `project` | `GeoTest` |
| `core/DocentStory.kt` | 이야기 데이터 모델 | - |
| `core/DocentPlanner.kt` | 트리거 정책, 쿨다운, 재무장, 우선순위, 다음 이야기 | `DocentPlannerTest` |
| `drive/SimulatedRouteDriver.kt` | 일정 속도로 경로 이동 | `SimulatedRouteDriverTest` |
| `audio/DocentSpeaker.kt` | TTS 초기화, 오디오 포커스, 완료 콜백 | 수동 |
| `DocentViewModel.kt` | 250ms 틱, 상태 머신, 재생/정지/재시작 | 수동 |
| `ui/RoadioScreen.kt` | 단일 화면(진행률, 이야기, 다음 이야기, 버튼) | 수동 |
| `data/DemoRoute.kt` | 광화문→시청→서울역→남산 데모 경로/7개 이야기 | - |

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
    val minGapM: Double = 900.0,
    val maxLateralM: Double = 150.0,
    val rearmM: Double = 3_000.0,
)
```

조건:

```text
playable =
    current >= storyAlong - leadM
    && current <= storyAlong + story.triggerRadiusM
    && current - lastPlayedAt(story) > rearmM
    && current - lastTriggerAt >= minGapM
    && story.lateralM <= maxLateralM
```

선택: `priority` 내림차순 → 현재 위치와의 거리 오름차순.

상태는 플래너 내부에만 존재한다. 위치가 뒤로 가거나 경로가 바뀌면 `reset()`으로 초기화한다.

## 5. 재생 상태

| 상태 | 조건 | UI |
|---|---|---|
| 대기 | 주행 전 | "대기 중", 시작 버튼 |
| 주행 | 틱 진행 중 | "주행 중", 정지 버튼 |
| 재생 | TTS 시작~완료 | "재생 중", 다시 듣기 |
| 종료 | 경로 끝 | 주행 정지, 처음부터 |

`speak()`는 오디오 포커스를 `GAIN_TRANSIENT_MAY_DUCK`으로 요청한다. 완료/에러 콜백에서 포커스를 반납하고 `isSpeaking`을 내린다.

## 6. Pleos SDK 연동 지점

| 현재 구현 | 교체 대상 | 비고 |
|---|---|---|
| `SimulatedRouteDriver.advance()` | FusedLocation 콜백 | 위치만 공급하면 코어는 그대로 |
| `DemoRoute.path` | NaviHelper 경로 폴리라인 | 경유지 변경 시 플래너 재생성 |
| `AndroidDocentSpeaker` | Pleos TTS SDK | 프리롤 실런스 적용 |
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

- 단위 테스트 14개: 거리/투영/보간/클램프, 트리거 윈도우, 재트리거 방지, 최소 간격, 우선순위, 측면 필터, 다음 이야기, 시뮬레이션 주행.
- 수동 시나리오: 시작 → 7개 이야기 순차 재생 → 종료 → 다시 시작.

## 9. 알려진 한계

- 실차 위치/GPS 튐에 대한 필터링은 아직 없다(정지 판정, 스무딩 필요).
- 이야기 콘텐츠가 코드에 하드코딩되어 있다(JSON/캐시로 분리 예정).
- TTS 한국어 데이터가 없는 에뮬레이터에서는 무음으로 진행된다.
- release 서명 설정이 아직 없다.
