package pro.mobiledev.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pro.mobiledev.innertube.Innertube
import pro.mobiledev.innertube.models.SearchSuggestionsResponse
import pro.mobiledev.innertube.models.bodies.SearchSuggestionsBody
import pro.mobiledev.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.searchSuggestions(body: SearchSuggestionsBody) = runCatchingNonCancellable {
    val response = client.post(searchSuggestions) {
        setBody(body)
        mask("contents.searchSuggestionsSectionRenderer.contents.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query")
    }.body<SearchSuggestionsResponse>()

    response
        .contents
        ?.firstOrNull()
        ?.searchSuggestionsSectionRenderer
        ?.contents
        ?.mapNotNull { content ->
            content
                .searchSuggestionRenderer
                ?.navigationEndpoint
                ?.searchEndpoint
                ?.query
        }
}
