package fluk.core

interface Middleware<T> {
    fun dispatch(state: T, action: Action, next: Middleware<T> = NoOpMiddleware()): T = state
}

class NoOpMiddleware<T>: Middleware<T> {
    override fun dispatch(state: T, action: Action, next: Middleware<T>) = state
}

fun <T> composeMiddlewares(vararg middlewares: Middleware<T>): Middleware<T> {
    return object : Middleware<T> {
        override fun dispatch(state: T, action: Action, next: Middleware<T>): T {
            return next.dispatch(
                middlewares.fold(state) { acc, middleware ->
                    middleware.dispatch(acc, action)
                },
                action
            )
        }
    }
}