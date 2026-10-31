package com.whohoo.roadio

import android.app.Application

class RoadioApp : Application() {

    /** One drive for the whole app: the screen and the media service share it. */
    val engine: DocentEngine by lazy { DocentEngine(this) }
}
