# 시연 영상 제작 프롬프트: "현대가 추억을 이어주다"

애니메이션 영상 AI에 줄 프롬프트입니다.

**조사와 검증**

- 시대 고증은 2026-11-03, 공모전 규정은 2026-11-04에 조사했고, 이 두 부분은 검증자가 다시 확인했습니다.
- 앱 동작은 소스 코드와 에뮬레이터에서 직접 확인했습니다.
- 이 문서 자체도 사실과 제작 실무 두 방향으로 검토해 고쳤습니다.
- 출처는 맨 아래에 있습니다.

**영상 AI에는 5장의 영문 블록만 붙여 넣습니다.** 나머지는 만드는 사람이 보는 설명입니다.

## 1. 한눈에

| 항목 | 정한 것 |
|---|---|
| 제목 | 현대가 추억을 이어주다. "현대(現代, 지금 이 시대)"와 "현대자동차"의 이중 의미. 쓰기 전에 운영사무국에 확인할 것(2.4). 대체 제목: "길이 추억을 이어주다" |
| 줄거리 | 2026년, 광화문 앞 신호에 멈춘 차. 조수석의 딸이 "아빠, 여기 어디야?" 하고 묻습니다. 아빠의 기억이 1995년으로 겹쳐집니다. 같은 자리 신호에서, 그랜저 조수석의 어린 아빠에게 할아버지가 광화문 이야기를 들려줍니다. 할아버지의 이야기는 그 시대까지입니다. 신호가 바뀌어 2026년의 차가 출발하면 Roadio가 광화문 이야기를 들려주고, "지금의 문은 2010년에 옛 모습대로 다시 세운 것입니다"로 할아버지의 이야기를 이어받습니다 |
| 길이 | 약 58초. 공식 최소·최대 시간은 없습니다. 숏폼 챌린지용 세로판은 30초~1분으로 따로 만듭니다(8장) |
| 화면비 | 본편 16:9(1920×1080). 숏폼 9:16(1080×1920) |
| 스타일 | 손그림 느낌의 2D 애니메이션. 현재는 맑은 색, 1995년은 따뜻하고 바랜 필름 색 |
| 언어 | 대사와 내레이션은 한국어, 한국어 자막 포함(6장) |

## 2. 반드시 지킬 것

### 2.1 앱을 사실대로 보여 주기

공모전 안내: "제출한 서류에 기재한 내용이 사실과 다를 경우 선정이 취소될 수 있습니다."

**Roadio의 실제 동작**

- 도슨트를 켜 둔 상태에서 경로상 광화문에 들어서면 이야기가 자동으로 시작됩니다.
- 질문에 답하지 않습니다. 딸의 "여기 어디야?"는 아빠에게 묻는 말이고, Roadio는 그 전에도 뒤에도 질문을 듣지 않습니다.
- 이야기는 한 번 시작하면 끝까지 이어서 나옵니다. 멈추는 것은 전화나 Gleo 음성 명령이 올 때뿐입니다. 그래서 영상에서도 **도슨트 음성은 한 번에 끝까지(약 15초) 나오게** 합니다.

**영상에서 지킬 것**

- 도슨트 대사는 앱 문장 그대로 씁니다: "방금 출발한 곳이 광화문 앞입니다. 광화문은 조선 왕조의 법궁인 경복궁의 정문으로, 지금의 문은 2010년에 옛 모습대로 다시 세운 것입니다. 이제 세종대로를 따라 남쪽으로 내려가며 서울의 오래된 길을 지나갑니다."
  - 재생 시간은 글자당 약 0.13초로 잡아 약 15초로 추정합니다.
  - 가족이나 1990년대를 언급하는 맞춤 대사는 앱에 없으므로 넣지 않습니다.
- 차 화면의 Roadio는 실제 앱 녹화본을 합성합니다(7장).
  - 마이크 아이콘, 말풍선, 대화창, 지도를 Roadio 화면에 넣지 않습니다.
  - 녹화본에 "실제 앱 화면 · 에뮬레이터 녹화" 자막을 붙입니다. 녹화본은 시뮬레이션 모드라 화면에 "시뮬레이션"이 보이기 때문입니다.

