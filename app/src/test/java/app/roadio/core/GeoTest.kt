package app.roadio.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTest {

    @Test
    fun `distance between city hall and gwanghwamun is about one kilometer`() {
        val cityHall = GeoPoint(37.5663, 126.9779)
        val gwanghwamun = GeoPoint(37.5759, 126.9769)

        val distance = cityHall.distanceTo(gwanghwamun)

        assertEquals(1_070.0, distance, 80.0)
    }

    @Test
    fun `projection on a straight route returns zero lateral distance`() {
        val path = straightRoute()
        val onRoute = path.pointAt(420.0)

        val projection = path.project(onRoute)

        assertEquals(420.0, projection.distanceAlongM, 1.0)
        assertTrue(projection.lateralM < 0.5)
    }

    @Test
    fun `projection reports lateral distance for an off route point`() {
        val path = straightRoute()
        val offRoute = GeoPoint(37.505, 127.01)

        val projection = path.project(offRoute)

        assertTrue(projection.lateralM > 800.0)
    }

    @Test
    fun `route point at distance is clamped to the route`() {
        val path = straightRoute()

        val start = path.pointAt(-100.0)
        val end = path.pointAt(path.totalDistanceM + 100.0)

        assertEquals(path.points.first().lat, start.lat, 0.000001)
        assertEquals(path.points.last().lat, end.lat, 0.000001)
    }

    companion object {
        fun straightRoute(): RoutePath = RoutePath(
            (0..10).map { index -> GeoPoint(37.5 + index * 0.001, 127.0) },
        )
    }
}
