package com.whohoo.roadio.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DocentPlannerTest {

    private val path = GeoTest.straightRoute()

    @Test
    fun `story triggers inside the lead window`() {
        val story = storyAt(330.0, id = "a")
        val planner = DocentPlanner(
            path = path,
            stories = listOf(story),
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0),
        )

        val result = planner.next(projectionAt(0.0))

        assertEquals("a", result?.id)
    }

    @Test
    fun `story does not trigger after the window has passed`() {
        val story = storyAt(330.0, id = "a")
        val planner = DocentPlanner(
            path = path,
            stories = listOf(story),
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0),
        )

        val result = planner.next(projectionAt(900.0))

        assertNull(result)
    }

    @Test
    fun `story is not repeated before it is rearmed`() {
        val story = storyAt(330.0, id = "a")
        val planner = DocentPlanner(
            path = path,
            stories = listOf(story),
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0, rearmM = 5_000.0),
        )

        assertEquals("a", planner.next(projectionAt(0.0))?.id)
        assertNull(planner.next(projectionAt(500.0)))
    }

    @Test
    fun `minimum gap blocks a second story`() {
        val stories = listOf(
            storyAt(50.0, id = "a", radius = 1_000.0),
            storyAt(120.0, id = "b", radius = 1_000.0),
        )
        val planner = DocentPlanner(
            path = path,
            stories = stories,
            config = TriggerConfig(leadM = 400.0, minGapM = 500.0, rearmM = 10_000.0),
        )

        assertEquals("a", planner.next(projectionAt(0.0))?.id)
        assertNull(planner.next(projectionAt(150.0)))
        assertEquals("b", planner.next(projectionAt(600.0))?.id)
    }

    @Test
    fun `higher priority story wins when both are eligible`() {
        val stories = listOf(
            storyAt(300.0, id = "normal", priority = 0),
            storyAt(310.0, id = "important", priority = 5),
        )
        val planner = DocentPlanner(
            path = path,
            stories = stories,
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0),
        )

        assertEquals("important", planner.next(projectionAt(0.0))?.id)
    }

    @Test
    fun `stories too far from the route are ignored`() {
        val farAway = DocentStory(
            id = "far",
            title = "멀리 있는 이야기",
            body = "본문",
            point = GeoPoint(37.505, 127.01),
        )
        val planner = DocentPlanner(
            path = path,
            stories = listOf(farAway),
            config = TriggerConfig(leadM = 5_000.0, minGapM = 0.0),
        )

        assertNull(planner.next(projectionAt(0.0)))
    }

    @Test
    fun `upcoming reports the next unplayed story`() {
        val planner = DocentPlanner(
            path = path,
            stories = listOf(storyAt(800.0, id = "a")),
        )

        val upcoming = planner.upcoming(projectionAt(0.0))

        assertEquals("a", upcoming?.story?.id)
        assertEquals(800.0, upcoming?.distanceAheadM ?: 0.0, 5.0)
    }

    private fun storyAt(
        distanceM: Double,
        id: String,
        radius: Double = 300.0,
        priority: Int = 0,
    ) = DocentStory(
        id = id,
        title = id,
        body = "본문",
        point = path.pointAt(distanceM),
        triggerRadiusM = radius,
        priority = priority,
    )

    private fun projectionAt(distanceM: Double): RouteProjection =
        path.project(path.pointAt(distanceM))
}
