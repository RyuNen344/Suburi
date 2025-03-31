package io.github.ryunen344.suburi.state

import io.github.ryunen344.mutton.Action
import io.github.ryunen344.mutton.Effect
import io.github.ryunen344.mutton.EffectHandle
import io.github.ryunen344.mutton.Graph
import io.github.ryunen344.mutton.State
import io.github.ryunen344.mutton.StateMachine
import kotlin.coroutines.CoroutineContext

sealed class MatterState : State(), java.io.Serializable {
    data object Solid : MatterState() {
        private fun readResolve(): Any = Solid
    }

    data object Liquid : MatterState() {
        private fun readResolve(): Any = Liquid
    }

    data object Gas : MatterState() {
        private fun readResolve(): Any = Gas
    }
}

sealed class MatterAction : Action() {
    object Melt : MatterAction()
    object Freeze : MatterAction()
    object Vaporize : MatterAction()
    object Condense : MatterAction()
}

sealed class MatterEffect : Effect()

class MatterStateMachine(
    initialState: MatterState = MatterState.Solid,
    context: CoroutineContext,
) : StateMachine<MatterState, MatterAction, MatterEffect>(
    initialState = initialState,
    graph = Graph<MatterState, MatterAction, MatterEffect> {
        state<MatterState.Solid> {
            action<MatterAction.Melt> { _, _ ->
                transition(MatterState.Liquid)
            }
        }
        state<MatterState.Liquid> {
            action<MatterAction.Freeze> { _, _ ->
                transition(MatterState.Solid)
            }
            action<MatterAction.Vaporize> { _, _ ->
                transition(MatterState.Gas)
            }
        }
        state<MatterState.Gas> {
            action<MatterAction.Condense> { _, _ ->
                transition(MatterState.Liquid)
            }
        }
    },
    effectHandle = EffectHandle<MatterState, MatterAction, MatterEffect> { _, _, _, _ ->
        // noop
    },
    logger = TimberStateMachineLogger,
    context = context,
)
