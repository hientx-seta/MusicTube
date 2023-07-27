package pro.mobiledev.musictube.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.mobiledev.musictube.LocalPlayerAwareWindowInsets
import pro.mobiledev.musictube.enums.ColorPaletteMode
import pro.mobiledev.musictube.enums.ColorPaletteName
import pro.mobiledev.musictube.enums.ThumbnailRoundness
import pro.mobiledev.musictube.ui.components.themed.Header
import pro.mobiledev.musictube.ui.styling.LocalAppearance
import pro.mobiledev.musictube.utils.applyFontPaddingKey
import pro.mobiledev.musictube.utils.colorPaletteModeKey
import pro.mobiledev.musictube.utils.colorPaletteNameKey
import pro.mobiledev.musictube.utils.isAtLeastAndroid13
import pro.mobiledev.musictube.utils.isShowingThumbnailInLockscreenKey
import pro.mobiledev.musictube.utils.rememberPreference
import pro.mobiledev.musictube.utils.thumbnailRoundnessKey
import pro.mobiledev.musictube.utils.useSystemFontKey

@ExperimentalAnimationApi
@Composable
fun AppearanceSettings() {
    val (colorPalette) = LocalAppearance.current

    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Light
    )
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        false
    )

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        Header(title = "Appearance")

        SettingsEntryGroupText(title = "COLORS")

        EnumValueSelectorSettingsEntry(
            title = "Theme",
            selectedValue = colorPaletteName,
            onValueSelected = { colorPaletteName = it }
        )

        EnumValueSelectorSettingsEntry(
            title = "Theme mode",
            selectedValue = colorPaletteMode,
            isEnabled = colorPaletteName != ColorPaletteName.PureBlack,
            onValueSelected = { colorPaletteMode = it }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "SHAPES")

        EnumValueSelectorSettingsEntry(
            title = "Thumbnail roundness",
            selectedValue = thumbnailRoundness,
            onValueSelected = { thumbnailRoundness = it },
            trailingContent = {
                Spacer(
                    modifier = Modifier
                        .border(width = 1.dp, color = colorPalette.accent,  shape = thumbnailRoundness.shape())
                        .background(color = colorPalette.background1, shape = thumbnailRoundness.shape())
                        .size(36.dp)
                )
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "TEXT")

        SwitchSettingEntry(
            title = "Use system font",
            text = "Use the font applied by the system",
            isChecked = useSystemFont,
            onCheckedChange = { useSystemFont = it }
        )

        SwitchSettingEntry(
            title = "Apply font padding",
            text = "Add spacing around texts",
            isChecked = applyFontPadding,
            onCheckedChange = { applyFontPadding = it }
        )

        if (!isAtLeastAndroid13) {
            SettingsGroupSpacer()

            SettingsEntryGroupText(title = "LOCKSCREEN")

            SwitchSettingEntry(
                title = "Show song cover",
                text = "Use the playing song cover as the lockscreen wallpaper",
                isChecked = isShowingThumbnailInLockscreen,
                onCheckedChange = { isShowingThumbnailInLockscreen = it }
            )
        }
    }
}
