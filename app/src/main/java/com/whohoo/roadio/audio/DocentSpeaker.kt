package com.whohoo.roadio.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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

class AndroidDocentSpeaker(context: Context) : DocentSpeaker, TextToSpeech.OnInitListener {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val textToSpeech = TextToSpeech(context.applicationContext, this)
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
        .setOnAudioFocusChangeListener { }
        .build()

    private var initialized = false
    private var languageSupported = false
    private var pendingText: String? = null
    private var pendingOnDone: (() -> Unit)? = null
    private var runningUtteranceId: String? = null
    private var completion: (() -> Unit)? = null

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
        textToSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == runningUtteranceId) finishUtterance()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Log.w(TAG, "TTS could not speak a story")
                    if (utteranceId == runningUtteranceId) finishUtterance()
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

        stopInternal(abandonFocus = true)
        completion = onDone
        runningUtteranceId = UUID.randomUUID().toString()
        val focus = audioManager.requestAudioFocus(focusRequest)
        val result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, runningUtteranceId)
        Log.i(TAG, "speaking a story: audio focus $focus, TTS ${if (result == TextToSpeech.SUCCESS) "queued" else "failed"}")
    }

    override fun stop() {
        pendingText = null
        pendingOnDone = null
        completion = null
        stopInternal(abandonFocus = true)
    }

    private fun completePending() {
        val callback = pendingOnDone ?: return
        pendingText = null
        pendingOnDone = null
        callback.invoke()
    }

    private fun stopInternal(abandonFocus: Boolean) {
        if (initialized) textToSpeech.stop()
        if (abandonFocus) audioManager.abandonAudioFocusRequest(focusRequest)
    }

    private fun finishUtterance() {
        completion?.invoke()
        completion = null
        runningUtteranceId = null
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    override fun release() {
        stop()
        textToSpeech.shutdown()
    }

    private companion object {
        const val TAG = "Roadio"
    }
}
