package app.roadio.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FixGateTest {

    private val gate = FixGate(maxSpeedMps = 60.0, toleranceM = 40.0, maxGapMillis = 10_000L)

    @Test
    fun `first fix is accepted`() {
        assertTrue(gate.accept(point(0.0), 1_000L))
    }

    @Test
    fun `a fix the car could have reached is accepted`() {
        gate.accept(point(0.0), 1_000L)

        assertTrue(gate.accept(point(11.0), 2_000L))
    }

    @Test
    fun `an impossible jump is rejected and does not move the baseline`() {
        gate.accept(point(0.0), 1_000L)

        assertFalse(gate.accept(point(1_200.0), 2_000L))
        assertTrue(gate.accept(point(30.0), 3_000L))
    }

    @Test
    fun `a jump after a long gap is accepted`() {
        gate.accept(point(0.0), 1_000L)

        assertTrue(gate.accept(point(1_200.0), 20_000L))
    }

    @Test
    fun `an out of order fix is rejected`() {
        gate.accept(point(0.0), 1_000L)

        assertFalse(gate.accept(point(11.0), 1_000L))
    }

    @Test
    fun `reset accepts a far first fix again`() {
        gate.accept(point(0.0), 1_000L)

        gate.reset()

        assertTrue(gate.accept(point(1_200.0), 2_000L))
    }

    private fun point(metersNorth: Double): GeoPoint =
        GeoPoint(37.5 + metersNorth / 111_320.0, 127.0)
}
