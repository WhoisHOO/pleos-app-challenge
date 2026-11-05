package app.roadio

import ai.pleos.playground.location.fused.FusedLocation
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import app.roadio.location.VehicleLocation
import app.roadio.media.RoadioMediaService
import app.roadio.ui.RoadioScreen
import app.roadio.ui.RoadioTheme

class MainActivity : ComponentActivity() {

    private val engine get() = (application as RoadioApp).engine
    private var session: ListenableFuture<MediaController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoadioTheme {
                val state by engine.state.collectAsStateWithLifecycle()
                val askLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    engine.locationPermissionResult(granted)
                }
                // The permission dialog is setup too: it waits while the car forbids setup.
                LaunchedEffect(state.locationStatus, state.setupLimited) {
                    when (state.locationStatus) {
                        LocationStatus.NEEDS_PERMISSION -> if (!state.setupLimited) askLocation.launch(VehicleLocation.PERMISSION)
                        // The car's own notice that location is off in its privacy settings; it needs an activity.
                        LocationStatus.DISABLED -> runCatching {
                            FusedLocation(this@MainActivity).showLocationAccessBlockedPopup()
                        }.onFailure { Log.w(TAG, "location blocked notice not shown", it) }
                        else -> Unit
                    }
                }
                RoadioScreen(
                    state = state,
                    onToggleDriving = engine::toggleDriving,
                    onToggleStory = engine::toggleStory,
                    onRestart = engine::restart,
                    onToggleMode = engine::toggleMode,
                    onLocationConsent = engine::answerLocationConsent,
                    onGuide = engine::guideAlongStories,
                    onVoice = engine::toggleListening,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Connecting to the media session brings up the media service, which keeps a drive going behind other apps.
        session = MediaController.Builder(this, SessionToken(this, ComponentName(this, RoadioMediaService::class.java)))
            .buildAsync()
        engine.screenShown()
    }

    override fun onStop() {
        session?.let(MediaController::releaseFuture)
        session = null
        engine.screenHidden()
        super.onStop()
    }

    private companion object {
        const val TAG = "Roadio"
    }
}
