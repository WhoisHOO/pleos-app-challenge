package com.whohoo.roadio.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID

interface DocentSpeaker {
    fun speak(text: String, onDone: () -> Unit)
    fun stop()
    fun release()
}

/**
 * Stories are spoken content, not route guidance: they belong to the car's media context, where route guidance plays
 * at the same time and a call or a Gleo voice command takes their place (see [FocusedDocentSpeaker]).
 */
internal val StoryAudioAttributes: AudioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_MEDIA)
    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
    .build()

/** Speaks with the device's TTS engine, which phones and plain emulators have and Pleos Connect does not. */
class AndroidDocentSpeaker(context: Context) : DocentSpeaker, TextToSpeech.OnInitListener {

    private val main = Handler(Looper.getMainLooper())
    private val textToSpeech = TextToSpeech(context.applicationContext, this)

    private var initialized = false
    private var languageSupported = false
    private var pendingText: String? = null
    private var pendingOnDone: (() -> Unit)? = null
    private var runningUtteranceId: String? = null
    private var completion: (() -> Unit)? = null

    // Called on the main thread; the progress callbacks below arrive on the engine's threads and are moved there.
    override fun onInit(status: Int) {
        initialized = true
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "TTS engine failed to start (status $status): stories are shown as text only")
            completePending()
            return
        }

        val languageResult = textToSpeech.setLanguage(Locale.KOREAN)
        languageSupported = languageResult != TextToSpeech.LANG_MISSING_DATA &&
            languageResult != TextToSpeech.LANG_NOT_SUPPORTED
        Log.i(TAG, "TTS engine ${textToSpeech.defaultEngine}: Korean ${if (languageSupported) "available" else "not available ($languageResult)"}")
        textToSpeech.setAudioAttributes(StoryAudioAttributes)
        textToSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    main.post { if (utteranceId == runningUtteranceId) finishUtterance() }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    main.post {
                        if (utteranceId == runningUtteranceId) {
                            Log.w(TAG, "TTS could not speak a story")
                            finishUtterance()
                        }
                    }
                }
            },
        )

        val text = pendingText
        val callback = pendingOnDone
        pendingText = null
        pendingOnDone = null
        if (text != null && callback != null) speak(text, callback)
    }

    override fun speak(text: String, onDone: () -> Unit) {
        if (!initialized) {
            pendingText = text
            pendingOnDone = onDone
            return
        }
        if (!languageSupported) {
            onDone()
            return
        }

        stopInternal()
        completion = onDone
        val utteranceId = UUID.randomUUID().toString()
        runningUtteranceId = utteranceId
        val result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            Log.w(TAG, "TTS did not take a story")
            finishUtterance()
        }
    }

    override fun stop() {
        pendingText = null
        pendingOnDone = null
        completion = null
        stopInternal()
    }

    private fun completePending() {
        val callback = pendingOnDone ?: return
        pendingText = null
        pendingOnDone = null
        callback.invoke()
    }

    private fun stopInternal() {
        runningUtteranceId = null
        if (initialized) textToSpeech.stop()
    }

    private fun finishUtterance() {
        runningUtteranceId = null
        val done = completion ?: return
        completion = null
        done()
    }

    override fun release() {
        stop()
        main.removeCallbacksAndMessages(null)
        textToSpeech.shutdown()
    }

    private companion object {
        const val TAG = "Roadio"
    }
}
