package pro.mobiledev.musictube.ui.screens.home

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import pro.mobiledev.compose.persist.PersistMapCleanup
import pro.mobiledev.compose.routing.RouteHandler
import pro.mobiledev.compose.routing.defaultStacking
import pro.mobiledev.compose.routing.defaultStill
import pro.mobiledev.compose.routing.defaultUnstacking
import pro.mobiledev.compose.routing.isStacking
import pro.mobiledev.compose.routing.isUnknown
import pro.mobiledev.compose.routing.isUnstacking
import pro.mobiledev.musictube.Database
import pro.mobiledev.musictube.R
import pro.mobiledev.musictube.models.SearchQuery
import pro.mobiledev.musictube.query
import pro.mobiledev.musictube.ui.components.themed.Scaffold
import pro.mobiledev.musictube.ui.screens.albumRoute
import pro.mobiledev.musictube.ui.screens.artistRoute
import pro.mobiledev.musictube.ui.screens.builtInPlaylistRoute
import pro.mobiledev.musictube.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import pro.mobiledev.musictube.ui.screens.globalRoutes
import pro.mobiledev.musictube.ui.screens.localPlaylistRoute
import pro.mobiledev.musictube.ui.screens.localplaylist.LocalPlaylistScreen
import pro.mobiledev.musictube.ui.screens.playlistRoute
import pro.mobiledev.musictube.ui.screens.search.SearchScreen
import pro.mobiledev.musictube.ui.screens.searchResultRoute
import pro.mobiledev.musictube.ui.screens.searchRoute
import pro.mobiledev.musictube.ui.screens.searchresult.SearchResultScreen
import pro.mobiledev.musictube.ui.screens.settings.SettingsScreen
import pro.mobiledev.musictube.ui.screens.settingsRoute
import pro.mobiledev.musictube.ui.theme.MusicTubeTheme
import pro.mobiledev.musictube.utils.homeScreenTabIndexKey
import pro.mobiledev.musictube.utils.pauseSearchHistoryKey
import pro.mobiledev.musictube.utils.preferences
import pro.mobiledev.musictube.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen(onPlaylistUrl: (String) -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl
            )
        }

        host {
            val (tabIndex, onTabChanged) = rememberPreference(
                homeScreenTabIndexKey,
                defaultValue = 0
            )

            Scaffold(
                topIconButtonId = R.drawable.equalizer,
                onTopIconButtonClick = { settingsRoute() },
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, "Quick picks", R.drawable.sparkles)
                    Item(1, "Songs", R.drawable.musical_notes)
                    Item(2, "Playlists", R.drawable.playlist)
                    Item(3, "Artists", R.drawable.person)
                    Item(4, "Albums", R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicks(
                            onAlbumClick = { albumRoute(it) },
                            onArtistClick = { artistRoute(it) },
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        1 -> HomeSongs(
                            onSearchClick = { searchRoute("") }
                        )

                        2 -> HomePlaylists(
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        3 -> HomeArtistList(
                            onArtistClick = { artistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        4 -> HomeAlbums(
                            onAlbumClick = { albumRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Preview("BottomBar")
@Preview("Drawer contents (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewMenu() {
    MusicTubeTheme {
        Scaffold(
            topIconButtonId = R.drawable.equalizer,
            onTopIconButtonClick = {  },
            tabIndex = 1,
            onTabChanged = {},
            tabColumnContent = { Item ->
                Item(0, "Quick picks", R.drawable.sparkles)
                Item(1, "Songs", R.drawable.musical_notes)
                Item(2, "Playlists", R.drawable.playlist)
                Item(3, "Artists", R.drawable.person)
                Item(4, "Albums", R.drawable.disc)
            }
        ){}
    }
}
