package com.whohoo.roadio.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whohoo.roadio.DocentUiState
import com.whohoo.roadio.DriveMode
import com.whohoo.roadio.LocationStatus
import com.whohoo.roadio.VoiceState
import com.whohoo.roadio.core.TripSummary
import com.whohoo.roadio.navigation.NavigationLink
import java.util.Locale

@Composable
fun RoadioScreen(
    state: DocentUiState,
    onToggleDriving: () -> Unit,
    onToggleStory: () -> Unit,
    onRestart: () -> Unit,
    onToggleMode: () -> Unit,
    onLocationConsent: (agreed: Boolean) -> Unit,
    onGuide: () -> Unit,
    onVoice: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        // The car's status bar and climate bar take the top and bottom of the screen: stay clear of them.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
        ) {
            Header(state)
            Spacer(Modifier.height(18.dp))
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Amber,
                trackColor = PanelRaised,
            )
            Spacer(Modifier.height(22.dp))
            if (state.mode == DriveMode.VEHICLE && state.locationStatus == LocationStatus.NEEDS_CONSENT) {
                LocationConsentPanel(
                    setupLimited = state.setupLimited,
                    onAnswer = onLocationConsent,
                    modifier = Modifier.weight(1f),
                )
            } else {
                StoryPanel(
                    state = state,
                    onToggleStory = onToggleStory,
                    onGuide = onGuide,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(18.dp))
            Controls(state, onToggleDriving, onVoice, onToggleMode, onRestart)
        }
    }
}