### 2.2 시대 고증

| | 1995년 봄(8월 전) | 2026년 |
|---|---|---|
| 광화문 | 1968년 철근 콘크리트로 다시 지은 문. 현판은 한글 "광화문". 경복궁 축에서 3.75° 틀어짐 | 2010년 제자리에 나무로 다시 지은 문. 현판은 한자 "光化門", 검은 바탕에 금색 글자(2023) |
| 문 앞 | 월대 없음. 도로가 문 바로 앞을 지남. 해태상 둘이 문 양옆 담장에 바짝 붙어 있음 | 돌로 된 월대(가운데 어도). 해태상은 월대 남쪽 끝 근처. 도로는 월대를 둥글게 돌아감 |
| 문 뒤 | 돔이 있는 거대한 화강암 건물(옛 조선총독부 청사, 당시 국립중앙박물관). 1995-08-15 첨탑부터 철거 시작 | 흥례문과 경복궁. 총독부 건물 없음 |
| 세종로 | 광화문에서 세종로 사거리까지 약 600m, 왕복 16차로. 광장 없음. 이순신 장군 동상(1968)은 사거리 쪽, 문에서 약 600m 남쪽 | 광화문광장(나무, 분수). 차도는 동쪽, 광장은 서쪽(세종문화회관 쪽). 이순신 장군 동상과 금색 좌상 세종대왕 동상(2009) |
| 차 | 각진 1세대 그랜저(1986~1992). 원스포크 핸들, 비행기 조종간 모양 변속 레버(현대차 헤리티지 자료) | 2026 더 뉴 그랜저(Pleos Connect가 처음 들어간 차, 17인치 중앙 디스플레이) |

**넣지 말 것**

- 1995년 장면: 세종대왕 동상, 광화문광장, 한자 현판, 월대, 유리 고층 빌딩
- 2026년 장면: 총독부 건물, 한글 현판(2026-10 기준 논의 중일 뿐), "감사의 정원" 석조물 줄(2026-05 개장, 정치적 논란)
- 공통: 한 화면에 광화문 해태상과 이순신 장군 동상을 같이 보이게 하지 않습니다. 약 600m 떨어져 있습니다.

### 2.3 안전(현대차가 심사함)

- **안전띠:** 모든 사람이 맵니다. 1995년의 할아버지와 어린 아빠도 맵니다. 당시 법도 앞좌석 안전띠는 의무였습니다(1990-11부터).
- **딸:** 고등학생(약 16세)으로 보이게 하고 좌석을 뒤로 빼 둡니다. 현대차 그랜저 설명서는 어린이는 뒷좌석에, 10~15세 정도가 앞에 앉아야 하면 안전띠를 매고 좌석을 최대한 뒤로 빼라고 합니다.
- **1995년의 어린 아빠:** 12세 정도로 보이게 하고 안전띠 맨 모습이 보이게 합니다.
- **운전자:** 차가 움직이는 동안에는 두 손을 핸들에, 눈을 앞에 둡니다. 할아버지가 손으로 가리키는 것은 신호에 완전히 멈춰 있을 때만입니다.
- **화면 조작:** 운전자는 화면을 만지지 않습니다. 도슨트를 켜는 장면을 넣는다면 딸(동승자)이 누릅니다.

### 2.4 상표, 개인정보, 권리

**운영사무국(appdevchallenge@hyundai-partners.com)에 먼저 물을 것**

- 공모전 규정은 상표·디자인권을 적법하게 쓸 것을 요구하고, 운영사(현대자동차·기아)의 지식재산권도 침해 대상에 넣습니다. 다음 넷은 허락이 없습니다: 현대 로고, 차 이름, 차량 디자인(오페라 글라스 같은 고유 특징), 제목의 "현대".
- 답을 받기 전에는 엠블럼·로고·글자 없는 일반적인 차(1995년은 각진 1980년대 세단, 2026년은 현대적인 대형 세단)로 만들고, 대체 제목을 준비합니다.
- 마지막 자막 "Pleos Connect 앱 · SOLOMON"은 남겨, Roadio가 현대차 기본 기능이 아니라 출품작임을 분명히 합니다.

