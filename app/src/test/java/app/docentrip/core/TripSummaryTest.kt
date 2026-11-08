package app.docentrip.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripSummaryTest {

    @Test
    fun theCarsTripMeterWinsWhenItMoved() {
        val summary = TripSummary.of(
            routeM = 4_000.0,
            start = CarMeters(tripM = 1_200.0, odometerKm = 500.0),
            end = CarMeters(tripM = 5_450.0, odometerKm = 504.0),
            drivenMs = 5 * 60_000L,
            played = 7,
            total = 7,
        )
        assertEquals(4_250.0, summary.distanceM, 0.001)
        assertTrue(summary.byCar)
        assertEquals(5, summary.minutes)
    }

    @Test
    fun aResetTripMeterFallsBackToTheOdometer() {
        val distance = TripSummary.carDistanceM(
            CarMeters(tripM = 9_000.0, odometerKm = 500.0),
            CarMeters(tripM = 300.0, odometerKm = 504.0),
        )
        assertEquals(4_000.0, distance!!, 0.001)
    }

    @Test
    fun aTripMeterResetThatPassedItsFirstReadingLosesToTheOdometer() {
        // Reset mid-drive at 3 km, then 8 km more: the trip meter reads past where it started, the odometer 11 km on.
        val distance = TripSummary.carDistanceM(
            CarMeters(tripM = 3_000.0, odometerKm = 500.0),
            CarMeters(tripM = 8_000.0, odometerKm = 511.0),
        )
        assertEquals(11_000.0, distance!!, 0.001)
    }

    @Test
    fun theTripMeterIsKeptWhenTheOdometerOnlyCountsWholeKm() {
        val distance = TripSummary.carDistanceM(
            CarMeters(tripM = 0.0, odometerKm = 500.0),
            CarMeters(tripM = 4_250.0, odometerKm = 504.0),
        )
        assertEquals(4_250.0, distance!!, 0.001)
        // The odometer did not move on a short drive: the trip meter alone.
        assertEquals(600.0, TripSummary.carDistanceM(CarMeters(0.0, 500.0), CarMeters(600.0, 500.0))!!, 0.001)
    }

    @Test
    fun withoutTheCarsMetersTheRouteIsUsed() {
        val summary = TripSummary.of(4_004.0, start = null, end = null, drivenMs = 288_000L, played = 7, total = 7)
        assertEquals(4_004.0, summary.distanceM, 0.001)
        assertFalse(summary.byCar)
    }

    @Test
    fun metersThatDidNotMoveAreNotTheCarsDistance() {
        // The emulator's odometer only changes when set by hand: an unchanged meter is no measurement.
        assertNull(TripSummary.carDistanceM(CarMeters(null, 500.0), CarMeters(null, 500.0)))
        assertNull(TripSummary.carDistanceM(CarMeters(null, null), CarMeters(1_000.0, null)))
        val summary = TripSummary.of(3_500.0, CarMeters(null, 500.0), CarMeters(null, 500.0), 60_000L, 5, 7)
        assertEquals(3_500.0, summary.distanceM, 0.001)
        assertFalse(summary.byCar)
    }

    @Test
    fun minutesAreRoundedAndNeverZero() {
        assertEquals(1, TripSummary.minutesOf(0L))
        assertEquals(1, TripSummary.minutesOf(80_000L))
        assertEquals(5, TripSummary.minutesOf(288_000L))
        assertEquals(1, TripSummary.minutesOf(-5L))
    }
}
