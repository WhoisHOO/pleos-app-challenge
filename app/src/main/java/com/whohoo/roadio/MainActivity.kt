package com.whohoo.roadio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.whohoo.roadio.ui.RoadioScreen
import com.whohoo.roadio.ui.RoadioTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoadioTheme {
                val viewModel: DocentViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                RoadioScreen(
                    state = state,
                    onToggleDriving = viewModel::toggleDriving,
                    onToggleStory = viewModel::toggleStory,
                    onRestart = viewModel::restart,
                )
            }
        }
    }
}
