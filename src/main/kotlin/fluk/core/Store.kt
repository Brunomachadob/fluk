package fluk.core

typealias Reducer<T> = (T, Action) -> T
typealias Subscriber<T> = (T) -> Unit
typealias Unsubscriber = () -> Unit

class Store<T> (initialState: T, private val middleware: Middleware<T>? = null, reducer: Reducer<T>) {
    var state: T = initialState
        private set

    private val subscribers = mutableListOf<Subscriber<T>>()

    private val reducerMiddleware: Middleware<T> = object : Middleware<T> {
        override fun dispatch(currentState: T, action: Action, next: Middleware<T>): T {
            return reducer(currentState, action)
        }
    }

    operator fun component1() = state

    fun subscribe(subscriber: Subscriber<T>): Unsubscriber {
        subscribers.add(subscriber)

        return { subscribers.remove(subscriber) }
    }

    fun dispatch(action: Action) {
        state = if (middleware == null) {
            reducerMiddleware.dispatch(state, action, NoOpMiddleware())
        } else {
            middleware.dispatch(state, action, reducerMiddleware)
        }

        subscribers.forEach { it(state) }
    }
}