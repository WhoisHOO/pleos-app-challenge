package com.whohoo.roadio

import android.app.Application
import android.content.Context
import android.os.SystemClock
import com.whohoo.roadio.audio.AndroidDocentSpeaker
import com.whohoo.roadio.audio.DocentSpeaker
import com.whohoo.roadio.audio.FocusedDocentSpeaker
import com.whohoo.roadio.audio.PleosDocentSpeaker
import com.whohoo.roadio.car.DrivingRestrictions
import com.whohoo.roadio.core.DocentPlanner
import com.whohoo.roadio.core.DocentStory
import com.whohoo.roadio.core.FixGate
import com.whohoo.roadio.core.GeoPoint
import com.whohoo.roadio.core.PlannerSnapshot
import com.whohoo.roadio.core.RouteProjection
import com.whohoo.roadio.core.SpeedEstimator
import com.whohoo.roadio.core.TurnGuard
import com.whohoo.roadio.data.DemoRoute
import com.whohoo.roadio.drive.SimulatedRouteDriver
import com.whohoo.roadio.location.VehicleLocation
import com.whohoo.roadio.navigation.NavigationLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** Where the car's position comes from. */
enum class DriveMode {
    /** A simulated car drives the demo route at 50 km/h. */
    SIMULATION,

    /** The car's own position (Pleos FusedLocation). */
    VEHICLE,
}

/** How the car's own position is doing, in [DriveMode.VEHICLE]. */
enum class LocationStatus {
    NONE,
    UNSUPPORTED,
    NEEDS_CONSENT,
    NEEDS_PERMISSION,
    DENIED,
    DISABLED,
    FAILED,
    WAITING,
    OFF_ROUTE,
    ON_ROUTE,
}

data class DocentUiState(
    val mode: DriveMode = DriveMode.SIMULATION,
    val locationStatus: LocationStatus = LocationStatus.NONE,
    // The drive is on: in a simulation the car moves, with the car's own position stories are told as it goes.
    val isDriving: Boolean = false,
    val isSpeaking: Boolean = false,
    val finished: Boolean = false,
    val textLimited: Boolean = false,
    val setupLimited: Boolean = false,
    val speedKmh: Int = 0,
    val progress: Float = 0f,
    val distanceAlongM: Double = 0.0,
    val totalDistanceM: Double = 0.0,
    val currentStory: DocentStory? = null,
    val upcomingStory: DocentStory? = null,
    val upcomingDistanceM: Double? = null,
    val playedCount: Int = 0,
    val storyCount: Int = 0,
    val position: GeoPoint = DemoRoute.points.first(),
    // The car's navigation: whether Roadio can hand it the story route, how that went, and whether a turn is close.
    val navigationAvailable: Boolean = false,
    val guidance: NavigationLink.Guidance? = null,
    val holdingForTurn: Boolean = false,
) {
    /** With the car's own position, the drive can only start once the position is coming in. */
    val canStart: Boolean
        get() = mode == DriveMode.SIMULATION || locationStatus in LOCATION_FLOWING

    private companion object {
        val LOCATION_FLOWING = setOf(LocationStatus.WAITING, LocationStatus.OFF_ROUTE, LocationStatus.ON_ROUTE)
    }
}

/**
 * The drive and its stories, once for the whole app: the screen and the media session (the car's media centre,
 * steering-wheel keys) show and control this one engine, and it keeps going while another app is in front. Call it
 * on the main thread.
 */
class DocentEngine(app: Application) {

    private val path = DemoRoute.path
    private val planner = DocentPlanner(path, DemoRoute.stories)
    private val driver = SimulatedRouteDriver(path)
    private val speed = SpeedEstimator()
    private val fixes = FixGate()
    private val turns = TurnGuard()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val settings = app.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    // Pleos Connect's own speech first; the device's TTS where there is no Pleos Speech Service.
    private val speaker: DocentSpeaker = FocusedDocentSpeaker(
        app,
        PleosDocentSpeaker(app) { AndroidDocentSpeaker(app) },
        retellWanted = ::storyStillWanted,
    )
    private val vehicleLocation = VehicleLocation(app, ::onVehicleFix, ::onLocationProblem)

    private var driveJob: Job? = null
    private var screenVisible = false
    private var playedCount = 0
    private var replaying = false
    private var projection = path.project(path.pointAt(0.0))
    private var lastSavedAtM = Double.NEGATIVE_INFINITY

    private val _state = MutableStateFlow(initialState(DriveMode.SIMULATION))
    val state: StateFlow<DocentUiState> = _state.asStateFlow()

    val stories: List<DocentStory> = DemoRoute.stories

    private val restrictions = DrivingRestrictions(app) { limits ->
        _state.update { it.copy(textLimited = limits.text, setupLimited = limits.setup) }
    }

