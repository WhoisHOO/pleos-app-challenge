package app.docentrip

import android.app.Application
import app.docentrip.data.DocentripRoute
import app.docentrip.data.RouteStore

class DocentripApp : Application() {

    /** The content packaged in `assets/route.json`, read once. */
    val route: DocentripRoute by lazy { RouteStore.load(this) }

    /** One drive for the whole app: the screen and the media service share it. */
    val engine: DocentEngine by lazy { DocentEngine(this, route) }
}
