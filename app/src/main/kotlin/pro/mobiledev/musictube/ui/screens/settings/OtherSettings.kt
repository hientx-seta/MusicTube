package pro.mobiledev.musictube.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import pro.mobiledev.musictube.Database
import pro.mobiledev.musictube.LocalPlayerAwareWindowInsets
import pro.mobiledev.musictube.query
import pro.mobiledev.musictube.service.PlayerMediaBrowserService
import pro.mobiledev.musictube.ui.components.themed.Header
import pro.mobiledev.musictube.ui.styling.LocalAppearance
import pro.mobiledev.musictube.utils.isAtLeastAndroid12
import pro.mobiledev.musictube.utils.isAtLeastAndroid6
import pro.mobiledev.musictube.utils.isIgnoringBatteryOptimizations
import pro.mobiledev.musictube.utils.isInvincibilityEnabledKey
import pro.mobiledev.musictube.utils.pauseSearchHistoryKey
import pro.mobiledev.musictube.utils.rememberPreference
import pro.mobiledev.musictube.utils.toast
import kotlinx.coroutines.flow.distinctUntilChanged

@SuppressLint("BatteryLife")
@ExperimentalAnimationApi
@Composable
fun OtherSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current

    var isAndroidAutoEnabled by remember {
        val component = ComponentName(context, PlayerMediaBrowserService::class.java)
        val disabledFlag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        val enabledFlag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        mutableStateOf(
            value = context.packageManager.getComponentEnabledSetting(component) == enabledFlag,
            policy = object : SnapshotMutationPolicy<Boolean> {
                override fun equivalent(a: Boolean, b: Boolean): Boolean {
                    context.packageManager.setComponentEnabledSetting(
                        component,
                        if (b) enabledFlag else disabledFlag,
                        PackageManager.DONT_KILL_APP
                    )
                    return a == b
                }
            }
        )
    }

    var isInvincibilityEnabled by rememberPreference(isInvincibilityEnabledKey, false)

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations)
    }

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations
        }

    var pauseSearchHistory by rememberPreference(pauseSearchHistoryKey, false)

    val queriesCount by remember {
        Database.queriesCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

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
        Header(title = "Other")

        SettingsEntryGroupText(title = "ANDROID AUTO")

        SettingsDescription(text = "Remember to enable \"Unknown sources\" in the Developer Settings of Android Auto.")

        SwitchSettingEntry(
            title = "Android Auto",
            text = "Enable Android Auto support",
            isChecked = isAndroidAutoEnabled,
            onCheckedChange = { isAndroidAutoEnabled = it }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "SEARCH HISTORY")

        SwitchSettingEntry(
            title = "Pause search history",
            text = "Neither save new searched queries nor show history",
            isChecked = pauseSearchHistory,
            onCheckedChange = { pauseSearchHistory = it }
        )

        SettingsEntry(
            title = "Clear search history",
            text = if (queriesCount > 0) {
                "Delete $queriesCount search queries"
            } else {
                "History is empty"
            },
            isEnabled = queriesCount > 0,
            onClick = { query(Database::clearQueries) }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "SERVICE LIFETIME")

        ImportantSettingsDescription(text = "If battery optimizations are applied, the playback notification can suddenly disappear when paused.")

        if (isAtLeastAndroid12) {
            SettingsDescription(text = "Since Android 12, disabling battery optimizations is required for the \"Invincible service\" option to take effect.")
        }

        SettingsEntry(
            title = "Ignore battery optimizations",
            isEnabled = !isIgnoringBatteryOptimizations,
            text = if (isIgnoringBatteryOptimizations) {
                "Already unrestricted"
            } else {
                "Disable background restrictions"
            },
            onClick = {
                if (!isAtLeastAndroid6) return@SettingsEntry

                try {
                    activityResultLauncher.launch(
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                } catch (e: ActivityNotFoundException) {
                    try {
                        activityResultLauncher.launch(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    } catch (e: ActivityNotFoundException) {
                        context.toast("Couldn't find battery optimization settings, please whitelist ViMusic manually")
                    }
                }
            }
        )

        SwitchSettingEntry(
            title = "Invincible service",
            text = "When turning off battery optimizations is not enough",
            isChecked = isInvincibilityEnabled,
            onCheckedChange = { isInvincibilityEnabled = it }
        )
    }
}
