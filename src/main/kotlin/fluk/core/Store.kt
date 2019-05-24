package fluk.core

typealias Reducer<T> = (T, Action) -> T
typealias Subscriber<T> = (T) -> Unit
typealias Unsubscriber = () -> Unit

class Store<T> (initialState: T, middlewares: List<Middleware<T>> = listOf(), reducer: Reducer<T>) {
    var state: T = initialState
        private set

    private val subscribers = mutableListOf<Subscriber<T>>()

    private val reducerMiddleware: Middleware<T> = object : Middleware<T> {
        override fun dispatch(state: T, action: Action, chain: DispatchChain<T>): T {
            return reducer(state, action)
        }
    }

    private val middlewares = mutableListOf<Middleware<T>>().apply {
        addAll(middlewares)
        add(reducerMiddleware)
    }.toList()

    fun subscribe(subscriber: Subscriber<T>): Unsubscriber {
        subscribers.add(subscriber)

        return { subscribers.remove(subscriber) }
    }

    fun dispatch(action: Action) {
        val middlewareChain = DispatchChain(middlewares)

        state = middlewareChain.next(state, action)

        subscribers.forEach { it(state) }
    }
}