package pro.mobiledev.musictube.ui.screens.search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import pro.mobiledev.compose.persist.persistList
import pro.mobiledev.innertube.models.NavigationEndpoint
import pro.mobiledev.musictube.Database
import pro.mobiledev.musictube.LocalPlayerAwareWindowInsets
import pro.mobiledev.musictube.LocalPlayerServiceBinder
import pro.mobiledev.musictube.models.Song
import pro.mobiledev.musictube.ui.components.LocalMenuState
import pro.mobiledev.musictube.ui.components.themed.FloatingActionsContainerWithScrollToTop
import pro.mobiledev.musictube.ui.components.themed.Header
import pro.mobiledev.musictube.ui.components.themed.InHistoryMediaItemMenu
import pro.mobiledev.musictube.ui.components.themed.SecondaryTextButton
import pro.mobiledev.musictube.ui.items.SongItem
import pro.mobiledev.musictube.ui.styling.Dimensions
import pro.mobiledev.musictube.ui.styling.LocalAppearance
import pro.mobiledev.musictube.ui.styling.px
import pro.mobiledev.musictube.utils.align
import pro.mobiledev.musictube.utils.asMediaItem
import pro.mobiledev.musictube.utils.forcePlay
import pro.mobiledev.musictube.utils.medium

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun LocalSongSearch(
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var items by persistList<Song>("search/local/songs")

    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.length > 1) {
            Database.search("%${textFieldValue.text}%").collect { items = it }
        }
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyListState = rememberLazyListState()

    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(
                    titleContent = {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChanged,
                            textStyle = typography.xxl.medium.align(TextAlign.End),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            cursorBrush = SolidColor(colorPalette.text),
                            decorationBox = decorationBox
                        )
                    },
                    actionsContent = {
                        if (textFieldValue.text.isNotEmpty()) {
                            SecondaryTextButton(
                                text = "Clear",
                                onClick = { onTextFieldValueChanged(TextFieldValue()) }
                            )
                        }
                    }
                )
            }

            items(
                items = items,
                key = Song::id,
            ) { song ->
                SongItem(
                    song = song,
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    InHistoryMediaItemMenu(
                                        song = song,
                                        onDismiss = menuState::hide
                                    )
                                }
                            },
                            onClick = {
                                val mediaItem = song.asMediaItem
                                binder?.stopRadio()
                                binder?.player?.forcePlay(mediaItem)
                                binder?.setupRadio(
                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }
}
