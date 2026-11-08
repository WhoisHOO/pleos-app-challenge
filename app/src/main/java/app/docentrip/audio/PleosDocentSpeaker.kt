package app.docentrip.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import java.util.Locale

/**
 * Speaks through Pleos Connect's Speech Service with the Pleos TextToSpeech SDK, on the car's on-device engine (no
 * network, no app registration). Pleos Connect has no standard Android TTS engine, so [AndroidDocentSpeaker] cannot
 * speak there. Where the Pleos service is missing (a phone, a plain emulator), this speaker hands over to [fallback].
 */
class PleosDocentSpeaker(
    context: Context,
    private val fallback: () -> DocentSpeaker,
) : DocentSpeaker {

    private enum class State { STARTING, READY, HANDED_OVER }

    private val main = Handler(Looper.getMainLooper())
    private var textToSpeech: TextToSpeech? = null
    private var state = State.STARTING
    private var other: DocentSpeaker? = null
    private var pendingText: String? = null
    private var pendingOnDone: (() -> Unit)? = null
    private var completion: (() -> Unit)? = null

    // Events carry no utterance id: after a story replaces another, the old one's stop or done can still arrive. They
    // are let pass until the new story starts.
    private var waitingForStart = false

    private val listener = object : EventListener {
        override fun onReady() = Unit
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onStart() = onMain { waitingForStart = false }
        override fun onDone() = onMain { if (!waitingForStart) finish() }
        override fun onStop() = onMain { if (!waitingForStart) finish() }
        override fun onError(errorType: ErrorType) = onMain {
            Log.w(TAG, "Pleos TTS could not speak a story: $errorType")
            finish()
        }
    }

    init {
        // No answer from the Speech Service at all: not a Pleos car.
        main.postDelayed({ if (state == State.STARTING) handOver("no answer") }, START_TIMEOUT_MS)
        try {
            val tts = TextToSpeech(context.applicationContext, Mode.ON_DEVICE)
            textToSpeech = tts
            tts.initialize(
                object : OnInitializeListener {
                    override fun onSuccess() = onMain { ready() }
                    override fun onFailure(errorType: ErrorType) = onMain { handOver(errorType.toString()) }
                },
            )
        } catch (e: RuntimeException) {
            handOver(e.toString())
        }
    }

    private fun ready() {
        val tts = textToSpeech
        if (state != State.STARTING || tts == null) return
        tts.addEventListener(listener)
        tts.setLocale(Locale.KOREA)
        tts.setAudioAttributes(StoryAudioAttributes)
        state = State.READY
        Log.i(TAG, "Pleos TTS ready (on-device engine)")
        takePending()?.let { (text, onDone) -> speak(text, onDone) }
    }

    private fun handOver(reason: String) {
        if (state != State.STARTING) return
        state = State.HANDED_OVER
        Log.i(TAG, "Pleos TTS not available ($reason): using the device's TTS")
        textToSpeech?.let { runCatching { it.release() } }
        textToSpeech = null
        val speaker = fallback()
        other = speaker
        takePending()?.let { (text, onDone) -> speaker.speak(text, onDone) }
    }

    override fun speak(text: String, onDone: () -> Unit) {
        when (state) {
            // A story still waiting for the engine is replaced by the newer one.
            State.STARTING -> {
                pendingText = text
                pendingOnDone = onDone
            }
            State.HANDED_OVER -> other?.speak(text, onDone)
            State.READY -> {
                val tts = textToSpeech ?: return
                completion = null
                tts.stop()
                completion = onDone
                waitingForStart = true
                try {
                    tts.speak(text, true)
                } catch (e: RuntimeException) {
                    Log.w(TAG, "Pleos TTS could not take a story: $e")
                    finish()
                }
            }
        }
    }

    override fun stop() {
        takePending()
        completion = null
        when (state) {
            State.READY -> textToSpeech?.stop()
            State.HANDED_OVER -> other?.stop()
            State.STARTING -> Unit
        }
    }

    override fun release() {
        stop()
        main.removeCallbacksAndMessages(null)
        textToSpeech?.let {
            if (state == State.READY) it.removeEventListener(listener)
            runCatching { it.release() }
        }
        textToSpeech = null
        other?.release()
        other = null
        state = State.HANDED_OVER
    }

    private fun finish() {
        waitingForStart = false
        val done = completion ?: return
        completion = null
        done()
    }

    private fun takePending(): Pair<String, () -> Unit>? {
        val text = pendingText ?: return null
        val onDone = pendingOnDone ?: return null
        pendingText = null
        pendingOnDone = null
        return text to onDone
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
    }

    private companion object {
        const val TAG = "Docentrip"
        const val START_TIMEOUT_MS = 3_000L
    }
}
