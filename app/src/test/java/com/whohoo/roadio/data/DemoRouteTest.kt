package com.whohoo.roadio.data

import com.whohoo.roadio.core.DocentPlanner
import com.whohoo.roadio.drive.SimulatedRouteDriver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoRouteTest {

    @Test
    fun `every story sits on the route`() {
        val planner = DocentPlanner(DemoRoute.path, DemoRoute.stories)

        assertEquals(DemoRoute.stories.size, planner.storyCount)
    }

    // On Pleos Connect a story takes 13 to 19 seconds to tell (about 0.13 s a character); shorter and much longer ones
    // are tried too.
    @Test
    fun `a full drive tells every story in route order, each before its place`() {
        for (storySeconds in listOf(5.0, 13.0, 20.0, 30.0)) {
            val told = drive(storySeconds)

            assertEquals("story length $storySeconds s", DemoRoute.stories.map { it.id }, told.map { it.first })
            for ((id, startedAtM) in told) {
                val placeAtM = DemoRoute.path.project(DemoRoute.stories.first { it.id == id }.point).distanceAlongM
                // Gwanghwamun is where the drive starts: its story comes on the first tick.
                assertTrue("$id ($storySeconds s) started at $startedAtM m, its place is at $placeAtM m", startedAtM <= placeAtM + 5.0)
            }
        }
    }

    // The loop of DocentEngine: 0.25 s ticks, and a story ends storySeconds after it starts.
    private fun drive(storySeconds: Double): List<Pair<String, Double>> {
        val path = DemoRoute.path
        val planner = DocentPlanner(path, DemoRoute.stories)
        val driver = SimulatedRouteDriver(path)
        val told = mutableListOf<Pair<String, Double>>()
        var speakingUntilS: Double? = null
        var timeS = 0.0
        while (!driver.finished) {
            timeS += TICK_S
            val projection = path.project(driver.advance(TICK_S))
            if (speakingUntilS != null && timeS >= speakingUntilS) {
                speakingUntilS = null
                planner.storyEnded(projection)
            }
            planner.next(projection)?.let {
                told += it.id to projection.distanceAlongM
                speakingUntilS = timeS + storySeconds
            }
        }
        return told
    }

    private companion object {
        const val TICK_S = 0.25
    }
}
