package pro.mobiledev.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pro.mobiledev.innertube.Innertube
import pro.mobiledev.innertube.models.ContinuationResponse
import pro.mobiledev.innertube.models.NextResponse
import pro.mobiledev.innertube.models.bodies.ContinuationBody
import pro.mobiledev.innertube.models.bodies.NextBody
import pro.mobiledev.innertube.utils.from
import pro.mobiledev.innertube.utils.runCatchingNonCancellable



suspend fun Innertube.nextPage(body: NextBody): Result<Innertube.NextPage>? =
    runCatchingNonCancellable {
        val response = client.post(next) {
            setBody(body)
            mask("contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.tabRenderer.content.musicQueueRenderer.content.playlistPanelRenderer(continuations,contents(automixPreviewVideoRenderer,$playlistPanelVideoRendererMask))")
        }.body<NextResponse>()

        val tabs = response
            .contents
            ?.singleColumnMusicWatchNextResultsRenderer
            ?.tabbedRenderer
            ?.watchNextTabbedResultsRenderer
            ?.tabs

        val playlistPanelRenderer = tabs
            ?.getOrNull(0)
            ?.tabRenderer
            ?.content
            ?.musicQueueRenderer
            ?.content
            ?.playlistPanelRenderer

        if (body.playlistId == null) {
            val endpoint = playlistPanelRenderer
                ?.contents
                ?.lastOrNull()
                ?.automixPreviewVideoRenderer
                ?.content
                ?.automixPlaylistVideoRenderer
                ?.navigationEndpoint
                ?.watchPlaylistEndpoint

            if (endpoint != null) {
                return nextPage(
                    body.copy(
                        playlistId = endpoint.playlistId,
                        params = endpoint.params
                    )
                )
            }
        }

        Innertube.NextPage(
            playlistId = body.playlistId,
            playlistSetVideoId = body.playlistSetVideoId,
            params = body.params,
            itemsPage = playlistPanelRenderer
                ?.toSongsPage()
        )
    }

suspend fun Innertube.nextPage(body: ContinuationBody) = runCatchingNonCancellable {
    val response = client.post(next) {
        setBody(body)
        mask("continuationContents.playlistPanelContinuation(continuations,contents.$playlistPanelVideoRendererMask)")
    }.body<ContinuationResponse>()

    response
        .continuationContents
        ?.playlistPanelContinuation
        ?.toSongsPage()
}

private fun NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer?.toSongsPage() =
    Innertube.ItemsPage(
        items = this
            ?.contents
            ?.mapNotNull(NextResponse.MusicQueueRenderer.Content.PlaylistPanelRenderer.Content::playlistPanelVideoRenderer)
            ?.mapNotNull(Innertube.SongItem::from),
        continuation = this
            ?.continuations
            ?.firstOrNull()
            ?.nextContinuationData
            ?.continuation
    )
