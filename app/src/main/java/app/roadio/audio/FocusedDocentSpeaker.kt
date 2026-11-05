package app.roadio.audio

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Holds the car's audio focus while [voice] tells a story, as Pleos Connect asks of every app that plays sound
 * (https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/audio-focus-development).
 * Pleos's Speech Service plays the story without asking for focus itself, so the app does.
 *
 * A call or a Gleo voice command interrupts the story; when it ends, the story is told again from the start if
 * [retellWanted] still wants it. Route guidance plays at the same time as the story (the car's NAVIGATION and MUSIC
 * contexts play together). Another app taking focus for good ends the story. When focus cannot be had at all, the
 * story is shown but not spoken.
 *
 * While Roadio listens for a voice command the story is held ([hold]): it stops so the microphone does not hear it,
 * keeps the focus so no music starts meanwhile, and after the command is told again from the start, or not ([resume]).
 */
class FocusedDocentSpeaker(
    context: Context,
    private val voice: DocentSpeaker,
    private val retellWanted: () -> Boolean = { true },
) : DocentSpeaker {

    private val main = Handler(Looper.getMainLooper())
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(StoryAudioAttributes)
        .setOnAudioFocusChangeListener(::onFocusChange, main)
        .build()

    // A voice that never reports back must not hold up the rest of the drive's stories. It runs only while the voice
    // is speaking, so a long call does not count against it. Seen on the Pleos emulator: right after a Gleo voice
    // command, the Speech Service played a story to its end and never reported it done.
    private val timeout = Runnable {
        Log.w(TAG, "a story did not finish in time: the next ones go on")
        voice.stop()
        finish()
    }

    private var text: String? = null
    private var onDone: (() -> Unit)? = null
    private var interrupted = false
    private var held = false

    override fun speak(text: String, onDone: () -> Unit) {
        voice.stop()
        this.text = text
        this.onDone = onDone
        interrupted = false
        held = false
        if (audioManager.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.i(TAG, "no audio focus (a call or a voice command is on): the story is shown, not spoken")
            finish()
            return
        }
        tell(retelling = false)
    }

    private fun tell(retelling: Boolean) {
        val story = text ?: return
        main.removeCallbacks(timeout)
        // Over twice as long as a story takes to tell (about 0.13 s a character on Pleos Connect), and short enough that
        // the next story is still on its stretch of road. A retold story starts on a voice that is already running, and
        // gets less, so that one interrupted story cannot push the last one off the end of the route.
        val startMs = if (retelling) 0L else TIMEOUT_MS
        main.postDelayed(timeout, startMs + story.length * TIMEOUT_PER_CHAR_MS)
        voice.speak(story) { finish() }
    }

    private fun onFocusChange(change: Int) {
        if (text == null) return
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                interrupted = true
                main.removeCallbacks(timeout)
                voice.stop()
            }
            AudioManager.AUDIOFOCUS_GAIN -> if (interrupted) {
                interrupted = false
                carryOn()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                voice.stop()
                finish()
            }
            // AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: the story keeps playing. Neither speech engine can lower the volume of
            // a story already playing.
        }
    }

    /** Stops the story being told for a moment, keeping it and the focus. */
    fun hold() {
        if (text == null || held) return
        held = true
        main.removeCallbacks(timeout)
        voice.stop()
    }

    /** After [hold]: the story is told again from the start if [retellWanted] still wants it, otherwise it ends. */
    fun resume() {
        if (!held) return
        held = false
        carryOn()
    }

    // A story that waited (for a call, a voice command, or Roadio's own listening) goes on once nothing holds it.
    private fun carryOn() {
        if (held || interrupted || text == null) return
        if (retellWanted()) tell(retelling = true) else finish()
    }

    private fun finish() {
        val done = onDone
        clear()
        done?.invoke()
    }

    override fun stop() {
        clear()
        voice.stop()
    }

    private fun clear() {
        main.removeCallbacks(timeout)
        text = null
        onDone = null
        interrupted = false
        held = false
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    override fun release() {
        stop()
        voice.release()
    }

    private companion object {
        const val TAG = "Roadio"
        const val TIMEOUT_MS = 15_000L
        const val TIMEOUT_PER_CHAR_MS = 200L
    }
}
