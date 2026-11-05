package app.roadio.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.roadio.RoadioApp
import java.io.File

/**
 * Debug builds only: lets Roadio hear a recorded command instead of the car's microphone, to try voice commands on the
 * emulator, whose microphone may hear nothing. The recording is 16 kHz, 16-bit mono PCM in the app's own files
 * folder (copied there with run-as); see tools/say-command.ps1.
 *
 *   adb shell am broadcast --user 10 -a app.roadio.debug.HEAR --es file command.pcm
 */
class HearRecordingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("file") ?: "command.pcm"
        val file = File(context.filesDir, File(name).name)
        val recording = runCatching { file.readBytes() }.getOrElse {
            Log.w(TAG, "no recording at $file", it)
            return
        }
        Log.i(TAG, "hearing a recording (${recording.size} bytes) instead of the microphone")
        (context.applicationContext as RoadioApp).engine.listenToRecording(recording)
    }

    private companion object {
        const val TAG = "Roadio"
    }
}