**노출 금지 조건(접수양식 문구. 팀은 "블라인드"라고 부름)**

- 원문: "공유 링크와 첨부자료에도 개인정보·계정정보·개발자 실명이 노출되지 않도록 확인하세요."
- 자막, 크레딧, 내레이션, 파일 정보(작성자·제목), 링크 소유자 이름에 실명과 계정을 넣지 않습니다. 크레딧은 "Roadio", "SOLOMON"만 씁니다.
- 영상 AI에 넣는 프롬프트에도 개인정보를 넣지 않습니다. 5장의 영문 블록에는 없습니다.

**그 밖의 권리**

- 번호판은 읽히지 않게, 실존 인물(전직 대통령 등)의 얼굴은 넣지 않습니다.
- 음악, 목소리, 글꼴(자막 포함)은 직접 만들었거나 영상 사용 허락을 받은 것만 씁니다.

**영상 AI 도구**

- 이용약관에서 결과물을 공모전과 상업 목적으로 써도 되는지 확인합니다.
- 워터마크(계정 이름이 들어가는 것 포함) 없이 내보낼 수 있는 요금제를 씁니다.
- 팀 규칙상 중국계 영상 AI(Kling, Hailuo, Seedance, Vidu, Wan 등)는 피합니다.

## 3. 등장인물과 차(모든 장면에서 같게)

| 이름표 | 설명 |
|---|---|
| DAD_2026 | 43세 한국인 남성. 짧은 검은 머리, 부드러운 인상, **자기 왼쪽 눈썹 위 작은 점**(정면에서 보면 화면 오른쪽). 연한 하늘색 셔츠. **왼손목에 오래된 은색 손목시계**(할아버지 것) |
| DAD_1995 | 12세 소년. 같은 얼굴형, 같은 자리의 작은 점. 90년대 줄무늬 티셔츠 |
| GRANDPA_1995 | 42세 한국인 남성. 흰 와이셔츠, 넥타이 느슨하게, **왼손목에 은색 손목시계**(DAD_2026과 같은 시계). 웃을 때 눈가 주름 |
| DAUGHTER_2026 | 16세 고등학생. 어깨 길이 머리, 회색 후드티 |
| CAR_2026 | 짙은 회색의 현대적인 대형 세단. 엠블럼·글자 없음. 17인치 와이드 중앙 디스플레이 |
| CAR_1995 | 각진 1980년대 대형 세단. 엠블럼·글자 없음. 벨벳 느낌 시트, 원스포크 핸들, 비행기 조종간 모양 변속 레버 |

- 점과 손목시계가 두 시대를 잇는 표시입니다. 할아버지의 시계를 지금 아빠가 차고 있습니다.
- DAD_1995와 DAD_2026은 같은 쪽(인물의 왼쪽)에서 찍어 점이 보이게 합니다.

## 4. 장면 구성(약 58초)

**소리 표기**

- 화면 대사: 입이 보이는 대사입니다. 립싱크가 필요합니다.
- 화면 밖: 입이 안 보이게 찍는 대사입니다.