    private val navigation = NavigationLink(
        app,
        onAvailable = { _state.update { it.copy(navigationAvailable = true) } },
        onNextTurn = { distance -> turns.update(distance, SystemClock.elapsedRealtime()) },
        onGuidance = { guidance -> _state.update { it.copy(guidance = guidance) } },
    )

    // The last mode and the drive so far come back, but the car's position is only asked for once the screen shows or
    // a drive starts.
    init {
        if (settings.getString(KEY_MODE, null) == DriveMode.VEHICLE.name) reset(DriveMode.VEHICLE, clearSaved = false)
        restoreDrive()
        navigation.connect()
    }

    private fun initialState(mode: DriveMode): DocentUiState {
        val upcoming = planner.upcoming(projection)
        return DocentUiState(
            mode = mode,
            progress = projection.progress.toFloat(),
            distanceAlongM = projection.distanceAlongM,
            totalDistanceM = path.totalDistanceM,
            position = path.pointAt(projection.distanceAlongM),
            upcomingStory = upcoming?.story,
            upcomingDistanceM = upcoming?.distanceAheadM,
            storyCount = planner.storyCount,
        )
    }

    // ---- The drive so far (kept across a process restart)

    private fun restoreDrive() {
        PlannerSnapshot.decode(settings.getString(KEY_PLANNER, null))?.let(planner::restore)
        playedCount = settings.getInt(KEY_PLAYED, 0)
        val savedM = settings.getFloat(KEY_PROGRESS_M, 0f).toDouble()
        if (_state.value.mode == DriveMode.SIMULATION && savedM > 0.0) {
            // With the car's own position the next fix is the truth; only the stories already told carry over.
            val distance = savedM.coerceIn(0.0, path.totalDistanceM)
            driver.seek(distance)
            projection = path.project(path.pointAt(distance))
        }
        val upcoming = planner.upcoming(projection)
        _state.update {
            it.copy(
                progress = projection.progress.toFloat(),
                distanceAlongM = projection.distanceAlongM,
                position = path.pointAt(projection.distanceAlongM),
                playedCount = playedCount,
                upcomingStory = upcoming?.story,
                upcomingDistanceM = upcoming?.distanceAheadM,
                finished = projection.distanceAlongM >= path.totalDistanceM - ARRIVAL_M,
            )
        }
        lastSavedAtM = projection.distanceAlongM
    }

    private fun saveDrive(force: Boolean = false) {
        if (!force && abs(projection.distanceAlongM - lastSavedAtM) < SAVE_EVERY_M) return
        lastSavedAtM = projection.distanceAlongM
        settings.edit()
            .putFloat(KEY_PROGRESS_M, projection.distanceAlongM.toFloat())
            .putString(KEY_PLANNER, planner.snapshot().encode())
            .putInt(KEY_PLAYED, playedCount)
            .apply()
    }

    private fun clearSavedDrive() {
        settings.edit().remove(KEY_PROGRESS_M).remove(KEY_PLANNER).remove(KEY_PLAYED).apply()
        lastSavedAtM = Double.NEGATIVE_INFINITY
    }

    // ---- Mode and the car's position

    fun toggleMode() {
        setMode(if (_state.value.mode == DriveMode.SIMULATION) DriveMode.VEHICLE else DriveMode.SIMULATION)
    }

    fun setMode(mode: DriveMode) {
        vehicleLocation.stop()
        val edit = settings.edit().putString(KEY_MODE, mode.name)
        // Choosing the car's position is the driver's own request: a permission refused before may be asked once more.
        if (mode == DriveMode.VEHICLE) edit.remove(KEY_LOCATION_REFUSED)
        edit.apply()
        reset(mode)
        if (mode == DriveMode.VEHICLE) connectVehicle()
    }

    private fun connectVehicle() {
        val status = when {
            !vehicleLocation.isSupported -> LocationStatus.UNSUPPORTED
            !settings.getBoolean(KEY_LOCATION_CONSENT, false) -> LocationStatus.NEEDS_CONSENT
            // A refused permission is not asked again on its own; the settings, or choosing the car's position again,
            // can change it.
            !vehicleLocation.hasPermission ->
                if (settings.getBoolean(KEY_LOCATION_REFUSED, false)) LocationStatus.DENIED else LocationStatus.NEEDS_PERMISSION
            else -> {
                settings.edit().remove(KEY_LOCATION_REFUSED).apply()
                vehicleLocation.start()
                LocationStatus.WAITING
            }
        }
        _state.update { it.copy(locationStatus = status) }
    }

    private fun reconnectVehicle() {
        if (_state.value.mode == DriveMode.VEHICLE && !vehicleLocation.isRunning) connectVehicle()
    }