@Composable
private fun Header(state: DocentUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ROADIO",
                color = Amber,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "차창 밖 이야기",
                color = Paper,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${state.speedKmh}",
                color = Paper,
                fontSize = 40.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "km/h · ${modeName(state.mode)} · ${kilometers(state.distanceAlongM)} / ${kilometers(state.totalDistanceM)} km",
                color = Mist,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun StoryPanel(
    state: DocentUiState,
    onToggleStory: () -> Unit,
    onGuide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val story = state.currentStory
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 26.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    text = when {
                        state.listening -> "듣는 중"
                        state.isSpeaking -> "재생 중"
                        state.isDriving && state.mode == DriveMode.VEHICLE -> "도슨트 켜짐"
                        state.isDriving -> "주행 중"
                        state.finished -> "도착"
                        else -> "대기 중"
                    },
                    active = state.isSpeaking || state.listening,
                )
                Spacer(Modifier.weight(1f))
                // Car touch targets are at least 76 dp.
                if (state.navigationAvailable) {
                    // Setting the navigation's route is setup, which the car forbids while driving.
                    TextButton(
                        onClick = onGuide,
                        enabled = !state.setupLimited,
                        modifier = Modifier.heightIn(min = 76.dp),
                    ) {
                        Text(
                            text = when {
                                state.setupLimited -> "정차 후 내비 안내"
                                state.guidance == NavigationLink.Guidance.STARTED -> "내비 안내 중"
                                state.guidance == NavigationLink.Guidance.FAILED -> "내비 안내 다시"
                                else -> "내비로 안내"
                            },
                            color = if (state.setupLimited) Mist else Amber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                TextButton(
                    onClick = onToggleStory,
                    enabled = story != null,
                    modifier = Modifier.heightIn(min = 76.dp),
                ) {
                    Text(
                        text = if (state.isSpeaking) "이야기 멈춤" else "다시 듣기",
                        color = if (story != null) Amber else Mist,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = story?.title ?: "아직 들려준 이야기가 없어요",
                color = Paper,
                fontSize = 42.sp,
                lineHeight = 50.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(14.dp))
            // While the car limits text on screen (driving restrictions), the story is heard and not read.
            Text(
                text = when {
                    story == null -> "주행을 시작하면 지나가는 곳의 이야기가 자동으로 흘러나옵니다. 화면을 보지 않아도 되는 오디오 도슨트입니다."
                    state.textLimited -> "주행 중에는 차량 규정에 따라 이야기를 글 대신 소리로만 들려 드려요."
                    else -> story.body
                },
                color = Paper.copy(alpha = 0.84f),
                fontSize = 21.sp,
                lineHeight = 31.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = PanelRaised)
            Spacer(Modifier.height(14.dp))
            val notice = voiceNotice(state) ?: locationNotice(state)
            if (notice != null) Notice(notice) else UpcomingLine(state)
        }
    }
}

// While Roadio listens, which words it understands and that it keeps none of them; afterwards, if it missed them.
private fun voiceNotice(state: DocentUiState): String? = when (state.voice) {
    VoiceState.OPENING -> "마이크를 켜고 있어요 · \"듣고 있어요\"가 나오면 말씀해 주세요"
    VoiceState.LISTENING -> "듣고 있어요 · \"다음\", \"다시\", \"그만\", \"켜 줘\" 중에 말씀해 주세요 · Roadio는 들은 말을 명령에만 쓰고 저장하지 않아요"
    VoiceState.MISSED -> "잘 듣지 못했어요 · 말로 하기를 누르고 \"다음\", \"다시\", \"그만\", \"켜 줘\" 중에 말씀해 주세요"
    VoiceState.FAILED -> "지금은 차량 마이크로 들을 수 없어요 · 화면의 버튼을 써 주세요"
    VoiceState.IDLE -> null
}

// Says why stories are not coming: the car is reversing, a turn is close, or the car's own position is the source and
// it is not on the demo route.
private fun locationNotice(state: DocentUiState): String? {
    if (state.isDriving && state.holdingForGear) return "후진 중에는 이야기를 쉬어요 · 기어를 바꾸면 다음 이야기를 들려 드려요"
    if (state.isDriving && state.holdingForTurn && !state.isSpeaking) return "곧 회전 구간이에요 · 다음 이야기는 회전한 뒤에 들려 드려요"
    if (state.mode != DriveMode.VEHICLE) return null
    // After the drive ended, where the car is now matters less than its summary.
    val ended = state.finished && state.summary != null
    return when (state.locationStatus) {
        LocationStatus.UNSUPPORTED -> "이 기기에서는 차량 위치를 쓸 수 없어요 · 시뮬레이션으로 바꿔 주세요"
        LocationStatus.NEEDS_PERMISSION -> "차량 위치 권한을 요청하고 있어요"
        LocationStatus.DENIED -> "차량 위치 권한이 없어요 · 설정에서 허용하거나 시뮬레이션으로 바꿔 주세요"
        LocationStatus.DISABLED -> "차량 설정에서 위치가 꺼져 있어요"
        LocationStatus.FAILED -> "차량 위치 서비스에 연결하지 못했어요 · 시뮬레이션으로 바꿔 주세요"
        LocationStatus.WAITING -> if (ended) null else "차량 위치를 기다리고 있어요"
        LocationStatus.OFF_ROUTE -> if (ended) null else "데모 경로(광화문 → N서울타워) 밖이에요 · 경로에 들어서면 이야기가 시작돼요"
        LocationStatus.NONE, LocationStatus.NEEDS_CONSENT, LocationStatus.ON_ROUTE -> null
    }
}

@Composable
private fun Notice(text: String) {
    Text(
        text = text,
        color = Amber,
        fontSize = 17.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

// The end of the route: how far (and whether the car's own meters measured it), how long, how many stories.
private fun summaryLine(summary: TripSummary): String {
    val km = String.format(Locale.ROOT, "%.1f", summary.distanceM / 1000.0)
    val source = if (summary.byCar) "차량 주행 거리" else "경로 기준"
    return "오늘 ${km}km($source) · ${summary.minutes}분 · 이야기 ${summary.played}/${summary.total}"
}

@Composable
private fun UpcomingLine(state: DocentUiState) {
    // Once the drive ended no story comes any more, even one whose place was not reached in time: its summary shows.
    val summary = state.summary?.takeIf { state.finished }
    val title = state.upcomingStory?.title
    if (title == null || summary != null) {
        Text(
            text = if (summary != null) summaryLine(summary) else "이 경로에 남은 이야기가 없습니다 · ${state.playedCount}/${state.storyCount}",
            color = if (summary != null) Paper else Mist,
            fontSize = 17.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "다음 이야기",
            color = Mist,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            color = Paper,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = (state.upcomingDistanceM ?: 0.0).toInt().let { if (it > 0) "${it}m 앞" else "곧" },
            color = Amber,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// The legal notice for the car's position: what is used, why, how long it is kept, and that it can be refused. Only a
// tap on agree counts as consent.
@Composable
private fun LocationConsentPanel(
    setupLimited: Boolean,
    onAnswer: (agreed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 26.dp),
        ) {
            Text(
                text = "차량 위치 사용 안내",
                color = Paper,
                fontSize = 34.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            listOf(
                "지나가는 곳의 이야기를 알맞은 때 들려주려고 차량 위치를 씁니다.",
                "위치는 이 차 안에서 이야기를 고르는 데만 쓰고, 저장하거나 밖으로 보내지 않습니다.",
                "동의하지 않아도 시뮬레이션 주행으로 쓸 수 있고, 언제든 시뮬레이션으로 돌아갈 수 있습니다.",
            ).forEach { line ->
                Text(
                    text = "· $line",
                    color = Paper.copy(alpha = 0.84f),
                    fontSize = 19.sp,
                    lineHeight = 28.sp,
                )
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.weight(1f))
            if (setupLimited) {
                Notice("주행 중에는 설정을 바꿀 수 없어요 · 정차한 뒤에 동의할 수 있어요")
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { onAnswer(true) },
                        modifier = Modifier.height(76.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Ink),
                    ) {
                        Text(text = "동의하고 차량 위치 사용", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onAnswer(false) },
                        modifier = Modifier.height(76.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Mist.copy(alpha = 0.45f)),
                    ) {
                        Text(text = "동의하지 않음", color = Paper, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Controls(
    state: DocentUiState,
    onToggleDriving: () -> Unit,
    onVoice: () -> Unit,
    onToggleMode: () -> Unit,
    onRestart: () -> Unit,
) {
    val vehicle = state.mode == DriveMode.VEHICLE
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onToggleDriving,
            enabled = state.isDriving || state.canStart,
            modifier = Modifier
                .weight(1f)
                .height(76.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isDriving) PanelRaised else Amber,
                contentColor = if (state.isDriving) Paper else Ink,
            ),
        ) {
            Text(
                text = when {
                    vehicle && state.isDriving -> "도슨트 끄기"
                    vehicle && state.finished -> "도슨트 다시 켜기"
                    vehicle -> "도슨트 켜기"
                    state.isDriving -> "주행 정지"
                    state.finished -> "처음부터 다시 주행"
                    state.distanceAlongM == 0.0 -> "시뮬레이션 주행 시작"
                    else -> "계속 주행"
                },
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        // A spoken command instead of a button: for while driving, so the car's restrictions do not turn it off. Only
        // where the car's speech engine can listen, and not over the location notice, which has no room to say what
        // was heard.
        if (state.voiceAvailable && !(vehicle && state.locationStatus == LocationStatus.NEEDS_CONSENT)) {
            OutlinedButton(
                onClick = onVoice,
                modifier = Modifier.height(76.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(if (state.listening) 2.dp else 1.dp, if (state.listening) Amber else Mist.copy(alpha = 0.45f)),
            ) {
                Text(
                    text = if (state.listening) "그만 듣기" else "말로 하기",
                    color = if (state.listening) Amber else Paper,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        // Choosing the position source is setup, which the car forbids while driving.
        OutlinedButton(
            onClick = onToggleMode,
            enabled = !state.setupLimited,
            modifier = Modifier.height(76.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Mist.copy(alpha = 0.45f)),
        ) {
            Text(
                text = when {
                    state.setupLimited -> "정차 후 변경"
                    vehicle -> "시뮬레이션으로"
                    else -> "차량 위치로"
                },
                color = if (state.setupLimited) Mist else Paper,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier.height(76.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Mist.copy(alpha = 0.45f)),
        ) {
            Text(
                text = "처음부터",
                color = Paper,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean) {
    val background = if (active) Amber.copy(alpha = 0.18f) else PanelRaised
    val foreground = if (active) Amber else Mist
    Text(
        text = text,
        color = foreground,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

private fun modeName(mode: DriveMode): String = when (mode) {
    DriveMode.SIMULATION -> "시뮬레이션"
    DriveMode.VEHICLE -> "차량 위치"
}

private fun kilometers(meters: Double): String =
    String.format(Locale.KOREA, "%.1f", meters / 1000.0)
