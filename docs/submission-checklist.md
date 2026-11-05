# 제출 체크리스트

공모전 안내(2026.10.21–11.15) 기준입니다. 마감 전 마지막 제출본이 최종 심사 대상입니다.

## 1. 접수 정보

| 항목 | 내용 |
|---|---|
| 접수 기간 | 2026.10.21 – 2026.11.15 |
| 접수 이메일 | appdevchallenge@hyundai-partners.com |
| 메일 제목 | `[차량용 앱 개발 공모전] 팀명(개인은 성명)_대표자명` |
| 수정 제출 제목 | `[수정제출] [차량용 앱 개발 공모전] 팀명(개인은 성명)_대표자명` |
| 참가 인원 | 대표자 포함 최대 5인 |
| 제출 방식 | 대표자가 팀원 전원을 CC로 참조해 이메일 제출 |

## 2. 필수 서류

- [ ] 공모전 접수양식(기획서) 작성 — `docs/plan.md` 초안을 양식에 맞게 이관
- [ ] 개인정보 수집·이용 동의서: 참가자 **전원**이 각각 동의 여부, 서명, 날짜 작성
- [ ] 대표자가 팀원 전원 CC
- [ ] 수정 제출 시에도 전체 자료 + 서명 동의서 + 전원 CC

## 3. 제출물

| 제출물 | 필수 | 상태 | 비고 |
|---|---|---|---|
| 접수양식(기획서) | 필수 | 초안 완료 | DOCX 양식으로 변환 필요 |
| 시연 영상 | 선택 | 예정 | 목업/프로토타입/화면 녹화 인정, 가산점 |
| GitHub 저장소 | 선택 | 소스 + release APK | 가산점 |
| 숏폼 링크 | 선택 | 미정 | 기획서에 기재, 가산점 없음(별도 특별상) |

> 1차 접수 시 개발 산출물을 제출하면 단계별 가산점. 2차 PT 대상팀은 PT 전까지 실제 동작하는 앱을 완성해야 하며, 1차 제출물은 그때까지 자유롭게 고도화할 수 있습니다.

## 4. GitHub 제출 요건

- [x] Public 저장소: https://github.com/WhoisHOO/pleos-app-challenge
- [x] 소스 코드 업로드
- [x] `apk/` 폴더에 release APK 업로드
- [x] APK 파일명: `SOLOMON_Roadio_release.apk`
- [ ] 100MB 초과 시 GitHub Releases에 등록하고 Release URL 제출
- [ ] README에 아래 4개 항목 포함
  - [x] 앱 이름 및 한 줄 소개
  - [x] 실행 방법
  - [x] 테스트 환경 및 필요한 설정
  - [x] APK 파일 위치: `apk/SOLOMON_Roadio_release.apk`
- [ ] Private 저장소로 전환할 경우 심사용 GitHub 계정을 Collaborator로 초대하고 마감 전 접근 확인 (개인 계정 저장소의 Collaborator는 항상 쓰기 권한을 받으므로, 초대 전 권한을 확인하거나 Public으로 유지)

## 5. release APK 준비

release 서명은 `app/build.gradle.kts`에 연결되어 있습니다. 저장소 루트에 `keystore.properties`(커밋 금지, `.gitignore`에 포함)와 그 파일이 가리키는 keystore(`.jks`)만 있으면 됩니다. `storeFile`은 절대 경로이거나 저장소 루트 기준 경로입니다.

```properties
storeFile=C:/path/to/roadio-release.jks
storePassword=...
keyAlias=roadio
keyPassword=...
```

```powershell
.\gradlew.bat :app:assembleRelease
copy app\build\outputs\apk\release\app-release.apk apk\SOLOMON_Roadio_release.apk
```

> keystore와 비밀번호를 잃으면 같은 서명으로 업데이트할 수 없습니다. 저장소 밖 두 곳 이상에 백업하세요.

- [x] signingConfig 연결
- [x] `apk/SOLOMON_Roadio_release.apk` 생성
- [x] Pleos 에뮬레이터에서 설치/실행 확인(정차·주행 상태, 음성 재생)
- [x] README의 APK 경로 갱신

## 6. 시연 영상 (선택, 길이 규정 없음)

공모전은 길이와 형식을 정하지 않았습니다(목업·프로토타입·화면 녹화 인정). 제출 영상은 다른 도구로 따로 만들고, 그때 쓴 프롬프트는 [demo-video-prompt.md](demo-video-prompt.md)에 있습니다. 아래는 에뮬레이터 화면을 녹화할 때 쓸 수 있는 구성입니다.

| 구간 | 화면 | 내레이션/자막 |
|---|---|---|
| 0–5s | 타이틀 | "지나가는 길이 콘텐츠가 됩니다. Roadio." |
| 5–15s | 문제 | "내비는 목적지만 알려줍니다. 지나가는 동네는요?" |
| 15–35s | 에뮬레이터 주행 | 광화문 → 시청, 이야기 자동 재생 |
| 35–50s | 기술 | "단순 반경이 아니라 경로 진행률로, 장소마다 한 번씩 재생합니다." |
| 50–65s | 안전/확장 | "화면을 보지 않아도 됩니다. 다국어, 아이 모드로 확장." |
| 65–75s | 마무리 | "Roadio — 차창 밖 이야기." |

촬영 팁:

- 16:9 또는 차량 와이드 비율, 자막 크게.
- Pleos 에뮬레이터는 Pleos TTS(온디바이스)로 음성이 나옵니다. 화면 녹화에 소리가 함께 담기는지 미리 확인.
- 진행률 바와 "다음 이야기 320m 앞"이 함께 보이게.

## 7. 개인정보 보관

- 수집 항목: 성명, 소속(선택), 휴대폰번호, 이메일, 팀원정보(팀 참가 시), 서명
- 보관: 공모전 및 현대자동차그룹 연계 행사 종료 및 후속 일정(2027.02.28)까지 보관 후 지체 없이 파기
- 수상자: 결과 발표일로부터 2년까지 보유 후 파기

## 8. 마감 전 최종 확인

- [ ] 접수양식/동의서 서명·날짜 누락 없음
- [ ] 팀원 전원 CC
- [ ] APK 실행 가능(release)
- [ ] GitHub 링크 접근 가능(비공개 시 Collaborator 확인)
- [ ] README 4개 항목 최신
- [ ] 시연 영상 링크/파일 첨부
- [ ] 메일 제목 형식 준수
