package app.roadio

import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.util.Log
import app.roadio.audio.AndroidDocentSpeaker
import app.roadio.audio.FocusedDocentSpeaker
import app.roadio.audio.PleosDocentSpeaker
import app.roadio.audio.PleosVoiceListener
import app.roadio.car.DrivingRestrictions
import app.roadio.car.VehicleTrip
import app.roadio.core.CarMeters
import app.roadio.core.DocentPlanner
import app.roadio.core.DocentStory
import app.roadio.core.FixGate
import app.roadio.core.Gear
import app.roadio.core.GeoPoint
import app.roadio.core.PlannerSnapshot
import app.roadio.core.RouteProjection
import app.roadio.core.SpeedEstimator
import app.roadio.core.TripSummary
import app.roadio.core.TurnGuard
import app.roadio.core.VoiceCommand
import app.roadio.data.RoadioRoute
import app.roadio.drive.SimulatedRouteDriver
import app.roadio.location.VehicleLocation
import app.roadio.navigation.NavigationLink
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

/** Roadio listening for a spoken command, and how the last one went. */
enum class VoiceState {
    IDLE,

    /** The car is opening its microphone. */
    OPENING,
    LISTENING,

    /** Nothing was said, or not one of the commands. */
    MISSED,

    /** The car could not listen. */
    FAILED,
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
    val position: GeoPoint = GeoPoint(0.0, 0.0),
    // The car's navigation: whether Roadio can hand it the story route, how that went, and whether a turn is close.
    val navigationAvailable: Boolean = false,
    val guidance: NavigationLink.Guidance? = null,
    val holdingForTurn: Boolean = false,
    // The gear the driver selected, from the car (unknown on phones).
    val gear: Gear = Gear.UNKNOWN,
    // How the drive went, once it reached the end of the route.
    val summary: TripSummary? = null,
    // The car's speech engine can take a spoken command (Pleos SpeechToText), and whether it is listening.
    val voiceAvailable: Boolean = false,
    val voice: VoiceState = VoiceState.IDLE,
) {
    /** With the car's own position, the drive can only start once the position is coming in. */
    val canStart: Boolean
        get() = mode == DriveMode.SIMULATION || locationStatus in LOCATION_FLOWING

    /** Roadio is listening for a command: no story is told meanwhile, so the microphone hears only the driver. */
    val listening: Boolean
        get() = voice == VoiceState.OPENING || voice == VoiceState.LISTENING

    /** Reversing (parking, manoeuvring) needs the driver's attention: no story then. A simulated car has no gear. */
    val holdingForGear: Boolean
        get() = mode == DriveMode.VEHICLE && gear == Gear.REVERSE

    private companion object {
        val LOCATION_FLOWING = setOf(LocationStatus.WAITING, LocationStatus.OFF_ROUTE, LocationStatus.ON_ROUTE)
    }
}

/**
 * The drive and its stories, once for the whole app: the screen and the media session (the car's media centre,
 * steering-wheel keys) show and control this one engine, and it keeps going while another app is in front. Call it
 * on the main thread.
 */
class DocentEngine(app: Application, route: RoadioRoute) {

    private val path = route.path
    private val planner = DocentPlanner(path, route.stories)
    private val navigationStops = route.navigationStops
    private val driver = SimulatedRouteDriver(path)
    private val speed = SpeedEstimator()
    private val fixes = FixGate()
    private val turns = TurnGuard()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val settings = app.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    // Pleos Connect's own speech first; the device's TTS where there is no Pleos Speech Service.
    private val speaker = FocusedDocentSpeaker(
        app,
        PleosDocentSpeaker(app) { AndroidDocentSpeaker(app) },
        retellWanted = ::storyStillWanted,
    )
    private val vehicleLocation = VehicleLocation(app, ::onVehicleFix, ::onLocationProblem)
    // The car's own trip meter and odometer, for the summary at the end of a drive (Pleos Vehicle SDK).
    private val trip = VehicleTrip(app)