    /** The screen is showing: the car's position is needed again, or was allowed in the settings meanwhile. */
    fun screenShown() {
        screenVisible = true
        reconnectVehicle()
    }

    /** The screen is gone: without a drive on, Roadio stops following the car. */
    fun screenHidden() {
        screenVisible = false
        releaseLocationIfIdle()
    }

    // Roadio follows the car only while its screen shows or a drive is on.
    private fun releaseLocationIfIdle() {
        if (!screenVisible && !_state.value.isDriving) vehicleLocation.stop()
    }

    /** The driver's answer on the screen that explains how Roadio uses the car's position. Only a tap on agree counts. */
    fun answerLocationConsent(agreed: Boolean) {
        if (!agreed) {
            setMode(DriveMode.SIMULATION)
            return
        }
        settings.edit().putBoolean(KEY_LOCATION_CONSENT, true).apply()
        connectVehicle()
    }

    fun locationPermissionResult(granted: Boolean) {
        if (_state.value.mode != DriveMode.VEHICLE) return
        if (granted) {
            connectVehicle()
        } else {
            settings.edit().putBoolean(KEY_LOCATION_REFUSED, true).apply()
            _state.update { it.copy(locationStatus = LocationStatus.DENIED) }
        }
    }

    private fun onVehicleFix(point: GeoPoint, takenMillis: Long) {
        if (_state.value.mode != DriveMode.VEHICLE) return
        // A GPS jump must not move the projection or start a story where the car is not.
        if (!fixes.accept(point, takenMillis)) return
        val speedKmh = (speed.update(point, takenMillis) * 3.6).roundToInt()
        val fix = path.project(point)
        if (fix.lateralM > OFF_ROUTE_M) {
            _state.update { it.copy(locationStatus = LocationStatus.OFF_ROUTE, speedKmh = speedKmh, position = point) }
            return
        }
        follow(fix, point)
        val arrived = fix.distanceAlongM >= path.totalDistanceM - ARRIVAL_M
        _state.update {
            it.copy(
                locationStatus = LocationStatus.ON_ROUTE,
                speedKmh = speedKmh,
                isDriving = it.isDriving && !arrived,
                finished = it.finished || (arrived && it.isDriving),
            )
        }
        if (arrived) releaseLocationIfIdle()
    }

    private fun onLocationProblem(problem: VehicleLocation.Problem) {
        if (_state.value.mode != DriveMode.VEHICLE) return
        val status = when (problem) {
            VehicleLocation.Problem.PERMISSION -> LocationStatus.DENIED
            VehicleLocation.Problem.DISABLED -> LocationStatus.DISABLED
            VehicleLocation.Problem.SERVICE -> LocationStatus.FAILED
        }
        if (problem == VehicleLocation.Problem.PERMISSION) settings.edit().putBoolean(KEY_LOCATION_REFUSED, true).apply()
        _state.update { it.copy(locationStatus = status) }
        // Location off or not allowed: a drive (switched on from the car, before any position came) cannot tell
        // anything. A service error may pass, so the drive stays on for it.
        if (_state.value.isDriving && problem != VehicleLocation.Problem.SERVICE) pause(stopStory = true)
    }

    // ---- The drive

    fun toggleDriving() {
        if (_state.value.isDriving) pause(stopStory = _state.value.mode == DriveMode.VEHICLE) else start()
    }

    fun start() {
        reconnectVehicle()
        val current = _state.value
        if (current.isDriving || !current.canStart) return
        if (current.finished) restart()
        when (current.mode) {
            DriveMode.SIMULATION -> startSimulation()
            // Stories now follow the car's position as it comes in.
            DriveMode.VEHICLE -> _state.update { it.copy(isDriving = true) }
        }
    }

    /** Stops the drive; with [stopStory], also the story being told (otherwise it is told to its end). */
    fun pause(stopStory: Boolean) {
        stopDrive(stopStory)
        releaseLocationIfIdle()
        saveDrive(force = true)
    }

    private fun stopDrive(stopStory: Boolean) {
        driveJob?.cancel()
        driveJob = null
        _state.update { it.copy(isDriving = false, speedKmh = if (it.mode == DriveMode.SIMULATION) 0 else it.speedKmh) }
        if (stopStory && _state.value.isSpeaking) {
            speaker.stop()
            storyEnded()
        }
    }

    private fun startSimulation() {
        _state.update { it.copy(isDriving = true, speedKmh = (driver.speedMps * 3.6).roundToInt()) }
        driveJob?.cancel()
        driveJob = scope.launch {
            while (isActive) {
                delay(TICK_MS)
                val position = driver.advance(TICK_MS / 1000.0)
                follow(path.project(position), position)
                if (driver.finished) {
                    stopDrive(stopStory = false)
                    _state.update { it.copy(finished = true) }
                    saveDrive(force = true)
                    break
                }
            }
        }
    }

