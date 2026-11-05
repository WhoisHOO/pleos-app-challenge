package com.whohoo.roadio.core

import kotlin.math.abs

/** What the car's own meters said at one moment, each when the car reported it: the trip meter in metres, the odometer in km. */
data class CarMeters(val tripM: Double?, val odometerKm: Double?)

/** The end of a drive: how far the car went, for how long the drive was on, and how many of the route's stories were heard. */
data class TripSummary(
    val distanceM: Double,
    /** The distance is the car's own (its trip meter or odometer), not measured along the route. */
    val byCar: Boolean,
    val minutes: Int,
    val played: Int,
    val total: Int,
) {
    companion object {
        /**
         * The car's own distance when its meters moved forward between [start] and [end] (the trip meter first, then the
         * odometer); otherwise the distance along the route, [routeM].
         */
        fun of(routeM: Double, start: CarMeters?, end: CarMeters?, drivenMs: Long, played: Int, total: Int): TripSummary {
            val car = carDistanceM(start, end)
            return TripSummary(car ?: routeM.coerceAtLeast(0.0), car != null, minutesOf(drivenMs), played, total)
        }

        fun carDistanceM(start: CarMeters?, end: CarMeters?): Double? {
            if (start == null || end == null) return null
            val trip = moved(start.tripM, end.tripM)
            val odometer = moved(start.odometerKm, end.odometerKm)?.let { it * 1000.0 }
            // A trip meter that was reset during the drive went back, or started again and passed its first reading:
            // then it disagrees with the odometer, which only goes forward. The odometer may count whole km only.
            if (trip != null && (odometer == null || abs(trip - odometer) <= ODOMETER_STEP_M)) return trip
            return odometer
        }

        private fun moved(from: Double?, to: Double?): Double? =
            if (from != null && to != null && to > from) to - from else null

        private const val ODOMETER_STEP_M = 1_000.0

        /** Whole minutes, rounded, and at least one: a drive that was on is never "0분". */
        fun minutesOf(drivenMs: Long): Int = ((drivenMs.coerceAtLeast(0L) + 30_000L) / 60_000L).toInt().coerceAtLeast(1)
    }
}
