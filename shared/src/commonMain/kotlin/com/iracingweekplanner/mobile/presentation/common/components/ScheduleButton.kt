package com.iracingweekplanner.mobile.presentation.common.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.iracingweekplanner.mobile.presentation.common.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.common.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.common.preview.ScheduleComponentPreviewTheme
import iracingweekplannermobile.shared.generated.resources.Res
import iracingweekplannermobile.shared.generated.resources.schedule_refresh_label
import iracingweekplannermobile.shared.generated.resources.schedule_retry_label
import org.jetbrains.compose.resources.stringResource

enum class ScheduleButtonEmphasis {
    Standard,
    Filled,
}

@Composable
fun ScheduleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasis: ScheduleButtonEmphasis = ScheduleButtonEmphasis.Standard,
) {
    val buttonModifier = modifier
        .sizeIn(
            minWidth = ScheduleUiTokens.MinimumIconTouchTarget,
            minHeight = ScheduleUiTokens.MinimumIconTouchTarget,
        )
        .semantics {
            role = Role.Button
        }

    when (emphasis) {
        ScheduleButtonEmphasis.Standard -> IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
            )
        }

        ScheduleButtonEmphasis.Filled -> FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
            )
        }
    }
}

object ScheduleButtonIcons {
    val Previous: ImageVector = chevronIcon(name = "SchedulePrevious", pointsLeft = true)
    val Next: ImageVector = chevronIcon(name = "ScheduleNext", pointsLeft = false)
    val Refresh: ImageVector = refreshIcon(name = "ScheduleRefresh")
    val Retry: ImageVector = Refresh
    val Today: ImageVector = ImageVector.Builder(
        name = "ScheduleToday",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(7f, 2f)
            lineTo(7f, 4f)
            lineTo(5f, 4f)
            curveTo(3.9f, 4f, 3f, 4.9f, 3f, 6f)
            lineTo(3f, 19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            lineTo(19f, 21f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            lineTo(21f, 6f)
            curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f)
            lineTo(17f, 4f)
            lineTo(17f, 2f)
            lineTo(15f, 2f)
            lineTo(15f, 4f)
            lineTo(9f, 4f)
            lineTo(9f, 2f)
            close()
            moveTo(5f, 9f)
            lineTo(19f, 9f)
            lineTo(19f, 19f)
            lineTo(5f, 19f)
            close()
        }
    }.build()

    private fun chevronIcon(name: String, pointsLeft: Boolean): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                if (pointsLeft) {
                    moveTo(15.4f, 7.4f)
                    lineTo(14f, 6f)
                    lineTo(8f, 12f)
                    lineTo(14f, 18f)
                    lineTo(15.4f, 16.6f)
                    lineTo(10.8f, 12f)
                    close()
                } else {
                    moveTo(8.6f, 7.4f)
                    lineTo(10f, 6f)
                    lineTo(16f, 12f)
                    lineTo(10f, 18f)
                    lineTo(8.6f, 16.6f)
                    lineTo(13.2f, 12f)
                    close()
                }
            }
        }.build()

    private fun refreshIcon(name: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(17.7f, 6.3f)
                curveTo(16.2f, 4.9f, 14.2f, 4f, 12f, 4f)
                curveTo(7.6f, 4f, 4f, 7.6f, 4f, 12f)
                curveTo(4f, 16.4f, 7.6f, 20f, 12f, 20f)
                curveTo(15.7f, 20f, 18.8f, 17.5f, 19.7f, 14f)
                lineTo(17.6f, 14f)
                curveTo(16.8f, 16.3f, 14.6f, 18f, 12f, 18f)
                curveTo(8.7f, 18f, 6f, 15.3f, 6f, 12f)
                curveTo(6f, 8.7f, 8.7f, 6f, 12f, 6f)
                curveTo(13.7f, 6f, 15.1f, 6.7f, 16.2f, 7.8f)
                lineTo(13f, 11f)
                lineTo(20f, 11f)
                lineTo(20f, 4f)
                close()
            }
        }.build()
}

@Composable
@IWPPreview
private fun ScheduleButtonPreview() {
    ScheduleComponentPreviewTheme {
        Row(modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal)) {
            ScheduleButton(
                icon = ScheduleButtonIcons.Refresh,
                contentDescription = stringResource(Res.string.schedule_refresh_label),
                onClick = {},
            )
            ScheduleButton(
                icon = ScheduleButtonIcons.Retry,
                contentDescription = stringResource(Res.string.schedule_retry_label),
                onClick = {},
                emphasis = ScheduleButtonEmphasis.Filled,
            )
        }
    }
}
