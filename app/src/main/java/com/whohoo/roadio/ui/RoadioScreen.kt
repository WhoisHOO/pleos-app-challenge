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
import androidx.compose.foundation.layout.padding
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
import java.util.Locale

@Composable
fun RoadioScreen(
    state: DocentUiState,
    onToggleDriving: () -> Unit,
    onReplay: () -> Unit,
    onRestart: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            StoryPanel(
                state = state,
                onReplay = onReplay,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(18.dp))
            Controls(state, onToggleDriving, onRestart)
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
                text = "km/h · ${kilometers(state.distanceAlongM)} / ${kilometers(state.totalDistanceM)} km",
                color = Mist,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun StoryPanel(
    state: DocentUiState,
    onReplay: () -> Unit,
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
                        state.isSpeaking -> "재생 중"
                        state.isDriving -> "주행 중"
                        else -> "대기 중"
                    },
                    active = state.isSpeaking,
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onReplay, enabled = story != null) {
                    Text(
                        text = "다시 듣기",
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
            Text(
                text = story?.body
                    ?: "주행을 시작하면 지나가는 곳의 이야기가 자동으로 흘러나옵니다. 화면을 보지 않아도 되는 오디오 도슨트입니다.",
                color = Paper.copy(alpha = 0.84f),
                fontSize = 21.sp,
                lineHeight = 31.sp,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = PanelRaised)
            Spacer(Modifier.height(14.dp))
            UpcomingLine(state)
        }
    }
}

@Composable
private fun UpcomingLine(state: DocentUiState) {
    val title = state.upcomingTitle
    if (title == null) {
        Text(
            text = "이 경로의 모든 이야기를 들었습니다.",
            color = Mist,
            fontSize = 17.sp,
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
            text = "${(state.upcomingDistanceM ?: 0.0).toInt().coerceAtLeast(0)}m 앞",
            color = Amber,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Controls(
    state: DocentUiState,
    onToggleDriving: () -> Unit,
    onRestart: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onToggleDriving,
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
                    state.isDriving -> "주행 정지"
                    state.playedCount == 0 -> "시뮬레이션 주행 시작"
                    else -> "계속 주행"
                },
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
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

private fun kilometers(meters: Double): String =
    String.format(Locale.KOREA, "%.1f", meters / 1000.0)
