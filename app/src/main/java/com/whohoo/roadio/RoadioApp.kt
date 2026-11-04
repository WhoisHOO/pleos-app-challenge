package com.whohoo.roadio

import android.app.Application
import com.whohoo.roadio.data.RoadioRoute
import com.whohoo.roadio.data.RouteStore

class RoadioApp : Application() {

    /** The content packaged in `assets/route.json`, read once. */
    val route: RoadioRoute by lazy { RouteStore.load(this) }

    /** One drive for the whole app: the screen and the media service share it. */
    val engine: DocentEngine by lazy { DocentEngine(this, route) }
}
