package fluk.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class StoreTest {

    @Test
    fun `it should have the initial state persisted`() {
        val store = Store(42) { _, _ -> 0 }

        Assertions.assertEquals(42, store.state)
    }

    @Test
    fun `it should mutate the state after a dispatch`() {
        val store = Store(0) { _, _ -> 42 }

        store.dispatch(object : Action {})

        Assertions.assertEquals(42, store.state)
    }

    @Test
    fun `it should fire subscribers after a dispatch`() {
        val store = Store(0) { _, _ -> 42 }
        var subscriberState: Int? = null

        store.subscribe { subscriberState = it  }

        store.dispatch(object : Action {})

        Assertions.assertEquals(42, store.state)
        Assertions.assertEquals(42, subscriberState)
    }

    @Test
    fun `it should be possible to break the dispatch chain and override the value with the middleware`() {
        val action = object: Action {}

        val middleware: Middleware<Int> = { _: Int, _: Action, _: DispatchChain<Int> ->
            43
        }

        val store = Store(0, listOf(middleware)) { _, _ -> 42 }

        store.dispatch(action)

        Assertions.assertEquals(43, store.state)
    }

    @Test
    fun `it should persist the reducer value if the middleware chain is not broken`() {
        val middleware: Middleware<Int> = { state: Int, action: Action, chain: DispatchChain<Int> ->
            chain.next(state, action)
        }

        val store = Store(0, listOf(middleware)) { _, _ -> 42 }

        store.dispatch(object: Action {})

        Assertions.assertEquals(42, store.state)
    }

    @Test
    fun `it should be possible to unsubscribe from the store`() {
        val store = Store(0) { state, _ -> state + 42 }
        var stateInListener: Int? = null

        val unsubscribe = store.subscribe { stateInListener = it  }

        store.dispatch(object : Action {})

        Assertions.assertEquals(42, store.state)
        Assertions.assertEquals(42, stateInListener)

        unsubscribe()

        store.dispatch(object : Action {})

        Assertions.assertEquals(84, store.state)
        Assertions.assertEquals(42, stateInListener)
    }

    @Test
    fun `it should run the reducer inside the middleware chain`() {
        val middleware = MonitoringMiddleware<Int>({ state, _ ->
            Assertions.assertEquals(0, state)
        }, {  state, _ ->
            Assertions.assertEquals(1, state)
        })

        val store = Store(0, listOf(middleware)) { state, _ -> state + 1 }

        store.dispatch(object : Action {})

        Assertions.assertEquals(middleware.called, true)
        Assertions.assertEquals(1, store.state)
    }

    @Test
    fun `it should be possible to create and use selectors`() {
        class User(val name: String)

        val store = Store(User("John Doe"), listOf()) { state, _ -> state }
        val userNameSelector = store.selector { it.name }

        Assertions.assertEquals("John Doe", userNameSelector())
    }
}