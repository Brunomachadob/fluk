package fluk.core

interface Middleware<T> {
    fun dispatch(state: T, action: Action, chain: DispatchChain<T>): T
}