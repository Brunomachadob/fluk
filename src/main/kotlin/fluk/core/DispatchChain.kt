package fluk.core

class DispatchChain<T>(middlewares: List<Middleware<T>>) {
    private val iterator = middlewares.iterator()

    fun next(state: T, action: Action): T {
       return when(iterator.hasNext()) {
            true -> iterator.next().invoke(state, action, this)
            false -> state
        }
    }
}