| 장면 | 시간 | 화면 | 소리 | 전환 |
|---|---|---|---|---|
| S1 | 0~6초 | 2026년 늦은 오후. 광화문 월대 앞 신호에 멈춘 CAR_2026. 뒤로 나무 광화문(현판은 작게, 읽히지 않을 만큼) | 도시 소리, 방향지시등 | - |
| S2 | 6~13초 | 차 안(뒷좌석에서 본 넓은 화면). DAUGHTER_2026이 창밖 광화문을 보다가 아빠에게 고개를 돌림 | 딸(화면 밖): "아빠, 여기 어디야?" 아빠(화면 밖): "광화문. 아빠도 어릴 때 할아버지랑 여기서 신호 기다렸었지…" | - |
| S3 | 13~16초 | DAD_2026의 왼손(은색 시계)이 핸들 위에 → 같은 구도로 GRANDPA_1995의 왼손(같은 시계)이 원스포크 핸들 위에 | 2026년 소리가 작아지고 1995년 거리 소리(옛 버스 엔진, 라디오)가 들어옴 | 편집에서 디졸브와 색 전환(클립 2개) |
| S4a | 16~21초 | 1995년 세종로 넓은 화면. 왕복 16차로, 90년대 차와 버스. 멀리 콘크리트 광화문과 그 뒤 돔 있는 화강암 건물 | 할아버지(화면 밖): "저기 광화문 보이지? 경복궁 정문이야." | - |
| S4b | 21~28초 | 광화문 바로 앞 신호에 멈춘 CAR_1995 안(뒷좌석에서). 앞 유리 너머 흐릿한 광화문과 거대한 돔 건물. 할아버지가 한 손을 들어 가리키고, 안전띠 맨 소년이 몸을 앞으로 기울임 | 할아버지(화면 밖): "6·25 때 불타서, 지금 저 문은 1968년에 콘크리트로 다시 지은 거란다." | - |
| S4c | 28~34초 | 소년의 얼굴 클로즈업 → 할아버지의 옆얼굴 | 소년: "뒤에 큰 건물은요?" 할아버지: "옛날 총독부 건물. 곧 헐린대. 광화문도 언젠가 제 모습을 찾겠지." | - |
| S5 | 34~37초 | 앞을 보는 DAD_1995의 옆얼굴 → 같은 구도로 앞을 보는 DAD_2026의 옆얼굴(같은 점) | 1995년 소리가 멀어짐. 신호가 바뀌는 소리 | 편집에서 디졸브와 색 전환(클립 2개) |
| S6 | 37~52초 | 2026년. 신호가 바뀌어 차가 출발. 중앙 디스플레이 클로즈업(왼쪽 주행 정보, 오른쪽 Roadio **실제 앱 녹화본**, 자막 "실제 앱 화면 · 에뮬레이터 녹화") → 딸이 오른쪽 창밖 광화문광장을 봄 → 아빠는 앞을 보며 미소 | **도슨트(앱 실제 음성, 끝까지 한 번에):** "방금 출발한 곳이 광화문 앞입니다. 광화문은 조선 왕조의 법궁인 경복궁의 정문으로, 지금의 문은 2010년에 옛 모습대로 다시 세운 것입니다. 이제 세종대로를 따라 남쪽으로 내려가며 서울의 오래된 길을 지나갑니다." | - |
| S7 | 52~55초 | 차 안(뒷좌석에서). 딸이 아빠를 봄 | 딸(화면 밖): "할아버지도 여기 얘기해 줬어?" 아빠(화면 밖): "응. 그땐 아빠가 네 자리에 앉아 있었어." | - |
| S8 | 55~59초 | 세종대로를 따라 남쪽으로 멀어지는 차(위에서 넓게) | 잔잔한 음악 | 자막 2장(각 2초): "현대가 추억을 이어주다" → "Roadio — 차창 밖 이야기 · Pleos Connect 앱 · SOLOMON" |

**대사를 쓸 때 지킬 것**

- 할아버지 대사는 1995년에 사실이었던 것만 말합니다. 2010년 복원, 월대, 광장은 말하지 않습니다.
- "6·25 때 불탔다", "1968년 콘크리트로"는 국가기록원 자료와 맞습니다.
- 연도가 자료마다 다른 것은 말하지 않습니다: 광화문 이름을 붙인 해, 일제강점기에 옮긴 해(1926·1927).
- S7 대사는 도슨트가 끝난 뒤에 나옵니다. 도슨트 위에 대사를 겹치지 않습니다.
- 시간이 부족하면 S4b의 할아버지 대사를 줄이고, 도슨트(15초)는 줄이지 않습니다.

## 5. 영상 AI용 프롬프트(이 장의 영문 블록만 붙여 넣기)

**넣는 방법**

- 장면마다 따로 생성하고 편집에서 이어 붙입니다.
- 각 장면 블록 위에 5.1 공통 스타일을 붙이고, 5.4 제외어는 도구의 negative prompt 칸(없으면 맨 끝 "Avoid:" 줄)에 넣습니다.
- 먼저 5.2 기준 이미지를 만들고, 장면은 그 이미지를 시작 프레임으로 쓰는 image-to-video로 만들면 인물과 차가 덜 바뀝니다.
- 생성된 클립의 자체 소리는 모두 끄고, 6장의 한국어 음성을 편집에서 넣습니다.

