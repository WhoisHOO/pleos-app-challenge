package com.whohoo.roadio.audio

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.OnInitializeListener
import ai.pleos.playground.stt.listener.ResultListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Locale

/**
 * Listens once for a short spoken command through Pleos Connect's Speech Service, with the Pleos SpeechToText SDK on
 * the car's on-device engine in Korean (no network, no app registration). The Speech Service opens the car's
 * microphone itself. [onAvailable] is told once the engine is ready; phones and plain emulators have no Speech Service
 * and never are.
 *
 * Roadio passes on what the driver said and forgets it: it keeps none of it, sends none of it, and its own log lines
 * hold only the command understood. The Pleos SDK itself writes recognition results to the system log (logcat).
 */
class PleosVoiceListener(
    context: Context,
    private val onAvailable: () -> Unit,
) {

    /** How one turn of listening ended. */
    sealed interface Hearing {
        data class Words(val text: String) : Hearing

        /** Nothing was said, or nothing came back in time. */
        data object Silence : Hearing

        /** The car could not listen (its microphone or speech engine). */
        data class Failed(val reason: String) : Hearing
    }

    private enum class State { STARTING, READY, UNAVAILABLE }

    private val main = Handler(Looper.getMainLooper())
    private var speechToText: SpeechToText? = null
    private var state = State.STARTING
    private var onHeard: ((Hearing) -> Unit)? = null
    private var onOpen: (() -> Unit)? = null

    // Events carry no session id: an end left over from the last turn must not end this one. A turn counts as begun
    // once the service reports anything of it; for the car's microphone that is when it has opened.
    private var begun = false
    private var lastWords: String? = null

    // The Speech Service takes a moment to open the microphone (2.6 s the first time on the emulator), and the driver
    // gets the full time to speak only from then on.
    private val openTimeout = Runnable {
        Log.w(TAG, "the car's microphone did not open in time")
        end(Hearing.Failed("not opened"))
    }
    private val listenTimeout = Runnable {
        Log.i(TAG, "nothing heard in time")
        end(heardSoFar())
    }
    // The service reports the end of speech first and the final words a moment later (65 ms on the emulator).
    private val finalWait = Runnable { end(heardSoFar()) }

    private val listener = object : ResultListener {
        override fun onReady() = onMain { begin() }
        override fun onStartedRecognition() = onMain { begin() }
        override fun onUpdatedRms(rms: Double) = onMain { begin() }
        override fun onUpdatedEpdData(on: Long, off: Long) = Unit

        override fun onUpdated(stt: String, completed: Boolean) = onMain {
            begin()
            if (stt.isNotBlank()) lastWords = stt
            if (completed) end(heardSoFar())
        }

        override fun onEndedRecognition() = onMain {
            if (!begun || onHeard == null) return@onMain
            main.removeCallbacks(listenTimeout)
            main.removeCallbacks(finalWait)
            main.postDelayed(finalWait, FINAL_WAIT_MS)
        }

        override fun onError(errorType: ErrorType) = onMain {
            Log.w(TAG, "the car could not listen: $errorType")
            end(
                when (errorType) {
                    ErrorType.TIMEOUT -> Hearing.Silence
                    else -> Hearing.Failed(errorType.toString())
                },
            )
        }
    }

    // Without a Speech Service (a phone, a plain emulator) this fails at once, or never answers: either way the button
    // to speak never shows. There is nothing to fall back to, so a late answer still makes voice commands available.
    init {
        try {
            val stt = SpeechToText(context.applicationContext, Mode.ON_DEVICE)
            speechToText = stt
            stt.addListener(listener)
            stt.initialize(
                object : OnInitializeListener {
                    override fun onSuccess() = onMain { ready() }
                    override fun onFailure(errorType: ErrorType) = onMain { unavailable(errorType.toString()) }
                },
            )
        } catch (e: RuntimeException) {
            unavailable(e.toString())
        } catch (e: LinkageError) {
            unavailable(e.toString())
        }
    }

    private fun ready() {
        val stt = speechToText
        if (state != State.STARTING || stt == null) return
        try {
            stt.setLocale(Locale.KOREA)
        } catch (e: RuntimeException) {
            unavailable(e.toString())
            return
        }
        state = State.READY
        Log.i(TAG, "Pleos STT ready (on-device engine, ko-KR)")
        onAvailable()
    }

    private fun unavailable(reason: String) {
        if (state != State.STARTING) return
        state = State.UNAVAILABLE
        Log.i(TAG, "Pleos STT not available ($reason): no voice commands")
        speechToText?.let { stt ->
            runCatching { stt.removeListener(listener) }
            runCatching { stt.release() }
        }
        speechToText = null
    }

    /**
     * Opens the car's microphone for one command: [opened] is told once it is open, then [heard] how the turn ended,
     * both on the main thread. Returns false, and tells nothing, when the engine is not ready or already listening.
     *
     * With a [recording] (16 kHz, 16-bit mono PCM) the engine hears that instead of the microphone; debug builds use
     * this on the emulator, whose microphone may hear nothing.
     */
    fun listen(opened: () -> Unit, heard: (Hearing) -> Unit, recording: ByteArray? = null): Boolean {
        val stt = speechToText
        if (state != State.READY || stt == null || onHeard != null) return false
        onHeard = heard
        onOpen = opened
        begun = false
        lastWords = null
        main.postDelayed(openTimeout, OPEN_TIMEOUT_MS)
        try {
            if (recording == null) {
                stt.request()
            } else {
                stt.sendAudio(recording)
                stt.completeAudioSend()
            }
        } catch (e: RuntimeException) {
            Log.w(TAG, "the car could not start listening", e)
            end(Hearing.Failed(e.javaClass.simpleName))
        }
        return true
    }

    private fun begin() {
        if (onHeard == null || begun) return
        begun = true
        main.removeCallbacks(openTimeout)
        main.postDelayed(listenTimeout, LISTEN_TIMEOUT_MS)
        onOpen?.let { opened ->
            onOpen = null
            opened()
        }
    }

    /** Stops listening without telling anyone: the driver changed their mind, or the drive was reset. */
    fun cancel() {
        if (onHeard == null) return
        onHeard = null
        onOpen = null
        lastWords = null
        removeTimers()
        closeMicrophone()
    }

    private fun end(hearing: Hearing) {
        val heard = onHeard ?: return
        onHeard = null
        onOpen = null
        lastWords = null
        removeTimers()
        // The engine ends on its own once the driver stops talking; stop() makes sure the microphone is closed.
        closeMicrophone()
        heard(hearing)
    }

    // The final words, or the last partial ones when the final never came.
    private fun heardSoFar(): Hearing = lastWords?.let { Hearing.Words(it) } ?: Hearing.Silence

    private fun removeTimers() {
        main.removeCallbacks(openTimeout)
        main.removeCallbacks(listenTimeout)
        main.removeCallbacks(finalWait)
    }

    private fun closeMicrophone() {
        runCatching { speechToText?.stop() }.onFailure { Log.w(TAG, "the car's microphone did not stop", it) }
    }

    fun release() {
        cancel()
        main.removeCallbacksAndMessages(null)
        speechToText?.let { stt ->
            runCatching { stt.removeListener(listener) }
            runCatching { stt.release() }
        }
        speechToText = null
        state = State.UNAVAILABLE
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
    }

    private companion object {
        const val TAG = "Roadio"
        const val OPEN_TIMEOUT_MS = 6_000L
        // Long enough to say a few words once the microphone is open, short enough that a held story is not held long.
        const val LISTEN_TIMEOUT_MS = 7_000L
        const val FINAL_WAIT_MS = 1_500L
    }
}
