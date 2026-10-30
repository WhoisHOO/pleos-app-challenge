package com.whohoo.roadio.core

import kotlin.math.abs

data class TriggerConfig(
    val leadM: Double = 400.0,
    val minGapM: Double = 900.0,
    val maxLateralM: Double = 150.0,
    val rearmM: Double = 3_000.0,
)

data class UpcomingStory(
    val story: DocentStory,
    val distanceAheadM: Double,
)

class DocentPlanner(
    path: RoutePath,
    stories: List<DocentStory>,
    private val config: TriggerConfig = TriggerConfig(),
) {

    private data class PlacedStory(
        val story: DocentStory,
        val distanceAlongM: Double,
        val lateralM: Double,
    )

    private val placed: List<PlacedStory> = stories
        .map { story ->
            val projection = path.project(story.point)
            PlacedStory(story, projection.distanceAlongM, projection.lateralM)
        }
        .filter { it.lateralM <= config.maxLateralM }
        .sortedBy { it.distanceAlongM }

    private var lastTriggerAtM = Double.NEGATIVE_INFINITY
    private val playedAtM = mutableMapOf<String, Double>()

    fun next(projection: RouteProjection): DocentStory? {
        val current = projection.distanceAlongM
        if (current - lastTriggerAtM < config.minGapM) return null

        val candidate = placed
            .asSequence()
            .filter { current >= it.distanceAlongM - config.leadM }
            .filter { current <= it.distanceAlongM + it.story.triggerRadiusM }
            .filter { story -> playedAtM[story.story.id]?.let { current - it > config.rearmM } ?: true }
            .sortedWith(
                compareByDescending<PlacedStory> { it.story.priority }
                    .thenBy { abs(it.distanceAlongM - current) },
            )
            .firstOrNull()
            ?: return null

        playedAtM[candidate.story.id] = current
        lastTriggerAtM = current
        return candidate.story
    }

    fun upcoming(projection: RouteProjection, maxAheadM: Double = 3_000.0): UpcomingStory? {
        val current = projection.distanceAlongM
        return placed
            .asSequence()
            .filter { it.distanceAlongM >= current }
            .filter { playedAtM[it.story.id] == null }
            .map { UpcomingStory(it.story, it.distanceAlongM - current) }
            .filter { it.distanceAheadM <= maxAheadM }
            .minByOrNull { it.distanceAheadM }
    }

    fun reset() {
        playedAtM.clear()
        lastTriggerAtM = Double.NEGATIVE_INFINITY
    }
}
