package com.juacie.littlewar.ui.battle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juacie.littlewar.battleengine.BattleSide
import com.juacie.littlewar.battleengine.UnitSnapshot

@Composable
fun BattleScreen(
    onNavigateToResult: () -> Unit,
    viewModel: BattleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BattleContract.Effect.NavigateToResult -> onNavigateToResult()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("敵方", style = MaterialTheme.typography.titleSmall)
        SideGrid(side = BattleSide.ENEMY, rowOrder = listOf(2, 1, 0), roster = state.roster, hp = state.hp, flashId = state.flashTargetId)

        Spacer(Modifier.height(12.dp))
        Text(
            state.logText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Spacer(Modifier.height(12.dp))

        Text("我方", style = MaterialTheme.typography.titleSmall)
        SideGrid(side = BattleSide.PLAYER, rowOrder = listOf(0, 1, 2), roster = state.roster, hp = state.hp, flashId = state.flashTargetId)

        Spacer(Modifier.weight(1f))
        if (!state.isFinished) {
            OutlinedButton(
                onClick = { viewModel.setEvent(BattleContract.Event.SkipAnimation) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("跳過動畫")
            }
        }
    }
}

@Composable
private fun SideGrid(
    side: BattleSide,
    rowOrder: List<Int>,
    roster: List<UnitSnapshot>,
    hp: Map<String, Int>,
    flashId: String?
) {
    val unitsByCell = remember(roster, side) {
        roster.filter { it.side == side }.associateBy { it.position.row to it.position.col }
    }

    Column {
        for (row in rowOrder) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until 5) {
                    val unit = unitsByCell[row to col]
                    BattleCell(
                        unit = unit,
                        currentHp = unit?.let { hp[it.id] } ?: 0,
                        isFlashing = unit != null && unit.id == flashId,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun BattleCell(unit: UnitSnapshot?, currentHp: Int, isFlashing: Boolean, modifier: Modifier = Modifier) {
    val alive = unit != null && currentHp > 0
    val baseColor = when {
        unit == null -> MaterialTheme.colorScheme.surface
        !alive -> MaterialTheme.colorScheme.surfaceVariant
        unit.side == BattleSide.PLAYER -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (isFlashing) MaterialTheme.colorScheme.error else baseColor,
        animationSpec = tween(150),
        label = "cellFlash"
    )
    val hpFraction by animateFloatAsState(
        targetValue = if (unit != null) (currentHp.toFloat() / unit.maxHp).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(300),
        label = "hpBar"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (unit != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (alive) unit.name else "×",
                    style = MaterialTheme.typography.labelSmall
                )
                if (alive) {
                    LinearProgressIndicator(
                        progress = { hpFraction },
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .fillMaxWidth(0.7f)
                            .height(3.dp)
                    )
                }
            }
        }
    }
}
