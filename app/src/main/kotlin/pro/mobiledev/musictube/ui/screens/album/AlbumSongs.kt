package pro.mobiledev.musictube.ui.screens.album

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import pro.mobiledev.compose.persist.persistList
import pro.mobiledev.musictube.Database
import pro.mobiledev.musictube.LocalPlayerAwareWindowInsets
import pro.mobiledev.musictube.LocalPlayerServiceBinder
import pro.mobiledev.musictube.R
import pro.mobiledev.musictube.models.Song
import pro.mobiledev.musictube.ui.components.LocalMenuState
import pro.mobiledev.musictube.ui.components.ShimmerHost
import pro.mobiledev.musictube.ui.components.themed.FloatingActionsContainerWithScrollToTop
import pro.mobiledev.musictube.ui.components.themed.LayoutWithAdaptiveThumbnail
import pro.mobiledev.musictube.ui.components.themed.NonQueuedMediaItemMenu
import pro.mobiledev.musictube.ui.components.themed.SecondaryTextButton
import pro.mobiledev.musictube.ui.items.SongItem
import pro.mobiledev.musictube.ui.items.SongItemPlaceholder
import pro.mobiledev.musictube.ui.styling.Dimensions
import pro.mobiledev.musictube.ui.styling.LocalAppearance
import pro.mobiledev.musictube.utils.asMediaItem
import pro.mobiledev.musictube.utils.center
import pro.mobiledev.musictube.utils.color
import pro.mobiledev.musictube.utils.enqueue
import pro.mobiledev.musictube.utils.forcePlayAtIndex
import pro.mobiledev.musictube.utils.forcePlayFromBeginning
import pro.mobiledev.musictube.utils.isLandscape
import pro.mobiledev.musictube.utils.semiBold

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun AlbumSongs(
    browseId: String,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    thumbnailContent: @Composable () -> Unit,
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs by persistList<Song>("album/$browseId/songs")

    LaunchedEffect(Unit) {
        Database.albumSongs(browseId).collect { songs = it }
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song

    val lazyListState = rememberLazyListState()

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
        Box {
            LazyColumn(
                state = lazyListState,
                contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        headerContent {
                            SecondaryTextButton(
                                text = "Enqueue",
                                enabled = songs.isNotEmpty(),
                                onClick = {
                                    binder?.player?.enqueue(songs.map(Song::asMediaItem))
                                }
                            )
                        }

                        if (!isLandscape) {
                            thumbnailContent()
                        }
                    }
                }

                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    SongItem(
                        title = song.title,
                        authors = song.artistsText,
                        duration = song.durationText,
                        thumbnailSizeDp = thumbnailSizeDp,
                        thumbnailContent = {
                            BasicText(
                                text = "${index + 1}",
                                style = typography.s.semiBold.center.color(colorPalette.textDisabled),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .width(thumbnailSizeDp)
                                    .align(Alignment.Center)
                            )
                        },
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
                                    binder?.player?.forcePlayAtIndex(
                                        songs.map(Song::asMediaItem),
                                        index
                                    )
                                }
                            )
                    )
                }

                if (songs.isEmpty()) {
                    item(key = "loading") {
                        ShimmerHost(
                            modifier = Modifier
                                .fillParentMaxSize()
                        ) {
                            repeat(4) {
                                SongItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.song)
                            }
                        }
                    }
                }
            }

            FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.shuffle,
                onClick = {
                    if (songs.isNotEmpty()) {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            songs.shuffled().map(Song::asMediaItem)
                        )
                    }
                }
            )
        }
    }
}
