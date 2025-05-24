/*
 * Copyright (C) 2025 RyuNen344
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE.md
 */

package io.github.ryunen344.suburi.ui.screen.mutton

import io.github.ryunen344.mutton.Action
import io.github.ryunen344.mutton.Effect
import io.github.ryunen344.mutton.EffectHandle
import io.github.ryunen344.mutton.Graph
import io.github.ryunen344.mutton.State
import io.github.ryunen344.mutton.StateMachine
import java.io.Serializable
import kotlin.coroutines.CoroutineContext

sealed class MatterState : State(), Serializable {

    data object Solid : MatterState() {
        private const val serialVersionUID = -5304911977511611813L
    }

    data object Liquid : MatterState() {
        private const val serialVersionUID = 3604356015829875965L
    }

    data object Gas : MatterState() {
        private const val serialVersionUID = -67445415829875965L
    }

    companion object {
        private const val serialVersionUID: Long = 8604736015829875965L
    }
}

sealed class MatterAction : Action() {
    data object Melt : MatterAction()
    data object Freeze : MatterAction()
    data object Vaporize : MatterAction()
    data object Condense : MatterAction()
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
