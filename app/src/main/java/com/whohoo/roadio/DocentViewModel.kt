package com.whohoo.roadio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whohoo.roadio.audio.AndroidDocentSpeaker
import com.whohoo.roadio.audio.DocentSpeaker
import com.whohoo.roadio.core.DocentPlanner
import com.whohoo.roadio.core.DocentStory
import com.whohoo.roadio.core.GeoPoint
import com.whohoo.roadio.data.DemoRoute
import com.whohoo.roadio.drive.SimulatedRouteDriver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DocentUiState(
    val isDriving: Boolean = false,
    val isSpeaking: Boolean = false,
    val speedKmh: Int = 50,
    val progress: Float = 0f,
    val distanceAlongM: Double = 0.0,
    val totalDistanceM: Double = 0.0,
    val currentStory: DocentStory? = null,
    val upcomingTitle: String? = null,
    val upcomingDistanceM: Double? = null,
    val playedCount: Int = 0,
    val position: GeoPoint = DemoRoute.points.first(),
)

class DocentViewModel(application: Application) : AndroidViewModel(application) {

    private val path = DemoRoute.path
    private val planner = DocentPlanner(path, DemoRoute.stories)
    private val driver = SimulatedRouteDriver(path)
    private val speaker: DocentSpeaker = AndroidDocentSpeaker(application)

    private var driveJob: Job? = null
    private var playedCount = 0

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<DocentUiState> = _uiState.asStateFlow()

    private fun initialState(): DocentUiState {
        val projection = path.project(path.pointAt(0.0))
        val upcoming = planner.upcoming(projection)
        return DocentUiState(
            progress = projection.progress.toFloat(),
            totalDistanceM = path.totalDistanceM,
            position = path.pointAt(0.0),
            upcomingTitle = upcoming?.story?.title,
            upcomingDistanceM = upcoming?.distanceAheadM,
        )
    }

    fun toggleDriving() {
        if (_uiState.value.isDriving) stopDriving() else startDriving()
    }

    private fun startDriving() {
        if (driver.finished) restart()
        _uiState.update { it.copy(isDriving = true) }
        driveJob?.cancel()
        driveJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS)
                val position = driver.advance(TICK_MS / 1000.0)
                val projection = path.project(position)
                planner.next(projection)?.let(::play)
                val upcoming = planner.upcoming(projection)
                _uiState.update {
                    it.copy(
                        position = position,
                        progress = projection.progress.toFloat(),
                        distanceAlongM = projection.distanceAlongM,
                        upcomingTitle = upcoming?.story?.title,
                        upcomingDistanceM = upcoming?.distanceAheadM,
                    )
                }
                if (driver.finished) {
                    stopDriving()
                    break
                }
            }
        }
    }

    private fun stopDriving() {
        driveJob?.cancel()
        driveJob = null
        _uiState.update { it.copy(isDriving = false) }
    }

    private fun play(story: DocentStory) {
        playedCount += 1
        _uiState.update {
            it.copy(
                currentStory = story,
                isSpeaking = true,
                playedCount = playedCount,
            )
        }
        speaker.speak(story.body) {
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    fun replayCurrentStory() {
        val story = _uiState.value.currentStory ?: return
        _uiState.update { it.copy(isSpeaking = true) }
        speaker.speak(story.body) {
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    fun restart() {
        stopDriving()
        speaker.stop()
        driver.reset()
        planner.reset()
        playedCount = 0
        _uiState.value = initialState()
    }

    override fun onCleared() {
        speaker.release()
        super.onCleared()
    }

    private companion object {
        const val TICK_MS = 250L
    }
}