    private fun follow(fix: RouteProjection, position: GeoPoint) {
        projection = fix
        // No new story while the car closes in on a turn of the navigation's route; the next one waits until after it.
        val holdingForTurn = turns.holding(SystemClock.elapsedRealtime())
        if (_state.value.isDriving && !holdingForTurn) planner.next(fix)?.let(::tell)
        val upcoming = planner.upcoming(fix)
        _state.update {
            it.copy(
                holdingForTurn = holdingForTurn,
                position = position,
                progress = fix.progress.toFloat(),
                distanceAlongM = fix.distanceAlongM,
                upcomingStory = upcoming?.story,
                upcomingDistanceM = upcoming?.distanceAheadM,
            )
        }
        saveDrive()
    }

    /** Starts the route's stories over; with the car's own position, from where the car is now. */
    fun restart() {
        reset(_state.value.mode, keepPosition = true)
    }

    private fun reset(mode: DriveMode, keepPosition: Boolean = false, clearSaved: Boolean = true) {
        val kept = _state.value
        stopDrive(stopStory = true)
        driver.reset()
        planner.reset()
        speed.reset()
        fixes.reset()
        playedCount = 0
        replaying = false
        if (clearSaved) clearSavedDrive()
        if (!keepPosition || mode == DriveMode.SIMULATION) projection = path.project(path.pointAt(0.0))
        _state.value = initialState(mode).copy(
            locationStatus = if (keepPosition) kept.locationStatus else LocationStatus.NONE,
            textLimited = kept.textLimited,
            setupLimited = kept.setupLimited,
            navigationAvailable = kept.navigationAvailable,
            guidance = kept.guidance,
        )
    }

    // ---- The car's navigation

    /** Hands the story route to the car's navigation: its stops as waypoints and its end as the destination. */
    fun guideAlongStories() {
        val stops = DemoRoute.navigationStops
            .mapNotNull { id -> stories.firstOrNull { it.id == id } }
            .map { NavigationLink.Place(it.point, it.placeName, it.address) }
        if (stops.isEmpty()) return
        _state.update { it.copy(guidance = null) }
        navigation.guide(destination = stops.last(), waypoints = stops.dropLast(1))
    }

    // ---- Stories

    private fun tell(story: DocentStory) {
        playedCount += 1
        replaying = false
        _state.update {
            it.copy(
                currentStory = story,
                isSpeaking = true,
                playedCount = playedCount,
            )
        }
        speaker.speak(story.body, ::storyEnded)
        saveDrive(force = true)
    }

    private fun storyEnded() {
        planner.storyEnded(projection)
        _state.update { it.copy(isSpeaking = false) }
        saveDrive(force = true)
    }

    // After a call or a voice command, a story is told again only while it still fits where the car is; one the driver
    // asked for always is.
    private fun storyStillWanted(): Boolean {
        val story = _state.value.currentStory ?: return false
        return replaying || !planner.hasPassed(story, projection)
    }

    /** Stops the story being told, or tells the last one again. */
    fun toggleStory() {
        if (_state.value.isSpeaking) skipStory() else replayStory()
    }

    /** Ends the story being told; the next one comes at its place. */
    fun skipStory() {
        if (!_state.value.isSpeaking) return
        speaker.stop()
        storyEnded()
    }

    fun replayStory() {
        val story = _state.value.currentStory ?: return
        tellOnRequest(story)
    }

    /** Tells the story [id] now, out of its turn (a choice in the car's media centre or a voice search). */
    fun tellStory(id: String) {
        val story = stories.firstOrNull { it.id == id } ?: return
        if (_state.value.isSpeaking && _state.value.currentStory?.id == id) return
        tellOnRequest(story)
    }

    private fun tellOnRequest(story: DocentStory) {
        if (_state.value.isSpeaking) speaker.stop()
        planner.storyStarted()
        replaying = true
        _state.update { it.copy(currentStory = story, isSpeaking = true) }
        speaker.speak(story.body, ::storyEnded)
    }

    private companion object {
        const val TICK_MS = 250L
        // Farther than this from the demo route, the car is not on it (the route was drawn by hand, roads run beside it).
        const val OFF_ROUTE_M = 200.0
        const val ARRIVAL_M = 30.0
        const val SETTINGS = "roadio"
        const val KEY_MODE = "mode"
        const val KEY_LOCATION_CONSENT = "location_consent"
        const val KEY_LOCATION_REFUSED = "location_refused"
        const val KEY_PROGRESS_M = "progress_m"
        const val KEY_PLANNER = "planner"
        const val KEY_PLAYED = "played"
        // Save the drive every this many metres (and at once when a story starts or ends).
        const val SAVE_EVERY_M = 50.0
    }
}