    private var driveJob: Job? = null
    private var screenVisible = false
    private var playedCount = 0
    private var replaying = false
    private var projection = path.project(path.pointAt(0.0))
    private var lastSavedAtM = Double.NEGATIVE_INFINITY
    // This drive, for its summary: where along the route it began, the car's meters then, and how long it has been on.
    private var tripStartAlongM: Double? = null
    private var tripStartMeters: CarMeters? = null
    private var drivenMs = 0L
    private var drivingSinceMs: Long? = null
    // Counts resets, so the car's meters answering late are not taken for a newer drive's.
    private var driveGeneration = 0
    private var voiceNoticeJob: Job? = null

    private val _state = MutableStateFlow(initialState(DriveMode.SIMULATION))
    val state: StateFlow<DocentUiState> = _state.asStateFlow()

    val stories: List<DocentStory> = route.stories

    private val restrictions = DrivingRestrictions(app, onGear = ::onGear) { limits ->
        _state.update { it.copy(textLimited = limits.text, setupLimited = limits.setup) }
    }

    // Spoken commands after a tap on 말로 하기 (Pleos SpeechToText); only on a car with Pleos's Speech Service.
    private val voice = PleosVoiceListener(app) { _state.update { it.copy(voiceAvailable = true) } }

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
        // The card shows the last story told, so a resumed drive does not read as a fresh one, and it can be heard again.
        val lastStory = settings.getString(KEY_LAST_STORY, null)?.let { id -> stories.firstOrNull { it.id == id } }
        drivenMs = settings.getLong(KEY_DRIVEN_MS, 0L)
        tripStartAlongM = settings.getFloat(KEY_TRIP_START_M, -1f).takeIf { it >= 0f }?.toDouble()
        val savedM = settings.getFloat(KEY_PROGRESS_M, 0f).toDouble()
        // A summary is saved only once a drive reaches its end, in either mode.
        val savedSummaryM = settings.getFloat(KEY_SUMMARY_M, -1f)
        val arrived = savedSummaryM >= 0f
        if (savedM > 0.0 && (_state.value.mode == DriveMode.SIMULATION || arrived)) {
            // With the car's own position the next fix is the truth; only the stories already told carry over. A drive
            // that had ended shows where it ended.
            val distance = savedM.coerceIn(0.0, path.totalDistanceM)
            if (_state.value.mode == DriveMode.SIMULATION) driver.seek(distance)
            projection = path.project(path.pointAt(distance))
        }
        val upcoming = planner.upcoming(projection)
        // A drive that had reached its end comes back finished, with its summary: the next start is a new drive.
        val finished = arrived || projection.distanceAlongM >= path.totalDistanceM - ARRIVAL_M
        val summary = savedSummaryM.takeIf { arrived }?.let { metres ->
            TripSummary(
                distanceM = metres.toDouble(),
                byCar = settings.getBoolean(KEY_SUMMARY_BY_CAR, false),
                minutes = TripSummary.minutesOf(drivenMs),
                played = playedCount,
                total = planner.storyCount,
            )
        }
        _state.update {
            it.copy(
                progress = projection.progress.toFloat(),
                distanceAlongM = projection.distanceAlongM,
                position = path.pointAt(projection.distanceAlongM),
                playedCount = playedCount,
                currentStory = lastStory,
                upcomingStory = upcoming?.story,
                upcomingDistanceM = upcoming?.distanceAheadM,
                finished = finished,
                summary = summary,
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
            .putString(KEY_LAST_STORY, _state.value.currentStory?.id)
            .putLong(KEY_DRIVEN_MS, drivenSoFarMs())
            .putFloat(KEY_TRIP_START_M, tripStartAlongM?.toFloat() ?: -1f)
            .putFloat(KEY_SUMMARY_M, _state.value.summary?.distanceM?.toFloat() ?: -1f)
            .putBoolean(KEY_SUMMARY_BY_CAR, _state.value.summary?.byCar ?: false)
            .apply()
    }

    private fun clearSavedDrive() {
        settings.edit()
            .remove(KEY_PROGRESS_M).remove(KEY_PLANNER).remove(KEY_PLAYED).remove(KEY_LAST_STORY)
            .remove(KEY_DRIVEN_MS).remove(KEY_TRIP_START_M).remove(KEY_SUMMARY_M).remove(KEY_SUMMARY_BY_CAR)
            .apply()
        lastSavedAtM = Double.NEGATIVE_INFINITY
    }

    // ---- This drive's summary

    private fun drivenSoFarMs(): Long = drivenMs + (drivingSinceMs?.let { SystemClock.elapsedRealtime() - it } ?: 0L)

    // The drive is on: time starts counting, and at its very start the car's meters are read.
    private fun beginTripLeg() {
        if (drivingSinceMs != null) return
        val firstLeg = drivenMs == 0L && tripStartAlongM == null
        drivingSinceMs = SystemClock.elapsedRealtime()
        val generation = driveGeneration
        if (firstLeg && _state.value.mode == DriveMode.VEHICLE) {
            trip.read { meters -> if (generation == driveGeneration) tripStartMeters = meters }
        }
    }

    private fun endTripLeg() {
        drivenMs = drivenSoFarMs()
        drivingSinceMs = null
    }

    // The route's end: how far (the car's own meters when they moved, otherwise along the route), how long, how many
    // stories. The car's meters answer later, so the summary along the route shows first.
    private fun summarize() {
        val routeM = projection.distanceAlongM - (tripStartAlongM ?: 0.0)
        val start = tripStartMeters
        fun publish(end: CarMeters?) {
            val summary = TripSummary.of(routeM, start, end, drivenMs, playedCount, planner.storyCount)
            _state.update { it.copy(summary = summary) }
            saveDrive(force = true)
        }
        publish(null)
        if (_state.value.mode == DriveMode.VEHICLE && start != null) {
            val generation = driveGeneration
            trip.read { end -> if (generation == driveGeneration && _state.value.summary != null) publish(end) }
        }
    }

    // ---- The car's gear

    private fun onGear(value: Int) {
        val gear = Gear.fromVehicle(value)
        val before = _state.value
        _state.update { it.copy(gear = gear) }
        // Reversing needs the driver's attention: the story being told stops (it can be heard again), and none starts.
        if (gear == Gear.REVERSE && before.gear != Gear.REVERSE && before.mode == DriveMode.VEHICLE) skipStory()
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
        val arriving = arrived && _state.value.isDriving
        _state.update {
            it.copy(
                locationStatus = LocationStatus.ON_ROUTE,
                speedKmh = speedKmh,
                isDriving = it.isDriving && !arrived,
                finished = it.finished || arriving,
            )
        }
        if (arriving) {
            endTripLeg()
            summarize()
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
        beginTripLeg()
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
        endTripLeg()
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
                    summarize()
                    break
                }
            }
        }
    }

    private fun follow(fix: RouteProjection, position: GeoPoint) {
        projection = fix
        // The summary measures the route from where this drive first was on it.
        if (_state.value.isDriving && tripStartAlongM == null) tripStartAlongM = fix.distanceAlongM
        // No new story while the car closes in on a turn of the navigation's route; the next one waits until after it.
        // None while reversing either.
        val holdingForTurn = turns.holding(SystemClock.elapsedRealtime())
        val holding = holdingForTurn || _state.value.holdingForGear || _state.value.listening
        if (_state.value.isDriving && !holding) planner.next(fix)?.let(::tell)
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
        stopListening()
        stopDrive(stopStory = true)
        driver.reset()
        planner.reset()
        speed.reset()
        fixes.reset()
        playedCount = 0
        replaying = false
        driveGeneration += 1
        tripStartAlongM = null
        tripStartMeters = null
        drivenMs = 0L
        drivingSinceMs = null
        if (clearSaved) clearSavedDrive()
        if (!keepPosition || mode == DriveMode.SIMULATION) projection = path.project(path.pointAt(0.0))
        _state.value = initialState(mode).copy(
            locationStatus = if (keepPosition) kept.locationStatus else LocationStatus.NONE,
            textLimited = kept.textLimited,
            setupLimited = kept.setupLimited,
            navigationAvailable = kept.navigationAvailable,
            guidance = kept.guidance,
            gear = kept.gear,
            voiceAvailable = kept.voiceAvailable,
        )
    }

    // ---- The car's navigation

    /** Hands the story route to the car's navigation: its stops as waypoints and its end as the destination. */
    fun guideAlongStories() {
        val stops = navigationStops
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
        // Asked for on the screen or in the media centre while Roadio listens: the microphone must not hear it.
        stopListening()
        if (_state.value.isSpeaking) speaker.stop()
        planner.storyStarted()
        replaying = true
        _state.update { it.copy(currentStory = story, isSpeaking = true) }
        speaker.speak(story.body, ::storyEnded)
    }

    // ---- Spoken commands

    /** The 말로 하기 button: listens for one command, or stops listening when tapped again. */
    fun toggleListening() {
        if (_state.value.listening) {
            stopListening()
            speaker.resume()
        } else {
            listen()
        }
    }

    /**
     * Debug builds only: one turn of listening to a [recording] (16 kHz, 16-bit mono PCM) instead of the car's
     * microphone, to try the commands on the emulator, whose microphone may hear nothing.
     */
    fun listenToRecording(recording: ByteArray) {
        if (!_state.value.listening) listen(recording)
    }

    private fun listen(recording: ByteArray? = null) {
        if (!_state.value.voiceAvailable) return
        voiceNoticeJob?.cancel()
        // The story waits so the microphone hears only the driver; after the command it goes on, or not.
        speaker.hold()
        // Before listening starts: a turn that fails at once is told back inside listen().
        _state.update { it.copy(voice = VoiceState.OPENING) }
        val listening = voice.listen(
            opened = { _state.update { if (it.voice == VoiceState.OPENING) it.copy(voice = VoiceState.LISTENING) else it } },
            heard = ::heard,
            recording = recording,
        )
        if (!listening) {
            _state.update { it.copy(voice = VoiceState.IDLE) }
            speaker.resume()
        }
    }

    private fun heard(hearing: PleosVoiceListener.Hearing) {
        if (!_state.value.listening) return
        val command = (hearing as? PleosVoiceListener.Hearing.Words)?.let { VoiceCommand.of(it.text) }
        // The command only, never the words.
        Log.i(TAG, "voice command: ${command ?: hearing.javaClass.simpleName}")
        showVoice(
            when {
                command != null -> VoiceState.IDLE
                hearing is PleosVoiceListener.Hearing.Failed -> VoiceState.FAILED
                else -> VoiceState.MISSED
            },
        )
        when (command) {
            VoiceCommand.SKIP -> skipStory()
            VoiceCommand.REPLAY -> replayStory()
            VoiceCommand.OFF -> pause(stopStory = true)
            VoiceCommand.ON -> {
                start()
                speaker.resume()
            }
            null -> speaker.resume()
        }
    }

    // A word that was missed stays on the screen for a moment, then the line goes back to the next story.
    private fun showVoice(state: VoiceState) {
        voiceNoticeJob?.cancel()
        _state.update { it.copy(voice = state) }
        if (state == VoiceState.MISSED || state == VoiceState.FAILED) {
            voiceNoticeJob = scope.launch {
                delay(VOICE_NOTICE_MS)
                _state.update { if (it.voice == state) it.copy(voice = VoiceState.IDLE) else it }
            }
        }
    }

    private fun stopListening() {
        if (!_state.value.listening) return
        voice.cancel()
        _state.update { it.copy(voice = VoiceState.IDLE) }
    }

    private companion object {
        const val TICK_MS = 250L
        const val TAG = "Roadio"
        const val VOICE_NOTICE_MS = 4_000L
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
        const val KEY_LAST_STORY = "last_story"
        const val KEY_DRIVEN_MS = "driven_ms"
        const val KEY_TRIP_START_M = "trip_start_m"
        const val KEY_SUMMARY_M = "summary_m"
        const val KEY_SUMMARY_BY_CAR = "summary_by_car"
        // Save the drive every this many metres (and at once when a story starts or ends).
        const val SAVE_EVERY_M = 50.0
    }
}
