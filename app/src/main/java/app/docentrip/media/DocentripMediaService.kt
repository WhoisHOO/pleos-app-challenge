package app.docentrip.media

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import app.docentrip.MainActivity
import app.docentrip.DocentripApp

/**
 * Docentrip's media session for the car (its media centre, steering-wheel keys) and the foreground service that keeps the
 * drive and its stories going while another app, such as navigation, is in front. Media3 holds the service in the
 * foreground while the drive is on, and for up to ten minutes after it stops.
 */
@OptIn(UnstableApi::class)
class DocentripMediaService : MediaLibraryService() {

    private var session: MediaLibrarySession? = null

    private val engine get() = (application as DocentripApp).engine

    override fun onCreate() {
        super.onCreate()
        val player = DocentPlayer(engine)
        val library = StoryLibrary(engine.stories) { engine.tellStory(it) }
        session = MediaLibrarySession.Builder(this, player, library)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            .build()
            // Registered at once, so that a drive started from Docentrip's screen takes the service into the foreground
            // even when nothing else is bound.
            .also(::addSession)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = session

    // The driver closed Docentrip: the drive ends with it.
    override fun onTaskRemoved(rootIntent: Intent?) {
        engine.pause(stopStory = true)
        engine.screenHidden()
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}
