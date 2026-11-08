package app.docentrip.media

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import app.docentrip.core.DocentStory

/**
 * The route's stories as the car's media centre browses and searches them: one list, each story playable. Choosing one,
 * or asking for one by voice, tells it now through [tell], without switching the drive on.
 */
class StoryLibrary(
    private val stories: List<DocentStory>,
    private val tell: (storyId: String) -> Unit,
) : MediaLibrarySession.Callback {

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(LibraryResult.ofItem(root, params))

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        if (parentId != ROOT_ID) return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
        return Futures.immediateFuture(LibraryResult.ofItemList(page(stories.map(::item), page, pageSize), params))
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val story = stories.firstOrNull { it.id == mediaId }
            ?: return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
        return Futures.immediateFuture(LibraryResult.ofItem(item(story), null))
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> {
        session.notifySearchResultChanged(browser, query, matching(query).size, params)
        return Futures.immediateFuture(LibraryResult.ofVoid())
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
        Futures.immediateFuture(LibraryResult.ofItemList(page(matching(query).map(::item), page, pageSize), params))

    // A controller asks to play a story by id (a choice in the media centre) or by words (a voice search). The story is
    // told now; the request itself is refused, so that the play that follows it does not switch the whole drive on, and
    // the route's list stays as it is.
    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val requested = mediaItems.getOrNull(startIndex.coerceAtLeast(0)) ?: mediaItems.firstOrNull()
        val story = requested?.let { item ->
            stories.firstOrNull { it.id == item.mediaId }
                ?: item.requestMetadata.searchQuery?.let { matching(it).firstOrNull() }
        }
        story?.let { tell(it.id) }
        return Futures.immediateFailedFuture(UnsupportedOperationException("a story is told, not queued"))
    }

    // Stories whose title has the most of the query's words come first, then those that only mention them.
    private fun matching(query: String): List<DocentStory> {
        val words = query.split(' ').map(String::trim).filter(String::isNotEmpty)
        if (words.isEmpty()) return emptyList()
        return stories
            .map { story -> story to words.sumOf { (if (story.title.contains(it)) 2 else 0) + (if (story.body.contains(it)) 1 else 0) } }
            .filter { (_, score) -> score > 0 }
            .sortedByDescending { (_, score) -> score }
            .map { (story, _) -> story }
    }

    private fun page(items: List<MediaItem>, page: Int, pageSize: Int): List<MediaItem> =
        if (pageSize <= 0) items else items.drop(page * pageSize).take(pageSize)

    companion object {
        const val ROOT_ID = "docentrip-root"

        private val root: MediaItem = MediaItem.Builder()
            .setMediaId(ROOT_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("차창 밖 이야기")
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PODCASTS)
                    .build(),
            )
            .build()

        fun item(story: DocentStory): MediaItem = MediaItem.Builder()
            .setMediaId(story.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(story.title)
                    .setArtist("Docentrip")
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST_EPISODE)
                    .build(),
            )
            .build()
    }
}
