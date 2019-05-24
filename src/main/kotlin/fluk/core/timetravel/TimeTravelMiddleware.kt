package fluk.core.timetravel

import fluk.core.*
import java.lang.IllegalArgumentException

class TimeTravelMiddleware<T>(initialState: T, maxItems: Int = 50): Middleware<T> {
    private val store = Store(TimeTravelState(initialState), listOf(), TimeTravelReducer(maxItems))

    val currentStateSelector = store.selector { it.currentState }
    val statesSelector = store.selector { it.states }

    override fun invoke(state: T, action: Action, chain: DispatchChain<T>): T {
        return when(action) {
            is TimeTravelUpdateAction<*> -> throw IllegalArgumentException("TimeTravelUpdateAction is an internal action that should not be used")
            is TimeTravelAction, is TimeTravelResetAction -> { store.dispatch(action); currentStateSelector() }
            else -> chain.next(state, action).also { store.dispatch(TimeTravelUpdateAction(it)) }
        }
    }
}