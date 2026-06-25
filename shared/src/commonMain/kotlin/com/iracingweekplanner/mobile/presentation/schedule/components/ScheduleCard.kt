package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme

@Composable
fun ScheduleCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(ScheduleUiTokens.CardRadius),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = BorderStroke(
        width = ScheduleUiTokens.RaceCardBorderWidth,
        color = MaterialTheme.colorScheme.outlineVariant,
    ),
    elevation: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(ScheduleUiTokens.RaceCardPadding),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
@IWPPreview
private fun ScheduleCardPreview() {
    ScheduleComponentPreviewTheme {
        ScheduleCard(
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        ) {
            Text("Schedule card")
        }
    }
}
