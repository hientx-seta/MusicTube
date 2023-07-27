package pro.mobiledev.musictube.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pro.mobiledev.musictube.models.Album
import pro.mobiledev.musictube.ui.components.themed.TextPlaceholder
import pro.mobiledev.musictube.ui.styling.LocalAppearance
import pro.mobiledev.musictube.ui.styling.shimmer
import pro.mobiledev.musictube.utils.secondary
import pro.mobiledev.musictube.utils.semiBold
import pro.mobiledev.musictube.utils.thumbnail
import pro.mobiledev.innertube.Innertube

@Composable
fun AlbumItem(
    album: Album,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnailUrl,
        title = album.title,
        authors = album.authorsText,
        year = album.year,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        modifier = modifier
    )
}

@Composable
fun AlbumItem(
    album: Innertube.AlbumItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnail?.url,
        title = album.info?.name,
        authors = album.authors?.joinToString("") { it.name ?: "" },
        year = album.year,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        modifier = modifier
    )
}

@Composable
fun AlbumItem(
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    year: String?,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (_, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        AsyncImage(
            model = thumbnailUrl?.thumbnail(thumbnailSizePx),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            BasicText(
                text = title ?: "",
                style = typography.xs.semiBold,
                maxLines = if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!alternative) {
                authors?.let {
                    BasicText(
                        text = authors,
                        style = typography.xs.semiBold.secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            BasicText(
                text = year ?: "",
                style = typography.xxs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AlbumItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()

            if (!alternative) {
                TextPlaceholder()
            }

            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}
