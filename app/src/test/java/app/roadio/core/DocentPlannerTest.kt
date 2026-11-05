package app.roadio.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `story is told once per drive until reset`() {
        val story = storyAt(330.0, id = "a")
        val planner = DocentPlanner(
            path = path,
            stories = listOf(story),
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0),
        )

        assertEquals("a", planner.next(projectionAt(0.0))?.id)
        planner.storyEnded(projectionAt(100.0))
        assertNull(planner.next(projectionAt(200.0)))

        planner.reset()
        assertEquals("a", planner.next(projectionAt(0.0))?.id)
    }

    @Test
    fun `nothing new starts while a story is being told`() {
        val stories = listOf(
            storyAt(50.0, id = "a", radius = 1_000.0),
            storyAt(120.0, id = "b", radius = 1_000.0),
        )
        val planner = DocentPlanner(
            path = path,
            stories = stories,
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0),
        )

        assertEquals("a", planner.next(projectionAt(0.0))?.id)
        assertNull(planner.next(projectionAt(300.0)))
        planner.storyEnded(projectionAt(300.0))
        assertEquals("b", planner.next(projectionAt(300.0))?.id)
    }

    @Test
    fun `a replay holds back the next story too`() {
        val planner = DocentPlanner(
            path = path,
            stories = listOf(storyAt(120.0, id = "a")),
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0),
        )

        planner.storyStarted()
        assertNull(planner.next(projectionAt(0.0)))
        planner.storyEnded(projectionAt(0.0))
        assertEquals("a", planner.next(projectionAt(0.0))?.id)
    }

    @Test
    fun `minimum gap of quiet road follows the end of a story`() {
        val stories = listOf(
            storyAt(50.0, id = "a", radius = 1_000.0),
            storyAt(120.0, id = "b", radius = 1_000.0),
        )
        val planner = DocentPlanner(
            path = path,
            stories = stories,
            config = TriggerConfig(leadM = 400.0, minGapM = 500.0),
        )

        assertEquals("a", planner.next(projectionAt(0.0))?.id)
        planner.storyEnded(projectionAt(150.0))
        assertNull(planner.next(projectionAt(600.0)))
        assertEquals("b", planner.next(projectionAt(700.0))?.id)
    }

    @Test
    fun `of two stories the one whose stretch ends first is told first`() {
        val stories = listOf(
            storyAt(300.0, id = "later", radius = 600.0),
            storyAt(330.0, id = "sooner", radius = 100.0),
        )
        val planner = DocentPlanner(
            path = path,
            stories = stories,
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0),
        )

        assertEquals("sooner", planner.next(projectionAt(250.0))?.id)
        planner.storyEnded(projectionAt(400.0))
        assertEquals("later", planner.next(projectionAt(400.0))?.id)
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
    fun `upcoming reports the next story still to be told`() {
        val planner = DocentPlanner(
            path = path,
            stories = listOf(storyAt(330.0, id = "a"), storyAt(800.0, id = "b")),
            config = TriggerConfig(leadM = 400.0, minGapM = 0.0),
        )

        assertEquals("a", planner.upcoming(projectionAt(0.0))?.story?.id)
        assertEquals("a", planner.next(projectionAt(0.0))?.id)

        val upcoming = planner.upcoming(projectionAt(0.0))

        assertEquals("b", upcoming?.story?.id)
        assertEquals(800.0, upcoming?.distanceAheadM ?: 0.0, 5.0)
    }

    @Test
    fun `upcoming keeps a story past its place until its stretch of road ends`() {
        val planner = DocentPlanner(
            path = path,
            stories = listOf(storyAt(330.0, id = "a", radius = 100.0)),
        )

        val passedButOpen = planner.upcoming(projectionAt(400.0))
        assertEquals("a", passedButOpen?.story?.id)
        assertEquals(-70.0, passedButOpen?.distanceAheadM ?: 0.0, 5.0)
        assertNull(planner.upcoming(projectionAt(500.0)))
    }

    @Test
    fun `a story's place is passed once the car is beyond it`() {
        val story = storyAt(330.0, id = "a")
        val planner = DocentPlanner(path = path, stories = listOf(story))

        assertFalse(planner.hasPassed(story, projectionAt(300.0)))
        assertTrue(planner.hasPassed(story, projectionAt(360.0)))
    }

    @Test
    fun `a story begun past its place still fits until its stretch of road ends`() {
        val story = storyAt(0.0, id = "start", radius = 300.0)
        val planner = DocentPlanner(path = path, stories = listOf(story), config = TriggerConfig(minGapM = 0.0))

        assertEquals("start", planner.next(projectionAt(3.5))?.id)

        assertFalse(planner.hasPassed(story, projectionAt(80.0)))
        assertTrue(planner.hasPassed(story, projectionAt(310.0)))
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
