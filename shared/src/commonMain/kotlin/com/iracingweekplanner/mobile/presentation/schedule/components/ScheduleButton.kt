package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme
import iracingweekplannermobile.shared.generated.resources.Res
import iracingweekplannermobile.shared.generated.resources.schedule_refresh_label
import iracingweekplannermobile.shared.generated.resources.schedule_retry_label
import org.jetbrains.compose.resources.stringResource

enum class ScheduleButtonStyle {
    Text,
    Filled,
}

@Composable
fun ScheduleButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: ScheduleButtonStyle = ScheduleButtonStyle.Text,
    contentDescription: String = label,
) {
    val buttonModifier = modifier
        .sizeIn(
            minWidth = ScheduleUiTokens.MinimumIconTouchTarget,
            minHeight = ScheduleUiTokens.MinimumIconTouchTarget,
        )
        .semantics {
            role = Role.Button
            this.contentDescription = contentDescription
        }

    when (style) {
        ScheduleButtonStyle.Text -> TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        ) {
            Text(label)
        }

        ScheduleButtonStyle.Filled -> Button(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        ) {
            Text(label)
        }
    }
}

@Composable
@ScheduleComponentPreview
private fun ScheduleButtonPreview() {
    ScheduleComponentPreviewTheme {
        Row(modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal)) {
            ScheduleButton(
                label = stringResource(Res.string.schedule_refresh_label),
                onClick = {},
            )
            ScheduleButton(
                label = stringResource(Res.string.schedule_retry_label),
                onClick = {},
                style = ScheduleButtonStyle.Filled,
            )
        }
    }
}
