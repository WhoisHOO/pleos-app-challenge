package app.docentrip.core

data class RouteSegment(
    val start: GeoPoint,
    val end: GeoPoint,
    val startDistanceM: Double,
    val lengthM: Double,
)

data class RouteProjection(
    val position: GeoPoint,
    val distanceAlongM: Double,
    val lateralM: Double,
    val segmentIndex: Int,
    val progress: Double,
)

class RoutePath(val points: List<GeoPoint>) {

    init {
        require(points.size >= 2) { "A route needs at least two points." }
    }

    val segments: List<RouteSegment> = buildList {
        var accumulated = 0.0
        for (index in 0 until points.lastIndex) {
            val start = points[index]
            val end = points[index + 1]
            val length = start.distanceTo(end)
            add(RouteSegment(start, end, accumulated, length))
            accumulated += length
        }
    }

    val totalDistanceM: Double = segments.sumOf { it.lengthM }

    fun project(position: GeoPoint): RouteProjection {
        var best: RouteProjection? = null
        for ((index, segment) in segments.withIndex()) {
            val projection = projectOnSegment(position, segment.start, segment.end)
            val distanceAlong = segment.startDistanceM + projection.t * segment.lengthM
            val candidate = RouteProjection(
                position = position,
                distanceAlongM = distanceAlong,
                lateralM = projection.lateralM,
                segmentIndex = index,
                progress = if (totalDistanceM == 0.0) 0.0 else (distanceAlong / totalDistanceM).coerceIn(0.0, 1.0),
            )
            if (best == null || candidate.lateralM < best.lateralM) {
                best = candidate
            }
        }
        return requireNotNull(best)
    }

    fun pointAt(distanceM: Double): GeoPoint {
        val clamped = distanceM.coerceIn(0.0, totalDistanceM)
        val segment = segments.firstOrNull { clamped <= it.startDistanceM + it.lengthM } ?: segments.last()
        val t = if (segment.lengthM == 0.0) {
            0.0
        } else {
            ((clamped - segment.startDistanceM) / segment.lengthM).coerceIn(0.0, 1.0)
        }
        return GeoPoint(
            lat = segment.start.lat + (segment.end.lat - segment.start.lat) * t,
            lon = segment.start.lon + (segment.end.lon - segment.start.lon) * t,
        )
    }
}
