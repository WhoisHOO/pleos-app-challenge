package com.whohoo.roadio.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlannerSnapshotTest {

    private val path = GeoTest.straightRoute()

    @Test
    fun `a snapshot survives a round trip through text`() {
        val snapshot = PlannerSnapshot(
            startedAtM = mapOf("a" to 120.5, "b" to 480.0),
            quietSinceM = 380.25,
        )

        assertEquals(snapshot, PlannerSnapshot.decode(snapshot.encode()))
    }

    @Test
    fun `an empty drive round-trips too`() {
        val snapshot = PlannerSnapshot()

        val decoded = PlannerSnapshot.decode(snapshot.encode())

        assertEquals(emptyMap<String, Double>(), decoded?.startedAtM)
        assertEquals(Double.NEGATIVE_INFINITY, decoded?.quietSinceM ?: 0.0, 0.0)
    }

    @Test
    fun `unreadable text is no snapshot`() {
        assertNull(PlannerSnapshot.decode(null))
        assertNull(PlannerSnapshot.decode(""))
        assertNull(PlannerSnapshot.decode("not a number"))
    }

    @Test
    fun `unreadable lines are skipped`() {
        val decoded = PlannerSnapshot.decode("-Infinity\na\t10.0\nbroken\nb\tnot a number\nc\t30.0")

        assertEquals(mapOf("a" to 10.0, "c" to 30.0), decoded?.startedAtM)
    }

    @Test
    fun `a restored drive does not tell its stories again`() {
        val stories = listOf(storyAt(50.0, id = "a"), storyAt(800.0, id = "b"))
        val before = DocentPlanner(
            path = path,
            stories = stories,
            config = TriggerConfig(leadM = 400.0, minGapM = 100.0),
        )
        assertEquals("a", before.next(projectionAt(0.0))?.id)
        before.storyEnded(projectionAt(150.0))

        val after = DocentPlanner(
            path = path,
            stories = stories,
            config = TriggerConfig(leadM = 400.0, minGapM = 100.0),
        )
        after.restore(PlannerSnapshot.decode(before.snapshot().encode())!!)

        assertEquals("b", after.upcoming(projectionAt(600.0))?.story?.id)
        assertEquals("b", after.next(projectionAt(600.0))?.id)
    }

    @Test
    fun `a restored drive keeps the quiet stretch after the last story`() {
        val stories = listOf(storyAt(50.0, id = "a", radius = 2_000.0), storyAt(120.0, id = "b", radius = 2_000.0))
        val config = TriggerConfig(leadM = 400.0, minGapM = 500.0)
        val before = DocentPlanner(path = path, stories = stories, config = config)
        assertEquals("a", before.next(projectionAt(0.0))?.id)
        before.storyEnded(projectionAt(150.0))

        val after = DocentPlanner(path = path, stories = stories, config = config)
        after.restore(PlannerSnapshot.decode(before.snapshot().encode())!!)

        assertNull(after.next(projectionAt(600.0)))
        assertEquals("b", after.next(projectionAt(700.0))?.id)
    }

    @Test
    fun `a story being told is not resumed by the new process`() {
        val planner = DocentPlanner(path = path, stories = listOf(storyAt(120.0, id = "a")))
        planner.storyStarted()

        val after = DocentPlanner(path = path, stories = listOf(storyAt(120.0, id = "a")))
        after.restore(PlannerSnapshot.decode(planner.snapshot().encode())!!)

        assertEquals("a", after.next(projectionAt(0.0))?.id)
    }

    private fun storyAt(
        distanceM: Double,
        id: String,
        radius: Double = 300.0,
    ) = DocentStory(
        id = id,
        title = id,
        body = "본문",
        point = path.pointAt(distanceM),
        triggerRadiusM = radius,
    )

    private fun projectionAt(distanceM: Double): RouteProjection =
        path.project(path.pointAt(distanceM))
}
