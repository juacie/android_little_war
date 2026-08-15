package com.juacie.littlewar.ui.battle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juacie.littlewar.battleengine.BattleSide
import com.juacie.littlewar.battleengine.Position
import com.juacie.littlewar.battleengine.SquadSnapshot
import kotlin.math.sqrt

/** 撲擊動畫的固定位移量：不管兩個方陣實際隔多遠，攻擊者都只朝目標方向「探身」這麼多，純粹當作出手提示。 */
private val LUNGE_DISTANCE = 14.dp

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
        // 戰鬥說明文字放最上方，下方戰場區域用來實際呈現方陣移動與攻擊動畫。
        Text(
            state.logText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Battlefield(
            roster = state.roster,
            hp = state.hp,
            positions = state.positions,
            flashId = state.flashTargetId,
            activeAttack = state.activeAttack
        )

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

/**
 * 敵我雙方各自的 3x5 陣區上下對齊組成一個戰場：敵方在上、我方在下、中間留一條「戰場」空白帶，
 * 對應 Targeting.distance() 裡雙方陣區隔著 1 格空隙互相對峙的規則。方陣圖示用絕對座標覆蓋在
 * 靜態格線之上，位置隨 state.positions 變動而滑動，讓移動與攻擊真的看得出來，不是瞬間跳格。
 */
@Composable
private fun Battlefield(
    roster: List<SquadSnapshot>,
    hp: Map<String, Int>,
    positions: Map<String, Position>,
    flashId: String?,
    activeAttack: BattleContract.ActiveAttack?
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellSize = maxWidth / 5
        val neutralHeight = cellSize * 0.6f
        val totalHeight = cellSize * 6 + neutralHeight

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
        ) {
            for (row in 0..2) {
                for (col in 0..4) {
                    val (x, y) = cellOrigin(BattleSide.ENEMY, Position(row, col), cellSize, neutralHeight)
                    GridCellBackground(x = x, y = y, size = cellSize)
                }
            }
            for (row in 0..2) {
                for (col in 0..4) {
                    val (x, y) = cellOrigin(BattleSide.PLAYER, Position(row, col), cellSize, neutralHeight)
                    GridCellBackground(x = x, y = y, size = cellSize)
                }
            }

            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = cellSize * 3)
                    .size(width = cellSize * 5, height = neutralHeight)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text("戰場", style = MaterialTheme.typography.labelSmall)
            }

            for (unit in roster) {
                key(unit.id) {
                    val pos = positions[unit.id] ?: unit.position
                    val (baseX, baseY) = cellOrigin(unit.side, pos, cellSize, neutralHeight)

                    var lungeDx = 0.dp
                    var lungeDy = 0.dp
                    if (activeAttack != null && activeAttack.attackerId == unit.id) {
                        val targetPos = positions[activeAttack.targetId]
                        val targetSide = roster.firstOrNull { it.id == activeAttack.targetId }?.side
                        if (targetPos != null && targetSide != null) {
                            val (targetX, targetY) = cellOrigin(targetSide, targetPos, cellSize, neutralHeight)
                            val dx = (targetX - baseX).value
                            val dy = (targetY - baseY).value
                            val magnitude = sqrt(dx * dx + dy * dy)
                            if (magnitude > 0f) {
                                lungeDx = (dx / magnitude * LUNGE_DISTANCE.value).dp
                                lungeDy = (dy / magnitude * LUNGE_DISTANCE.value).dp
                            }
                        }
                    }

                    UnitToken(
                        unit = unit,
                        currentHp = hp[unit.id] ?: 0,
                        baseX = baseX,
                        baseY = baseY,
                        cellSize = cellSize,
                        isFlashing = unit.id == flashId,
                        isAttackingNow = activeAttack?.attackerId == unit.id,
                        attackSeq = activeAttack?.seq,
                        lungeDx = lungeDx,
                        lungeDy = lungeDy
                    )
                }
            }
        }
    }
}

/** 方陣座標換算成戰場上的絕對座標：敵方前排（row 0）貼著中間戰場帶，我方前排也貼著戰場帶另一側。 */
private fun cellOrigin(side: BattleSide, position: Position, cellSize: Dp, neutralHeight: Dp): Pair<Dp, Dp> {
    val visualRow = if (side == BattleSide.ENEMY) 2 - position.row else 4 + position.row
    val y = if (visualRow <= 2) {
        cellSize * visualRow
    } else {
        cellSize * 3 + neutralHeight + cellSize * (visualRow - 4)
    }
    val x = cellSize * position.col
    return x to y
}

@Composable
private fun GridCellBackground(x: Dp, y: Dp, size: Dp) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .padding(2.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun UnitToken(
    unit: SquadSnapshot,
    currentHp: Int,
    baseX: Dp,
    baseY: Dp,
    cellSize: Dp,
    isFlashing: Boolean,
    isAttackingNow: Boolean,
    attackSeq: Int?,
    lungeDx: Dp,
    lungeDy: Dp
) {
    val alive = currentHp > 0
    val baseColor = when {
        !alive -> MaterialTheme.colorScheme.surfaceVariant
        unit.side == BattleSide.PLAYER -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (isFlashing) MaterialTheme.colorScheme.error else baseColor,
        animationSpec = tween(150),
        label = "unitFlash"
    )
    val hpFraction by animateFloatAsState(
        targetValue = if (unit.maxHp > 0) (currentHp.toFloat() / unit.maxHp).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(300),
        label = "unitHpBar"
    )

    // 位置本身用 animateDpAsState 平滑追蹤 baseX/baseY：MoveEvent 更新目標格子時，方陣是滑過去，不是瞬移。
    val animatedX by animateDpAsState(targetValue = baseX, animationSpec = tween(380), label = "unitSlideX")
    val animatedY by animateDpAsState(targetValue = baseY, animationSpec = tween(380), label = "unitSlideY")

    // 撲擊動畫：出手瞬間往目標方向探身再彈回，用一個 0f~1f 的 Animatable 控制位移比例。
    val lungeProgress = remember { Animatable(0f) }
    LaunchedEffect(attackSeq) {
        if (isAttackingNow) {
            lungeProgress.animateTo(1f, tween(140))
            lungeProgress.animateTo(0f, tween(180))
        }
    }

    Box(
        modifier = Modifier
            .offset(
                x = animatedX + lungeDx * lungeProgress.value,
                y = animatedY + lungeDy * lungeProgress.value
            )
            .size(cellSize)
            .padding(3.dp)
            .background(backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.outline),
        contentAlignment = Alignment.Center
    ) {
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
