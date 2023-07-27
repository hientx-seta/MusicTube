package pro.mobiledev.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pro.mobiledev.innertube.Innertube
import pro.mobiledev.innertube.models.BrowseResponse
import pro.mobiledev.innertube.models.NextResponse
import pro.mobiledev.innertube.models.bodies.BrowseBody
import pro.mobiledev.innertube.models.bodies.NextBody
import pro.mobiledev.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.lyrics(body: NextBody): Result<String?>? = runCatchingNonCancellable {
    val nextResponse = client.post(next) {
        setBody(body)
        mask("contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.tabRenderer(endpoint,title)")
    }.body<NextResponse>()

    val browseId = nextResponse
        .contents
        ?.singleColumnMusicWatchNextResultsRenderer
        ?.tabbedRenderer
        ?.watchNextTabbedResultsRenderer
        ?.tabs
        ?.getOrNull(1)
        ?.tabRenderer
        ?.endpoint
        ?.browseEndpoint
        ?.browseId
        ?: return@runCatchingNonCancellable null

    val response = client.post(browse) {
        setBody(BrowseBody(browseId = browseId))
        mask("contents.sectionListRenderer.contents.musicDescriptionShelfRenderer.description")
    }.body<BrowseResponse>()

    response.contents
        ?.sectionListRenderer
        ?.contents
        ?.firstOrNull()
        ?.musicDescriptionShelfRenderer
        ?.description
        ?.text
}
