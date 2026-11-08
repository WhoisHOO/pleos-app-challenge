# 시연 영상 제작 문서: Docentrip(도슨트립)

이 문서로 시연 영상 하나를 만듭니다. 영상은 두 부분입니다.

- **Part A "현대가 추억을 이어주다"(약 73초):** 영상 AI로 만드는 짧은 애니메이션입니다. 차 화면 속 Docentrip만 실제 앱 녹화를 합성합니다.
- **Part B 기능 시연(약 43초):** Pleos Connect 에뮬레이터에서 실제로 녹화한 앱 화면으로만 만듭니다. AI로 만든 앱 화면은 쓰지 않습니다.

**어디에 무엇을 넣나**

- **영상 AI(이미지 생성 포함)에는 5장의 영문 블록만 붙여 넣습니다.**
- 7장의 명령 블록은 녹화 담당이 이 PC의 PowerShell에서 씁니다(저장소 루트 기준).
- 나머지는 만드는 사람이 보는 설명입니다.

**조사와 검증**

- 시대 고증은 2026-11-03, 공모전 규정은 2026-11-04에 조사했고, 이 두 부분은 검증자가 다시 확인했습니다. 2026-11-07에 1995년 세종로 가운데의 은행나무 중앙녹지대를 더했습니다.
- 앱 동작과 화면 문구는 2026-11-07 기준 저장소 소스(`app/src/main/assets/route.json`, `ui/DocentripScreen.kt`, `DocentEngine.kt` 등)와 README·`docs/architecture.md`의 에뮬레이터 확인 기록에서 옮겼습니다.
- 2026-11-07 시험 녹화(`C:\tmp\docentrip-demo\`, 새 이름 화면)의 프레임과 소리 데이터로 시간과 화면 글자 일부를 확인했습니다. 시험 녹화의 소리는 사람이 직접 듣지 않았습니다. 음량을 재어 이야기가 나오는 구간에서만 소리가 커지는 것을 확인했고, 음량은 작습니다(광화문 이야기 구간 평균 약 -43dB, 최대 약 -27dB).
- 녹화 옵션은 에뮬레이터 콘솔 도움말(`adb emu help screenrecord start`)로 확인했습니다(2026-11-07).
- 영상 AI 도구의 길이·글자 수 제한은 2026-11-07에 각 공식 안내로 확인했습니다. 도구는 자주 바뀌므로 쓰기 전에 다시 봅니다.
- Part B는 최종 녹화 전 계획입니다. 녹화한 뒤 화면 글자와 시간이 이 문서와 맞는지 한 번 더 봅니다(11장, 12장 7번).
- 출처는 맨 아래에 있습니다.

## 1. 한눈에

| 항목 | 정한 것 |
|---|---|
| 앱 / 팀 | Docentrip(도슨트립 = Docent + Trip) / YOUNG:IN |
| 구성 | Part A(약 73초) → 연결 자막(4초) → Part B(약 43초) → 끝 카드(8초). 합계 약 2분 8초 |
| 제목(Part A) | 현대가 추억을 이어주다. "현대(現代, 지금 이 시대)"와 "현대자동차"의 이중 의미. 쓰기 전에 운영사무국에 확인할 것(2.4). 대체 제목: "길이 추억을 이어주다" |
| Part A 줄거리 | 2026년, 광화문 앞 신호에 멈춘 차. 조수석의 딸이 "아빠, 여기 어디야?" 하고 묻습니다. 아빠의 기억이 1995년으로 겹쳐집니다. 같은 자리 신호에서, 옛 세단 조수석의 어린 아빠에게 할아버지가 광화문 이야기를 들려줍니다. 할아버지의 이야기는 그 시대까지입니다. 신호가 바뀌어 출발하면서 딸이 Docentrip을 켜면, Docentrip이 광화문 이야기를 들려주고 "지금의 문은 2010년에 옛 모습대로 다시 세운 것입니다"로 할아버지의 이야기를 이어받습니다 |
| Part B 내용 | 실제 앱 기능 6가지: 경로 기반 자동 이야기, 말로 하기, 내비 연동, Gleo가 끼어들 때, 후진, 도착 요약. 공모전이 안내한 Pleos SDK 5개가 자막에 한 번씩 나옵니다 |
| 길이 | 공식 최소·최대 시간은 없습니다. 숏폼 챌린지용 세로판은 30초~1분으로 따로 만듭니다(10장) |
| 화면비 | 본편 16:9(1920×1080). 숏폼 9:16(1080×1920) |
| 스타일 | Part A: 손그림 느낌의 2D 애니메이션. 현재는 맑은 색, 1995년은 따뜻하고 바랜 필름 색. Part B: 에뮬레이터 화면 그대로, 자막과 확대(펀치 인)만 얹음 |
| 언어 | 대사와 자막은 한국어. 내레이션은 넣지 않습니다(9장). Part A의 대사는 모두 입이 안 보이게 찍습니다(4장) |
| 크레딧 | "Docentrip · Pleos Connect 앱 · YOUNG:IN" |
| 파일 이름 | `YOUNGIN_Docentrip_*.mp4`. Windows 파일 이름에는 ":"를 쓸 수 없어 YOUNG:IN을 YOUNGIN으로 씁니다(제출 APK `apk/YOUNGIN_Docentrip_release.apk`와 같은 규칙) |

## 2. 반드시 지킬 것

### 2.1 앱을 사실대로 보여 주기

공모전 안내: "제출한 서류에 기재한 내용이 사실과 다를 경우 선정이 취소될 수 있습니다."

**Docentrip의 실제 동작(2026-11-07 코드 기준)**

- 도슨트를 켜면 경로 위 이야기 장소 약 400m 앞에서 그곳 이야기가 저절로 시작됩니다.
  - 켜는 버튼은 시뮬레이션에서 "시뮬레이션 주행 시작", 차량 위치 모드에서 "도슨트 켜기"입니다.
  - 출발지인 광화문 이야기는 켜자마자 나옵니다.
- 이야기는 한 번에 하나, 장소마다 한 번 나옵니다. 하나가 끝나면 적어도 100m는 조용히 달리고, 다음 이야기는 그 장소 약 400m 앞에 이르러야 나옵니다. 데모 경로에서 이야기 사이 조용한 구간은 약 100~850m입니다.
- 이야기가 도중에 멈추거나 처음부터 다시 나오는 경우는 아래가 전부입니다.
  - **통화나 Gleo 음성 비서가 소리를 잠시 가져갈 때:** 멈췄다가, 끝나면 그 장소를 아직 지나지 않았으면 그 이야기를 처음부터 다시 들려주고, 지났으면 그 이야기를 끝냅니다. 다음 이야기는 평소처럼 그 장소 앞에서 나옵니다. 에뮬레이터에서는 Gleo로만 확인했습니다.
  - **다른 앱이 소리를 계속 가져갈 때(예: 음악을 틀 때):** 그 이야기는 끝나고 Docentrip은 일시정지합니다.
  - **사람의 조작**
    - 화면: "이야기 멈춤", "도슨트 끄기", "처음부터", "차량 위치로"·"시뮬레이션으로"
    - 미디어 위젯·핸들 키: 다음(이야기를 끝냄), 일시정지(이야기와 주행을 멈춤), 이전(마지막 이야기를 처음부터). 미디어 화면에서 다른 이야기를 고르면 그 이야기를 바로 들려줍니다.
    - "말로 하기": 듣는 동안 이야기를 잠시 멈추고, 들은 명령대로 합니다(아래). 명령이 아니거나 아무 말이 없으면, 그 장소를 아직 지나지 않았을 때 그 이야기를 처음부터 다시 들려줍니다.
  - **차량 위치 모드에서:** 기어가 R일 때, 위치 권한이 없어지거나 위치가 꺼질 때.
  - **최근 앱 목록에서 Docentrip을 닫을 때.**
  - 멈추지 않는 것: 시뮬레이션의 "주행 정지"(가상 차만 서고 이야기는 끝까지 나옴), 내비 경로 안내 음성(이야기와 함께 나옴).
- 질문에 답하지 않습니다. "말로 하기"를 누른 뒤에만 듣고, 명령 네 가지만 알아듣습니다. 비슷한 말도 같은 명령으로 받습니다.
  - "다음"(넘겨, 건너뛰어 등): 지금 이야기를 끝냅니다. 다음 이야기는 그 장소 앞에서 나옵니다.
  - "다시"(한 번 더, 반복 등): 마지막 이야기를 처음부터 들려줍니다.
  - "그만"(멈춰, 조용, 정지, 꺼 줘 등): 도슨트를 끕니다(시뮬레이션은 주행 정지). 이야기도 멈춥니다.
  - "켜 줘"(시작 등): 도슨트를 켭니다. 듣느라 멈춘 이야기는 그 장소를 아직 지나지 않았으면 처음부터 다시 들려줍니다.
  - Docentrip은 들은 말을 명령을 고르는 데만 쓰고 저장하지 않습니다. 화면과 Docentrip 로그에는 고른 명령만 남습니다.
- 차가 설정 제한을 걸면 설정 버튼(내비로 안내, 위치 소스 바꾸기)이 "정차 후 내비 안내", "정차 후 변경"으로 잠기고, 위치 동의 화면은 "주행 중에는 설정을 바꿀 수 없어요 · 정차한 뒤에 동의할 수 있어요"로 바뀝니다. 에뮬레이터는 기어가 D이면 0km/h에서도 잠갔습니다(2026-11-07 화면 캡처).

**Part A에서 지킬 것**

- 도슨트 대사는 `route.json`의 광화문 이야기 문장 그대로입니다: "방금 출발한 곳이 광화문 앞입니다. 광화문은 조선 왕조의 법궁인 경복궁의 정문으로, 지금의 문은 2010년에 옛 모습대로 다시 세운 것입니다. 이제 세종대로를 따라 남쪽으로 내려가며 서울의 오래된 길을 지나갑니다."
  - 앱 화면에 보이는 이야기 제목은 "광화문, 조선의 얼굴"입니다.
  - 길이는 약 19초로 잡습니다. 11-07 시험 녹화(`B1_auto_story.webm`)에서 이 이야기의 소리는 약 18.5초 이어졌습니다(소리 데이터로 본 추정. 글자당 0.13초로 셈하면 119자 약 15.5초). 테이크 M(7장)에서 다시 잽니다.
  - 가족이나 1990년대를 언급하는 맞춤 대사는 앱에 없으므로 넣지 않습니다.
- **도슨트 음성은 한 번에 끝까지 나오게 합니다.** Part A에서는 아무도 위의 조작을 하지 않으므로 앱도 끊지 않습니다. 대사나 효과음을 도슨트 위에 겹치지 않습니다.
- 딸의 "여기 어디야?"는 아빠에게 묻는 말입니다. 아무도 "말로 하기"를 누르지 않았으므로 Docentrip은 이 대화를 듣지 않습니다.
- **출발할 때 켭니다(S6-0).** 앱은 켜는 순간 차가 경로 출발점에 있으면 광화문 이야기를 바로 시작합니다. 신호를 기다리는 동안 이미 켜 두었다면 그때 이야기가 나왔어야 합니다. 그래서 신호가 바뀔 때 딸(동승자)이 화면을 한 번 누르는 1초 장면을 넣습니다. 그래야 "방금 출발한 곳이…"와도 맞습니다.
- 차 화면의 Docentrip은 실제 앱 녹화본(테이크 M)을 합성합니다.
  - 크로마 키 대신, 앱 자리를 꺼진 검은 화면으로 그린 정지 그림 위에 녹화본을 네 모서리에 맞춰 얹습니다(코너 핀, 5.3 S6a).
  - 마이크 아이콘, 말풍선, 대화창, 지도를 Docentrip 화면에 넣지 않습니다. 다른 실내 장면의 중앙 화면은 어둡고 흐리게 둡니다. AI가 그린 지도나 아이콘이 Docentrip 화면으로 보이면 안 됩니다.
  - 합성한 화면에 "실제 앱 화면 · 에뮬레이터 녹화(시뮬레이션 주행)" 자막을 붙입니다. 녹화본 화면에 "시뮬레이션"이 보이기 때문입니다.

**Part B에서 지킬 것**

- 앱 화면은 모두 에뮬레이터 녹화본입니다. AI로 만든 UI, 목업, 다시 그린 버튼을 쓰지 않습니다. 녹화본의 글자를 고치거나 지우지 않습니다.
- Part B 내내 오른쪽 위에 "실제 앱 화면 · 에뮬레이터 녹화"를 작게 띄웁니다.
- 화면의 "시뮬레이션"은 가리지 않고 자막으로 설명합니다. 시뮬레이션 클립(B1, B2, B3, B4, B6)에는 오른쪽 위 표시 아래에 "시뮬레이션 주행(가상 50km/h)"을 띄우고, 전체 화면 클립(B3, B4)에서는 왼쪽 차량 정보가 에뮬레이터 차의 정차 상태(P, 0km/h)라는 것도 적습니다(6.2).
- 빨리 감거나, 건너뛰거나, 느리게 하거나, 멈춘 곳은 "16배속", "약 16초 뒤", "정지 화면"처럼 표시합니다.
- 앱이 이야기를 멈추는 장면(B2, B4, B5)은 멈추기 전부터 멈춘 뒤(또는 다시 시작할 때)까지 자르지 않습니다. 편집으로 자른 것이 앱이 멈춘 것처럼 보이면 안 됩니다.
- 자막과 표시는 모두 영상에 입힙니다(9장).
- 자막에는 확인한 것만 씁니다. 아래 말은 쓰지 않습니다.

| 쓰지 않을 말 | 이유 |
|---|---|
| 사람 목소리를 알아듣는 장면처럼 보이는 자막 | 이 PC 마이크가 무음이라 녹음한 명령으로만 확인했습니다 |
| 주어 없는 "들은 말은 저장하지 않음" | Pleos SDK와 Speech Service는 들은 글을 시스템 로그에 남기고, Docentrip은 그것을 막을 수 없습니다. "Docentrip은 …"으로 Docentrip이 하는 일만 말합니다(architecture.md 5.4) |
| "통화가 오면 멈춥니다"(시연 자막으로) | 처리는 같지만 에뮬레이터에서는 Gleo로만 확인했습니다 |
| "회전 직전에는 이야기를 미룹니다"(화면으로) | 에뮬레이터 내비가 판교에 고정돼 있어 단위 테스트로만 확인했습니다 |
| "차의 트립 미터로 잽니다" | 에뮬레이터가 트립 미터를 지원하지 않아 단위 테스트로만 확인했습니다 |
| "실차에서 동작합니다" | 실차 시험은 없습니다 |
| "어느 길에서나 이야기를 들려줍니다" | 이야기는 데모 경로(광화문 → N서울타워) 하나뿐이고, 차량 위치 모드도 그 경로 위에서만 들려줍니다 |
| "Gleo에게 '이야기 꺼줘'" | Pleos 스키마 승인이 필요해 아직 없습니다 |
| 다국어, 아이 모드, 이야기 자동 생성 | 계획입니다 |

### 2.2 시대 고증

| | 1995년 봄(8월 전) | 2026년 |
|---|---|---|
| 광화문 | 1968년 철근 콘크리트로 다시 지은 문. 현판은 한글 "광화문". 경복궁 축에서 3.75° 틀어짐 | 2010년 제자리에 나무로 다시 지은 문. 현판은 한자 "光化門", 검은 바탕에 금색 글자(2023) |
| 문 앞 | 월대 없음. 도로가 문 바로 앞을 지남. 해태상 둘이 문 양옆 담장에 바짝 붙어 있음 | 돌로 된 월대(가운데 어도). 해태상은 월대 남쪽 끝 근처. 도로는 월대를 둥글게 돌아감 |
| 문 뒤 | 돔이 있는 거대한 화강암 건물(옛 조선총독부 청사, 당시 국립중앙박물관). 1995-08-15 첨탑부터 철거 시작 | 흥례문과 경복궁. 총독부 건물 없음 |
| 세종로 | 광화문에서 세종로 사거리까지 약 600m, 왕복 16차로. 가운데에 키 큰 은행나무가 줄지어 선 좁은 중앙녹지대(29그루. 2008년 광화문광장 공사 때 옮겨 심음). 광장 없음. 이순신 장군 동상(1968)은 사거리 쪽, 문에서 약 600m 남쪽 | 광화문광장(나무, 분수). 차도는 동쪽, 광장은 서쪽(세종문화회관 쪽). 광화문을 정면(북쪽)으로 보면 광장은 왼쪽, 차도는 오른쪽. 이순신 장군 동상과 금색 좌상 세종대왕 동상(2009) |
| 차 | 각진 1세대 그랜저(1986~1992). 원스포크 핸들, 비행기 조종간 모양 변속 레버(현대차 헤리티지 자료). 이 고유 특징은 운영사무국 답을 받은 뒤에만 씀(2.4) | 2026 더 뉴 그랜저(Pleos Connect가 처음 들어간 차, 17인치 중앙 디스플레이) |

**넣지 말 것**

- 1995년 장면: 세종대왕 동상, 광화문광장, 한자 현판, 월대, 유리 고층 빌딩
- 2026년 장면: 총독부 건물, 한글 현판(2026-10 기준 논의 중일 뿐), "감사의 정원" 석조물 줄(2026-05 개장, 정치적 논란)
- 공통: 한 화면에 광화문 해태상과 이순신 장군 동상을 같이 보이게 하지 않습니다. 약 600m 떨어져 있습니다.

### 2.3 안전(현대차가 심사함)

- **안전띠:** 모든 사람이 맵니다. 1995년의 할아버지와 어린 아빠도 맵니다. 당시 법도 앞좌석 안전띠는 의무였습니다(1990-11부터).
- **딸:** 고등학생(약 16세)으로 보이게 하고 좌석을 뒤로 빼 둡니다. 현대차 그랜저 설명서는 어린이는 뒷좌석에, 10~15세 정도가 앞에 앉아야 하면 안전띠를 매고 좌석을 최대한 뒤로 빼라고 합니다.
- **1995년의 어린 아빠:** 12세 정도로 보이게 하고 안전띠 맨 모습이 보이게 합니다.
- **운전자:** 차가 움직이는 동안에는 두 손을 핸들에, 눈을 앞에 둡니다. 할아버지가 손으로 가리키는 것은 신호에 완전히 멈춰 있을 때만입니다.
- **화면 조작:** 운전자는 화면을 만지지 않습니다. 도슨트를 켜는 장면(S6-0)은 딸(동승자)이 누릅니다.

**Part B 클립별 조작과 그때 차의 상태**

| 클립 | 화면 조작 | 그때 차 | 지킬 것 |
|---|---|---|---|
| B1 | "시뮬레이션 주행 시작" 한 번 | 정차(0 km/h에서 누름) | 누르는 장면은 출발 전입니다. 실차라면 출발 전이나 동승자가 누릅니다 |
| B2 | 없음(녹음한 명령) | 시뮬레이션 주행 중 | 자막에 "운전자가 화면을 누른다"고 쓰지 않습니다. 실제로는 "말로 하기"를 누른 뒤에만 듣는다는 것을 정직 표시로 밝힙니다(6.4) |
| B3 | "내비로 안내", 미디어 위젯 재생 버튼 | 정차 필수 | 실차는 달리는 동안 이 버튼이 "정차 후 내비 안내"로 잠깁니다. 선택 장면 B3-0이 그것을 보여 줍니다 |
| B4 | 없음(핸들 음성 버튼. 에뮬레이터에서는 키 입력 명령) | 주행 중 | 화면을 보지 않는 조작입니다 |
| B5 | "차량 위치로", 동의, "도슨트 켜기" | 정차, 그 뒤 기어 R | 조작은 모두 정차 중에 합니다. 후진도 멈춘 차에서 합니다 |
| B6 | 없음 | 도착 | - |

- 녹화본에는 손이 나오지 않습니다. 그래도 자막과 편집이 "달리는 차에서 운전자가 화면을 만진다"로 읽히지 않게 합니다.

### 2.4 상표, 개인정보, 권리

**운영사무국(appdevchallenge@hyundai-partners.com)에 먼저 물을 것**

- 공모전 규정은 상표·디자인권을 적법하게 쓸 것을 요구하고, 운영사(현대자동차·기아)의 지식재산권도 침해 대상에 넣습니다. 다음은 허락이 없습니다: 현대 로고, 차 이름, 차량 디자인(오페라 글라스 같은 고유 특징, 1세대 그랜저 실내의 원스포크 핸들과 비행기 조종간 모양 변속 레버), 제목의 "현대".
- 답을 받기 전에는 엠블럼·로고·글자 없는 일반적인 차(1995년은 각진 1980년대 세단과 평범한 실내, 2026년은 현대적인 대형 세단)로 만들고, 대체 제목을 준비합니다.
- 끝 카드의 크레딧 "Docentrip · Pleos Connect 앱 · YOUNG:IN"은 남겨, Docentrip이 현대차 기본 기능이 아니라 출품작임을 분명히 합니다.

**노출 금지 조건(접수양식 문구. 팀은 "블라인드"라고 부름)**

- 원문: "공유 링크와 첨부자료에도 개인정보·계정정보·개발자 실명이 노출되지 않도록 확인하세요."
- 자막, 크레딧, 내레이션, 파일 정보(작성자·제목), 링크 소유자 이름에 실명과 계정을 넣지 않습니다. 크레딧은 "Docentrip", "YOUNG:IN"만 씁니다.
- 영상 AI에 넣는 프롬프트에도 개인정보를 넣지 않습니다. 5장의 영문 블록에는 없습니다.
- 저장소 주소에는 계정 이름이 들어 있습니다. 주소는 접수양식에 적으므로 영상(자막, 끝 카드)에는 넣지 않습니다.
- `adb emu screenrecord`는 에뮬레이터 화면만 담습니다. PC 바탕화면, PowerShell 창, 파일 경로는 녹화되지 않습니다. 다른 녹화 도구를 쓰면 녹화 범위를 에뮬레이터 창으로 제한합니다.

**에뮬레이터 화면**

- Part B에는 Pleos Connect 시스템 화면(왼쪽 주행 정보와 미디어 위젯, 내비, Gleo, 후방 카메라)이 나옵니다.
- 공식 개발용 에뮬레이터이고, 출품 유의사항도 2차 PT에서 "에뮬레이터 구동 화면" 시연을 인정합니다. 그래서 심사 제출용 영상에는 그대로 씁니다.
- 11-07 시험 녹화의 전체 화면에는 왼쪽 위에 에뮬레이터 차의 "안전 벨트를 매세요." 경고가 늘 보였습니다. 7.1에서 끌 수 있는지 보고, 끌 수 없으면 그대로 두되 자막으로 가리지 않습니다.
- SNS에 올리는 숏폼에 이 화면을 쓸지는 운영사무국 질문에 함께 넣습니다(12장).

**그 밖의 권리**

- 번호판은 읽히지 않게, 실존 인물(전직 대통령 등)의 얼굴은 넣지 않습니다.
- 음악, 목소리, 효과음, 글꼴(자막 포함)은 직접 만들었거나, 공모전 제출·SNS 게시·행사 상영까지 쓸 수 있게 허락받은 것만 씁니다. 숏폼 안내는 "숏폼 챌린지로 제출한 영상은 현대자동차그룹 행사 시상식에서 사용될 수 있습니다"라고 하고, 초상권·저작권 문제의 책임을 응모자에게 둡니다.
- 사람이 목소리를 녹음하면 서면 동의를 받습니다(어린이는 보호자 동의). 합성 음성은 상업 이용이 되는 비중국계 서비스로 하고, 실존 인물의 목소리를 흉내 내거나 복제하지 않습니다(9장).
- 1995년 라디오 소리는 실제 방송이나 노래 없이 잡음만 씁니다.

**영상 AI와 편집 도구**

- 이용약관에서 결과물을 공모전과 상업 목적으로 써도 되는지 확인합니다.
- 워터마크(계정 이름이 들어가는 것 포함) 없이 내보낼 수 있는 요금제를 씁니다.
- 팀 규칙상 중국계 도구는 피합니다.
  - 영상 AI: Kling, Hailuo, Seedance, Vidu, Wan, PixVerse, Hunyuan, Jimeng(Dreamina) 등
  - 편집기: CapCut
- 중국계가 아닌 예입니다. 쓰기 전에 약관을 확인합니다.
  - 영상 AI: Runway, Google Veo, Adobe Firefly
  - 편집기: DaVinci Resolve, Adobe Premiere Pro, Microsoft Clipchamp
  - 합성 음성: Typecast(한국), ElevenLabs(미국)

## 3. 등장인물과 차(모든 장면에서 같게)

| 이름표 | 설명 |
|---|---|
| DAD_2026 | 43세 한국인 남성. 짧은 검은 머리, 부드러운 인상, **자기 왼쪽 눈썹 위 작은 점**(정면에서 보면 화면 오른쪽). 연한 하늘색 셔츠. **왼손목에 오래된 은색 손목시계**(할아버지 것) |
| DAD_1995 | 12세 소년. 같은 얼굴형, 같은 자리의 작은 점. 90년대 줄무늬 티셔츠 |
| GRANDPA_1995 | 42세 한국인 남성. 흰 와이셔츠, 넥타이 느슨하게, **왼손목에 은색 손목시계**(DAD_2026과 같은 시계). 웃을 때 눈가 주름 |
| DAUGHTER_2026 | 16세 고등학생. 어깨 길이 머리, 회색 후드티 |
| WATCH | 오래된 은색 손목시계(둥근 흰 판, 금속 줄). 1995년에는 할아버지가, 2026년에는 아빠가 왼손목에 참 |
| CAR_2026 | 짙은 회색의 현대적인 대형 세단. 엠블럼·글자 없음. 17인치 와이드 중앙 디스플레이 |
| CAR_1995 | 각진 1980년대 대형 세단. 엠블럼·글자 없음. 벨벳 느낌 시트, 가는 두 갈래(투 스포크) 핸들, 평범한 콘솔 변속 레버. 1세대 그랜저의 원스포크 핸들·비행기 조종간 모양 변속 레버는 운영사무국 답을 받은 뒤에만 씀(2.4) |

- 점과 손목시계가 두 시대를 잇는 표시입니다. 할아버지의 시계를 지금 아빠가 차고 있습니다.
- DAD_1995와 DAD_2026은 같은 쪽(인물의 왼쪽)에서 찍어 점이 보이게 합니다.
- 이름표는 만드는 사람이 보는 표시입니다. 영상 AI에 붙여 넣는 영문에는 "the father"처럼 씁니다(5장).

## 4. Part A 장면 구성(약 73초)

**소리 표기**

- 화면 밖: 입이 안 보이게 찍는 대사입니다. Part A의 대사는 모두 화면 밖입니다(립싱크 없음). 2D 손그림은 립싱크가 불안정하고, 팀 규칙상 중국계 립싱크 도구는 쓸 수 없습니다.
- 말하는 사람은 뒤에서 보이거나 화면 밖에 있습니다. 얼굴이 보이는 사람은 입을 다물고 듣습니다.

| 장면 | 시간 | 화면 | 소리 | 전환 |
|---|---|---|---|---|
| S1 | 0~6초 | 2026년 늦은 오후. 광화문을 정면(북쪽)으로 본 넓은 화면. 월대 앞 신호에 멈춘 CAR_2026(화면 왼쪽을 향하고, 문이 차의 오른쪽). 화면 왼쪽은 나무 많은 광장, 오른쪽은 차도. 뒤로 나무 광화문(현판은 작게, 읽히지 않을 만큼) | 도시 소리, 방향지시등 | - |
| S2 | 6~15초 | 차 안(뒷좌석에서 본 넓은 화면). DAUGHTER_2026이 창밖 광화문을 보며 묻고(얼굴은 창 쪽), 아빠가 대답하는 동안 아빠 쪽으로 고개를 돌려 들음. 아빠는 뒤에서 보임 | 딸(화면 밖): "아빠, 여기 어디야?" 아빠(화면 밖): "광화문. 아빠도 어릴 때 할아버지랑 여기서 신호 기다렸었지…" | - |
| S3 | 15~18초 | DAD_2026의 왼손(은색 시계)이 핸들 위에 → 같은 구도로 GRANDPA_1995의 왼손(같은 시계)이 1980년대 핸들 위에 | 2026년 소리가 작아지고 1995년 거리 소리(옛 버스 엔진, 라디오 잡음)가 들어옴 | 편집에서 디졸브와 색 전환(클립 2개) |
| S4a | 18~23초 | 1995년 세종로 넓은 화면. 왕복 16차로와 가운데 은행나무 중앙녹지대, 90년대 차와 버스. 멀리 콘크리트 광화문과 그 뒤 돔 있는 화강암 건물 | 할아버지(화면 밖): "저기 광화문 보이지? 경복궁 정문이야." | - |
| S4b | 23~31초 | 광화문 바로 앞 신호에 멈춘 CAR_1995 안(뒷좌석에서, 두 사람은 뒤에서 보임). 앞 유리 너머 흐릿한 광화문과 거대한 돔 건물. 할아버지가 한 손을 들어 가리키고, 안전띠 맨 소년이 몸을 앞으로 기울임 | 할아버지(화면 밖): "6·25 때 불타서, 지금 저 문은 1968년에 콘크리트로 다시 지은 거란다." | - |
| S4c | 31~39초 | S4c-1(2초): 소년의 뒷모습, 돔 건물을 올려다봄 → S4c-2(6초): 소년 얼굴 클로즈업, 입을 다물고 듣다가 살짝 웃음. 할아버지는 화면 밖 | 소년(화면 밖, S4c-1): "뒤에 큰 건물은요?" 할아버지(화면 밖, S4c-2): "옛날 총독부 건물. 곧 헐린대. 광화문도 언젠가 제 모습을 찾겠지." | - |
| S5 | 39~42초 | 앞을 보는 DAD_1995의 옆얼굴 → 같은 구도로 앞을 보는 DAD_2026의 옆얼굴(같은 점) | 1995년 소리가 멀어짐. 신호가 초록으로 바뀌는 순간은 음악 큐로(신호등 소리는 차 안에서 들리지 않음) | 편집에서 디졸브와 색 전환(클립 2개) |
| S6-0 | 42~43초 | 신호가 초록으로 바뀐 순간, 차 안(뒷좌석에서). 딸이 중앙 디스플레이 아래쪽을 한 번 누름. 디스플레이는 흐릿해 내용이 안 보임(편집에서 테이크 M의 누르기 전 화면을 흐리게 얹음). 아빠는 두 손을 핸들에, 눈은 앞에 | 차 안 소리 | - |
| S6 | 43~62초 | S6a(5초): 차가 출발. 중앙 디스플레이 클로즈업. 왼쪽 주행 정보는 가장자리만, 오른쪽 앱 자리에 Docentrip **실제 앱 녹화본**(테이크 M에서 누른 직후의 "재생 중 · 광화문, 조선의 얼굴")을 코너 핀으로 합성. 앱 자리가 화면 폭의 약 70% 이상. 자막 "실제 앱 화면 · 에뮬레이터 녹화(시뮬레이션 주행)" → S6b-1(7초): 딸이 오른쪽 창밖 광화문광장을 봄 → S6b-2(7초): 아빠가 앞을 보며 미소 | **도슨트(앱 실제 음성, 끝까지 한 번에):** "방금 출발한 곳이 광화문 앞입니다. 광화문은 조선 왕조의 법궁인 경복궁의 정문으로, 지금의 문은 2010년에 옛 모습대로 다시 세운 것입니다. 이제 세종대로를 따라 남쪽으로 내려가며 서울의 오래된 길을 지나갑니다." | - |
| S7 | 62~69초 | 앞좌석 머리받침 바로 뒤에서(또는 차 밖 뒤쪽 3/4에서) 본 두 사람의 뒷모습. 딸이 아빠 쪽으로 고개를 돌렸다가 앞을 봄. 아빠는 앞을 본 채 고개를 살짝 끄덕임 | 딸(화면 밖): "할아버지도 여기 얘기해 줬어?" 아빠(화면 밖): "응. 그땐 아빠가 네 자리에 앉아 있었어." | - |
| S8 | 69~73초 | 광화문 위 높은 곳에서 남쪽으로 세종대로를 내려다보는 항공 화면. 차가 카메라에서 멀어짐. 지평선에 남산과 작은 탑, 광장은 화면 오른쪽 | 잔잔한 음악 | 제목 자막 1장(70~72초): "현대가 추억을 이어주다". 크레딧은 끝 카드로 옮겼습니다(8장) |

**대사를 쓸 때 지킬 것**

- 할아버지 대사는 1995년에 사실이었던 것만 말합니다. 2010년 복원, 월대, 광장은 말하지 않습니다.
- "6·25 때 불탔다", "1968년 콘크리트로"는 국가기록원 자료와 맞습니다.
- 연도가 자료마다 다른 것은 말하지 않습니다: 광화문 이름을 붙인 해, 일제강점기에 옮긴 해(1926·1927).
- **대사를 먼저 녹음합니다(임시 녹음도 됨).** 그 길이로 정지 그림 애니마틱을 만들어 장면 길이를 정한 뒤 영상을 생성합니다. 위 표의 시간은 도슨트와 같은 빠르기(초당 약 5.4음절)로 읽고 주고받는 틈과 동작을 더한 추정입니다. 대사 음절 수: S2 31, S4a 15, S4b 32, S4c 32, S7 27(숫자는 읽는 대로 셈: 육이오, 천구백육십팔년).
- S7 대사는 도슨트가 끝난 뒤에 나옵니다. 도슨트 위에 대사를 겹치지 않습니다.
- S6 길이는 약 19초(11-07 시험 녹화 기준)로 잡고, 테이크 M에서 잰 실제 도슨트 길이에 맞춥니다. 길이가 달라진 만큼 뒤 장면을 밉니다.
- 시간이 부족하면 S4b의 할아버지 대사를 줄이고, 도슨트(약 19초)는 줄이지 않습니다. S7은 대사를 줄여도 5초 밑으로는 줄일 수 없습니다.

## 5. Part A 영상 AI용 프롬프트(이 장의 영문 블록만 붙여 넣기)

**만드는 순서**

1. 5.2 기준 시트를 만듭니다(인물·차·손목시계마다 한 장). 시트에도 5.1 공통 줄과 그 인물의 시대 줄을 붙입니다.
2. 장면마다 시작 프레임을 이미지로 만듭니다. 이미지 생성 도구에 5.3 표의 시트를 참조 이미지로 넣고(예: Runway References, Google Veo의 참조 이미지, Adobe Firefly 참조 이미지), 5.1 공통 줄 + 그 장면의 시대 줄 + 5.3 블록의 `Start frame:` 뒤 문장을 붙입니다.
3. 그 시작 프레임으로 image-to-video를 하고, 5.3 블록의 `Motion:` 뒤 문장만 붙입니다. 그림은 시작 프레임이 정하므로 스타일 줄은 다시 붙이지 않습니다.
4. 5.4 제외어는 negative prompt 칸이 있는 도구에만, 그 장면의 시대에 맞는 블록만 넣습니다.
5. 편집에서 자르고 이어 붙입니다.

**지킬 것**

- 시트를 시작 프레임으로 쓰지 않습니다. 시트 자체가 움직이는 영상이 나옵니다.
- 영상 AI는 따로 만든 이미지를 기억하지 못합니다. DAD_2026 같은 이름표는 사람이 보는 표시이고, 영문 블록에서는 "the father", "the teenage daughter"처럼 씁니다. 참조 태그를 쓰는 도구(예: Runway References의 @태그)에서만 태그로 잇습니다.
- 도구마다 넣을 수 있는 참조 이미지 수가 정해져 있습니다. 많으면 인물 시트를 먼저 넣습니다.
- DAD_1995 시트는 DAD_2026 시트를 참조로 넣어 같은 사람의 12세로 만듭니다. S3b·S5b 시작 프레임은 S3a·S5a 시작 프레임을 참조로 넣고 고쳐서 구도를 맞춥니다.
- 길이는 영문 블록에 쓰지 않습니다(모델에게는 뜻이 없음). 도구는 정해진 길이로만 만드는 경우가 많습니다(2026-11-07 확인: Runway Gen-4는 5·10초, Google Veo 3.1은 4·6·8초이고 참조 이미지로 만들면 8초). 4장 길이보다 길게 만들고 편집에서 자릅니다. 2초짜리 S3a·S3b·S5a·S5b는 시작 프레임 정지 그림에 편집기의 느린 밀기를 얹어도 됩니다(모양이 덜 바뀜).
- 글자 수 한도가 있습니다(예: Runway Gen-4 프롬프트 1,000자). 시작 프레임용(스타일 줄 + `Start frame:`)과 움직임용(`Motion:`)을 나눠 붙이면 한도 안에 들어갑니다.
- 원하는 상태를 긍정형으로 씁니다. Runway 안내는 부정형 문장이 듣지 않거나 반대 결과를 낼 수 있다고 합니다. negative 칸이 없는 도구에는 "Avoid:" 줄을 붙이지 않고, 어긋난 결과는 다시 만들어 고릅니다.
- 생성된 클립의 자체 소리는 모두 끄고, 9장의 한국어 음성을 편집에서 넣습니다.
- 편집에서 좌우 반전하지 않습니다. 점, 시계, 운전석 자리가 바뀝니다.

### 5.1 스타일 줄

공통(모든 시트와 시작 프레임):

```text
Warm, cinematic 2D hand-painted animation, soft lighting, Korean family drama tone. All signs, signboards and licence plates are blank or blurred. Cars carry no badges, logos or lettering. Everyone in a car wears a seat belt.
```

2026년 장면:

```text
Present day: clean natural colours, late-afternoon golden light.
```

1995년 장면:

```text
Spring 1995 flashback: warm faded film look, slight grain, lower saturation.
```

### 5.2 기준 시트(먼저 생성, 인물·차·소품마다 한 장)

DAD_2026:

```text
Character sheet on a plain background: front, three-quarter and left-profile views of a Korean man, 43, with short black hair, a gentle face, a small mole just above his own left eyebrow, a light-blue shirt and an old silver wristwatch on his left wrist.
```

DAD_1995(DAD_2026 시트를 참조 이미지로 넣음):

```text
Character sheet on a plain background: front, three-quarter and left-profile views of the man in the reference image as a 12-year-old Korean boy, with the same face shape and the same small mole just above his own left eyebrow, wearing a striped 1990s T-shirt.
```

GRANDPA_1995:

```text
Character sheet on a plain background: front, three-quarter and left-profile views of a Korean man, 42, in 1995, wearing a white dress shirt with a loosened tie and an old silver wristwatch on his left wrist, with a kind smile and crow's feet.
```

DAUGHTER_2026:

```text
Character sheet on a plain background: front, three-quarter and left-profile views of a Korean girl, 16, a high-school student with shoulder-length black hair, wearing a grey hoodie.
```

WATCH:

```text
Prop sheet on a plain background: an old silver wristwatch with a round white face and a metal bracelet, front and side views.
```

CAR_2026:

```text
Reference sheet on a plain background: a modern dark-grey full-size luxury sedan with smooth, plain bodywork, front three-quarter and side views, plus its interior: a wide 17-inch central display across the dashboard, its left part for driving information and its right part an app area, shown dark.
```

CAR_1995:

```text
Reference sheet on a plain background: a boxy 1980s full-size sedan with straight lines and plain bodywork, front three-quarter and side views, plus its interior: velour seats, a thin two-spoke steering wheel and a plain console gear lever.
```

- CAR_1995의 핸들과 변속 레버는 운영사무국의 답을 받으면 1세대 그랜저 특징(원스포크 핸들, 비행기 조종간 모양 변속 레버)으로 바꿀 수 있습니다(2.4).

### 5.3 장면별 블록

| 장면 | 참조로 넣을 시트·프레임 | 시대 줄 | 제외어(5.4) | 편집에서 쓰는 길이 |
|---|---|---|---|---|
| S1 | CAR_2026 | 2026 | 5.4a + 5.4c | 6초 |
| S2 | DAD_2026, DAUGHTER_2026, CAR_2026 | 2026 | 5.4a + 5.4c | 9초 |
| S3a | DAD_2026, WATCH | 2026 | 5.4a + 5.4c | 2초(디졸브로 겹쳐 S3a·S3b 합계 3초) |
| S3b | S3a 시작 프레임, GRANDPA_1995, WATCH | 1995 | 5.4a + 5.4b | 2초 |
| S4a | 없음 | 1995 | 5.4a + 5.4b | 5초 |
| S4b | GRANDPA_1995, DAD_1995, CAR_1995 | 1995 | 5.4a + 5.4b | 8초 |
| S4c-1 | DAD_1995, CAR_1995 | 1995 | 5.4a + 5.4b | 2초 |
| S4c-2 | DAD_1995, CAR_1995 | 1995 | 5.4a + 5.4b | 6초 |
| S5a | DAD_1995 | 1995 | 5.4a + 5.4b | 2초(디졸브로 겹쳐 S5a·S5b 합계 3초) |
| S5b | S5a 시작 프레임, DAD_2026 | 2026 | 5.4a + 5.4c | 2초 |
| S6-0 | DAD_2026, DAUGHTER_2026, CAR_2026 | 2026 | 5.4a + 5.4c | 1초(5초 이상 만들어 누르는 1초만) |
| S6a | CAR_2026 | 2026 | 5.4a + 5.4c | 5초(정지 그림 + 합성) |
| S6b-1 | DAD_2026, DAUGHTER_2026, CAR_2026 | 2026 | 5.4a + 5.4c | 7초 |
| S6b-2 | DAD_2026, CAR_2026 | 2026 | 5.4a + 5.4c | 7초 |
| S7 | DAD_2026, DAUGHTER_2026, CAR_2026 | 2026 | 5.4a + 5.4c | 7초 |
| S8 | CAR_2026 | 2026 | 5.4a + 5.4c | 4초 |

- 각 블록에서 `Start frame:` 뒤 문장은 이미지 생성에, `Motion:` 뒤 문장은 image-to-video에 붙입니다(앞의 표시는 빼고).
- S6a는 영상을 만들지 않습니다. 정지 그림 한 장을 만들고 편집에서 합성합니다. ① 앱 자리는 꺼진 검은 화면으로 둡니다. ② 테이크 M 앱 영역(6.2의 자르기)을 네 모서리에 맞춰 얹습니다(코너 핀). ③ 유리 반사와 밝기만 살짝 얹고 앱 글자는 바꾸지 않습니다. ④ 창밖 빛의 흐름과 아주 느린 밀기는 편집에서 넣습니다. 영상 AI가 만든 초록 면은 색과 1.39:1 비율이 흔들려 쓰지 않습니다.

S1:

```text
Start frame: Seoul, late afternoon. The camera looks north at Gwanghwamun, the wooden traditional palace gate, with a broad stone terrace in front of it and a small dark signboard too far away to read. A tree-lined square fills the left side of the frame and the roadway the right. A dark-grey sedan waits at a red light on the road that curves in front of the terrace, facing frame-left, with the gate on its right side.
Motion: Static wide shot. Light traffic and a few pedestrians move slowly; the sedan's turn signal blinks. The camera holds completely still.
```

S2:

```text
Start frame: Inside the dark-grey sedan, stopped at a red light, seen from the back seat. The father sits in the driver's seat with both hands on the wheel, seen from behind. The teenage daughter sits in the front passenger seat, pushed far back, seat belt on, looking out of her right-side window at the old wooden gate, her face turned away from the camera. The wide central display is dark and softly out of focus.
Motion: The daughter keeps looking out of the window, then slowly turns her head toward her father and listens. The father faces forward the whole time. Characters remain silent. The camera holds still.
```

S3a:

```text
Start frame: Close-up of the father's left hand resting on a modern steering wheel, an old silver wristwatch on his wrist.
Motion: The hand rests still; soft light shifts slightly. Locked camera.
```

S3b:

```text
Start frame: The same framing as the reference image: the grandfather's left hand resting on a thin two-spoke 1980s steering wheel, the same old silver wristwatch on his wrist.
Motion: The hand rests still; soft light shifts slightly. Locked camera.
```

S4a:

```text
Start frame: Seoul, a wide street view looking north along a very wide 16-lane boulevard with 1990s Korean cars and buses. A narrow central median with a row of tall ginkgo trees in fresh spring leaf runs down the middle, and traffic lanes fill the rest of the width. At the far end stands a traditional Korean gate, and directly behind it a huge domed grey granite neoclassical building towers over the gate.
Motion: Slow traffic moves along the boulevard. The camera holds still or pushes in very slowly.
```

S4b:

```text
Start frame: Inside the boxy 1980s sedan, fully stopped at a red light with traffic stationary, seen from the back seat. The grandfather sits in the driver's seat and the 12-year-old boy in the front passenger seat, both wearing seat belts, both seen from behind. Through the windshield, softly out of focus: a traditional gate close ahead and a huge domed granite building behind it.
Motion: Traffic stays stopped. The grandfather briefly lifts one hand from the wheel to point ahead, then puts it back; the boy leans forward to look. Characters remain silent. The camera holds still.
```

S4c-1:

```text
Start frame: Inside the stopped boxy 1980s sedan, seen from just behind the front passenger seat: the back of the 12-year-old boy's head and shoulders, seat belt on, as he looks up through the windshield at the huge domed granite building beyond the gate.
Motion: The boy tilts his head up a little toward the building. Characters remain silent. Locked camera.
```

S4c-2:

```text
Start frame: Close-up of the 12-year-old boy in the passenger seat of the stopped sedan, seat belt on, turned toward the driver's seat, listening with curiosity, his mouth closed.
Motion: The boy listens and slowly breaks into a small closed-mouth smile. Characters remain silent. Locked camera.
```

S5a:

```text
Start frame: Close-up of the boy's left profile in the passenger seat, looking forward through the windshield, the small mole just above his left eyebrow visible.
Motion: A slow blink; the light changes softly. Locked camera.
```

S5b:

```text
Start frame: The same framing as the reference image: close-up of the father's left profile in the driver's seat, looking forward through the windshield, the same small mole just above his left eyebrow.
Motion: A slow blink; the light changes softly. Locked camera.
```

S6-0:

```text
Start frame: Inside the dark-grey sedan the moment the light turns green, seen from the back seat. The teenage daughter in the front passenger seat, seat belt on, seat pushed far back, reaches out with her left hand toward the lower part of the wide central display. The display is dark and softly out of focus. The father keeps both hands on the wheel and his eyes on the road.
Motion: The daughter taps the display once and pulls her hand back. The father keeps both hands on the wheel and his eyes on the road. Characters remain silent. The camera holds still.
```

S6a(정지 그림만):

```text
Start frame: Straight-on close-up of the dashboard of the dark-grey sedan. The right part of the wide central display is a switched-off black screen with a 1.39:1 shape and fills about 70 percent of the frame width; a narrow strip of the left driving-information part shows at the left edge. Soft late-afternoon light.
```

S6b-1:

```text
Start frame: Inside the dark-grey sedan, driving slowly south on a wide boulevard, seen from the back seat. The teenage daughter, seat belt on, seat pushed back, looks out of her right-side window at a tree-lined square passing by. The father drives with both hands on the wheel. The central display is dark and softly out of focus.
Motion: The square glides past the window and the daughter watches it. The father keeps both hands on the wheel and his eyes on the road. Characters remain silent. The camera holds still.
```

S6b-2:

```text
Start frame: Close-up of the father driving, seen from the passenger side: both hands on the wheel, eyes on the road, a soft smile with his mouth closed, late-afternoon city light on his face.
Motion: The car moves slowly and light passes over his face. He keeps his eyes on the road and smiles softly. Characters remain silent. The camera holds still.
```

S7:

```text
Start frame: Inside the dark-grey sedan, driving slowly, seen from directly behind the front seats: the backs of the father's and the teenage daughter's heads and the boulevard ahead through the windshield. The central display is dark and softly out of focus.
Motion: The daughter turns her head toward her father and then back to the road; the father nods slightly with both hands on the wheel and his eyes on the road. Characters remain silent. The camera holds still.
```

S8:

```text
Start frame: High aerial view from above the old gate, looking south down a long tree-lined boulevard at golden hour. The dark-grey sedan drives away from the camera toward a green hill with a small tower on the horizon. The tree-lined square runs along the right side of the frame. Empty sky at the top for title text.
Motion: The sedan drives away along the boulevard. The camera holds still or drifts forward very slowly.
```

### 5.4 제외어(negative prompt 칸이 있는 도구에만)

negative 칸은 조건을 읽지 못하므로 시대별 블록을 나눴습니다. 1995년 장면에 5.4c를, 2026년 장면에 5.4b를 넣으면 장면 문장과 부딪칩니다.

5.4a 공통(모든 장면):

```text
readable text, signage, logos, emblems, brand names, readable licence plates, real politicians, celebrities, unbelted passengers, children under 10 in the front seat, driver looking at or touching the screen while moving, phone in the driver's hand, chat bubbles, microphone icons, maps on the app screen, Chinese-style guardian lion statues, talking, lip movement
```

5.4b 1995년 장면에만(S3b, S4a, S4b, S4c-1, S4c-2, S5a):

```text
plaza, seated golden statue, stone terrace in front of the gate, glass skyscrapers
```

5.4c 2026년 장면에만(S1, S2, S3a, S5b, S6-0, S6a, S6b-1, S6b-2, S7, S8):

```text
domed granite building behind the gate, rows of large stone monuments in the square
```

## 6. Part B 기능 시연(약 43초)

Pleos Connect 에뮬레이터 v2.1.0(이 PC, 2560×1440)에서 녹화한 실제 앱 화면만 씁니다. 화면 글자는 `DocentripScreen.kt`, `DocentEngine.kt`, `route.json`에서 옮겼고, 일부는 11-07 시험 녹화로 확인했습니다. 아래에서 코드 글꼴로 적은 글자는 화면에 그대로 나오는 글자입니다. 거리와 시간 숫자는 시속 50km 기준 계산값입니다. 에뮬레이터의 0.25초 간격은 조금씩 늦어지므로 녹화에서는 계산보다 조금 늦을 수 있습니다.

### 6.1 구성

| 클립 | 길이 | 보여 줄 기능 | 자막의 근거(작은 줄) | 테이크(7장) | 배치(6.2) |
|---|---|---|---|---|---|
| B1 | 9초 | 경로를 따라 저절로 시작되는 이야기 | Pleos TextToSpeech SDK | M | 앱 영역 |
| B2 | 8초 | 말로 하기: "다음" | Pleos SpeechToText SDK | V(debug 빌드) | 앱 영역 |
| B3 | 7초 | 내비로 안내, 다른 앱 위에서도 계속 | Pleos NaviHelper SDK, Media3 미디어 세션 | N | 전체 화면 |
| B4 | 6초 | Gleo가 끼어들면 멈추고, 끝나면 처음부터 | 오디오 포커스 처리 | G | 전체 화면 |
| B5 | 7초 | 후진하면 이야기를 쉼 | 차량 기어 신호, Pleos FusedLocation SDK | L | 전체 화면 |
| B6 | 6초 | 도착 요약 | Pleos Vehicle SDK(차량 위치 모드의 동작으로 설명) | M | 앱 영역 |
| B3-0(선택) | 2초 | 달리는 동안 설정 잠금 | 차량 주행 제한(CarUxRestrictions) | D | 앱 영역 |
| B5-0(선택) | 2초 | 차량 위치 안내·동의 | Pleos FusedLocation SDK | L | 앱 영역 |

- 공모전이 안내한 SDK 5개(TextToSpeech, SpeechToText, NaviHelper, FusedLocation, Vehicle)가 자막에 한 번씩 나옵니다.
- 선택 장면을 넣으면 Part B가 2~4초 길어집니다.

### 6.2 화면 배치와 자막 모양

**앱 영역 클립(B1, B2, B6, 선택 장면)**

- 녹화(2560×1440)에서 가로 853~2535, 세로 90~1300(1682×1210, 약 1.39:1)만 자릅니다. Part A S6a에 합성하는 것과 같은 영역입니다.
- 1252×900으로 줄여 1920×1080 화면의 가운데 위(세로 30~930)에 둡니다.
- 아래 띠(세로 약 945~1060)에 자막 두 줄을 넣습니다.
- 글자를 더 키우려면: 앱 영역을 높이에 맞춰 1501×1080(0.89배)으로 왼쪽에 두고, 오른쪽 419px 칸에 자막을 쌓는 배치도 됩니다. 기본 글자가 약 20% 커집니다.

**전체 화면 클립(B3, B4, B5)**

- 2560×1440을 1920×1080으로 줄입니다. 비율이 같아 자를 것이 없습니다. 펀치 인을 원본 해상도에서 하려고, 줄이는 것은 편집기에서 합니다(7.4).
- 자막은 화면 맨 아래 공조 막대 자리에 반투명 띠로 겹칩니다. 앱과 내비의 글자는 가리지 않습니다.
- 왼쪽 위의 "안전 벨트를 매세요." 경고(7.1에서 끄지 못한 경우)도 자막으로 가리지 않습니다.

**왜 두 가지로 나누나**

- 왼쪽 주행 정보는 에뮬레이터 차의 상태(11-07 시험 녹화: P, 0km/h)를 보여 주므로, 시뮬레이션 주행 중에는 앱 화면의 "50 km/h"와 어긋나 보입니다. 그래서 시뮬레이션 클립은 되도록 앱 영역만 씁니다.
- 시스템 화면(내비, 미디어 위젯, Gleo, 후방 카메라)이 주인공인 클립은 전체 화면을 씁니다. 그중 시뮬레이션인 B3·B4는 이 차이를 오른쪽 위 표시로 설명합니다.

**오른쪽 위 고정 표시**

- Part B 내내: "실제 앱 화면 · 에뮬레이터 녹화"
- 앱 영역 시뮬레이션 클립(B1, B2, B6)에는 그 아래에: "시뮬레이션 주행(가상 50km/h)"
- 전체 화면 시뮬레이션 클립(B3, B4)에는 그 아래에: "시뮬레이션 주행: Docentrip만 가상 50km/h · 왼쪽 차량 정보는 정차(P)"
- B5(차량 위치 모드)에는 시뮬레이션 표시를 넣지 않습니다.

**자막 모양**

- 큰 줄(기능): 약 44px, 굵게. 작은 줄(근거·SDK): 약 30px. 흰 글자에 60% 검은 띠.
- 정직 표시(에뮬레이터에서 어떻게 했는지): 약 26px, 연한 색, 큰 줄 위나 화면 위쪽.
- 분량: 클립마다 새로 읽을 글자를 초당 약 8자 안팎으로 둡니다. 넷플릭스 한국어 자막의 성인 기준은 초당 최대 12자인데, 그동안 앱 화면 글자도 읽어야 하므로 더 적게 씁니다. 줄 하나가 화면 폭(1920px)을 넘지 않게 하고, 빠진 설명(명령 네 가지 등)은 접수양식에 둡니다.

**펀치 인(필수)**

- 에뮬레이터는 1dp=1px입니다(밀도 160, 2026-11-07 확인). 앱 영역을 0.744배로 줄이면 상태 표시와 "다음 이야기"(14sp)는 약 10px, "…m 앞"(15sp)은 약 11px, 아래 줄 안내·요약 줄(17sp)은 약 13px, 본문(21sp)은 약 16px이 되어 노트북 화면에서는 읽기 어렵습니다.
- 그래서 화면 글자로 기능을 보여 주는 순간마다, 녹화 원본(2560×1440)에서 가로 약 900~1,000px 영역을 화면 폭까지 키웁니다(약 2배. 요약 줄이 약 35px).
- 펀치 인 할 순간: B1 아래 줄 숫자와 제목이 바뀔 때, B2 "듣는 중"과 안내 줄, B3 "경유지 2개 포함"과 왼쪽 미디어 위젯, B4 위쪽 가장자리의 Gleo 빛, B5 P로 돌아온 뒤 "도슨트 켜짐"·"다시 듣기", B6 요약 줄.

### 6.3 B1 경로를 따라 저절로 시작되는 이야기(9초, 테이크 M)

**보여 줄 것:** 정차 상태에서 주행을 시작하면 출발지 이야기가 바로 나옵니다. 다음 장소까지 남은 거리가 줄어들다가, 장소 약 400m 앞에서 다음 이야기가 저절로 시작됩니다.

**화면 글자**

| 순간 | 보이는 글자 |
|---|---|
| 누르기 전 | 맨 위 `DOCENTRIP`, `차창 밖 이야기` / 오른쪽 위 `0`, `km/h · 시뮬레이션 · 0.0 / 4.0 km` / 상태 `대기 중` / 카드 위 버튼 `내비로 안내`, `다시 듣기`(흐림) / 제목 `아직 들려준 이야기가 없어요` / 본문 `주행을 시작하면 지나가는 곳의 이야기가 자동으로 흘러나옵니다. 화면을 보지 않아도 되는 오디오 도슨트입니다.` / 아래 줄 `다음 이야기` `광화문, 조선의 얼굴` `곧` / 버튼 `시뮬레이션 주행 시작`, `말로 하기`, `차량 위치로`, `처음부터` |
| 누른 직후 | `50`, `km/h · 시뮬레이션 · 0.0 / 4.0 km` / `재생 중` / `이야기 멈춤` / 제목 `광화문, 조선의 얼굴` / 본문은 이야기 글(3줄까지, 넘치면 …) / 아래 줄 `다음 이야기` `청계천, 도심을 흐르는 물길` `760m 앞`(계산값) / 버튼 `주행 정지` |
| 이야기 사이(누른 뒤 약 19~26초) | 상태 `주행 중`, `다시 듣기`. 진행 막대가 늘고 `…m 앞`의 숫자가 줄어듦 |
| 누른 뒤 약 26.3초(청계천 약 400m 앞) | 제목 `청계천, 도심을 흐르는 물길`, `재생 중` / 아래 줄 `다음 이야기` `서울시청, 유리와 돌의 대화` `710m 앞`(계산값) |

- 시간과 거리는 시속 50km 기준 계산값입니다. 11-07 시험 녹화에서는 광화문 이야기 소리가 누른 뒤 약 18.5초까지 이어졌고, 청계천 이야기 소리가 누른 뒤 약 26.4초에 시작했습니다.

**자막**

- 큰 줄: "장소 약 400m 앞에서, 이야기가 저절로"
- 작은 줄: "한 번에 하나, 장소마다 한 번 · 목소리: Pleos TextToSpeech SDK(차량 온디바이스)"
- 정직 표시: 빨리 감는 동안 "16배속". 시뮬레이션은 오른쪽 위 표시로 설명합니다.

**편집(시간은 누른 순간 P 기준. P는 버튼이 `주행 정지`로 바뀌는 프레임이고, 테이크 M에서는 녹화 약 2초)**

- 0~1.5초: P−0.5 ~ P+1.0초, 원래 속도. 누르는 순간과 `재생 중` · `광화문, 조선의 얼굴`로 바뀌는 모습. 광화문 이야기 첫마디는 Part A에서 이미 들었으므로 작게 둡니다.
- 1.5~3초: P+1.0 ~ P+24.5초를 16배속(약 1.5초)으로. 소리는 끄고 "16배속"을 띄웁니다.
- 3~9초: P+24.5 ~ P+30.5초, 원래 속도. 아래 줄이 `청계천, 도심을 흐르는 물길` `423m 앞`(계산값)에서 줄어들다가 400 근처(마지막 `403m 앞`)에서 청계천 이야기로 바뀌는 순간(P+약 26.3초)과, 첫 문장 "곧 왼쪽으로 청계천이 시작되는 청계광장이 지나갑니다."(29자, 약 3.8초)가 끝까지 들리게 합니다. 아래 줄을 펀치 인합니다.
- 녹화본에서 첫 문장이 P+30.5초보다 늦게 끝나면 그만큼 늘립니다.

### 6.4 B2 말로 하기: "다음"(8초, 테이크 V)

**보여 줄 것:** 이야기 도중에 "다음"을 들으면 지금 이야기를 끝냅니다. 다음 이야기로 바로 건너뛰는 것은 아니고, 다음 이야기는 그 장소 앞에서 다시 저절로 나옵니다(아래 줄에 보임).

**화면 글자**

| 순간 | 보이는 글자 |
|---|---|
| 명령 전 | `재생 중` / 제목 `청계천, 도심을 흐르는 물길` / 버튼 `말로 하기` |
| 듣기 시작 | 상태 `듣는 중` / 버튼 `그만 듣기`(주황 테두리) / 아래 줄 `마이크를 켜고 있어요 · "듣고 있어요"가 나오면 말씀해 주세요` → `듣고 있어요 · "다음", "다시", "그만", "켜 줘" 중에 말씀해 주세요 · Docentrip은 들은 말을 명령에만 쓰고 저장하지 않아요`(두 줄까지, 넘치면 …). 이때 이야기 소리가 멈춥니다 |
| "다음"을 알아들은 뒤 | 상태 `주행 중` / `다시 듣기` / 버튼 `말로 하기` / 아래 줄 `다음 이야기` `서울시청, 유리와 돌의 대화` `…m 앞` |

- 11-07 시험 녹화(녹음 명령): `마이크를 켜고 있어요`는 약 2.6초, `듣고 있어요 · …`는 약 0.2초만 보였습니다. 이야기 소리는 `듣는 중`이 뜬 뒤 약 1초 안에 멈췄습니다(소리 데이터로 본 추정).
- 앱은 알아들은 말(인식 결과)을 화면에 보여 주지 않습니다. 듣는 동안 아래 줄의 "다음", "다시", "그만", "켜 줘"는 말할 수 있는 명령을 알려 주는 고정 안내문이고, 명령 뒤의 `다음 이야기`도 원래 있는 라벨입니다.

**자막**

- 큰 줄: "말로 하기 → '다음' 한마디로 지금 이야기를 끝냄"
- 작은 줄: "Pleos SpeechToText SDK(차량 온디바이스 인식) · Docentrip은 들은 말을 저장하지 않음"
- 정직 표시: "에뮬레이터 시연: '말로 하기'를 누르는 대신 녹음한 '다음'을 넣어 듣기를 시작했습니다"
- 명령 순간에 "[명령] 다음". 앱 안내문과 떨어진 큰 줄 띠에 둡니다.

**편집**

- 0~1.4초: 청계천 이야기 중(`재생 중`).
- 1.4~4초: `듣는 중`, `마이크를 켜고 있어요 · …`(원래 속도, 약 2.6초). 이야기 소리가 멈춥니다.
- 4~6초: `듣고 있어요 · …` 프레임을 2초 정지 화면으로 둡니다. "정지 화면(실제 약 0.2초)"과 "[명령] 다음"을 띄우고 안내 줄을 펀치 인합니다.
- 6~8초: 결과(`주행 중`, 아래 줄 `다음 이야기` `서울시청, 유리와 돌의 대화`). 아래 줄을 펀치 인합니다.
- 듣기 시작부터 결과까지 자르지 않습니다. 정지 화면은 더하기만 하고 표시합니다.
- 명령 소리는 넣지 않습니다. 녹음 파일은 Windows 한국어 합성 음성이라 "[명령] 다음" 자막으로만 보여 줍니다.
- 녹음을 넣어도 화면에는 "마이크를 켜고 있어요"가 나옵니다. 앱이 같은 단계를 거치기 때문이고, 정직 표시가 이것을 설명합니다.

### 6.5 B3 내비로 안내, 다른 앱 위에서도 계속(7초, 테이크 N)

**보여 줄 것:** 정차 중에 "내비로 안내"를 누르면 차량 내비가 이야기 장소(숭례문, 문화역서울284)를 경유지로, N서울타워를 목적지로 안내를 시작합니다. 내비를 앞에 둔 채 주행해도 이야기는 계속되고, 왼쪽 미디어 위젯에 지금 이야기 제목이 보입니다.

**화면 글자**

| 순간 | 보이는 글자 |
|---|---|
| 누르기 전(Docentrip) | `대기 중` / `0`, `km/h · 시뮬레이션 · 0.0 / 4.0 km` / 카드 위 `내비로 안내`(주황) |
| 내비: 경로 선택 | `N서울타워`, `경유지 2개 포함`, 경로 세 가지(11-07 시험 녹화: `추천` `1시간 36분` `34km`, `한강대로`, `사당로`), `안내 시작`(숫자가 줄어든 뒤 저절로 시작) |
| 내비: 안내 중 | 경로 미리보기(`삼일대로`, `경부고속도로`, 경로별 예상 시간), 아래 카드 `…분 · 23km`와 첫 경유지 `숭례문`, 버튼 `안내 종료`, `내 위치로`. 안내 화면 `700m` `대왕판교IC 방면`, `수정구 시흥동`. 모두 판교 출발 기준이고 숫자는 실행마다 다름. 새 녹화에서 다시 확인합니다 |
| 주행 중 왼쪽 미디어 위젯 | 제목 `광화문, 조선의 얼굴`, 그 아래 `Docentrip` |
| Docentrip으로 돌아오면(선택) | 카드 위 글자는 녹화본에서 확인합니다. 내비가 안내 시작을 알려 와야 `내비 안내 중`으로 바뀌는데, 11-07 시험 녹화에서는 내비가 안내 중인데도 `내비로 안내` 그대로였습니다. 자막과 표에 `내비 안내 중`을 미리 쓰지 않습니다 |

- Docentrip이 보내는 이름은 경유지 `숭례문`, `문화역서울284`, 목적지 `N서울타워`입니다.

**자막**

- 큰 줄: "내비로 안내 → 이야기 장소가 경유지로"
- 작은 줄: "Pleos NaviHelper SDK · 내비를 앞에 띄워도 이야기는 계속, 위젯에 지금 이야기(Media3 미디어 세션)"
- 정직 표시(B3 내내): "에뮬레이터 내비는 판교에 고정: 화면의 경로·시간·거리는 판교 출발 기준(데모 경로는 광화문 → N서울타워 약 4km)"

**편집**

- 0~1.5초: `내비로 안내` → 내비가 앞으로 나옴.
- "약 N초 뒤"(실제 건너뛴 시간)를 띄우고 1.5~3초: 경로 선택 화면. `N서울타워` · `경유지 2개 포함`을 펀치 인. 안내가 시작됨.
- 3~4초: 미디어 위젯의 재생 버튼 → 주행 시작(건너뛴 곳이 있으면 표시).
- 4~7초: 내비 화면 위로 이야기가 들림. 미디어 위젯을 펀치 인.
- 이야기 본문은 화면에 없으므로, 필요하면 들리는 문장을 `route.json` 글자 그대로 자막으로 넣습니다(9장).
- 회전 직전에 이야기를 미루는 기능은 에뮬레이터에서 보이지 않으므로 넣지 않습니다(2.1).

**선택 B3-0 달리는 동안 설정 잠금(2초, 테이크 D)**

- 화면 글자: 시뮬레이션 주행 중 Virtual Console로 차를 주행 상태로 바꾸면 카드 위 `내비로 안내`가 `정차 후 내비 안내`(흐림)로, 아래 버튼 `차량 위치로`가 `정차 후 변경`으로 바뀝니다. 에뮬레이터는 기어를 D로 바꾸는 순간(0km/h) 바뀔 수 있습니다.
- 큰 줄: "달리는 동안에는 설정을 잠금". 작은 줄: "차량 주행 제한(CarUxRestrictions)에 맞춘 화면"
- 정직 표시: "에뮬레이터 Virtual Console로 주행 상태(D, 시속 30km)를 만들었습니다"
- B3 바로 앞에 넣으면 "그래서 내비 안내는 정차 중에 누른다"는 흐름이 됩니다.

### 6.6 B4 Gleo가 끼어들면(6초, 테이크 G)

**보여 줄 것:** 이야기 중에 Gleo 음성 비서가 열리면 이야기가 바로 멈추고, Gleo가 닫히면 그 이야기를 처음부터 다시 들려줍니다.

**화면 글자와 소리**

- Docentrip 쪽 글자는 그대로입니다: `재생 중`, 제목 `서울시청, 유리와 돌의 대화`. 앱이 이야기를 붙잡아 두기 때문입니다. 차이는 소리로 납니다.
- Gleo는 화면 위쪽 가장자리의 파란 빛으로만 보입니다(11-07 시험 녹화). 소리를 못 들으면 화면만으로는 무슨 일이 있었는지 알 수 없습니다.
- 소리: "곧 왼쪽으로 돌로 지은 옛 서울시청이 보입니다…" → Gleo가 열림 → 이야기가 멈춤 → Gleo가 닫힘 → 다시 "곧 왼쪽으로 돌로 지은 옛 서울시청이 보입니다…"(처음부터). 11-07 시험에서는 멈춘 뒤 약 3초 만에 처음부터 다시 나왔습니다.
- Gleo 화면과 소리는 Pleos Connect 시스템의 것입니다.

**자막**

- 큰 줄: "Gleo가 끼어들면 멈추고, 끝나면 그 이야기를 처음부터"
- 작은 줄: "오디오 포커스 처리(Pleos 오디오 포커스 가이드)"
- 정직 표시: "에뮬레이터: 핸들 음성 버튼을 키 입력 명령으로 눌렀습니다"
- 멈추는 순간에 "[Gleo 호출]", 다시 시작할 때 "[Gleo 닫힘 → 처음부터]"

**편집**

- 멈추는 순간과 다시 시작하는 순간은 자르지 않습니다. Gleo가 약 3초면 6초 안에 자르지 않고 들어갑니다(멈추기 전 약 1초, Gleo 약 3초, 다시 시작 뒤 약 2초). 더 오래 열려 있으면 가운데만 줄이고 "몇 초 뒤"를 띄웁니다.
- 위쪽 가장자리의 Gleo 빛을 펀치 인합니다.
- 멈춘 것이 들리도록 그동안 음악을 거의 뺍니다.

### 6.7 B5 후진하면 이야기를 쉼(7초, 테이크 L)

**보여 줄 것:** 차량 위치 모드에서 이야기 중에 기어를 R로 바꾸면 듣던 이야기를 멈추고, R인 동안은 새 이야기를 시작하지 않습니다. 시뮬레이션 모드는 기어를 따르지 않으므로 이 클립만 차량 위치 모드로 찍습니다. 화면에 "시뮬레이션" 대신 "차량 위치"가 보입니다.

**화면 글자**

| 순간 | 보이는 글자 |
|---|---|
| 켜기 전 | `대기 중` / `0`, `km/h · 차량 위치 · 0.0 / 4.0 km` / 버튼 `도슨트 켜기`, `말로 하기`, `시뮬레이션으로`, `처음부터` / 아래 줄 `다음 이야기` `광화문, 조선의 얼굴` `곧`. 위치가 오기 전이면 `차량 위치를 기다리고 있어요`, 경로 밖 위치가 먼저 잡히면 `데모 경로(광화문 → N서울타워) 밖이에요 · 경로에 들어서면 이야기가 시작돼요` |
| 켠 뒤 | `재생 중` / 제목 `광화문, 조선의 얼굴` / 버튼 `도슨트 끄기` |
| 기어 R | 앱 영역 전체를 후방 카메라 화면이 덮습니다(11-07 시험 녹화에서 확인). 후진 안내 문구는 보이지 않으므로 자막에 쓰지 않고, 왼쪽 주행 정보의 R 표시와 이야기 소리가 멈추는 것으로 보여 줍니다. 왼쪽 미디어 위젯에는 이야기 제목이 그대로 남습니다 |
| P로 돌아온 뒤 | `도슨트 켜짐` / `다시 듣기` / 아래 줄 `다음 이야기` `청계천, 도심을 흐르는 물길` `…m 앞` |

**자막**

- 큰 줄: "후진하면 이야기를 쉼"
- 작은 줄: "R인 동안 새 이야기 없음 · 기어: 차량 신호(Android Automotive) · 위치: Pleos FusedLocation SDK"
- 정직 표시: "에뮬레이터: 위치는 에뮬레이터 GPS, 기어는 Virtual Console로 바꿈 · R에서는 후방 카메라 화면이 앱을 덮음"
- R 동안: "[기어 R · 후방 카메라]"

**편집과 주의**

- 이야기 소리(약 2초) → R(약 3초, 후방 카메라) → P로 돌아와 `도슨트 켜짐`·`다시 듣기`(약 2초, 펀치 인)까지 자르지 않습니다. 합계 약 7초입니다.
- 후진 안내 문구는 화면에 나오지 않으므로 자막에도 넣지 않습니다. 화면에 없는 글자를 있는 것처럼 보여 주지 않습니다.
- 기어는 Pleos Vehicle SDK가 아니라 Android Automotive 표준 차량 속성으로 받습니다. 자막에 Vehicle SDK라고 쓰지 않습니다.
- R 동안 이야기 본문은 화면에 없으므로, 필요하면 들린 문장을 `route.json` 글자 그대로 자막으로 넣습니다(9장).

**선택 B5-0 차량 위치 안내·동의(2초)**

- 화면 글자: 제목 `차량 위치 사용 안내` / `· 지나가는 곳의 이야기를 알맞은 때 들려주려고 차량 위치를 씁니다.` / `· 위치는 이 차 안에서 이야기를 고르는 데만 쓰고, 저장하거나 밖으로 보내지 않습니다.` / `· 동의하지 않아도 시뮬레이션 주행으로 쓸 수 있고, 언제든 시뮬레이션으로 돌아갈 수 있습니다.` / 버튼 `동의하고 차량 위치 사용`, `동의하지 않음`
- 큰 줄: "차량 위치는 안내하고 동의를 받은 뒤에만". 작은 줄: "Pleos FusedLocation SDK"
- 이어서 나오는 시스템 권한 창(`Docentrip에서 알 수 없는 작업 실행하도록 허용하시겠습니까?`)은 헷갈리므로 편집에서 빼고, 그 자리에 "시스템 권한 허용 단계 생략"을 띄웁니다. 권한 허용은 앱이 실제로 거치는 단계(동의 → 권한 → 위치)라 표시 없이 빼면 동의 뒤 바로 위치를 쓰는 것처럼 보입니다.

### 6.8 B6 도착 요약(6초, 테이크 M 끝부분)

**보여 줄 것:** 경로 끝에 닿으면 오늘 주행을 한 줄로 요약합니다.

**화면 글자**

| 순간 | 보이는 글자 |
|---|---|
| 마지막 이야기 | `재생 중` / 제목 `N서울타워, 도착` / 아래 줄 `이 경로에 남은 이야기가 없습니다 · 7/7` |
| 도착 | 상태 `도착` / `0`, `km/h · 시뮬레이션 · 4.0 / 4.0 km` / 진행 막대 끝까지 / 아래 줄 `오늘 4.0km(경로 기준) · 5분 · 이야기 7/7` / 버튼 `처음부터 다시 주행` |

- "5분"은 주행이 켜져 있던 시간입니다. 테이크 M처럼 도중에 아무것도 누르지 않고 달려야 위 글자가 됩니다. 11-07 시험 녹화의 도착 화면도 위와 같았습니다.
- 계산으로는 마지막 이야기가 누른 뒤 약 4분 19.5초에 시작해 약 4분 32.5초에 끝나고, 도착은 약 4분 48초입니다. 그사이 약 16초는 조용히 달립니다.

**자막**

- 큰 줄: "도착하면 오늘의 요약"
- 작은 줄: "Pleos Vehicle SDK · 차량 위치 모드에서는 차의 주행 거리계로 거리를 잼"
- 정직 표시: "이 화면은 시뮬레이션이라 '경로 기준' 거리입니다"
- 이 클립(시뮬레이션)에서는 Vehicle SDK를 부르지 않습니다. 차량 위치 모드에서만 출발·도착 때 차의 미터를 읽고, 값이 늘었을 때만 그 거리("차량 주행 거리")를 씁니다. 늘지 않았으면 경로 기준입니다.
- 선택: SDK 동작을 화면으로 보이려면 차량 위치 모드 도착을 따로 찍습니다. 데모 경로 전체를 GPS로 달리고(`tools/drive-demo-route.ps1`, 약 5분), Virtual Console 주행 거리계를 출발 전 104km, 도착 직전 108km로 바꾸면 `오늘 4.0km(차량 주행 거리)`가 나옵니다(architecture.md 10장). 에뮬레이터 주행 거리계는 저절로 늘지 않으므로, 이때 정직 표시에 "주행 거리계 값은 Virtual Console로 104 → 108km로 바꿨습니다"를 넣습니다.

**편집**

- 0~3초: 마지막 문장 "안전하게 목적지에 도착하시길 바랍니다."(21자, 약 2.7초)만 씁니다. 문장 시작에서 자릅니다(계산으로 누른 뒤 약 4분 29.8초~4분 32.5초). 화면에는 `N서울타워, 도착`과 `이 경로에 남은 이야기가 없습니다 · 7/7`이 보입니다.
- 3~6초: "약 16초 뒤"(실제 건너뛴 시간)를 띄우고 도착 화면(누른 뒤 약 4분 48초부터). 요약 줄을 펀치 인합니다.
- 앞 문장 "오늘은 여기서 이야기를 마칩니다."까지 쓰려면 B6는 약 8.5초가 됩니다.

## 7. 녹화 방법(에뮬레이터)

### 7.1 준비

- 이 PC의 Pleos Connect 에뮬레이터 v2.1.0을 Cold boot로 켭니다.
- **남아 있는 Docentrip을 지우고** 제출 APK(`apk/YOUNGIN_Docentrip_release.apk`)를 설치합니다. `install -r`은 앱 데이터를 남깁니다. 이 에뮬레이터에는 11-07 최종 점검 때 준 위치 동의가 남아 있고 위치 권한도 허용된 상태라(2026-11-07 확인), 그대로 두면 테이크 L에서 `차량 위치 사용 안내`와 권한 창이 나오지 않습니다. debug 빌드가 깔려 있으면 서명이 달라 덮어 설치도 실패합니다.
- 설치한 뒤 **에뮬레이터를 한 번 더 켭니다.** 재설치 직후에는 왼쪽 미디어 위젯이 "제목 정보 없음"으로 남고 위젯 버튼이 듣지 않습니다(handoff.md 7장).
- Docentrip을 열고 시뮬레이션 모드인지 보고 `처음부터`를 누릅니다.
- 왼쪽 아래 패널이 미디어 위젯 페이지인지 봅니다. 11-07 시험 녹화에는 다른 페이지(차량 기능 아이콘)가 보인 때가 있었습니다. 아래의 점 표시를 보며 페이지를 넘겨 맞춥니다.
- Virtual Console(주행 상태)은 기어 P, 속도 0으로 둡니다.
- 11-07 시험 녹화의 전체 화면에는 왼쪽 위에 "안전 벨트를 매세요." 경고가 늘 보였습니다. Virtual Console의 경고등(Telltales)에서 안전벨트(SeatBelt)를 Off로 바꿔 보고, 10초 시험 녹화에서 경고가 사라졌는지 봅니다. 사라지지 않으면 그대로 두고 자막으로 가리지 않습니다.
- 개발자 옵션의 터치 표시가 켜져 있으면 끕니다.
- 녹화 파일은 `C:\tmp\docentrip-video\`에 모읍니다. 2026-11-03에 만든 예전 녹화본은 앱 이름이 바뀌기 전 화면이라 쓰지 않습니다. 2026-11-07 시험 녹화(`C:\tmp\docentrip-demo\`)는 새 이름 화면입니다. B1·B2·B4·B6과 S6에는 그대로 쓸 수 있고, B3와 B5는 새로 찍어야 합니다(7.5).
- 먼저 기본값으로 10초 시험 녹화를 해 글자 선명도와 소리를 봅니다.

```powershell
# 새 PowerShell 창마다 먼저(그 창에만 적용). gradlew를 쓸 테이크 V 전에는 handoff.md 6장 첫 블록(JAVA_HOME)도 실행
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
Set-ExecutionPolicy -Scope Process Bypass -Force
New-Item -ItemType Directory -Force C:\tmp\docentrip-video | Out-Null

# 에뮬레이터 켜기 → 남은 앱 지우기 → release 설치 → 에뮬레이터 다시 켜기 → Docentrip 열기
.\tools\start-emulator.ps1
& $adb uninstall app.docentrip    # 남은 debug 빌드와 예전 위치 동의·권한을 지움(B5-0 동의 화면이 나오게). 설치돼 있지 않으면 실패 메시지만 나옴
& $adb install apk\YOUNGIN_Docentrip_release.apk
& $adb emu kill
.\tools\start-emulator.ps1
& $adb shell am start -n app.docentrip/.MainActivity
```

### 7.2 녹화 명령

```powershell
# 녹화 시작: 에뮬레이터 화면을 PC 파일로 바로 저장. 기본은 180초에서 멈추므로 길이를 늘림
& $adb emu screenrecord start --time-limit 400 --fps 24 --bit-rate 12M C:\tmp\docentrip-video\M_master.webm
# 녹화 끝
& $adb emu screenrecord stop
```

- 옵션(콘솔 도움말 기준): `--time-limit` 기본 180초·최대 14400초, `--fps` 기본 24·최대 60, `--bit-rate` 기본 4Mbps, `--size` 기본은 화면 해상도(2560×1440).
- 프레임레이트는 영상 AI 클립과 같은 값으로 맞춥니다(대개 24). 녹화본은 실제로는 프레임 간격이 일정하지 않으므로(11-07 시험 녹화: 초당 약 20~27프레임) 편집 전에 24fps로 바꿉니다(7.4).
- 녹화가 끊기면 비트레이트를 낮추거나 `--size 1920x1080`으로 녹화합니다. 이때 앱 영역 자르기 값은 0.75배(`crop=1262:908:640:68`)이고, 펀치 인은 덜 선명합니다.

**자주 쓰는 명령**

| 하려는 것 | 명령 |
|---|---|
| Gleo 부르기(핸들 음성 버튼) | `& $adb shell cmd car_service inject-key 231` |
| GPS를 데모 경로 출발점에 | `& $adb emu geo fix 126.97690 37.57594` (경도, 위도 순) |
| Virtual Console 열기 | `& $adb forward tcp:8088 tcp:8088` 후 브라우저로 `http://127.0.0.1:8088/` |
| 녹음한 음성 명령 넣기(debug 빌드만) | `.\tools\say-command.ps1 '다음'` |
| 명령 결과 보기 | `& $adb logcat -s Docentrip` → `voice command: SKIP` |

### 7.3 테이크 순서

| 순서 | 테이크 | 빌드 | 모드 | 쓰는 곳 | 녹화 길이 | 파일(`C:\tmp\docentrip-video\`) |
|---|---|---|---|---|---|---|
| 1 | M | release | 시뮬레이션 | Part A S6, B1, B6 | 약 5분 10초 | `M_master.webm` |
| 2 | N | release | 시뮬레이션 | B3 | 약 1분 | `N_navi.webm` |
| 3 | D(선택) | release | 시뮬레이션 + Virtual Console | B3-0 | 약 30초 | `D_lock.webm` |
| 4 | G | release | 시뮬레이션 | B4 | 약 1분 30초 | `G_gleo.webm` |
| 5 | L | release | 차량 위치 | B5, B5-0 | 약 1분 | `L_reverse.webm` |
| 6 | V | debug | 시뮬레이션 | B2 | 약 1분 | `V_voice.webm` |

- 7.5의 시험 녹화를 쓰면 N과 L(선택 장면을 넣으면 D도)만 새로 찍으면 됩니다. M, G, V는 시험 녹화가 마음에 들지 않을 때 다시 찍습니다.
- release로 찍을 것을 먼저 다 찍고, 마지막에 debug로 바꿔 V를 찍은 뒤 release로 되돌립니다.
- 테이크마다 시작 전에 `처음부터`를 누릅니다.

**M(끊지 않는 한 번의 주행)**

1. `처음부터` → `대기 중`, `아직 들려준 이야기가 없어요`인지 확인합니다.
2. 녹화를 시작하고(7.2), 2초 뒤 `시뮬레이션 주행 시작`을 누릅니다.
3. 도중에 아무것도 누르지 않습니다. 누르면 요약의 분과 7/7이 바뀝니다.
4. 도착해 `오늘 4.0km(경로 기준) · 5분 · 이야기 7/7`이 나오고 5초 뒤 녹화를 끝냅니다.

- 쓰는 곳(P = 누른 순간, 녹화 시간은 P에 2초를 더함): S6(P부터 약 20초. 화면은 S6a 5초에, 소리는 S6 전체에), B1(P−0.5초~P+30.5초), B6(P+약 4분 20초부터 끝까지. 계산으로 마지막 이야기 시작 약 4분 19.5초, 마지막 문장 약 4분 29.8초, 도착 약 4분 48초).

**N(내비)**

1. `대기 중`(0 km/h)에서 왼쪽 아래 패널이 미디어 위젯 페이지인지 보고, 위젯에 Docentrip과 다음 이야기가 보이는지 확인합니다.
2. 녹화 시작 → 2초 뒤 `내비로 안내` → 내비가 앞으로 나와 경로를 계산합니다. 경로 선택 화면(`N서울타워`, `경유지 2개 포함`)이 나오면 `안내 시작`을 누르거나 저절로 시작되기를 기다립니다(11-07 시험에서는 숫자 5가 줄어든 뒤 시작).
3. 미디어 위젯의 재생 버튼 → 시뮬레이션 주행이 시작되고 광화문 이야기가 내비 위로 들립니다. 위젯에 `광화문, 조선의 얼굴`, `Docentrip`이 보입니다.
4. 15초 더 녹화하고 끝냅니다.

- 위젯에 Docentrip이 안 보이거나 버튼이 듣지 않으면: 앱 목록에서 Docentrip → `시뮬레이션 주행 시작` → 홈 → 내비. 편집에서 그사이를 건너뛰고 표시합니다.
- 내비 화면에 무엇이 보이는지(판교 출발인지, 경유지 표시)와, Docentrip으로 돌아갔을 때 카드 위 글자를 적어 둡니다(6.5 표, 12장).

**D(선택, 설정 잠금)**

1. 정차 상태에서 `시뮬레이션 주행 시작`을 먼저 누릅니다. 차가 움직이는 동안 누르지 않기 위해서입니다.
2. 녹화 시작 → Virtual Console에서 기어 D, 속도 30. 에뮬레이터는 기어를 D로 바꾸는 순간 잠글 수 있습니다.
3. `정차 후 내비 안내`, `정차 후 변경`으로 바뀌면 5초 뒤 속도 0, 기어 P로 되돌리고 녹화를 끝냅니다.

**G(Gleo)**

1. 녹화 시작 → `시뮬레이션 주행 시작`.
2. 약 49초 뒤 `서울시청, 유리와 돌의 대화`가 시작되면 2~3초 뒤 Gleo를 부릅니다(`inject-key 231`).
3. 이야기가 멈추고, Gleo가 닫히면 그 이야기가 처음부터 다시 나옵니다. 10초 더 녹화하고 끝냅니다.

- Gleo는 약 20초 안에 닫혀야 합니다. 더 늦으면 차가 서울시청을 지나, 앱이 (맞게) 그 이야기를 다시 들려주지 않고 넘어갑니다.
- 11-07 시험에서는 Gleo가 약 3초 만에 저절로 닫혔습니다. 녹화 전에 한 번 해 보고 닫히는 방법과 걸리는 시간을 확인합니다.

**L(차량 위치, 후진)**

1. GPS를 데모 경로 출발점에 먼저 둡니다(`geo fix`). 앱은 직전 위치에서 10초 안에 불가능하게 먼 곳으로 뛴 위치를 버립니다. 예전 위치(예: 지난 GPS 주행의 끝점)가 먼저 잡히면 출발점 위치가 최대 10초 동안 무시됩니다.
2. `차량 위치로` → `차량 위치 사용 안내`(7.1에서 지웠으므로 나옴) → `동의하고 차량 위치 사용` → 권한 창에서 허용. B5-0을 쓰려면 이 부분부터 녹화합니다.
3. 머리글이 `0 km/h · 차량 위치 · 0.0 / 4.0 km`이고 아래 줄이 `다음 이야기` `광화문, 조선의 얼굴` `곧`인지 확인합니다. `geo fix`를 `차량 위치로` 뒤에 보냈다면 10초 넘게 기다린 뒤 확인합니다. 안내 문구가 사라진 것만으로는 출발점에 있는지 알 수 없습니다.
4. Virtual Console을 열고 기어 P를 확인합니다.
5. 녹화 시작 → `도슨트 켜기` → 광화문 이야기가 시작됩니다.
6. 3~4초 뒤 Virtual Console에서 기어 R → 3초 → P.
7. 5초 더 녹화하고 끝냅니다. 끝나면 `시뮬레이션으로`를 누릅니다.

**V(음성 명령, debug 빌드)**

1. 지우고 debug 빌드를 설치합니다(debug와 release는 서명이 달라 덮어쓸 수 없습니다. 위치 동의 같은 앱 설정도 지워집니다).
2. 에뮬레이터를 다시 켭니다.
3. Docentrip을 열고 `말로 하기` 버튼이 보이는지 확인합니다. 버튼이 없으면 음성 엔진이 아직 준비되지 않은 것이고, 이때 넣은 녹음은 화면 변화 없이 무시됩니다.
4. 녹화 전 시험: `대기 중`에서 `.\tools\say-command.ps1 '다음'`을 한 번 실행해 `듣는 중`이 나왔다가 `대기 중`으로 돌아오는지 봅니다(이야기가 없어 "다음"은 아무것도 하지 않음). `지금은 차량 마이크로 들을 수 없어요 · 화면의 버튼을 써 주세요`가 나오면 한 번 더 합니다. 설치 직후 첫 번째는 음성 서비스가 6초 안에 답하지 않아 실패한 적이 있습니다(handoff.md 2.3). 녹화 중에 실패하면 청계천 이야기가 처음부터 다시 나와 테이크를 버리게 됩니다.
5. `처음부터` → 녹화 시작 → `시뮬레이션 주행 시작`.
6. 약 26초 뒤 `청계천, 도심을 흐르는 물길`이 시작되면 바로 `.\tools\say-command.ps1 '다음'`을 실행합니다(만들고 넣는 데 1~2초 걸립니다).
7. `듣는 중` → `주행 중`, 아래 줄 `다음 이야기` `서울시청, 유리와 돌의 대화`로 바뀌면 5초 더 녹화하고 끝냅니다. `voice command: SKIP`도 확인합니다.
8. release로 되돌립니다(아래 명령).

- 이 테이크에서는 `말로 하기`를 누르지 않습니다. 누르면 에뮬레이터 마이크(이 PC는 무음)로 듣기 시작하고, 듣는 동안에는 녹음을 넣어도 무시됩니다. 녹음한 명령은 스크립트가 듣기를 시작합니다.
- debug 빌드는 녹음을 넣는 수신기만 더 있고 화면은 release와 같습니다.

```powershell
# V 전: debug 빌드로 바꾸기
& $adb uninstall app.docentrip
.\gradlew.bat :app:assembleDebug
& $adb install -r app\build\outputs\apk\debug\app-debug.apk
& $adb emu kill
.\tools\start-emulator.ps1

# V 뒤: 제출 상태(release)로 되돌리기
& $adb uninstall app.docentrip
& $adb install -r apk\YOUNGIN_Docentrip_release.apk
& $adb emu kill
.\tools\start-emulator.ps1
```

### 7.4 녹화한 뒤

**소리 확인**

- 11-07 시험 녹화본에는 소리 트랙(Vorbis, 44.1kHz, 2채널)이 있고, 이야기가 나오는 구간에서만 소리가 커집니다(음량을 잼. 사람이 직접 듣지는 않음).
- 음량이 작습니다(광화문 이야기 구간 평균 약 -43dB, 최대 약 -27dB). 편집에서 약 20dB 키운 뒤 9장 음량 기준에 맞춥니다.
- 최종 녹화본도 각각 재생해 도슨트 목소리가 들리는지 직접 들어 봅니다.
- 목소리가 없으면 OBS Studio(무료 오픈소스)로 에뮬레이터 창 화면과 "데스크톱 오디오"를 함께 녹화하며 다시 찍습니다. 에뮬레이터 녹화본과 OBS 녹화본을 버튼이 바뀌는 프레임(예: `시뮬레이션 주행 시작` → `주행 정지`)으로 맞춘 뒤 OBS 소리를 씁니다. 상태가 `재생 중`으로 바뀌는 순간에 소리 시작을 맞추지 않습니다. 앱은 음성 엔진에 말하라고 하기 직전에 `재생 중`으로 바꾸므로 화면이 소리보다 먼저 바뀝니다.
- Part B에는 대체 음성을 쓰지 않습니다. 기능 시연이므로 실제 소리만 씁니다.

**변환과 자르기**

- 편집기에는 아래처럼 24fps(CFR)로 바꾼 mp4만 넣습니다. 소리는 48kHz로 맞춥니다.
- PATH에는 ffmpeg가 없습니다(2026-11-07 이 PC 확인). 이 PC에는 Python 패키지 imageio-ffmpeg에 든 ffmpeg가 있으므로, 아래 첫 줄로 `ffmpeg`를 그 파일에 잇습니다(그 창에만 적용). ffprobe는 없으므로 `ffmpeg -hide_banner -i 파일`로 소리 트랙(`Audio: vorbis` 줄)을 봅니다.
- ffmpeg 대신 편집 프로그램의 자르기에 같은 값을 넣고 24fps 타임라인에 넣어도 됩니다.
- 바꾼 뒤 테이크 M의 마지막 이야기에서 소리와 화면이 어긋나지 않는지 봅니다.

```powershell
# ffmpeg 잇기(이 PC: imageio-ffmpeg에 든 ffmpeg. 그 창에만 적용)
Set-Alias ffmpeg (python -c "import imageio_ffmpeg as f; print(f.get_ffmpeg_exe())")
# 소리 트랙이 있는지 확인("Audio: vorbis" 줄)
ffmpeg -hide_banner -i C:\tmp\docentrip-video\M_master.webm
# 앱 영역만(S6a 합성, B1, B2, B6, 선택 장면): 가로 853~2535, 세로 90~1300
ffmpeg -i C:\tmp\docentrip-video\M_master.webm -vf "crop=1682:1210:853:90" -r 24 -c:v libx264 -crf 16 -preset slow -pix_fmt yuv420p -c:a aac -b:a 192k -ar 48000 C:\tmp\docentrip-video\M_app.mp4
# 전체 화면(B3, B4, B5): 크기는 그대로(펀치 인을 원본 해상도에서 하려고). 1920×1080으로 줄이는 것은 편집기에서
ffmpeg -i C:\tmp\docentrip-video\N_navi.webm -r 24 -c:v libx264 -crf 16 -preset slow -pix_fmt yuv420p -c:a aac -b:a 192k -ar 48000 C:\tmp\docentrip-video\N_navi.mp4
```

- 이 PC 에뮬레이터는 왼쪽이 주행 정보, 오른쪽이 앱 영역입니다. S6a도 같은 배치로 합성하면 실제와 맞습니다.
- 녹화 원본은 제출하지 않고, 저장소에도 넣지 않습니다.

### 7.5 2026-11-07 시험 녹화

release 제출본(새 이름)을 시뮬레이션 모드로 녹화했습니다. B2만 debug 빌드입니다. 모두 2560×1440 webm이고 도슨트 소리가 작게 담겨 있습니다(7.4).

| 파일(`C:\tmp\docentrip-demo\`) | 길이 | 담긴 것 | 쓰는 곳 |
|---|---|---|---|
| `B1_auto_story.webm` | 47초 | 누르기 전 약 3초 → `시뮬레이션 주행 시작` → 광화문 이야기 → 청계천 이야기 시작 | B1, Part A S6(화면과 도슨트 소리), S6-0(누르기 전 화면) |
| `B2_voice_next.webm` | 26초 | 청계천 이야기 중 녹음한 "다음" → 이야기 끝, 아래 줄 `서울시청, 유리와 돌의 대화` | B2. 듣는 동안의 약 1초 소리는 들어 보고 판단(12장 7번) |
| `B3_navi.webm` | 25초 | 남산 이야기 중(시뮬레이션 50km/h) `내비로 안내` → 경로 선택 → 안내 시작 | 쓰지 않음. 정차 상태에서 누르지 않았고, 왼쪽 아래 패널이 미디어 위젯이 아님. 테이크 N으로 다시 찍음 |
| `B4_gleo_interrupt.webm` | 35초 | 서울시청 이야기 중 Gleo 호출 → 멈춤 → 처음부터 다시. 왼쪽 아래 미디어 위젯에 이야기 제목 | B4 |
| `B6a_last_stories.webm` | 1분 35초 | 마지막 이야기들(`N서울타워, 도착`까지) | B6 앞부분(마지막 문장) |
| `B6b_arrival_summary.webm` | 1분 25초 | 약 6초부터 도착 화면과 요약 `오늘 4.0km(경로 기준) · 5분 · 이야기 7/7`. 소리 없음 | B6 뒷부분 |
| B5 | 없음 | 시뮬레이션 모드로 찍어 기어를 따르지 않았고(후진해도 이야기가 이어짐), 후방 카메라가 앱을 덮어 버렸습니다 | 테이크 L로 찍음 |

- B1, B4, B6a, B6b는 파일 시각으로 보아 같은 시뮬레이션 주행을 이어서 녹화한 것입니다. 도착 요약은 위 글자 그대로 나왔습니다.
- B6a와 B6b는 따로 녹화한 파일이라, B6 편집의 "약 16초 뒤" 표시는 그대로 둡니다(시속 50km 기준 계산값).
- B3, B4의 전체 화면에는 왼쪽 위 "안전 벨트를 매세요." 경고가 보입니다(2.4).

## 8. 편집 타임라인(전체 약 2분 8초)

| 시간 | 장면 | 그림 | 소리 | 화면 글자 |
|---|---|---|---|---|
| 0:00~0:06 | S1 | AI | 도시 소리, 방향지시등 | - |
| 0:06~0:15 | S2 | AI | 딸·아빠(화면 밖) | 대사 자막 |
| 0:15~0:18 | S3a·S3b | AI 2개(또는 정지 그림 + 느린 밀기), 디졸브·색 전환 | 2026년 → 1995년 거리 소리 | - |
| 0:18~0:23 | S4a | AI | 할아버지(화면 밖) | 대사 자막 |
| 0:23~0:31 | S4b | AI | 할아버지(화면 밖) | 대사 자막 |
| 0:31~0:39 | S4c-1·S4c-2 | AI 2개 | 소년, 할아버지(모두 화면 밖) | 대사 자막 |
| 0:39~0:42 | S5a·S5b | AI 2개(또는 정지 그림 + 느린 밀기), 디졸브·색 전환 | 1995년 소리가 멀어짐, 신호가 바뀌는 순간 음악 큐 | - |
| 0:42~0:43 | S6-0 | AI(중앙 화면에 테이크 M의 누르기 전 화면을 흐리게) | 차 안 소리 | - |
| 0:43~1:02 | S6a(5초)·S6b-1(7초)·S6b-2(7초) | S6a는 정지 그림 + 테이크 M 앱 영역 코너 핀, S6b는 AI | 도슨트 실제 음성, 끝까지 한 번에 | 도슨트 자막(앱 문장 그대로). S6a에 "실제 앱 화면 · 에뮬레이터 녹화(시뮬레이션 주행)" |
| 1:02~1:09 | S7 | AI | 딸·아빠(화면 밖) | 대사 자막 |
| 1:09~1:13 | S8 | AI | 음악 | 1:10~1:12 "현대가 추억을 이어주다" |
| 1:13~1:17 | 연결 | 어두운 바탕 자막 카드 | 음악 이어짐 | "실제 앱 화면으로 보는 Docentrip" / 작게 "Pleos Connect 에뮬레이터 v2.1.0 녹화 · 편집한 곳은 표시" |
| 1:17~1:26 | B1 | 테이크 M, 앱 영역 | 앱 소리 + 낮은 음악 | 6.3 자막 |
| 1:26~1:34 | B2 | 테이크 V, 앱 영역 | 앱 소리. 듣는 동안 조용 | 6.4 자막 |
| 1:34~1:41 | B3 | 테이크 N, 전체 화면 | 앱 소리 | 6.5 자막 |
| 1:41~1:47 | B4 | 테이크 G, 전체 화면 | 앱 소리, Gleo 소리. 음악 거의 뺌 | 6.6 자막 |
| 1:47~1:54 | B5 | 테이크 L, 전체 화면 | 앱 소리. 음악 거의 뺌 | 6.7 자막 |
| 1:54~2:00 | B6 | 테이크 M 끝부분, 앱 영역 | 마지막 이야기의 마지막 문장 | 6.8 자막 |
| 2:00~2:08 | 끝 카드 | 자막 카드 | 음악 마무리 | 아래 |

- S6 길이가 실제 도슨트 길이에 따라 달라지면 뒤를 모두 밉니다. 대사 녹음과 애니마틱으로 Part A 장면 길이가 바뀌어도 마찬가지입니다(4장). 선택 장면 B3-0, B5-0은 각 2초씩 늘어납니다.
- Part B 클립 사이는 0.3초 정도의 짧은 디졸브나 바로 자르기로 잇습니다.

**끝 카드(8초)**

```text
Docentrip
도슨트립 · 차창 밖 이야기

Docentrip · Pleos Connect 앱 · YOUNG:IN
```

- 맨 아래 작은 글씨: "애니메이션 장면은 생성형 AI 도구로 만들었고, 앱 화면은 Pleos Connect 에뮬레이터에서 실제로 녹화했습니다."
- 필요하면 그 아래에 음악 출처를 적습니다. 합성 음성 서비스가 약관에서 표시를 요구하면 그것도 적습니다. Part A 도슨트에 대체 음성을 썼다면 "Part A 도슨트 목소리는 대체 합성 음성"도 적습니다.
- 저장소 주소, 실명, 계정은 넣지 않습니다(2.4).

## 9. 목소리, 소리, 자막

**목소리**

| 소리 | 정한 것 | 출처 |
|---|---|---|
| 도슨트(Part A) | Docentrip 앱의 실제 음성(Pleos TextToSpeech, 차량 온디바이스)을 테이크 M에서 씁니다. 못 쓰면 실제 Pleos 음성과 같은 성별·톤의 차분한 한국어 합성 음성으로 같은 문장만 읽게 하고, 끝 카드에 대체 음성임을 적습니다 | 녹화본. 대체 음성은 아래 합성 음성과 같은 기준 |
| 도슨트(Part B) | 녹화본의 실제 음성만 씁니다. 대체 음성은 쓰지 않습니다 | 녹화본 |
| 할아버지 | 40대 남성, 따뜻하고 느긋한 1990년대 아버지 말투 | 사람이 녹음하면 서면 동의. 합성 음성은 상업 이용이 되는 비중국계 서비스(예: Typecast(한국), ElevenLabs(미국), 약관 확인). 실존 인물의 목소리를 흉내 내거나 복제하지 않음 |
| 아빠 | 40대 남성, 부드러운 목소리 | 위와 같음 |
| 딸 | 10대 후반 여성 | 위와 같음 |
| 어린 아빠 | 12세 남자아이 | 위와 같음. 어린이가 녹음하면 보호자 서면 동의 |
| 내레이션 | 넣지 않습니다. Part B는 도슨트 목소리와 겹치므로 설명을 자막으로 합니다 | - |

**배경음과 음악**

- 배경음: 2026년은 조용한 차 안, 방향지시등. 1995년은 옛 버스 엔진, 경적, 라디오 잡음(실제 방송이나 노래는 넣지 않음).
- 효과음도 직접 녹음했거나, 상업 이용과 행사 상영이 허락된 것만 씁니다(2.4).
- 음악: 직접 만들었거나 사용 허락받은 잔잔한 피아노. S3·S5 전환에서도 끊기지 않게 합니다. S5에서 신호가 초록으로 바뀌는 순간은 음악 큐로 표현합니다.
- Part B에서는 같은 곡을 낮게 이어 깔고, 도슨트가 나오면 더 줄입니다.
- B2, B4, B5에서는 이야기가 멈춘 것이 들리도록 그 2~3초 동안 음악을 거의 뺍니다.
- 음량(팀 제안): 전체 약 -16 LUFS, 피크 -1 dBTP 이하. 대사와 도슨트가 음악보다 분명히 커야 합니다.

**자막**

- Part A: 모든 대사에 한국어 자막을 넣습니다. 도슨트 자막은 앱 문장을 글자 그대로 씁니다.
- Part B: 클립마다 큰 줄, 작은 줄, 정직 표시(6장). 도슨트 말은 B1·B2·B4·B6에서는 앱 본문이 보이므로 자막을 달지 않습니다. B3(내비가 앞에 있어 위젯 제목만 보임)와 B5(R에서 후방 카메라가 앱을 덮음)는 본문이 화면에 없으므로, 필요하면 들리는 문장을 `route.json` 글자 그대로 자막으로 넣습니다. B2의 명령은 "[명령] 다음" 자막으로만 보여 줍니다.
- 글꼴은 영상 사용이 허락된 것으로 합니다(예: 네이버 나눔 글꼴, SIL OFL).
- S6a의 앱 화면, S8의 제목 자막, Part B의 앱 글자를 가리지 않게 배치합니다.
- **입히기:** 본편도 Part B 자막(큰 줄·작은 줄·정직 표시·배속·건너뜀·정지 화면 표시·오른쪽 위 고정 표시)과 S6a의 "실제 앱 화면" 표시는 반드시 영상에 입힙니다. SRT는 심사위원의 플레이어에서 꺼져 있을 수 있고, 그러면 정직 표시가 사라져 영상만으로는 사실과 다르게 보일 수 있습니다. SRT는 Part A 대사의 보조로만 둡니다. 세로판은 모든 자막을 영상에 입힙니다.

## 10. 내보내기와 제출

| 판 | 내용 | 형식 | 파일 이름 |
|---|---|---|---|
| 본편 | Part A + 연결 + Part B + 끝 카드, 약 2분 8초 | 16:9, 1920×1080, MP4 | `YOUNGIN_Docentrip_demo_16x9.mp4` |
| 기능 시연(선택) | 연결 + Part B + 끝 카드, 약 55초 | 16:9, 1920×1080, MP4 | `YOUNGIN_Docentrip_features_16x9.mp4` |
| 숏폼(선택) | Part A 세로판, 약 54초(30초~1분), 자막을 영상에 입힘 | 9:16, 1080×1920, MP4 | `YOUNGIN_Docentrip_shorts_9x16.mp4` |

**내보내기 설정(공식 기준은 없음, 호환성을 위한 팀 제안)**

- 영상: H.264(High), yuv420p, 프레임레이트는 영상 AI 클립·녹화와 같은 값(대개 24fps), 12~16Mbps.
- 소리: AAC-LC, 48kHz 스테레오, 192kbps 이상.
- 본편은 약 195~260MB입니다. 메일 첨부가 어려우면 링크로 냅니다.

**내보내기 전**

- 파일 정보(작성자·제목)를 지우고, 웹에서 바로 재생되게 합니다: `ffmpeg -i in.mp4 -map_metadata -1 -c copy -movflags +faststart YOUNGIN_Docentrip_demo_16x9.mp4`
- 지운 결과를 확인합니다: `ffmpeg -hide_banner -i YOUNGIN_Docentrip_demo_16x9.mp4`(7.4처럼 ffmpeg를 이은 창에서)로 제목·작성자·설명 같은 태그와 파일 경로(Windows 사용자 폴더 이름 포함)가 남지 않았는지 보고, 파일 속성 → 자세히에서도 봅니다. 편집기에서 내보낸 파일에는 작성자나 프로젝트 경로가 들어갈 수 있습니다. ffmpeg가 없으면 편집기 내보내기 설정에서 메타데이터를 비우고 파일 속성으로 확인합니다.
- 워터마크가 없는지 확인합니다.
- 영상 AI의 공유 링크는 제출하지 않습니다.

**제출**

- 접수양식 7번 "참고자료"에 파일이나 링크로 냅니다. 마감은 2026-11-15 23:59입니다.
- 링크는 로그인 없이 열려야 하고, 소유자 이름에 실명이나 개인 계정이 보이지 않아야 합니다. 로그아웃한 브라우저로 열어 확인합니다.
- 저장소의 Releases에 올리는 방법도 있습니다. 저장소는 이미 제출하기로 했으므로 새 계정이 드러나지 않습니다. 다만 공개 저장소에 영향을 주는 일이라 대표자 확인 뒤에 하고(handoff.md 4장), 영상이 누구에게나 공개된다는 점도 함께 판단합니다.

**숏폼 챌린지 규정**

- 세로, 30초~1분
- Instagram 릴스나 YouTube Shorts에 게시
- 해시태그 `#HMG차량용앱개발공모전` 포함 2~3개
- 링크는 접수양식 8번. 가산점은 없습니다.
- 숏폼으로 낸 영상은 현대자동차그룹 행사 시상식에서 쓰일 수 있습니다(2.4).
- 개인 계정 대신 팀 계정을 권합니다.

**세로판 장면 구성(가로 영상을 가운데만 자르면 인물이 잘리므로 따로 만듦)**

| 장면 | 세로판에서 |
|---|---|
| S2, S7 | 딸과 아빠를 번갈아 한 사람씩 크게. 말하는 사람은 입이 안 보이게(4장) |
| S4 | 앞 유리 너머 세로 구도 |
| S6 | 실제 Docentrip 화면(테이크 M 앱 영역)을 폭에 맞춰 1080×777로 가운데 두고, 위아래에는 흐린 S6b를 깖. "실제 앱 화면 · 에뮬레이터 녹화" 자막 |
| S8 | 세로 항공 화면과 제목, 이어서 크레딧 "Docentrip · Pleos Connect 앱 · YOUNG:IN" |

- Part A가 약 73초라 세로판은 따로 줄입니다. 예: S4a·S4b를 빼고 S1·S3·S5를 각 2초로 줄이면 약 54초입니다. 도슨트(약 19초)와 S7은 자르지 않습니다. Part B는 넣지 않습니다.
- 앱 화면(1.39:1)을 세로 화면 높이에 꽉 채우면 가로의 약 60%가 잘려 제목과 버튼이 사라지므로, 위처럼 폭에 맞춥니다.
- 자막은 가운데 위쪽에 둡니다. 릴스·쇼츠의 아래쪽 설명과 오른쪽 버튼 줄 자리는 비우고, 게시 전에 앱 미리보기로 가려지지 않는지 확인합니다.

## 11. 체크리스트

**녹화 전**

- [ ] 에뮬레이터 Cold boot → `adb uninstall app.docentrip` → release APK 설치 → 에뮬레이터를 한 번 더 켬(미디어 위젯 제목, 테이크 L의 동의 화면)
- [ ] Docentrip 시뮬레이션 모드, `처음부터`
- [ ] 왼쪽 아래 패널이 미디어 위젯 페이지
- [ ] Virtual Console 기어 P, 속도 0. 경고등(Telltales)의 안전벨트를 Off로 바꿔 보고 경고가 사라지는지 확인
- [ ] 터치 표시 끔
- [ ] 10초 시험 녹화로 글자 선명도와 소리 확인

**녹화**

- [ ] M: 끊지 않고 도착까지. 마지막에 `오늘 4.0km(경로 기준) · 5분 · 이야기 7/7`
- [ ] N, (D), G, L, V를 7.3 순서대로
- [ ] L: `geo fix`를 `차량 위치로`보다 먼저. 머리글 `0.0 / 4.0 km`와 아래 줄 `광화문, 조선의 얼굴` `곧` 확인
- [ ] V: `말로 하기` 버튼 확인, 녹화 전에 `say-command.ps1 '다음'`을 한 번 시험
- [ ] 테이크마다 소리가 담겼는지 들어 봄
- [ ] B3 내비 화면과 Docentrip 카드 위 글자, B4 Gleo가 닫히기까지 걸린 시간을 적어 둠
- [ ] 끝나면 release로 되돌리고 에뮬레이터를 다시 켬

**편집**

- [ ] 대사를 먼저 녹음하고 애니마틱으로 Part A 장면 길이를 정함
- [ ] Part A 도슨트 문장이 `route.json`과 글자 하나까지 같고, 끝까지 한 번에 나옴
- [ ] Part A 대사는 모두 입이 안 보이게(립싱크 없음)
- [ ] S6-0에서 딸(동승자)이 누름. 운전자가 화면을 만지는 장면 없음
- [ ] S6a는 코너 핀 합성이고 "실제 앱 화면 · 에뮬레이터 녹화(시뮬레이션 주행)"
- [ ] 녹화본은 24fps mp4로 바꿔 넣음
- [ ] Part B 앱 화면은 모두 녹화본이고 글자를 고치지 않음
- [ ] 빨리 감기, 건너뛰기, 느리게 한 곳, 정지 화면을 표시함
- [ ] B2, B4, B5의 멈추는 장면을 자르지 않음
- [ ] 펀치 인(6.2)
- [ ] 자막에 SDK 5개가 한 번씩. 2.1 "쓰지 않을 말"이 없음
- [ ] 정직 표시: B2(녹음 명령), B3(판교 고정), B4(키 입력), B5(GPS·Virtual Console·후방 카메라), B5-0(권한 단계 생략), B6(경로 기준), 시뮬레이션 표시(B1, B2, B3, B4, B6)
- [ ] Part B 자막과 표시를 영상에 입힘(SRT로만 두지 않음)
- [ ] 끝 카드 크레딧 "Docentrip · Pleos Connect 앱 · YOUNG:IN"과 AI 표시
- [ ] Part A에 로고, 엠블럼, 차 이름, 읽히는 글자 없음. 운영사무국 답 전에는 1995년 차에 원스포크 핸들·조종간 모양 변속 레버 없음
- [ ] 음악·목소리·효과음·글꼴이 제출·SNS 게시·행사 상영까지 허락됨
- [ ] 실명, 연락처, 이메일, 계정, 저장소 주소 없음

**내보내기와 제출**

- [ ] 1920×1080, H.264, AAC 48kHz, 파일 이름 `YOUNGIN_Docentrip_*.mp4`
- [ ] 파일 정보를 지우고 `ffmpeg -i`·파일 속성으로 확인, 워터마크 없음
- [ ] 링크를 로그아웃 상태에서 열어 봄, 소유자 이름 확인
- [ ] 접수양식 7번에 적음(숏폼은 8번), 마감 2026-11-15 23:59

## 12. 확인이 필요한 것

1. **생성형 AI 사용 규정**
   - [공식 설명회 다시보기](https://youtu.be/na6tSJ44pWc)에 "공모전 Q&A: 생성형 AI 활용"(20:28~21:05) 구간이 있습니다. 내용은 확인하지 못했습니다. 이 구간과 "필수·선택 제출물"(09:37~), "심사 기준과 가산점"(12:27~)을 직접 봐 주세요.
   - 써 있는 규정에는 AI 영상에 대한 언급이 없습니다.
   - 참고로 숏폼 안내의 예시 이미지는 "AI 생성"으로 표시돼 있습니다. 규정은 아닙니다. 이 영상은 끝 카드에 AI 사용을 적습니다.
2. **가산점 인정 범위**
   - 가산점 항목은 "[가산점] 개발 산출물"이고, 그 아래 선택 제출물로 "프로토타입 시연 영상 (화면 녹화 포함)"이 있습니다. 제출 안내의 인정 범위는 "목업 · 프로토타입 · 화면 녹화"입니다.
   - Part B가 실제 앱 화면 녹화이므로 이 항목에 맞습니다. 애니메이션(Part A)은 언급이 없어, 1번과 함께 설명회 구간을 확인합니다.
3. **현대 이름·로고·차량 디자인·제목:** 운영사무국에 묻습니다(2.4). 1세대 그랜저 실내 특징(원스포크 핸들, 비행기 조종간 모양 변속 레버)도 함께 묻습니다. 답이 늦으면 대체 제목과 일반적인 차로 갑니다.
4. **1968년 한글 현판 색과 1세대 그랜저 시트 무늬:** 공식 자료가 없습니다. 당시 사진을 참고합니다. 지금 프롬프트는 현판을 읽히지 않게 둡니다.
5. **비슷한 이전 작품:** 심사위원이 알 수 있는 것이 셋 있습니다.
   - 이전 Pleos Connect 앱 아이디어 공모전 대상 "창밖동화": 지나가는 지역 이야기를 아이에게 생성(인사이트 2026-10-01).
   - 현대차그룹 해커톤 수상작 TaleSync: 차량용 대화형 오디오 이야기(현대차그룹 뉴스룸 2026-08-24).
   - DocentPro: 스마트폰 GPS 오디오 투어 가이드 앱.
   - Docentrip은 차의 경로 진행에 맞춰 사실 기반 장소 해설을 들려주고, 차와 묶여 있습니다(차량 음성, 미디어 키, 내비, 차량 정보, 오디오 포커스, 주행 제한). 화면을 볼 필요가 없습니다. Part A에서는 앱 문장 그대로의 도슨트가, Part B에서는 차량 연동 장면(B2~B6)이 이 차이를 보여 줍니다. 아이용 동화나 대화형 이야기처럼 보이는 연출은 피합니다.
6. **에뮬레이터 시스템 화면을 SNS에 써도 되는지:** Part B의 내비 지도, Gleo, 후방 카메라 화면을 숏폼 같은 공개 게시물에 쓰려면 3번과 함께 운영사무국에 묻습니다. 심사 제출용 본편에는 그대로 씁니다(2.4).
7. **녹화해 봐야 아는 것**(11-07 시험 녹화로 확인한 것은 표시)
   - B5(확인됨): R에서는 후방 카메라 화면이 앱 영역을 모두 덮어 후진 안내 문구가 보이지 않습니다. 문구 없이 갑니다(6.7).
   - B3(일부 확인): 내비는 판교 출발로 경로를 계산하고(경로 선택에 `경유지 2개 포함`, 안내 중 `대왕판교IC 방면`), `안내 시작`은 숫자가 줄어든 뒤 저절로 시작했습니다. Docentrip 카드 위 글자는 `내비로 안내`로 남았습니다. 위젯의 재생 버튼이 듣는지는 새 녹화에서 봅니다.
   - B2(일부 확인): `마이크를 켜고 있어요` 약 2.6초, `듣고 있어요 · …` 약 0.2초(녹음 명령). 듣는 동안 약 1초 길이의 다른 소리가 있었는데 무엇인지는 확인하지 못했습니다. 들어 보고 명령 녹음이 들리면 그 부분 소리를 뺍니다.
   - B4(확인됨): Gleo는 화면 위쪽 가장자리의 파란 빛으로만 보이고, 약 3초 만에 닫혀 이야기가 처음부터 다시 나왔습니다.
   - 소리: 시험 녹화본에는 소리 트랙이 있고 이야기 구간에서만 소리가 커집니다(음량을 잼). 음량이 작아 편집에서 키웁니다(7.4). 최종 녹화본은 직접 들어 확인합니다.
   - 광화문 이야기의 실제 길이(약 19초로 추정. Part A S6 길이).
   - 안전띠 경고를 Virtual Console로 끌 수 있는지(7.1).

## 13. 출처(주요)

- 공모전 공식 사이트(2026-11-04 확인)
  - [지원·제출 방법](https://appdevchallenge.auto-hmg.io/guide/submission)
  - [심사·시상](https://appdevchallenge.auto-hmg.io/guide/judging-awards)
  - [숏폼 챌린지](https://appdevchallenge.auto-hmg.io/guide/shortform)(유의사항: 행사 시상식에서 사용될 수 있음, 초상권·저작권 책임은 응모자)
  - [FAQ](https://appdevchallenge.auto-hmg.io/faq)
  - [출품 유의사항](https://appdevchallenge.auto-hmg.io/notice/c0a1a000-2026-4923-8000-000000000006)(사실과 다른 내용, 지식재산권, 2차 PT의 에뮬레이터 구동 화면 시연)
  - [접수양식](https://appdevchallenge.auto-hmg.io/downloads/contest-application-consent.docx)
  - [설명회 다시보기](https://youtu.be/na6tSJ44pWc)
- 광화문과 세종로
  - 국가기록원 [광화문](https://theme.archives.go.kr/next/koreaOfRecord/Gwanghwamun.do), [조선총독부 청사](https://theme.archives.go.kr/next/place/governmentOffice.do?flag=1)
  - [서울신문 2010-08-15](https://m.seoul.co.kr/news/2010/08/15/20100815800024)
  - [주간경향 2023-10-27(해태상)](https://weekly.khan.co.kr/article/202310271120451)
  - [서울시 2023-10-25(월대·현판)](https://mediahub.seoul.go.kr/archives/2009329)
  - [서울시 2007-03-20(16차로)](https://mediahub.seoul.go.kr/archives/165974)
  - [서울신문 2024-01-17(이순신 동상)](https://www.seoul.co.kr/news/culture/20240117500105)
  - [서울시 2006-12-27(세종로 중앙녹지대 은행나무 29주)](https://mediahub.seoul.go.kr/archives/154514), [경향신문 2009-08-07(은행나무 29그루를 2008년 5~9월 옮겨 심음)](https://www.khan.co.kr/article/20090807112340A/)(2026-11-07 확인)
- 그랜저와 Pleos Connect
  - [현대차 그랜저 역사](https://www.hyundai.com/kr/ko/brand/brandstory/model/grandeur-history/1986-grandeur-l1)
  - [HMG Journal 2026-06-02](https://www.hyundaimotorgroup.com/ko/story/hyundai-grandeur-history)
  - [현대차 뉴스룸 2026-04-30(Pleos Connect)](https://www.hyundai.com/worldwide/en/newsroom/detail/hyundai-motor-group-redefines-in-vehicle-experience-with-pleos-connect-next-generation-infotainment-system-0000001168)
- 안전
  - [도로교통법](https://www.law.go.kr/법령/도로교통법) 제50조
  - [1990년 도로교통법](https://www.law.go.kr/LSW/lsInfoP.do?lsiSeq=5608&efYd=19901102) 제48조의2
  - [2026 그랜저 취급설명서(에어백)](https://ownersmanual.hyundai.com/full_webhelp/GN7/2026/ko_KR/id0830046650f.html)
- 비슷한 이전 작품: [인사이트 2026-10-01](https://www.insight.co.kr/amp/news/576146), 현대차그룹 뉴스룸 2026-08-24(TaleSync)
- 영상 AI 도구와 자막(2026-11-07 확인)
  - [Runway: Creating with Gen-4](https://help.runwayml.com/hc/en-us/articles/37327109429011-Creating-with-Gen-4)(프롬프트 1,000자, 5·10초, 시작 이미지 필요)
  - [Runway: Gen-4 프롬프트 안내](https://help.runwayml.com/hc/en-us/articles/39789879462419)(부정형 문장은 듣지 않거나 반대로 나올 수 있음)
  - [Google Cloud: Veo 3.1](https://docs.cloud.google.com/vertex-ai/generative-ai/docs/models/veo/3-1-generate)(4·6·8초, 참조 이미지로 만들면 8초)
  - [Netflix Korean Timed Text Style Guide](https://backlothelp.netflix.com/hc/en-us/articles/216001127)(성인 초당 12자)
- 앱(저장소 https://github.com/WhoisHOO/pleos-app-challenge, 2026-11-07 기준)
  - 이야기 문장과 경로: `app/src/main/assets/route.json`
  - 화면 글자: `app/src/main/java/app/docentrip/ui/DocentripScreen.kt`, 상태와 동작: `DocentEngine.kt`
  - 음성 명령 낱말: `core/VoiceCommand.kt`, Gleo·통화 때 동작: `audio/FocusedDocentSpeaker.kt`, 미디어 위젯 글자: `media/StoryLibrary.kt`, 미디어 키 동작: `media/DocentPlayer.kt`
  - 트리거와 조용한 구간: `core/DocentPlanner.kt`, GPS 점프 거르기: `core/FixGate.kt`, 트립 요약: `core/TripSummary.kt`, 주행 제한: `car/DrivingRestrictions.kt`, 내비 안내 상태: `navigation/NavigationLink.kt`
  - 에뮬레이터 확인 기록: `README.md`, `docs/architecture.md` 10장
  - 명령과 알려진 문제: `docs/handoff.md` 2.3, 6장, 7장
  - 녹화 도구: `tools/start-emulator.ps1`, `tools/say-command.ps1`(차량 위치 모드로 달리는 장면이 더 필요하면 `tools/drive-demo-route.ps1`)
- 2026-11-07 시험 녹화: `C:\tmp\docentrip-demo\`(파일 목록은 7.5. 저장소에 넣지 않음)
- 에뮬레이터 녹화 옵션: 에뮬레이터 콘솔 도움말 `adb emu help screenrecord start`(2026-11-07)
