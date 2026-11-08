package app.docentrip.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedEstimatorTest {

    private val path = GeoTest.straightRoute()

    @Test
    fun `steady driving gives its speed`() {
        val estimator = SpeedEstimator()

        var speed = 0.0
        for (second in 0..5) speed = estimator.update(path.pointAt(second * 13.9), second * 1_000L)

        assertEquals(13.9, speed, 0.3)
    }

    @Test
    fun `a parked car's jitter is no speed`() {
        val estimator = SpeedEstimator()

        var speed = 0.0
        for (second in 0..5) speed = estimator.update(path.pointAt(100.0 + (second % 2) * 1.5), second * 1_000L)

        assertEquals(0.0, speed, 0.01)
    }

    @Test
    fun `a long gap starts over`() {
        val estimator = SpeedEstimator()
        estimator.update(path.pointAt(0.0), 0L)

        val speed = estimator.update(path.pointAt(500.0), 60_000L)

        assertEquals(0.0, speed, 0.01)
    }
}
