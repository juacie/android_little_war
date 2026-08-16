package com.juacie.littlewar.ui.formation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juacie.littlewar.battleengine.UnitDefinition
import kotlin.math.roundToInt

// 拖曳過程中的手指座標/浮動 ghost 是畫面暫態視覺狀態，不放進 MVI State——只有放開手指、
// 確定目的格時才會發 Event.MoveUnit。offset 全程維持在 root 座標系（見下方 cellBounds）。
private data class DragState(
    val unitName: String,
    val fromRow: Int,
    val fromCol: Int,
    val offset: Offset
)

private const val GHOST_SIZE_DP = 48

@Composable
fun FormationScreen(
    onNavigateToBattle: () -> Unit,
    viewModel: FormationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cellBounds = remember { mutableStateMapOf<Pair<Int, Int>, Rect>() }
    var dragState by remember { mutableStateOf<DragState?>(null) }
    var gridContainerOffset by remember { mutableStateOf(Offset.Zero) }

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
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("排兵佈陣", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        if (state.enemyStageName.isNotEmpty()) {
            Text("敵方陣容：${state.enemyStageName}", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
        }

        // 敵我雙方的 3x5 陣區疊在同一個戰場區塊裡（跟 BattleScreen 同一個上敵下我＋中間「戰場」帶
        // 的視覺邏輯一致），排陣時就能同時看到對方陣容跟自己排了什麼，不用分開兩塊各自捲動比對。
        Box(
            modifier = Modifier.onGloballyPositioned { coords ->
                gridContainerOffset = coords.boundsInRoot().topLeft
            }
        ) {
            Column {
                if (state.enemyStageName.isNotEmpty()) {
                    for (row in listOf(2, 1, 0)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (col in 0 until 5) {
                                val index = row * 5 + col
                                val unitId = state.enemySlots[index]
                                val unitName = unitId?.let { id -> state.units.firstOrNull { it.id == id }?.name } ?: ""
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .border(1.dp, MaterialTheme.colorScheme.outline)
                                        .background(
                                            if (unitId != null) MaterialTheme.colorScheme.errorContainer
                                            else MaterialTheme.colorScheme.surface
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(unitName, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "戰場",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }

                for (row in 0 until 3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (col in 0 until 5) {
                            val index = row * 5 + col
                            val unitId = state.slots[index]
                            val unitName = unitId?.let { id -> state.units.firstOrNull { it.id == id }?.name } ?: ""
                            val cellKey = row to col
                            val isBeingDragged = dragState?.fromRow == row && dragState?.fromCol == col
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .onGloballyPositioned { coords -> cellBounds[cellKey] = coords.boundsInRoot() }
                                    .border(1.dp, MaterialTheme.colorScheme.outline)
                                    .background(
                                        if (unitId != null) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { viewModel.setEvent(FormationContract.Event.TapCell(row, col)) }
                                    .let { base ->
                                        if (unitId == null) {
                                            base
                                        } else {
                                            base.pointerInput(unitId, row, col) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        dragState = DragState(
                                                            unitName = unitName,
                                                            fromRow = row,
                                                            fromCol = col,
                                                            offset = cellBounds[cellKey]?.center ?: Offset.Zero
                                                        )
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragState = dragState?.let { it.copy(offset = it.offset + dragAmount) }
                                                    },
                                                    onDragEnd = {
                                                        val drag = dragState
                                                        if (drag != null) {
                                                            val target = cellBounds.entries
                                                                .firstOrNull { it.value.contains(drag.offset) }
                                                                ?.key
                                                            if (target != null && target != (drag.fromRow to drag.fromCol)) {
                                                                viewModel.setEvent(
                                                                    FormationContract.Event.MoveUnit(
                                                                        fromRow = drag.fromRow,
                                                                        fromCol = drag.fromCol,
                                                                        toRow = target.first,
                                                                        toCol = target.second
                                                                    )
                                                                )
                                                            }
                                                        }
                                                        dragState = null
                                                    },
                                                    onDragCancel = { dragState = null }
                                                )
                                            }
                                        }
                                    }
                                    .alpha(if (isBeingDragged) 0.3f else 1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(unitName, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            dragState?.let { drag ->
                val localOffset = drag.offset - gridContainerOffset
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (localOffset.x - GHOST_SIZE_DP.dp.toPx() / 2).roundToInt(),
                                (localOffset.y - GHOST_SIZE_DP.dp.toPx() / 2).roundToInt()
                            )
                        }
                        .size(GHOST_SIZE_DP.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        drag.unitName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.units.forEach { unit ->
                val placedCount = state.slots.count { it == unit.id }
                UnitCard(
                    unit = unit,
                    placedCount = placedCount,
                    isSelected = state.selectedUnitId == unit.id,
                    onClick = { viewModel.setEvent(FormationContract.Event.SelectUnit(unit.id)) }
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

// 卡片式兵種選單：取代原本的 FilterChip 列表，選取中的兵種用外框標示，已放置至少 1 隻時
// 右上角疊一個打勾圖示（沒有美術素材，先用內建 icon 表達「已放置」狀態）。
@Composable
private fun UnitCard(
    unit: UnitDefinition,
    placedCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cap = unit.formationCap
    Box(modifier = Modifier.width(88.dp)) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    unit.name,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    if (cap != null) "$placedCount/$cap" else "$placedCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (placedCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
