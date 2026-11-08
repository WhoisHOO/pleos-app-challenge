package app.docentrip.core

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

const val EARTH_RADIUS_M: Double = 6_371_000.0

data class GeoPoint(val lat: Double, val lon: Double)

fun GeoPoint.distanceTo(other: GeoPoint): Double {
    val dLat = (other.lat - lat).toRadians()
    val dLon = (other.lon - lon).toRadians()
    val sinLat = sin(dLat / 2.0)
    val sinLon = sin(dLon / 2.0)
    val a = sinLat * sinLat +
        cos(lat.toRadians()) * cos(other.lat.toRadians()) * sinLon * sinLon
    return 2.0 * EARTH_RADIUS_M * atan2(sqrt(a), sqrt((1.0 - a).coerceAtLeast(0.0)))
}

internal data class SegmentProjection(val t: Double, val lateralM: Double)

internal fun projectOnSegment(point: GeoPoint, a: GeoPoint, b: GeoPoint): SegmentProjection {
    val lat0 = (a.lat + b.lat) / 2.0
    val metersPerDegLat = 111_320.0
    val metersPerDegLon = 111_320.0 * cos(lat0.toRadians())

    val bx = (b.lon - a.lon) * metersPerDegLon
    val by = (b.lat - a.lat) * metersPerDegLat
    val px = (point.lon - a.lon) * metersPerDegLon
    val py = (point.lat - a.lat) * metersPerDegLat

    val lengthSquared = bx * bx + by * by
    val t = if (lengthSquared == 0.0) 0.0 else ((px * bx + py * by) / lengthSquared).coerceIn(0.0, 1.0)
    val dx = px - bx * t
    val dy = py - by * t
    return SegmentProjection(t = t, lateralM = sqrt(dx * dx + dy * dy))
}

private fun Double.toRadians(): Double = this * PI / 180.0
