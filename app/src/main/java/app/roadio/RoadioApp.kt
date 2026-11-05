package app.roadio

import android.app.Application
import app.roadio.data.RoadioRoute
import app.roadio.data.RouteStore

class RoadioApp : Application() {

    /** The content packaged in `assets/route.json`, read once. */
    val route: RoadioRoute by lazy { RouteStore.load(this) }

    /** One drive for the whole app: the screen and the media service share it. */
    val engine: DocentEngine by lazy { DocentEngine(this, route) }
}
