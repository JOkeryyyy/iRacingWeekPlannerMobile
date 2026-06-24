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
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.theme.IwpAppTheme

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
@Preview
private fun ScheduleButtonPreview() {
    IwpAppTheme {
        Row(modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal)) {
            ScheduleButton(
                label = "Refresh",
                onClick = {},
            )
            ScheduleButton(
                label = "Retry",
                onClick = {},
                style = ScheduleButtonStyle.Filled,
            )
        }
    }
}