### 5.1 공통 스타일

```text
Style: warm, cinematic 2D hand-painted animation, soft lighting, Korean family drama tone.
Present-day shots (2026): clean natural colours, late-afternoon golden light.
Flashback shots (1995): warm faded film look, slight grain, lower saturation.
Locked or very slow camera. No readable text, signage, logos, emblems or brand names anywhere in frame.
Licence plates unreadable. Everyone in a car wears a seat belt. While the car is moving, the driver keeps both
hands on the wheel and eyes on the road. Characters' mouths closed unless the shot is marked as on-screen speech.
```

### 5.2 기준 이미지(먼저 생성, 인물·차마다 한 장씩)

```text
DAD_2026: character sheet, 2D hand-painted style, front, three-quarter and left-profile views. Korean man, 43,
short black hair, gentle face, small mole just above his own left eyebrow, light-blue shirt, old silver wristwatch
on his left wrist.
```

```text
DAD_1995: character sheet, same style, front, three-quarter and left-profile views. Korean boy, 12, the same face
shape as DAD_2026 and the same small mole above his own left eyebrow, striped 1990s T-shirt.
```

```text
GRANDPA_1995: character sheet, same style, front, three-quarter and left-profile views. Korean man, 42, in 1995,
white dress shirt with a loosened tie, silver wristwatch on his left wrist, kind smile with crow's feet.
```

```text
DAUGHTER_2026: character sheet, same style, front, three-quarter and left-profile views. Korean girl, 16,
high-school student, shoulder-length black hair, grey hoodie.
```

```text
CAR_2026: reference sheet, same style. Modern dark-grey full-size luxury sedan, no badge, no logo, no lettering.
Interior: a wide 17-inch central display across the dashboard, left part showing driving information,
right part an app area.
```

```text
CAR_1995: reference sheet, same style. Boxy 1980s full-size sedan with straight lines, no badge, no logo,
no lettering. Interior: velour seats, a single-spoke steering wheel, an aircraft-throttle-style gear lever.
```

### 5.3 장면별

```text
S1 (6s) — Seoul, 2026, late afternoon. CAR_2026 waits at a red light on the road that curves in front of a broad
stone royal terrace. Behind it, the wooden traditional gate Gwanghwamun with a small dark signboard too distant
to read. Tree-lined square to the right (west) side of the boulevard. Static wide shot.
```

```text
S2 (7s) — Inside CAR_2026, 2026, car stopped at a red light, shot from the back seat. DAD_2026 in the driver's
seat, hands on the wheel. DAUGHTER_2026 in the front passenger seat, pushed far back, seat belt on, looks out of
the window at the old gate, then turns her head toward her father. Warm golden light. Mouths closed.
```

```text
S3a (2s) — Close-up, locked camera: DAD_2026's left hand with an old silver wristwatch resting on a modern
steering wheel. Clean 2026 colours.
```

```text
S3b (2s) — Close-up, the same framing: GRANDPA_1995's left hand with the same silver wristwatch resting on a
single-spoke 1980s steering wheel. Warm faded 1995 film look.
```

```text
S4a (5s) — Seoul, spring 1995, warm faded film look. Wide street view looking north along a very wide 16-lane
boulevard with 1990s Korean cars and buses. At the far end, a traditional Korean gate, and directly behind it a
huge domed grey granite neoclassical building that towers over the gate. No plaza, no trees in the middle.
```

```text
S4b (7s) — Spring 1995, inside CAR_1995, fully stopped at a red light, traffic stationary, shot from the back seat.
GRANDPA_1995 in the driver's seat, seat belt on, briefly lifts one hand from the wheel to point ahead.
DAD_1995 in the front passenger seat, seat belt on, leans forward to look. Through the windshield, softly out of
focus: a traditional gate close ahead and a huge domed granite building behind it. Mouths closed.
```

