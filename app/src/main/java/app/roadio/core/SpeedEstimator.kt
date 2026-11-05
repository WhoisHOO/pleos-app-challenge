package app.roadio.core

/**
 * Speed from consecutive position fixes, for a location source that reports none (Pleos FusedLocation gives only a
 * position about once a second). Averages over the last few fixes; small steps count as standing still, so that the
 * jitter of a parked car does not show as speed.
 */
class SpeedEstimator(
    private val fixes: Int = 4,
    private val minStepM: Double = 3.0,
    private val maxGapMillis: Long = 5_000L,
) {

    private data class Fix(val point: GeoPoint, val timeMillis: Long)

    private val recent = ArrayDeque<Fix>()

    /** Adds a fix taken at [timeMillis] and returns the speed in metres per second. A clock going back starts over. */
    fun update(point: GeoPoint, timeMillis: Long): Double {
        val last = recent.lastOrNull()
        if (last != null && (timeMillis <= last.timeMillis || timeMillis - last.timeMillis > maxGapMillis)) recent.clear()
        recent.addLast(Fix(point, timeMillis))
        while (recent.size > fixes) recent.removeFirst()
        if (recent.size < 2) return 0.0

        val steps = recent.zipWithNext { a, b -> a.point.distanceTo(b.point) }
        val distanceM = steps.filter { it >= minStepM }.sum()
        val seconds = (recent.last().timeMillis - recent.first().timeMillis) / 1000.0
        return if (seconds > 0.0) distanceM / seconds else 0.0
    }

    fun reset() {
        recent.clear()
    }
}
