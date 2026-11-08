package app.docentrip.drive

import app.docentrip.core.GeoPoint
import app.docentrip.core.RoutePath

class SimulatedRouteDriver(
    private val path: RoutePath,
    val speedMps: Double = 13.9,
) {

    var distanceM: Double = 0.0
        private set

    val finished: Boolean
        get() = distanceM >= path.totalDistanceM

    fun advance(deltaSeconds: Double): GeoPoint {
        distanceM = (distanceM + speedMps * deltaSeconds).coerceAtMost(path.totalDistanceM)
        return path.pointAt(distanceM)
    }

    fun seek(distanceM: Double) {
        this.distanceM = distanceM.coerceIn(0.0, path.totalDistanceM)
    }

    fun reset() {
        distanceM = 0.0
    }
}