```text
S4c (6s) — Spring 1995, inside CAR_1995, stopped. Close-up of DAD_1995 looking up at something outside with
curiosity (on-screen speech), then a close-up of GRANDPA_1995 answering with a gentle smile (on-screen speech).
Warm faded film look.
```

```text
S5a (2s) — Spring 1995, close-up left profile of DAD_1995 in the passenger seat looking forward through the
windshield, warm faded film look, locked camera.
```

```text
S5b (2s) — 2026, close-up left profile of DAD_2026 in the driver's seat looking forward through the windshield,
the same framing as S5a, clean colours, locked camera.
```

```text
S6a (5s) — Inside CAR_2026, 2026, the car pulling away slowly. Close-up of the wide central display, static
locked-off camera, no camera movement. Left part shows simple driving information without readable text.
The right part is a uniform solid green rectangle with no UI, aspect ratio 1.39:1, for compositing.
```

```text
S6b (10s) — Inside CAR_2026, 2026, driving slowly on a wide boulevard, shot from the back seat.
DAUGHTER_2026, seat belt on, seat pushed back, looks out of her right-side window at a tree-lined square passing by.
DAD_2026 drives with both hands on the wheel, eyes on the road, smiling softly. Mouths closed.
```

```text
S7 (3s) — Inside CAR_2026, 2026, driving, shot from the back seat. DAUGHTER_2026 turns to look at DAD_2026,
who keeps his eyes on the road and smiles. Mouths small and not in focus.
```

```text
S8 (4s) — Wide aerial view, golden hour: CAR_2026 drives south along a tree-lined boulevard in Seoul, the old gate
and mountains small in the distance behind. Empty sky at the top for title text.
```

### 5.4 공통 제외어

```text
Avoid: readable text or signage, logos, emblems, brand names, readable licence plates, real politicians or
celebrities, unbelted passengers, children under 10 in the front seat, a driver looking at or touching the screen
while moving, a phone in the driver's hand, chat bubbles, microphone icons or maps on the app screen,
guardian lion statues in Chinese style.
1995 shots also avoid: a plaza, a seated golden statue, a stone terrace in front of the gate, glass skyscrapers.
2026 shots also avoid: a domed granite building behind the gate, rows of large stone monuments in the square.
```

## 6. 목소리, 소리, 자막

| 소리 | 정한 것 |
|---|---|
| 도슨트 | Roadio 앱의 실제 음성(Pleos 음성 서비스)을 씁니다(7장 녹화본). 못 쓰면 실제 Pleos 음성과 같은 성별·톤의 차분한 한국어 합성 음성으로 같은 문장만 읽게 합니다 |
| 할아버지 | 40대 남성, 따뜻하고 느긋한 1990년대 아버지 말투 |
| 아빠 | 40대 남성, 부드러운 목소리 |
| 딸 | 10대 후반 여성 |
| 어린 아빠 | 12세 남자아이 |
| 배경음 | 2026: 조용한 차 안, 방향지시등. 1995: 옛 버스 엔진, 경적, 라디오 |
| 음악 | 직접 만들었거나 사용 허락받은 잔잔한 피아노. S3·S5 전환에서도 끊기지 않게 |

**자막**

- 모든 대사에 한국어 자막을 넣습니다. 도슨트 자막은 앱 문장을 글자 그대로 씁니다.
- 자막 글꼴은 영상 사용이 허락된 것으로 합니다.
- S6a의 앱 화면과 S8의 제목 자막을 가리지 않게 배치합니다.
- 세로판은 자막을 영상에 입히고, 본편은 입히거나 SRT를 따로 둡니다.

## 7. 실제 앱 화면 넣기

**녹화본:** 이 PC의 Pleos 에뮬레이터에서 실제 release APK로 녹화했습니다(2026-11-03).

- 파일: `C:\tmp\roadio-demo\roadio-gwanghwamun.webm`. 25초, 2560×1440, VP9 영상과 Vorbis 오디오 트랙.
- 내용: "시뮬레이션 주행 시작" 뒤 "광화문, 조선의 얼굴"이 재생 중인 화면.

**쓰는 방법**

