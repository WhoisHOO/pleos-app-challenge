# 작업 인계: 현재 상태와 이어서 작업하는 법

다른 컴퓨터나 새 작업 세션에서 바로 이어서 작업하기 위한 문서입니다. 처음 왔다면 이 문서부터 읽으세요. 기능이나 남은 작업이 바뀌면 이 문서도 같이 고칩니다.

- 마지막 갱신: 2026-10-31
- 마감: 2026-11-15 접수(이메일), 2026-11-23 1차 결과, 2026-12-03~04 2차 PT(실제 동작하는 앱으로 시연)
- 브랜치: `master` 하나. 동시에 여러 명이 작업하면 브랜치를 만들어 합칩니다.

## 0. 시작 체크리스트

1. 새 컴퓨터면 [5. 개발 환경](#5-개발-환경-새-컴퓨터)대로 설치하고 저장소를 clone 합니다. 이미 있으면 `git pull`.
2. [2. 남은 작업](#2-남은-작업)과 [3. 정해야 할 것](#3-정해야-할-것)을 확인하고, [4. 작업 규칙](#4-작업-규칙)을 지킵니다.
3. 새 PowerShell 창마다 [6. 자주 쓰는 명령](#6-자주-쓰는-명령)의 첫 블록(JAVA_HOME, adb, 스크립트 실행 허용)을 먼저 실행합니다.
4. 빌드와 테스트: `.\gradlew.bat :app:testDebugUnitTest` → 31개 통과면 정상입니다.
5. 에뮬레이터 켜기 → `.\gradlew.bat :app:assembleDebug` → 설치 → 실행(6장).
6. 작업을 마치면 커밋하고 `git push` 합니다. 다른 컴퓨터로 옮기기 전에 `git status -sb`에 `ahead`가 없어야 합니다.

## 1. 현재 상태

공모전 접수에 필요한 앱 기능은 모두 들어가 있고, Pleos Connect 에뮬레이터 v2.1.0에서 확인했습니다. 제출 APK(`apk/SOLOMON_Roadio_release.apk`)는 이 코드로 만든 release 서명본입니다.

| 기능 | 상태 | 확인 |
|---|---|---|
| 경로 투영·트리거·한 번에 하나씩 재생(코어) | 완료 | 단위 테스트 |
| 단일 화면 UI, 시뮬레이션 주행(데모 경로 4km, 이야기 7개) | 완료 | 에뮬레이터 |
| 음성: Pleos TextToSpeech(온디바이스) | 완료 | 에뮬레이터 |
| 오디오 포커스(통화·Gleo 중단 후 다시 들려주기) | 완료 | 에뮬레이터(Gleo) |
| 주행 중 사용(Distraction Optimized, CarUxRestrictions) | 완료 | Virtual Console |
| 차량 위치 모드: Pleos FusedLocation, 위치 안내·동의 | 완료 | 에뮬레이터 GPS 스크립트 |
| 백그라운드 재생, 차량 미디어 위젯, 스티어링 휠 키(Media3) | 완료 | 에뮬레이터 |
| 내비 연동: Pleos NaviHelper 경유지 안내, 회전 직전 이야기 미루기 | 완료 | 안내 시작은 에뮬레이터, 미루기는 단위 테스트 |
| 단위 테스트 | 31개 통과 | `:app:testDebugUnitTest` |

자세한 동작은 [README](../README.md), 설계는 [architecture.md](architecture.md)에 있습니다.

## 2. 남은 작업

### 2.1 접수 전(2026-11-15까지)

| 순서 | 작업 | 메모 |
|---|---|---|
| 1 | 블라인드 심사 대응 결정 | [3.1](#31-블라인드-심사). 새 팀원이 커밋하기 전에 정해야 합니다 |
| 2 | 접수양식(기획서) DOCX 작성 | [plan.md](plan.md) 초안을 양식으로 옮깁니다. §14의 MAU 숫자는 가정이라 공식 자료(Pleos Connect 탑재 차량 수 등)로 바꿉니다. 팀원 실명은 DOCX에만 쓰고 plan.md §15에는 쓰지 않습니다 |
| 3 | 개인정보 수집·이용 동의서 | 참가자 전원이 각각 서명·날짜. 저장소에 넣지 않습니다 |
| 4 | 시연 영상(60~90초, 선택·가산점) | 시나리오는 [submission-checklist.md](submission-checklist.md) §6. 에뮬레이터 화면 녹화에 이야기 소리가 담기는지 아직 확인하지 않았습니다 |
| 5 | 코드 라이선스 결정 | README에 "미정"으로 되어 있습니다 |
| 6 | release APK 다시 만들기 | 코드나 패키지 이름이 바뀐 경우. 서명 키를 가진 사람이 합니다 |
| 7 | 마감 전 최종 확인 | [submission-checklist.md](submission-checklist.md) §8, 대표자가 팀원 전원 CC로 이메일 제출 |

### 2.2 2차 PT 준비(2026-11-16~12-02)

- **Vehicle SDK 2.3.2**: 트립 요약(주행 거리·시간), 주행/정차 상태. 속도·기어는 표준 `CarPropertyManager`로도 받히며 에뮬레이터 Virtual Console 값을 따릅니다(확인함). 아직 의존성은 넣지 않았습니다.
- **이야기 콘텐츠 확충**: 지금은 데모 경로 1개, 이야기 7개가 `data/DemoRoute.kt`에 코드로 들어 있습니다. JSON·오프라인 캐시로 분리하고 경로와 이야기를 늘립니다. 콘텐츠는 직접 쓰거나 상업적으로 쓸 수 있는 공공데이터만 쓰고 출처를 적습니다.
- **PT 시연 시나리오**: 시뮬레이션 주행, 차량 위치 모드, 내비 안내, 휠 키, Gleo 중단을 한 흐름으로.
- **Gleo AI 음성 명령(선택)**: "이야기 꺼줘" 등. Gleo Interface 스키마 등록이 필요합니다(Media3 기반 음성 제어는 Pleos가 "향후 업데이트"로 안내).
- 확장(접수 뒤): 다국어, 아이 모드, 콘텐츠 파이프라인(공공 관광 데이터 → 초안 → 사람 검수 → 오프라인 캐시).

### 2.3 알려진 한계(필요하면 고칠 것)

- 프로세스가 끝나면 주행 진행 상태가 사라집니다(모드와 위치 동의만 남음).
- 경로 투영은 GPS 튐을 거르지 않습니다(속도 계산만 3m 미만을 정지로 봄).
- 차량 위치 모드도 데모 경로 위에서만 이야기가 나옵니다.

## 3. 정해야 할 것

### 3.1 블라인드 심사

접수양식에 "공유 링크와 첨부자료에도 개인정보·계정정보·개발자 실명이 노출되지 않도록" 하라는 조건이 있습니다. 지금 저장소에서는 다음이 보입니다.

- 저장소 주소의 GitHub 계정 이름(README, submission-checklist, 이 문서에 적혀 있음)
- 모든 커밋의 작성자 이름과 이메일. 첫 두 커밋은 개인 이메일, 나머지는 계정 이름과 숫자 ID가 든 GitHub noreply 주소
- 앱 패키지 이름 `com.whohoo.roadio`: 소스 경로, 문서의 명령, APK 안. 아직 출시 전이라 바꿀 수 있습니다(바꾸면 release APK를 다시 만듭니다)

대응은 셋 중 하나입니다. 대표자가 팀과 합의해 정합니다(이 합의가 4장의 강제 푸시 합의입니다).

1. 히스토리를 팀 이름으로 다시 쓰고 강제 푸시: 커밋 정보만 바뀝니다. 저장소 주소의 계정 이름과 패키지 이름은 남습니다. 이미 받아 둔 사람은 모두 다시 clone 합니다.
2. 익명 계정이나 조직으로 새 저장소를 만들어 그 주소로 제출: 팀 이름으로 정리한 히스토리를 올리면 주소와 커밋 정보가 정리됩니다. 패키지 이름은 남습니다.
3. 그대로 둠.

1이나 2를 고르면 패키지 이름도 같이 바꾸는 것이 좋습니다.

### 3.2 그 밖에

- 코드 라이선스(2.1의 5번)
- 팀원과 역할(접수양식에만 실명)

## 4. 작업 규칙

사람이든 도구든 이 저장소에서 작업할 때 모두 지킵니다.

**커밋과 저장소**

- 커밋 메시지는 영어로, `feat:`, `fix:`, `docs:`, `build:`로 시작하고 본문에 무엇을 왜 바꿨는지 적습니다.
- 커밋 메시지와 PR에 도구 서명이나 `Co-Authored-By` 같은 공동 작성자 줄을 넣지 않습니다.
- 커밋에는 작성자 이름과 이메일이 남습니다. 3.1이 정해지면 각자 저장소 안에서만(`--global` 없이) `git config user.name`, `git config user.email`을 팀 이름과 익명 주소로 맞춥니다.
- 작업을 마치면 커밋하고 바로 `git push` 합니다. 다른 컴퓨터는 push된 것만 받습니다.
- 공개 저장소에 영향을 주는 일(태그, 릴리스, 공개 범위 변경)은 대표자 확인 뒤에 합니다. 강제 푸시와 히스토리 재작성은 팀이 합의한 경우에만 합니다.

**비밀 정보와 개인정보**

- `keystore.properties`, `*.jks`, `local.properties`는 git에 넣지 않습니다(`.gitignore`에 있음).
- release 키와 비밀번호는 대표자가 저장소 밖에 보관합니다. 잃으면 같은 서명으로 업데이트할 수 없습니다.
- 저장소에 올리는 파일과 공유 링크에 실명, 연락처, 이메일, 개인 컴퓨터 경로를 쓰지 않습니다(블라인드 심사).
- 작성한 접수양식과 서명한 동의서는 저장소에 넣지 않고 접수 이메일로만 냅니다.
- 개인 이메일 같은 개인정보를 외부 서비스(요청 헤더, URL, 본문)에 넣지 않습니다.

**구성요소와 라이선스**

- 중국계 구성요소(라이브러리, 모델, 데이터)는 쓰지 않습니다.
- 코드, 모델 가중치, 데이터는 상업적으로 쓸 수 있는 라이선스만 씁니다.

**테스트**

- 실차나 다른 기기 없이, 에뮬레이터와 단위 테스트로 확인할 수 있는 것만 테스트합니다.
- 테스트 코드는 loopback(`127.0.0.1`)에만 바인딩합니다.

**개발 환경**

- 도구는 사용자 폴더에 설치하고 환경변수와 시스템 설정은 바꾸지 않습니다. `JAVA_HOME`과 스크립트 실행 허용은 그 창에만 적용합니다(6장 첫 블록). 시스템 설정을 바꿔야 한다면 선택 사항으로만 안내합니다.

**코드와 문서**

- 주변 코드의 이름, 주석 밀도, 방식을 따릅니다. 코드 주석과 커밋은 영어, README와 `docs/`는 한국어입니다.
- 이상해 보일 수 있는 동작(예: 주행 중 막힌 버튼)은 없애지 말고 화면에서 이유를 알려 줍니다("정차 후 변경").
- 기능을 바꾸면 README "현재 상태", [architecture.md](architecture.md), 테스트 개수, 이 문서를 같이 고칩니다.

## 5. 개발 환경 (새 컴퓨터)

지금 개발 PC는 Windows 11 x86_64, 메모리 16GB입니다. 에뮬레이터가 4GB를 쓰므로 16GB 이상을 권장합니다. 디스크는 약 30GB가 비어 있어야 합니다(Pleos 시스템 이미지는 받을 때 약 4GB, 풀면 약 18GB, AVD가 최대 약 10GB).

### 5.1 버전

| 항목 | 값 |
|---|---|
| JDK | 17 (Eclipse Temurin). JDK 24 이상과 Android Studio 번들 JBR 25에서는 Gradle 8.10.2가 실행되지 않습니다 |
| Gradle / AGP / Kotlin | 8.10.2(wrapper) / 8.8.2 / 2.0.0 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 28 |
| Media3 | 1.9.4 (1.10 이상은 compileSdk 36이 필요해 올리지 않음) |
| Pleos SDK | TextToSpeech 3.3.1, FusedLocation 1.1.3(+ fused-location-data 0.0.3), NaviHelper 3.0.5(+ navi-data 3.0.4) |
| 에뮬레이터 | Pleos Connect v2.1.0, `system-images;android-34;pleos_car_emulator;x86_64`, 기기 `Pleos_Connect_v2_0`, 2560×1440, RAM 4GB, Cold boot |

Pleos SDK는 `settings.gradle.kts`의 Pleos Maven 저장소에서 받습니다. 계정은 필요 없습니다.

### 5.2 설치(PowerShell, 모두 사용자 폴더)

Windows 사용자 이름에 한글이나 공백이 있으면 영문 경로(예: `C:\dev\`)에 clone 합니다. AGP는 영문이 아닌 경로에서 빌드를 멈춥니다. SDK와 AVD가 이런 경로에 있을 때는 이 PC에서 확인하지 않았습니다.

1. **Git**: [Git for Windows](https://git-scm.com/downloads/win). 관리자 권한 없이 쓰려면 PortableGit을 사용자 폴더에 풉니다.
2. **JDK 17**: [Temurin 17](https://adoptium.net/temurin/releases/?version=17)에서 Windows x64 **JDK**의 `.zip`을 받아 `%USERPROFILE%\.jdks\` 아래에 풉니다. 폴더 이름이 `jdk-17`로 시작해야 아래 명령이 찾습니다(예: `jdk-17.0.x+y`). Android Studio를 쓰면 `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`를 17로 지정합니다.
3. **Android SDK**: [Android 명령줄 도구](https://developer.android.com/studio#command-line-tools-only)(`commandlinetools-win-*_latest.zip`)를 받아 `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest\`에 풉니다(`latest\bin\sdkmanager.bat`이 되도록). 그다음:

   ```powershell
   $env:JAVA_HOME = (Get-ChildItem "$env:USERPROFILE\.jdks" -Directory | Where-Object Name -like 'jdk-17*' | Select-Object -First 1).FullName
   $sdk = "$env:LOCALAPPDATA\Android\Sdk"
   $sm = "$sdk\cmdline-tools\latest\bin\sdkmanager.bat"
   & $sm --sdk_root=$sdk --licenses
   & $sm --sdk_root=$sdk platform-tools 'platforms;android-35' 'build-tools;35.0.0' emulator
   ```

   `--licenses`는 묻는 라이선스마다 직접 `y`를 입력합니다(PowerShell에서 y를 파이프로 넣으면 실패했습니다). 4·5번도 이 세 변수(`$env:JAVA_HOME`, `$sdk`, `$sm`)를 씁니다. 창을 새로 열었다면 위 세 줄을 다시 실행합니다.

4. **가상화 가속 확인**: 에뮬레이터는 WHPX(Windows 하이퍼바이저 플랫폼)를 씁니다. `& "$sdk\emulator\emulator.exe" -accel-check`로 확인합니다. 꺼져 있으면 Windows 기능에서 켜야 합니다(시스템 설정이므로 각자 판단, 재부팅 필요). 이 PC는 따로 켜지 않아도 동작했습니다.
5. **Pleos 시스템 이미지**: `%USERPROFILE%\.android\repositories.cfg`를 아래 내용으로 만들고(이미 있으면 항목을 추가) 이미지를 받습니다(42dot SDK 라이선스 동의).

   ```text
   ### User Sources for Android SDK Manager
   count=1
   disp00=Pleos Connect System Image
   dist00=
   enabled00=true
   src00=https\://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml
   ```

   ```powershell
   & $sm --sdk_root=$sdk 'system-images;android-34;pleos_car_emulator;x86_64'
   ```

6. **AVD 만들기**:

   ```powershell
   'no' | & "$sdk\cmdline-tools\latest\bin\avdmanager.bat" create avd -n Pleos_Connect_v2.1.0 -k 'system-images;android-34;pleos_car_emulator;x86_64' -d Pleos_Connect_v2_0
   ```

   `%USERPROFILE%\.android\avd\Pleos_Connect_v2.1.0.avd\config.ini`에서 `hw.ramSize=4096`, `fastboot.forceColdBoot=yes`로 맞춥니다.

7. **저장소**:

   ```powershell
   git clone https://github.com/WhoisHOO/pleos-app-challenge.git
   ```

   저장소 루트에 `local.properties`를 만듭니다(Android Studio로 열면 자동으로 생김). 3.1이 정해졌다면 4장대로 이 저장소의 커밋 작성자도 맞춥니다.

   ```properties
   sdk.dir=C\:\\Users\\<사용자>\\AppData\\Local\\Android\\Sdk
   ```

8. **release 서명(서명하는 사람만)**: 대표자에게 받은 `.jks`를 저장소 밖에 두고, 저장소 루트에 `keystore.properties`를 둡니다. 형식은 [submission-checklist.md](submission-checklist.md) §5에 있습니다. 없어도 debug 빌드와 테스트는 됩니다.

## 6. 자주 쓰는 명령

PowerShell에서 저장소 루트 기준입니다. 새 창마다 먼저(모두 그 창에만 적용):

```powershell
$env:JAVA_HOME = (Get-ChildItem "$env:USERPROFILE\.jdks" -Directory | Where-Object Name -like 'jdk-17*' | Select-Object -First 1).FullName
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
Set-ExecutionPolicy -Scope Process Bypass -Force    # tools\*.ps1 실행 허용(Windows 기본값은 스크립트를 막음)
```

**빌드와 테스트**

```powershell
.\gradlew.bat :app:testDebugUnitTest    # 단위 테스트 31개
.\gradlew.bat :app:assembleDebug        # app\build\outputs\apk\debug\app-debug.apk
.\gradlew.bat :app:assembleRelease      # keystore.properties가 있어야 서명됨
copy app\build\outputs\apk\release\app-release.apk apk\SOLOMON_Roadio_release.apk
```

**에뮬레이터**

```powershell
.\tools\start-emulator.ps1              # 따로 떠 있는 프로세스로 켜고 부팅까지 기다림(이 PC에서 약 40초)
& $adb emu kill                         # 끄기. adb reboot은 에뮬레이터가 멈춰서 쓰지 않습니다
```

**설치와 실행**

```powershell
& $adb install -r app\build\outputs\apk\debug\app-debug.apk
& $adb shell am start -n com.whohoo.roadio/.MainActivity
```

debug와 release는 서명이 달라 서로 덮어쓸 수 없습니다. 바꿀 때는 `& $adb uninstall com.whohoo.roadio`를 먼저 합니다.

**확인용**

| 하려는 것 | 명령 |
|---|---|
| 화면 캡처 | `& $adb emu screenrecord screenshot "$env:TEMP\shot.png"` (`screencap`은 검은 화면) |
| 데모 경로로 GPS 주행 | `.\tools\drive-demo-route.ps1` (시속 50km, 1초마다) |
| GPS 한 점 | `& $adb emu geo fix 126.97690 37.57594` (경도, 위도 순) |
| Gleo 부르기(포커스 중단 시험) | `& $adb shell cmd car_service inject-key 231` |
| 휠 재생·일시정지 / 다음 | `& $adb shell cmd car_service inject-key 85` / `87` |
| 주행 상태(기어·속도) | `& $adb forward tcp:8088 tcp:8088` 후 브라우저로 `http://127.0.0.1:8088/` |
| 주행 제한 상태 | `& $adb shell dumpsys car_service --services CarUxRestrictionsManagerService` |
| 백그라운드 서비스 | `& $adb shell dumpsys activity services com.whohoo.roadio` (`isForeground=true`) |
| 미디어 세션 | `& $adb shell dumpsys media_session` |

Virtual Console을 브라우저 없이 쓰려면, 1초마다 `POST http://127.0.0.1:8088/browser/session?client=<이름>`을 보내 세션을 유지하면서 `PUT /vehicle/gear?gear=Drive`, `PUT /vehicle/controls?speed=30&accelerator=20&brake=0`을 보냅니다. 상태는 `GET /vehicle/state`. 끝나면 `speed=0&accelerator=0&brake=50`, `gear=Park`으로 되돌립니다.

## 7. 알려진 문제와 우회

| 문제 | 우회 |
|---|---|
| 에뮬레이터가 갑자기 꺼짐 | PC 메모리 부족입니다(에뮬레이터 약 4GB). 다른 도구의 백그라운드 작업으로 띄우면 메모리가 모자랄 때 그 도구가 에뮬레이터를 끌 수 있어, `tools/start-emulator.ps1`로 따로 띄웁니다. 그래도 느리면 다른 프로그램과 Gradle 데몬(`.\gradlew.bat --stop`)을 끕니다 |
| `adb reboot` 뒤 멈춤 | `adb emu kill`(안 되면 `qemu-system-x86_64` 프로세스 종료) 후 다시 켭니다 |
| 재설치 뒤 미디어 위젯이 "제목 정보 없음", 위젯 버튼 무반응 | 에뮬레이터를 다시 켭니다. 시연은 부팅 뒤 설치·실행한 상태에서 합니다 |
| 내비(Navi42)가 판교에 고정, 회전 거리가 변하지 않음 | 에뮬레이터 GPS 문장(NMEA) 체크섬 오류입니다. 회전 직전 미루기는 `TurnGuardTest`로 확인합니다 |
| Gleo 뒤 다시 들려준 이야기의 끝 알림이 오지 않음 | Pleos 음성 서비스 문제입니다. 이야기마다 시간 제한으로 넘어갑니다 |
| 위치 권한 창에 "알 수 없는 작업"만 보임 | 시스템 창 문구입니다. 앱 안 안내에서 먼저 설명합니다 |
| FusedLocation 오류가 일반 `Exception`으로 옴 | 메시지가 오류 코드입니다(800001 권한, 800002 위치 꺼짐). `VehicleLocation`이 풀어 씁니다 |
| 휠 키가 Roadio로 오지 않음 | 미디어 키는 가장 최근에 소리를 낸 앱으로 갑니다. 이야기는 Pleos 음성 서비스가 재생하므로 `MediaKeyClaim`이 0.2초 무음을 재생합니다. 첫 주행은 화면이나 미디어 위젯에서 시작합니다 |
| Pleos SDK가 `automotive required=true`를 넣음 | 매니페스트에서 `tools:replace`로 `required=false`로 덮어씁니다 |
| 빌드 때 "SDK XML version 4" 경고 | 명령줄 도구가 AGP보다 새것이라 나오는 경고입니다. 빌드에는 영향이 없습니다 |
| `.ps1`이 "스크립트를 실행할 수 없으므로" 오류 | Windows 기본 실행 정책입니다. 6장 첫 블록의 `Set-ExecutionPolicy -Scope Process`(그 창에만 적용)를 실행합니다 |

## 8. 어디에 무엇이 있나

| 위치 | 내용 |
|---|---|
| [README.md](../README.md) | 소개, 실행 방법, 조작, 오디오 동작(공모전 필수 항목 포함) |
| [docs/architecture.md](architecture.md) | 설계, SDK 연동, 검증 방법, 알려진 한계 |
| [docs/plan.md](plan.md) | 기획서 초안(접수양식으로 옮길 원본) |
| [docs/submission-checklist.md](submission-checklist.md) | 제출 요건과 마감 전 확인 |
| `app/src/main/java/com/whohoo/roadio/DocentEngine.kt` | 앱 전체 상태와 흐름의 중심. 여기서부터 읽습니다 |
| `app/src/main/java/com/whohoo/roadio/data/DemoRoute.kt` | 데모 경로와 이야기 7개 |
| `tools/` | 에뮬레이터 켜기, 데모 경로 GPS 주행 스크립트 |
| `apk/` | 제출 APK |
