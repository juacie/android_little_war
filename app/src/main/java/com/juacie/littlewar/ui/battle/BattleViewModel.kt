package com.juacie.littlewar.ui.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juacie.littlewar.battleengine.AttackEvent
import com.juacie.littlewar.battleengine.BattleEndEvent
import com.juacie.littlewar.battleengine.BattleEvent
import com.juacie.littlewar.battleengine.BattleOutcome
import com.juacie.littlewar.battleengine.BattleStartEvent
import com.juacie.littlewar.battleengine.CriticalEvent
import com.juacie.littlewar.battleengine.DamageEvent
import com.juacie.littlewar.battleengine.DeathEvent
import com.juacie.littlewar.battleengine.MissEvent
import com.juacie.littlewar.battleengine.UnitSnapshot
import com.juacie.littlewar.domain.usecase.ObserveBattleResultUseCase
import com.juacie.littlewar.ui.battle.BattleContract.Effect
import com.juacie.littlewar.ui.battle.BattleContract.Event
import com.juacie.littlewar.ui.battle.BattleContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STEP_DELAY_MS = 450L
private const val FINISH_HOLD_MS = 600L

@HiltViewModel
class BattleViewModel @Inject constructor(
    observeBattleResult: ObserveBattleResultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val effectChannel = Channel<Effect>()
    val effect = effectChannel.receiveAsFlow()

    @Volatile
    private var skipRequested = false

    init {
        val result = observeBattleResult().value
        if (result == null) {
            // Shouldn't happen — FormationScreen always calls startBattle() before navigating here.
            viewModelScope.launch { effectChannel.send(Effect.NavigateToResult) }
        } else {
            val rosterById = result.roster.associateBy { it.id }
            _state.value = State(roster = result.roster, hp = result.roster.associate { it.id to it.maxHp })

            viewModelScope.launch {
                for (event in result.events) {
                    applyDamage(event)
                    _state.value = _state.value.copy(
                        flashTargetId = flashedUnitId(event),
                        logText = describeEvent(event, rosterById)
                    )
                    delay(if (skipRequested) 0L else STEP_DELAY_MS)
                }
                _state.value = _state.value.copy(isFinished = true)
                delay(FINISH_HOLD_MS)
                effectChannel.send(Effect.NavigateToResult)
            }
        }
    }

    fun setEvent(event: Event) {
        when (event) {
            Event.SkipAnimation -> skipRequested = true
        }
    }

    private fun applyDamage(event: BattleEvent) {
        if (event is DamageEvent) {
            _state.value = _state.value.copy(hp = _state.value.hp + (event.targetId to event.remainingHp))
        }
    }

    private fun flashedUnitId(event: BattleEvent): String? = when (event) {
        is DamageEvent -> event.targetId
        is MissEvent -> event.targetId
        is CriticalEvent -> event.targetId
        else -> null
    }

    private fun describeEvent(event: BattleEvent, roster: Map<String, UnitSnapshot>): String {
        fun name(id: String) = roster[id]?.name ?: id
        return when (event) {
            is BattleStartEvent -> "雙方就位，戰鬥開始！"
            is AttackEvent -> "${name(event.attackerId)} 攻擊 ${name(event.primaryTargetId)}"
            is CriticalEvent -> "${name(event.attackerId)} 對 ${name(event.targetId)} 造成會心一擊！"
            is DamageEvent -> "${name(event.attackerId)} 對 ${name(event.targetId)} 造成 ${event.amount} 點傷害"
            is MissEvent -> "${name(event.attackerId)} 的攻擊被 ${name(event.targetId)} 閃避了"
            is DeathEvent -> "${name(event.unitId)} 倒下了"
            is BattleEndEvent -> when (event.outcome) {
                BattleOutcome.PLAYER_VICTORY -> "我方勝利！"
                BattleOutcome.ENEMY_VICTORY -> "我方敗北……"
                BattleOutcome.DRAW -> "戰鬥平手"
            }
        }
    }
}
