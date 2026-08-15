package com.juacie.littlewar.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juacie.littlewar.battleengine.BattleOutcome

@Composable
fun ResultScreen(
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ResultContract.Effect.NavigateToFormation -> onPlayAgain()
                ResultContract.Effect.NavigateToHome -> onHome()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val result = state.result
        if (result == null) {
            Text("沒有戰鬥紀錄", style = MaterialTheme.typography.headlineSmall)
        } else {
            val title = when (result.outcome) {
                BattleOutcome.PLAYER_VICTORY -> "勝利！"
                BattleOutcome.ENEMY_VICTORY -> "敗北"
                BattleOutcome.DRAW -> "平手"
            }
            Text(title, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(16.dp))
            Text("戰鬥回合數：${result.totalTicks}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "我方存活：${result.survivingPlayerUnitIds.size} ／ 敵方存活：${result.survivingEnemyUnitIds.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(32.dp))
        Button(onClick = { viewModel.setEvent(ResultContract.Event.PlayAgain) }, modifier = Modifier.fillMaxWidth()) {
            Text("再玩一次")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.setEvent(ResultContract.Event.GoHome) }, modifier = Modifier.fillMaxWidth()) {
            Text("回首頁")
        }
    }
}
