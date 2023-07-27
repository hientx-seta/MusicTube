package pro.mobiledev.musictube.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pro.mobiledev.compose.persist.PersistMapCleanup
import pro.mobiledev.compose.persist.persistMap
import pro.mobiledev.innertube.Innertube
import pro.mobiledev.innertube.models.bodies.ContinuationBody
import pro.mobiledev.innertube.models.bodies.SearchBody
import pro.mobiledev.innertube.requests.searchPage
import pro.mobiledev.innertube.utils.from
import pro.mobiledev.compose.routing.RouteHandler
import pro.mobiledev.musictube.LocalPlayerServiceBinder
import pro.mobiledev.musictube.R
import pro.mobiledev.musictube.ui.components.LocalMenuState
import pro.mobiledev.musictube.ui.components.themed.Header
import pro.mobiledev.musictube.ui.components.themed.NonQueuedMediaItemMenu
import pro.mobiledev.musictube.ui.components.themed.Scaffold
import pro.mobiledev.musictube.ui.items.AlbumItem
import pro.mobiledev.musictube.ui.items.AlbumItemPlaceholder
import pro.mobiledev.musictube.ui.items.ArtistItem
import pro.mobiledev.musictube.ui.items.ArtistItemPlaceholder
import pro.mobiledev.musictube.ui.items.PlaylistItem
import pro.mobiledev.musictube.ui.items.PlaylistItemPlaceholder
import pro.mobiledev.musictube.ui.items.SongItem
import pro.mobiledev.musictube.ui.items.SongItemPlaceholder
import pro.mobiledev.musictube.ui.items.VideoItem
import pro.mobiledev.musictube.ui.items.VideoItemPlaceholder
import pro.mobiledev.musictube.ui.screens.albumRoute
import pro.mobiledev.musictube.ui.screens.artistRoute
import pro.mobiledev.musictube.ui.screens.globalRoutes
import pro.mobiledev.musictube.ui.screens.playlistRoute
import pro.mobiledev.musictube.ui.styling.Dimensions
import pro.mobiledev.musictube.ui.styling.px
import pro.mobiledev.musictube.utils.asMediaItem
import pro.mobiledev.musictube.utils.forcePlay
import pro.mobiledev.musictube.utils.rememberPreference
import pro.mobiledev.musictube.utils.searchResultScreenTabIndexKey

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SearchResultScreen(query: String, onSearchAgain: () -> Unit) {
    val context = LocalContext.current
    val saveableStateHolder = rememberSaveableStateHolder()
    val (tabIndex, onTabIndexChanges) = rememberPreference(searchResultScreenTabIndexKey, 0)

    PersistMapCleanup(tagPrefix = "searchResults/$query/")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit = {
                Header(
                    title = query,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures {
                                context.persistMap?.keys?.removeAll {
                                    it.startsWith("searchResults/$query/")
                                }
                                onSearchAgain()
                            }
                        }
                )
            }

            val emptyItemsText = "No results found. Please try a different query or category"

            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabIndexChanges,
                tabColumnContent = { Item ->
                    Item(0, "Songs", R.drawable.musical_notes)
                    Item(1, "Albums", R.drawable.disc)
                    Item(2, "Artists", R.drawable.person)
                    Item(3, "Videos", R.drawable.film)
                    Item(4, "Playlists", R.drawable.playlist)
                    Item(5, "Featured", R.drawable.playlist)
                }
            ) { tabIndex ->
                saveableStateHolder.SaveableStateProvider(tabIndex) {
                    when (tabIndex) {
                        0 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val menuState = LocalMenuState.current
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/songs",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = Innertube.SearchFilter.Song.value),
                                            fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { song ->
                                    SongItem(
                                        song = song,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onLongClick = {
                                                    menuState.display {
                                                        NonQueuedMediaItemMenu(
                                        onDismiss = menuState::hide,
                                        mediaItem = song.asMediaItem,
                                    )
                                                    }
                                                },
                                                onClick = {
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlay(song.asMediaItem)
                                                    binder?.setupRadio(song.info?.endpoint)
                                                }
                                            )
                                    )
                                },
                                itemPlaceholderContent = {
                                    SongItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        1 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/albums",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = Innertube.SearchFilter.Album.value),
                                            fromMusicShelfRendererContent = Innertube.AlbumItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.AlbumItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { album ->
                                    AlbumItem(
                                        album = album,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(onClick = { albumRoute(album.key) })
                                    )

                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        2 -> {
                            val thumbnailSizeDp = 64.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/artists",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = Innertube.SearchFilter.Artist.value),
                                            fromMusicShelfRendererContent = Innertube.ArtistItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.ArtistItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { artist ->
                                    ArtistItem(
                                        artist = artist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(onClick = { artistRoute(artist.key) })
                                    )
                                },
                                itemPlaceholderContent = {
                                    ArtistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        3 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val menuState = LocalMenuState.current
                            val thumbnailHeightDp = 72.dp
                            val thumbnailWidthDp = 128.dp

                            ItemsPage(
                                tag = "searchResults/$query/videos",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = Innertube.SearchFilter.Video.value),
                                            fromMusicShelfRendererContent = Innertube.VideoItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.VideoItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { video ->
                                    VideoItem(
                                        video = video,
                                        thumbnailWidthDp = thumbnailWidthDp,
                                        thumbnailHeightDp = thumbnailHeightDp,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onLongClick = {
                                                    menuState.display {
                                                        NonQueuedMediaItemMenu(
                                                            mediaItem = video.asMediaItem,
                                                            onDismiss = menuState::hide
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlay(video.asMediaItem)
                                                    binder?.setupRadio(video.info?.endpoint)
                                                }
                                            )
                                    )
                                },
                                itemPlaceholderContent = {
                                    VideoItemPlaceholder(
                                        thumbnailHeightDp = thumbnailHeightDp,
                                        thumbnailWidthDp = thumbnailWidthDp
                                    )
                                }
                            )
                        }

                        4, 5 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/${if (tabIndex == 4) "playlists" else "featured"}",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        val filter = if (tabIndex == 4) {
                                            Innertube.SearchFilter.CommunityPlaylist
                                        } else {
                                            Innertube.SearchFilter.FeaturedPlaylist
                                        }

                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = filter.value),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(onClick = { playlistRoute(playlist.key) })
                                    )
                                },
                                itemPlaceholderContent = {
                                    PlaylistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
