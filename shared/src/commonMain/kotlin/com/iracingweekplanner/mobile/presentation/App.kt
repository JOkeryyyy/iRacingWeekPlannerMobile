package com.iracingweekplanner.mobile.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.domain.usecase.GetAppInfoUseCase
import com.iracingweekplanner.mobile.domain.model.PlannerAppInfo
import com.iracingweekplanner.mobile.domain.repository.PlannerAppInfoRepository
import com.iracingweekplanner.mobile.presentation.theme.IwpAppTheme
import org.jetbrains.compose.resources.painterResource

import iracingweekplannermobile.shared.generated.resources.Res
import iracingweekplannermobile.shared.generated.resources.compose_multiplatform

@Composable
fun App(stateHolder: AppInfoStateHolder) {
    IwpAppTheme {
        var showContent by remember { mutableStateOf(false) }
        val appInfo = remember(stateHolder) { stateHolder.uiState }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(appInfo.appName)
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Source: ${appInfo.sourceSet}")
                    Text(appInfo.statusMessage)
                }
            }
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    App(
        stateHolder = AppInfoStateHolder(
            getAppInfo = GetAppInfoUseCase(
                repository = object : PlannerAppInfoRepository {
                    override fun getAppInfo(): PlannerAppInfo =
                        PlannerAppInfo(
                            name = "iRacing Week Planner Mobile",
                            sourceSet = "shared",
                        )
                },
            ),
        ),
    )
}
