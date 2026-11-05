package app.roadio.navigation

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.constants.WaypointIndex
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.RequestWaypointInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteWithWaypointsRequest
import ai.pleos.playground.navi.data.TBTInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import app.roadio.core.GeoPoint

/**
 * Roadio and the car's navigation app, through Pleos NaviHelper: the story route goes to the navigation as a
 * destination with waypoints, and the distance to the next turn comes back while it guides, so that no story starts
 * just before a turn. The navigation does not share the shape of its route, so Roadio keeps its own story route.
 * Cars without navigation, and phones, never become [available]. Callbacks come on the main thread.
 */
class NavigationLink(
    context: Context,
    private val onAvailable: () -> Unit,
    private val onNextTurn: (distanceM: Double?) -> Unit,
    private val onGuidance: (Guidance) -> Unit,
) {

    /** Where a request to guide along the story route stands. */
    enum class Guidance { STARTED, FAILED, ENDED }

    /** A place on the route, as the navigation is given it. */
    data class Place(val point: GeoPoint, val name: String, val address: String)

    private val main = Handler(Looper.getMainLooper())
    private val helper = NaviHelper(context.applicationContext)
    private var guiding = false

    var available = false
        private set

    // The navigation reports the next turn when it changes; while it guides, Roadio also asks every few seconds.
    private val poll = object : Runnable {
        override fun run() {
            if (!guiding) return
            runCatching { helper.getTBTInfo() }
            main.postDelayed(this, POLL_MS)
        }
    }

    private val listener = object : NaviHelperEventListener {
        override fun onRouteStarted(routeStartInfo: RouteStartInfo) = onMain { onGuidance(Guidance.STARTED) }

        override fun onNavigationStatus(navigationStatus: NavigationStatus) = onMain {
            guiding = navigationStatus == NavigationStatus.ROUTE_DRIVE
            main.removeCallbacks(poll)
            if (guiding) poll.run() else onNextTurn(null)
        }

        override fun onTBTInfo(tbtInfo: List<TBTInfo>) = onMain {
            if (guiding) onNextTurn(tbtInfo.firstOrNull()?.distance)
        }

        override fun onRouteCancelled() = onMain { ended() }

        override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) = onMain { ended() }

        override fun onError(errorCode: NaviErrorCode) = onMain {
            Log.w(TAG, "navigation: $errorCode")
            onGuidance(Guidance.FAILED)
        }
    }

    fun connect() {
        try {
            helper.initialize(
                {
                    onMain {
                        available = true
                        helper.addListener(listener)
                        onAvailable()
                    }
                },
                { error -> Log.i(TAG, "no navigation to work with: $error") },
            )
        } catch (e: RuntimeException) {
            Log.i(TAG, "no navigation to work with", e)
        }
    }

    /** Asks the navigation to guide to [destination] by way of [waypoints] (the first two are used). */
    fun guide(destination: Place, waypoints: List<Place>) {
        if (!available) return
        val indices = listOf(WaypointIndex.FIRST, WaypointIndex.SECOND)
        try {
            helper.requestRouteWithWaypoints(
                RouteWithWaypointsRequest(
                    destination = RouteInfo(
                        latitude = destination.point.lat,
                        longitude = destination.point.lon,
                        poiName = destination.name,
                        // Plain coordinates, not a place from the navigation's own database.
                        poiId = "",
                        address = destination.address,
                        poiSubId = "0",
                    ),
                    waypoints = waypoints.take(indices.size).mapIndexed { i, place ->
                        RequestWaypointInfo(
                            latitude = place.point.lat,
                            longitude = place.point.lon,
                            poiName = place.name,
                            waypointIndex = indices[i],
                            poiId = "",
                            address = place.address,
                            poiSubId = "0",
                        )
                    },
                ),
            )
        } catch (e: RuntimeException) {
            Log.w(TAG, "navigation refused the story route", e)
            onGuidance(Guidance.FAILED)
        }
    }

    private fun ended() {
        guiding = false
        main.removeCallbacks(poll)
        onNextTurn(null)
        onGuidance(Guidance.ENDED)
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
    }

    private companion object {
        const val TAG = "Roadio"
        const val POLL_MS = 2_000L
    }
}
