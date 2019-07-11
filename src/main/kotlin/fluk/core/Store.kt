package fluk.core

typealias Reducer<T> = (T, Action) -> T
typealias Middleware<T> = (T, Action, DispatchChain<T>) -> T
typealias Selector<T, S> = (T) -> S
typealias Subscriber<T> = (T) -> Unit
typealias Unsubscriber = () -> Unit

class Store<T> (initialState: T, middlewares: List<Middleware<T>> = listOf(), reducer: Reducer<T>) {
    var state: T = initialState
        private set

    private val subscribers = mutableListOf<Subscriber<T>>()

    private val middlewares = mutableListOf<Middleware<T>>().apply {
        addAll(middlewares)
        add { state: T, action: Action, _: DispatchChain<T> ->
            reducer(state, action)
        }
    }

    fun subscribe(subscriber: Subscriber<T>): Unsubscriber {
        subscribers.add(subscriber)

        return { subscribers.remove(subscriber) }
    }

    fun <S> selector(selector: Selector<T, S>): () -> S {
        return { selector(state) }
    }

    fun <S> valueWatcher(selector: Selector<T, S>, onValueChange: (S, S) -> Unit): Unsubscriber {
        val valueWatcherMiddleware: Middleware<T> = { state, action, chain ->
            val oldValue = selector(state)

            chain.next(state, action).also {
                val newValue = selector(it)

                if (oldValue != newValue) onValueChange(oldValue, newValue)
            }
        }

        middlewares.add(0, valueWatcherMiddleware)

        return { middlewares.remove(valueWatcherMiddleware) }
    }

    fun dispatch(action: Action) {
        val dispatchChain = DispatchChain(middlewares)

        synchronized(this) {
            state = dispatchChain.next(state, action)
        }

        subscribers.forEach { it(state) }
    }
}