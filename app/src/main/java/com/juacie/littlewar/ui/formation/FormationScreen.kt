package com.juacie.littlewar.ui.formation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FormationScreen(
    onNavigateToBattle: () -> Unit,
    viewModel: FormationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                FormationContract.Effect.NavigateToBattle -> onNavigateToBattle()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("排兵佈陣", style = MaterialTheme.typography.headlineSmall)
        Text(
            "先選下方兵種，再點格子放置或移除。前排（下方）距離近，後排（上方）距離遠，射程與陣型會決定戰局。",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))

        for (row in 0 until 3) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until 5) {
                    val index = row * 5 + col
                    val unitId = state.slots[index]
                    val unitName = unitId?.let { id -> state.units.firstOrNull { it.id == id }?.name } ?: ""
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                            .background(
                                if (unitId != null) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { viewModel.setEvent(FormationContract.Event.TapCell(row, col)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(unitName, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text("兵種", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.units.forEach { unit ->
                FilterChip(
                    selected = state.selectedUnitId == unit.id,
                    onClick = { viewModel.setEvent(FormationContract.Event.SelectUnit(unit.id)) },
                    label = { Text(unit.name) }
                )
            }
        }

        Spacer(Modifier.weight(1f))
        if (!state.isValid) {
            Text(
                "需恰好放置 1 位領袖，並至少 1 位其他兵種",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = { viewModel.setEvent(FormationContract.Event.ConfirmFormation) },
            enabled = state.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("開始戰鬥")
        }
    }
}
