package com.whohoo.roadio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whohoo.roadio.audio.AndroidDocentSpeaker
import com.whohoo.roadio.audio.DocentSpeaker
import com.whohoo.roadio.audio.FocusedDocentSpeaker
import com.whohoo.roadio.audio.PleosDocentSpeaker
import com.whohoo.roadio.car.DrivingRestrictions
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
import kotlin.math.roundToInt

data class DocentUiState(
    val isDriving: Boolean = false,
    val isSpeaking: Boolean = false,
    val finished: Boolean = false,
    val textLimited: Boolean = false,
    val speedKmh: Int = 0,
    val progress: Float = 0f,
    val distanceAlongM: Double = 0.0,
    val totalDistanceM: Double = 0.0,
    val currentStory: DocentStory? = null,
    val upcomingTitle: String? = null,
    val upcomingDistanceM: Double? = null,
    val playedCount: Int = 0,
    val storyCount: Int = 0,
    val position: GeoPoint = DemoRoute.points.first(),
)

class DocentViewModel(application: Application) : AndroidViewModel(application) {

    private val path = DemoRoute.path
    private val planner = DocentPlanner(path, DemoRoute.stories)
    private val driver = SimulatedRouteDriver(path)
    // Pleos Connect's own speech first; the device's TTS where there is no Pleos Speech Service.
    private val speaker: DocentSpeaker = FocusedDocentSpeaker(
        application,
        PleosDocentSpeaker(application) { AndroidDocentSpeaker(application) },
        retellWanted = ::storyStillWanted,
    )

    private var driveJob: Job? = null
    private var playedCount = 0
    private var replaying = false
    private var projection = path.project(path.pointAt(0.0))

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<DocentUiState> = _uiState.asStateFlow()

    private val restrictions = DrivingRestrictions(application) { limited ->
        _uiState.update { it.copy(textLimited = limited) }
    }

    private fun initialState(): DocentUiState {
        val upcoming = planner.upcoming(projection)
        return DocentUiState(
            progress = projection.progress.toFloat(),
            totalDistanceM = path.totalDistanceM,
            position = path.pointAt(0.0),
            upcomingTitle = upcoming?.story?.title,
            upcomingDistanceM = upcoming?.distanceAheadM,
            storyCount = planner.storyCount,
        )
    }

    fun toggleDriving() {
        if (_uiState.value.isDriving) stopDriving() else startDriving()
    }

    private fun startDriving() {
        if (driver.finished) restart()
        _uiState.update { it.copy(isDriving = true, speedKmh = (driver.speedMps * 3.6).roundToInt()) }
        driveJob?.cancel()
        driveJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS)
                val position = driver.advance(TICK_MS / 1000.0)
                projection = path.project(position)
                planner.next(projection)?.let(::tell)
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
                    _uiState.update { it.copy(finished = true) }
                    break
                }
            }
        }
    }

    // The car stops; a story being told is told to its end.
    private fun stopDriving() {
        driveJob?.cancel()
        driveJob = null
        _uiState.update { it.copy(isDriving = false, speedKmh = 0) }
    }

    private fun tell(story: DocentStory) {
        playedCount += 1
        replaying = false
        _uiState.update {
            it.copy(
                currentStory = story,
                isSpeaking = true,
                playedCount = playedCount,
            )
        }
        speaker.speak(story.body, ::storyEnded)
    }

    private fun storyEnded() {
        planner.storyEnded(projection)
        _uiState.update { it.copy(isSpeaking = false) }
    }

    // After a call or a voice command, a story is told again only while its place is still ahead; a replay always is.
    private fun storyStillWanted(): Boolean {
        val story = _uiState.value.currentStory ?: return false
        return replaying || !planner.hasPassed(story, projection)
    }

    /** Stops the story being told, or tells the last one again. */
    fun toggleStory() {
        if (_uiState.value.isSpeaking) {
            speaker.stop()
            storyEnded()
            return
        }
        val story = _uiState.value.currentStory ?: return
        planner.storyStarted()
        replaying = true
        _uiState.update { it.copy(isSpeaking = true) }
        speaker.speak(story.body, ::storyEnded)
    }

    fun restart() {
        stopDriving()
        speaker.stop()
        driver.reset()
        planner.reset()
        playedCount = 0
        replaying = false
        projection = path.project(path.pointAt(0.0))
        _uiState.value = initialState().copy(textLimited = _uiState.value.textLimited)
    }

    override fun onCleared() {
        restrictions.release()
        speaker.release()
        super.onCleared()
    }

    private companion object {
        const val TICK_MS = 250L
    }
}
