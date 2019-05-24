package fluk.core

typealias MiddlewareListener<T> = (T, Action?) -> Unit

class MonitoringMiddleware<T>(
    private val beforeChain: MiddlewareListener<T>,
    private val afterChain: MiddlewareListener<T>
): Middleware<T> {
    var called = false
        private set

    override fun dispatch(state: T, action: Action, chain: DispatchChain<T>): T {
        called = true

        beforeChain(state, action)

        return chain.next(state, action).also {
            afterChain(it, action)
        }
    }
}