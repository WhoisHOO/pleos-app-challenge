package app.roadio.data

import app.roadio.core.DocentPlanner
import app.roadio.drive.SimulatedRouteDriver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RouteContentTest {

    private val route = RouteJson.parse(assetFile().readText())

    @Test
    fun `the packaged route has its stories and navigation stops`() {
        assertEquals(7, route.stories.size)
        assertEquals(listOf("namdaemun", "seoulstation", "nseoultower"), route.navigationStops)
    }

    @Test
    fun `every story sits on the route`() {
        val planner = DocentPlanner(route.path, route.stories)

        assertEquals(route.stories.size, planner.storyCount)
    }

    // On Pleos Connect a story takes 13 to 19 seconds to tell (about 0.13 s a character); shorter and much longer ones
    // are tried too.
    @Test
    fun `a full drive tells every story in route order, each before its place`() {
        for (storySeconds in listOf(5.0, 13.0, 20.0, 30.0)) {
            val told = drive(storySeconds)

            assertEquals("story length $storySeconds s", route.stories.map { it.id }, told.map { it.first })
            for ((id, startedAtM) in told) {
                val placeAtM = route.path.project(route.stories.first { it.id == id }.point).distanceAlongM
                // Gwanghwamun is where the drive starts: its story comes on the first tick.
                assertTrue("$id ($storySeconds s) started at $startedAtM m, its place is at $placeAtM m", startedAtM <= placeAtM + 5.0)
            }
        }
    }

    @Test
    fun `a story can sit at a fraction of the route instead of a point`() {
        val json = """
            {
              "points": [{ "lat": 0.0, "lon": 0.0 }, { "lat": 0.0, "lon": 1.0 }],
              "stories": [{ "id": "half", "title": "Half", "body": "Body", "atFraction": 0.5 }]
            }
        """.trimIndent()

        val route = RouteJson.parse(json)

        assertEquals(route.path.totalDistanceM / 2, route.path.project(route.stories.single().point).distanceAlongM, 1.0)
    }

    @Test
    fun `a story without a place or fraction is rejected`() {
        val json = """
            {
              "points": [{ "lat": 0.0, "lon": 0.0 }, { "lat": 0.0, "lon": 1.0 }],
              "stories": [{ "id": "nowhere", "title": "Nowhere", "body": "Body" }]
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) { RouteJson.parse(json) }
    }

    @Test
    fun `duplicate story ids are rejected`() {
        val json = """
            {
              "points": [{ "lat": 0.0, "lon": 0.0 }, { "lat": 0.0, "lon": 1.0 }],
              "stories": [
                { "id": "a", "title": "A", "body": "Body", "lat": 0.0, "lon": 0.0 },
                { "id": "a", "title": "B", "body": "Body", "lat": 0.0, "lon": 0.5 }
              ]
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) { RouteJson.parse(json) }
    }

    @Test
    fun `a navigation stop must be a story id`() {
        val json = """
            {
              "points": [{ "lat": 0.0, "lon": 0.0 }, { "lat": 0.0, "lon": 1.0 }],
              "stories": [{ "id": "a", "title": "A", "body": "Body", "lat": 0.0, "lon": 0.0 }],
              "navigationStops": ["missing"]
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) { RouteJson.parse(json) }
    }

    @Test
    fun `a broken file is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { RouteJson.parse("{ not json") }
    }

    // The loop of DocentEngine: 0.25 s ticks, and a story ends storySeconds after it starts.
    private fun drive(storySeconds: Double): List<Pair<String, Double>> {
        val planner = DocentPlanner(route.path, route.stories)
        val driver = SimulatedRouteDriver(route.path)
        val told = mutableListOf<Pair<String, Double>>()
        var speakingUntilS: Double? = null
        var timeS = 0.0
        while (!driver.finished) {
            timeS += TICK_S
            val projection = route.path.project(driver.advance(TICK_S))
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

    private fun assetFile(): File = listOf(
        File("src/main/assets/${RouteStore.FILE}"),
        File("app/src/main/assets/${RouteStore.FILE}"),
    ).first { it.isFile }

    private companion object {
        const val TICK_S = 0.25
    }
}
