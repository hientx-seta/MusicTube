package pro.mobiledev.musictube.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import pro.mobiledev.musictube.ui.styling.PureBlackColorPalette
import pro.mobiledev.musictube.ui.styling.LocalAppearance
import pro.mobiledev.musictube.utils.center
import pro.mobiledev.musictube.utils.color
import pro.mobiledev.musictube.utils.medium

@Composable
fun PlaybackError(
    isDisplayed: Boolean,
    messageProvider: () -> String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (_, typography) = LocalAppearance.current

    Box {
        AnimatedVisibility(
            visible = isDisplayed,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Spacer(
                modifier = modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onDismiss()
                            }
                        )
                    }
                    .fillMaxSize()
                    .background(Color.Black.copy(0.8f))
            )
        }

        AnimatedVisibility(
            visible = isDisplayed,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
        ) {
            BasicText(
                text = remember { messageProvider() },
                style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                modifier = Modifier
                    .background(Color.Black.copy(0.4f))
                    .padding(all = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}