- 앱 영역(2560×1440 기준 가로 853~2535, 세로 90~1300, 약 1.39:1)만 잘라서 S6a의 초록 사각형에 키잉·코너핀으로 합성합니다.
- 이 PC 에뮬레이터는 왼쪽이 주행 정보, 오른쪽이 앱 영역입니다. 같은 배치로 보여 주면 실제와 맞습니다.
- 왼쪽 미디어 위젯은 잘라 냅니다. 재설치 직후라 "제목 정보 없음"이 떠 있습니다. 재설치 뒤 에뮬레이터를 다시 켜기 전까지 생기는 알려진 현상입니다(handoff.md 7장).

**아직 확인하지 못한 것:** 녹화본의 오디오 트랙에 도슨트 목소리가 실제로 담겼는지 들어 보지 못했습니다. 직접 들어 봐 주세요. 소리가 없으면 다음 방법이 있습니다.

- 에뮬레이터 소리를 PC에서 따로 녹음합니다.
- 6장의 대체 음성을 씁니다.

**다시 녹화하고 싶다면:** 말씀해 주시면 에뮬레이터를 다시 켜서 녹화하겠습니다. 다시 켜면 위젯에 이야기 제목이 나옵니다. "차량 위치" 모드로 녹화하면 화면에 "시뮬레이션" 대신 차량 위치가 표시됩니다.

## 8. 내보내기와 제출

| 판 | 형식 | 파일 이름(예) |
|---|---|---|
| 본편 | 16:9, 1920×1080, MP4(H.264), 약 58초 | `SOLOMON_Roadio_demo_16x9.mp4` |
| 숏폼(선택) | 9:16, 1080×1920, MP4, 30초~1분, 자막을 영상에 입힘 | `SOLOMON_Roadio_shorts_9x16.mp4` |

- 형식 기준은 공식에 없습니다. 위 형식은 호환성을 위한 팀 제안입니다.

**내보내기 전**

- 파일 정보(작성자·제목)를 지웁니다. 예: `ffmpeg -i in.mp4 -map_metadata -1 -c copy out.mp4`
- 워터마크가 없는지 확인합니다.
- 영상 AI의 공유 링크는 제출하지 않습니다.

**제출**

- 접수양식 7번 "참고자료"에 파일이나 링크로 냅니다. 마감은 2026-11-15 23:59(KST)입니다.
- 링크는 로그인 없이 열려야 하고, 소유자 이름에 실명이나 개인 계정이 보이지 않아야 합니다.

**숏폼 챌린지 규정**

- 세로, 30초~1분
- Instagram 릴스나 YouTube Shorts에 게시
- 해시태그 `#HMG차량용앱개발공모전` 포함 2~3개
- 링크는 접수양식 8번
- 개인 계정 대신 팀 계정을 권합니다.

**세로판 장면 구성(가로 영상을 가운데만 자르면 인물이 잘리므로 따로 만듦)**

| 장면 | 세로판에서 |
|---|---|
| S2, S7 | 딸과 아빠를 번갈아 한 사람씩 클로즈업 |
| S4 | 앞 유리 너머 세로 구도 |
| S6 | 실제 Roadio 화면을 3~4초 꽉 차게 |
| S8 | 세로 항공 화면과 제목 |

## 9. 확인이 필요한 것

