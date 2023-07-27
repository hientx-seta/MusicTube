package pro.mobiledev.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pro.mobiledev.innertube.Innertube
import pro.mobiledev.innertube.models.GetQueueResponse
import pro.mobiledev.innertube.models.bodies.QueueBody
import pro.mobiledev.innertube.utils.from
import pro.mobiledev.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.queue(body: QueueBody) = runCatchingNonCancellable {
    val response = client.post(queue) {
        setBody(body)
        mask("queueDatas.content.$playlistPanelVideoRendererMask")
    }.body<GetQueueResponse>()

    response
        .queueDatas
        ?.mapNotNull { queueData ->
            queueData
                .content
                ?.playlistPanelVideoRenderer
                ?.let(Innertube.SongItem::from)
        }
}

suspend fun Innertube.song(videoId: String): Result<Innertube.SongItem?>? =
    queue(QueueBody(videoIds = listOf(videoId)))?.map { it?.firstOrNull() }
