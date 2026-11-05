package app.roadio.media

import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import app.roadio.DocentEngine
import app.roadio.DocentUiState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The drive as the car sees a media player: the media centre and the steering-wheel keys show the story being told and
 * play, pause, skip or replay through it. The sound itself comes from the car's speech service, not from this player.
 *
 * Play is the drive on, pause is the drive off and the story stopped, next skips the story, previous tells it again.
 * Only a play starts a drive: from the driver, or from the car resuming a drive that was on when it was switched off
 * (the car's own boot setting).
 *
 * While the drive is on, Roadio is the car's media source, as any audio app: music stops, and starting music pauses
 * the drive.
 */
@OptIn(UnstableApi::class)
class DocentPlayer(private val engine: DocentEngine) : SimpleBasePlayer(Looper.getMainLooper()) {

    private val scope = MainScope()
    private val mediaKeys = MediaKeyClaim()

    private val playlist = engine.stories.map { story ->
        MediaItemData.Builder(story.id)
            .setMediaItem(StoryLibrary.item(story))
            .setIsSeekable(false)
            .setDurationUs(C.TIME_UNSET)
            .build()
    }

    init {
        scope.launch {
            var before = engine.state.value
            engine.state.collect { now ->
                val storyStarted = now.isSpeaking && (!before.isSpeaking || now.currentStory != before.currentStory)
                if ((now.isDriving && !before.isDriving) || storyStarted) mediaKeys.claim()
                before = now
                invalidateState()
            }
        }
    }

    override fun getState(): State {
        val drive = engine.state.value
        return State.Builder()
            .setAvailableCommands(COMMANDS)
            .setPlaylist(playlist)
            .setCurrentMediaItemIndex(currentIndex(drive))
            // A story told on request, with the drive off, plays too: pause then stops it instead of starting a drive.
            .setPlayWhenReady(drive.isDriving || drive.isSpeaking, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            // Ready, never idle: before a drive the media centre shows the next story, paused, and play starts the drive.
            // Between stories the drive is still on, so the session stays playing and the app keeps running.
            .setPlaybackState(if (drive.finished) Player.STATE_ENDED else Player.STATE_READY)
            .build()
    }

    // The story being told or last told, else the next one.
    private fun currentIndex(drive: DocentUiState): Int {
        val id = drive.currentStory?.id ?: drive.upcomingStory?.id
        return engine.stories.indexOfFirst { it.id == id }.coerceAtLeast(0)
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        if (playWhenReady) engine.start() else engine.pause(stopStory = true)
        return Futures.immediateVoidFuture()
    }

    override fun handleStop(): ListenableFuture<*> {
        engine.pause(stopStory = true)
        return Futures.immediateVoidFuture()
    }

    override fun handlePrepare(): ListenableFuture<*> = Futures.immediateVoidFuture()

    override fun handleRelease(): ListenableFuture<*> {
        scope.cancel()
        return Futures.immediateVoidFuture()
    }

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: Int): ListenableFuture<*> {
        when (seekCommand) {
            Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> engine.skipStory()
            Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> engine.replayStory()
            else -> engine.stories.getOrNull(mediaItemIndex)?.let { engine.tellStory(it.id) }
        }
        return Futures.immediateVoidFuture()
    }

    private companion object {
        val COMMANDS: Player.Commands = Player.Commands.Builder()
            .addAll(
                Player.COMMAND_PLAY_PAUSE,
                Player.COMMAND_STOP,
                Player.COMMAND_PREPARE,
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_MEDIA_ITEM,
                // Lets the media centre ask for a story; StoryLibrary tells it and refuses the change itself.
                Player.COMMAND_SET_MEDIA_ITEM,
                Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                Player.COMMAND_GET_METADATA,
                Player.COMMAND_GET_TIMELINE,
                Player.COMMAND_RELEASE,
            )
            .build()
    }
}