1. **생성형 AI 사용 규정**
   - [공식 설명회 다시보기](https://youtu.be/na6tSJ44pWc)에 "공모전 Q&A: 생성형 AI 활용"(20:28~21:05) 구간이 있습니다. 내용은 확인하지 못했습니다. 이 구간과 "필수·선택 제출물"(09:37~), "심사 기준과 가산점"(12:27~)을 직접 봐 주세요.
   - 써 있는 규정에는 AI 영상에 대한 언급이 없습니다.
   - 참고로 숏폼 안내의 예시 이미지는 "AI 생성"으로 표시돼 있습니다. 규정은 아닙니다.
2. **애니메이션도 가산점이 되는지**
   - 가산점 항목은 "[가산점] 개발 산출물"이고, 그 아래 선택 제출물로 "프로토타입 시연 영상 (화면 녹화 포함)"이 있습니다. 제출 안내의 인정 범위는 "목업 · 프로토타입 · 화면 녹화"입니다.
   - 애니메이션은 언급이 없습니다. 실제 앱 녹화본(7장)을 넣으면 이 항목에 맞습니다.
3. **현대 이름·로고·차량 디자인·제목:** 운영사무국에 묻습니다(2.4). 답이 늦으면 대체 제목과 일반적인 차로 갑니다.
4. **1968년 한글 현판 색과 1세대 그랜저 시트 무늬:** 공식 자료가 없습니다. 당시 사진을 참고합니다. 지금 프롬프트는 현판을 읽히지 않게 둡니다.
5. **비슷한 이전 수상작:** 보도 기준으로 둘이 있습니다.
   - 이전 Pleos Connect 앱 아이디어 공모전 대상 "창밖동화": 지나가는 지역 이야기를 아이에게 생성(인사이트 2026-10-01).
   - 현대차그룹 해커톤 수상작 TaleSync: 차량용 대화형 오디오 이야기(현대차그룹 뉴스룸 2026-08-24).
   - Roadio는 경로 위치에 맞춰 사실 기반 장소 해설을 들려준다는 점이 영상에서 분명히 보여야 합니다. 이 영상에서는 실제 앱 화면과 앱 문장 그대로의 도슨트가 그 역할을 합니다.

## 10. 출처(주요)

- 공모전 공식 사이트(2026-11-04 확인)
  - [지원·제출 방법](https://appdevchallenge.auto-hmg.io/guide/submission)
  - [심사·시상](https://appdevchallenge.auto-hmg.io/guide/judging-awards)
  - [숏폼 챌린지](https://appdevchallenge.auto-hmg.io/guide/shortform)
  - [FAQ](https://appdevchallenge.auto-hmg.io/faq)
  - [출품 유의사항](https://appdevchallenge.auto-hmg.io/notice/c0a1a000-2026-4923-8000-000000000006)
  - [접수양식](https://appdevchallenge.auto-hmg.io/downloads/contest-application-consent.docx)
  - [설명회 다시보기](https://youtu.be/na6tSJ44pWc)
- 광화문
  - 국가기록원 [광화문](https://theme.archives.go.kr/next/koreaOfRecord/Gwanghwamun.do), [조선총독부 청사](https://theme.archives.go.kr/next/place/governmentOffice.do?flag=1)
  - [서울신문 2010-08-15](https://m.seoul.co.kr/news/2010/08/15/20100815800024)
  - [주간경향 2023-10-27(해태상)](https://weekly.khan.co.kr/article/202310271120451)
  - [서울시 2023-10-25(월대·현판)](https://mediahub.seoul.go.kr/archives/2009329)
  - [서울시 2007-03-20(16차로)](https://mediahub.seoul.go.kr/archives/165974)
  - [서울신문 2024-01-17(이순신 동상)](https://www.seoul.co.kr/news/culture/20240117500105)
- 그랜저와 Pleos Connect
  - [현대차 그랜저 역사](https://www.hyundai.com/kr/ko/brand/brandstory/model/grandeur-history/1986-grandeur-l1)
  - [HMG Journal 2026-06-02](https://www.hyundaimotorgroup.com/ko/story/hyundai-grandeur-history)
  - [현대차 뉴스룸 2026-04-30(Pleos Connect)](https://www.hyundai.com/worldwide/en/newsroom/detail/hyundai-motor-group-redefines-in-vehicle-experience-with-pleos-connect-next-generation-infotainment-system-0000001168)
- 안전
  - [도로교통법](https://www.law.go.kr/법령/도로교통법) 제50조
  - [1990년 도로교통법](https://www.law.go.kr/LSW/lsInfoP.do?lsiSeq=5608&efYd=19901102) 제48조의2
  - [2026 그랜저 취급설명서(에어백)](https://ownersmanual.hyundai.com/full_webhelp/GN7/2026/ko_KR/id0830046650f.html)
- 비슷한 이전 수상작: [인사이트 2026-10-01](https://www.insight.co.kr/amp/news/576146)
- 앱 문장: 저장소의 `DemoRoute.kt`(데모 경로 이야기)
