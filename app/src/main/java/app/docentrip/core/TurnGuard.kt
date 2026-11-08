package app.docentrip.core

/**
 * Keeps a new story from starting while the car closes in on a turn of its navigation route, so that the story does
 * not talk over the turn's guidance or take the driver's attention just then. A story already being told goes on.
 *
 * It holds only while the turn is near and getting nearer, and never longer than [maxHoldMillis] for one turn, so that
 * a navigation app that stops updating (or a car standing at a junction) cannot hold the stories back for good.
 */
class TurnGuard(
    private val nearM: Double = 150.0,
    private val approachWindowMillis: Long = 5_000L,
    private val maxHoldMillis: Long = 20_000L,
) {

    private var lastDistanceM: Double? = null
    private var lastCloserAtMillis: Long? = null
    private var holdStartedAtMillis: Long? = null

    /** The distance to the next turn reported at [nowMillis], or null when there is no route guidance. */
    fun update(distanceM: Double?, nowMillis: Long) {
        val last = lastDistanceM
        lastDistanceM = distanceM
        when {
            distanceM == null -> reset()
            // A new turn: the next one is farther away than the one just passed.
            last != null && distanceM > last + NEW_TURN_JUMP_M -> {
                lastCloserAtMillis = null
                holdStartedAtMillis = null
            }
            last != null && distanceM < last -> lastCloserAtMillis = nowMillis
        }
    }

    fun holding(nowMillis: Long): Boolean {
        val distance = lastDistanceM ?: return false
        val closerAt = lastCloserAtMillis ?: return false
        if (distance > nearM || nowMillis - closerAt > approachWindowMillis) return false
        val started = holdStartedAtMillis ?: nowMillis.also { holdStartedAtMillis = it }
        return nowMillis - started < maxHoldMillis
    }

    fun reset() {
        lastDistanceM = null
        lastCloserAtMillis = null
        holdStartedAtMillis = null
    }

    private companion object {
        const val NEW_TURN_JUMP_M = 30.0
    }
}
