package com.juacie.littlewar.ui.stageselect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StageSelectScreen(
    onNavigateToFormation: () -> Unit,
    viewModel: StageSelectViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                StageSelectContract.Effect.NavigateToFormation -> onNavigateToFormation()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("選擇關卡", style = MaterialTheme.typography.headlineSmall)
        Text(
            "不同關卡有不同的敵方陣容，難度由左到右遞增。",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))

        state.stages.forEach { stage ->
            val isSelected = stage.id == state.selectedStageId
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { viewModel.setEvent(StageSelectContract.Event.SelectStage(stage.id)) }
                    .padding(16.dp)
            ) {
                Text(stage.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "敵方單位數：${stage.enemyFormation.slots.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = { viewModel.setEvent(StageSelectContract.Event.ConfirmStage) },
            enabled = state.selectedStageId != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("排兵佈陣")
        }
    }
}
