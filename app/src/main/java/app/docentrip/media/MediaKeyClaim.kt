package app.docentrip.media

import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.util.Log
import app.docentrip.audio.StoryAudioAttributes

/**
 * Media keys (the steering wheel's play, pause and next) go to the media session of the app that played sound most
 * recently. Docentrip's stories are played by the car's speech service, under that service's app id, so the keys found no
 * session (seen on the Pleos emulator: "Media button session is null"). Playing a moment of silence under Docentrip's own
 * id when a drive or a story starts makes Docentrip the app the keys go to.
 */
internal class MediaKeyClaim {

    private val main = Handler(Looper.getMainLooper())

    fun claim() {
        try {
            val bytes = SAMPLE_RATE * 2 * SILENCE_MS / 1000
            val track = AudioTrack.Builder()
                .setAudioAttributes(StoryAudioAttributes)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(bytes)
                .build()
            track.write(ByteArray(bytes), 0, bytes)
            track.play()
            main.postDelayed({ runCatching { track.stop() }; track.release() }, SILENCE_MS.toLong() + 100)
        } catch (e: RuntimeException) {
            Log.w("Docentrip", "media keys not claimed", e)
        }
    }

    private companion object {
        const val SAMPLE_RATE = 16_000
        const val SILENCE_MS = 200
    }
}
