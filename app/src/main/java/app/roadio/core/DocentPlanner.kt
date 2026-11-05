package app.roadio.core

import kotlin.math.abs

data class TriggerConfig(
    val leadM: Double = 400.0,
    // Quiet road between the end of one story and the start of the next (about 7 seconds at 50 km/h).
    val minGapM: Double = 100.0,
    val maxLateralM: Double = 150.0,
)

data class UpcomingStory(
    val story: DocentStory,
    val distanceAheadM: Double,
)

/** Tells each story once per drive (until [reset]), one at a time, inside the stretch of road where it belongs. */
class DocentPlanner(
    path: RoutePath,
    stories: List<DocentStory>,
    private val config: TriggerConfig = TriggerConfig(),
) {

    private data class PlacedStory(
        val story: DocentStory,
        val distanceAlongM: Double,
        val lateralM: Double,
    ) {
        val windowEndM: Double get() = distanceAlongM + story.triggerRadiusM
    }

    private val placed: List<PlacedStory> = stories
        .map { story ->
            val projection = path.project(story.point)
            PlacedStory(story, projection.distanceAlongM, projection.lateralM)
        }
        .filter { it.lateralM <= config.maxLateralM }
        .sortedBy { it.distanceAlongM }

    val storyCount: Int get() = placed.size

    private var telling = false
    private var quietSinceM = Double.NEGATIVE_INFINITY
    private val startedAtM = mutableMapOf<String, Double>()

    fun next(projection: RouteProjection): DocentStory? {
        val current = projection.distanceAlongM
        if (telling || current - quietSinceM < config.minGapM) return null

        // Of the stories whose stretch of road the car is on, the important one first, then the one whose stretch
        // ends soonest, so that no story is lost to the one after it.
        val candidate = placed
            .asSequence()
            .filter { current >= it.distanceAlongM - config.leadM }
            .filter { current <= it.windowEndM }
            .filter { it.story.id !in startedAtM }
            .sortedWith(
                compareByDescending<PlacedStory> { it.story.priority }
                    .thenBy { it.windowEndM }
                    .thenBy { abs(it.distanceAlongM - current) },
            )
            .firstOrNull()
            ?: return null

        startedAtM[candidate.story.id] = current
        storyStarted()
        return candidate.story
    }

    /** A story is being told outside [next], such as a replay: nothing new starts until [storyEnded]. */
    fun storyStarted() {
        telling = true
    }

    /** The story being told has ended at [projection]; the next one waits for [TriggerConfig.minGapM] of quiet road. */
    fun storyEnded(projection: RouteProjection) {
        telling = false
        quietSinceM = projection.distanceAlongM
    }

    /**
     * Whether the car at [projection] has gone past where [story] still fits: its place, or for a story that began
     * once its place was already behind (such as the place a drive starts from), the end of its stretch of road.
     */
    fun hasPassed(story: DocentStory, projection: RouteProjection): Boolean {
        val place = placed.firstOrNull { it.story.id == story.id } ?: return true
        val startedAt = startedAtM[story.id] ?: Double.NEGATIVE_INFINITY
        val fitsUntilM = if (startedAt >= place.distanceAlongM) place.windowEndM else place.distanceAlongM
        return projection.distanceAlongM > fitsUntilM
    }

    /** The next story still to be told, with its distance from the car (zero or less once the car is at its place). */
    fun upcoming(projection: RouteProjection, maxAheadM: Double = 3_000.0): UpcomingStory? {
        val current = projection.distanceAlongM
        return placed
            .asSequence()
            .filter { it.story.id !in startedAtM }
            .filter { current <= it.windowEndM }
            .map { UpcomingStory(it.story, it.distanceAlongM - current) }
            .filter { it.distanceAheadM <= maxAheadM }
            .minByOrNull { it.distanceAheadM }
    }

    fun reset() {
        startedAtM.clear()
        telling = false
        quietSinceM = Double.NEGATIVE_INFINITY
    }

    /** The stories already told and the quiet stretch after the last one, for the next process. */
    fun snapshot(): PlannerSnapshot = PlannerSnapshot(startedAtM.toMap(), quietSinceM)

    /** Goes on from [snapshot]; a story that was being told is not resumed (the new process tells no story). */
    fun restore(snapshot: PlannerSnapshot) {
        startedAtM.clear()
        startedAtM.putAll(snapshot.startedAtM)
        quietSinceM = snapshot.quietSinceM
        telling = false
    }
}
