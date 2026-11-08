package app.docentrip.drive

import app.docentrip.core.GeoTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedRouteDriverTest {

    @Test
    fun `advance moves the driver by speed times time`() {
        val driver = SimulatedRouteDriver(GeoTest.straightRoute(), speedMps = 10.0)

        driver.advance(10.0)

        assertEquals(100.0, driver.distanceM, 0.001)
    }

    @Test
    fun `driver stops at the end of the route`() {
        val path = GeoTest.straightRoute()
        val driver = SimulatedRouteDriver(path, speedMps = 10.0)

        repeat(1_000) { driver.advance(10.0) }

        assertTrue(driver.finished)
        assertEquals(path.totalDistanceM, driver.distanceM, 0.001)
    }

    @Test
    fun `reset returns to the start`() {
        val driver = SimulatedRouteDriver(GeoTest.straightRoute(), speedMps = 10.0)
        driver.advance(10.0)

        driver.reset()

        assertEquals(0.0, driver.distanceM, 0.001)
        assertFalse(driver.finished)
    }
}
