package fluk.core.timetravel

import fluk.core.Action
import fluk.core.Reducer

class TimeTravelReducer<T>(private val maxItems: Int): Reducer<TimeTravelState<T>> {
    override fun invoke(state: TimeTravelState<T>, action: Action): TimeTravelState<T> {
        return when(action) {
            is TimeTravelUpdateAction<*> -> state
                .apply { currentState = action.newState as T }
                .apply { if (states.size == maxItems) states.removeAt(0); states.add(currentState) }
            is TimeTravelAction -> state.apply { currentState = states[action.index] }
            is TimeTravelResetAction -> state.apply { currentState = states.last() }
            else -> state
        }
    }
}