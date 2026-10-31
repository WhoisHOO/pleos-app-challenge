package com.whohoo.roadio.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TurnGuardTest {

    @Test
    fun `holds while the car closes in on a near turn`() {
        val guard = TurnGuard()
        guard.update(300.0, 0L)
        guard.update(140.0, 1_000L)

        assertTrue(guard.holding(1_000L))
    }

    @Test
    fun `does not hold for a turn still far away`() {
        val guard = TurnGuard()
        guard.update(500.0, 0L)
        guard.update(400.0, 1_000L)

        assertFalse(guard.holding(1_000L))
    }

    @Test
    fun `does not hold when the distance does not change`() {
        val guard = TurnGuard()
        guard.update(80.0, 0L)
        guard.update(80.0, 1_000L)

        assertFalse(guard.holding(1_000L))
    }

    @Test
    fun `holds no longer than the limit for one turn`() {
        val guard = TurnGuard(maxHoldMillis = 20_000L)
        guard.update(150.0, 0L)
        val held = (1..30).map { second ->
            guard.update(150.0 - second * 4.0, second * 1_000L)
            guard.holding(second * 1_000L)
        }

        assertTrue(held.take(20).all { it })
        assertFalse(held.drop(20).any { it })
    }

    @Test
    fun `the next turn starts over`() {
        val guard = TurnGuard()
        guard.update(120.0, 0L)
        guard.update(60.0, 1_000L)
        assertTrue(guard.holding(1_000L))

        guard.update(600.0, 2_000L)
        assertFalse(guard.holding(2_000L))

        guard.update(140.0, 40_000L)
        guard.update(100.0, 41_000L)
        assertTrue(guard.holding(41_000L))
    }

    @Test
    fun `no guidance, no hold`() {
        val guard = TurnGuard()
        guard.update(120.0, 0L)
        guard.update(60.0, 1_000L)
        guard.update(null, 2_000L)

        assertFalse(guard.holding(2_000L))
    }
}
