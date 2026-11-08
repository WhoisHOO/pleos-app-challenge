package app.docentrip.core

/**
 * Drops position fixes that the car cannot have reached since the last accepted fix, such as a GPS jump across the
 * city. Small jitter is left to [SpeedEstimator]; this gate only rejects physically impossible moves, so a bad fix
 * cannot move the route projection or trigger a story ahead of the car.
 */
class FixGate(
    private val maxSpeedMps: Double = 60.0,
    private val toleranceM: Double = 40.0,
    private val maxGapMillis: Long = 10_000L,
) {

    private var lastPoint: GeoPoint? = null
    private var lastMillis: Long = 0L

    fun accept(point: GeoPoint, timeMillis: Long): Boolean {
        val previous = lastPoint
        if (previous == null) {
            remember(point, timeMillis)
            return true
        }
        if (timeMillis <= lastMillis) return false
        if (timeMillis - lastMillis > maxGapMillis) {
            // The car may have gone anywhere while no fixes were coming in.
            remember(point, timeMillis)
            return true
        }

        val seconds = (timeMillis - lastMillis) / 1000.0
        val movedM = previous.distanceTo(point)
        if (movedM > toleranceM + maxSpeedMps * seconds) return false

        remember(point, timeMillis)
        return true
    }

    fun reset() {
        lastPoint = null
        lastMillis = 0L
    }

    private fun remember(point: GeoPoint, timeMillis: Long) {
        lastPoint = point
        lastMillis = timeMillis
    }
}
