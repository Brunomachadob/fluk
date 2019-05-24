package fluk.core

typealias MiddlewareListener<T> = (T) -> Unit

class AssertionMiddleware<T>(
    private val beforeListener: MiddlewareListener<T>,
    private val afterListener: MiddlewareListener<T>
): Middleware<T> {
    var called = false
        private set

    override fun dispatch(state: T, action: Action, next: Middleware<T>): T {
        called = true

        beforeListener(state)

        return next.dispatch(state, action).also {
            afterListener(it)
        }
    }
}