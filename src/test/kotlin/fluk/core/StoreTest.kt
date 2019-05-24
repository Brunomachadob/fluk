package fluk.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class StoreTest {

    @Test
    fun `A store should persist the initial state`() {
        val store = Store(42) { _, _ -> 0 }

        Assertions.assertEquals(42, store.state)
    }

    @Test
    fun `A store should mutate the state after a dispatch`() {
        val store = Store(0) { _, _ -> 42 }

        store.dispatch(object : Action {})

        Assertions.assertEquals(42, store.state)
    }

    @Test
    fun `Subscribers should be notified after a dispatch`() {
        val store = Store(0) { _, _ -> 42 }
        var subscriberState: Int? = null

        store.subscribe { subscriberState = it  }

        store.dispatch(object : Action {})

        Assertions.assertEquals(42, store.state)
        Assertions.assertEquals(42, subscriberState)
    }

    @Test
    fun `A middleware can break the dispatch chain and override the value`() {
        val action = object: Action {}

        val middleware: Middleware<Int> = { _: Int, _: Action, _: DispatchChain<Int> ->
            43
        }

        val store = Store(0, listOf(middleware)) { _, _ -> 42 }

        store.dispatch(action)

        Assertions.assertEquals(43, store.state)
    }

    @Test
    fun `The reducer value should be persisted if the middleware chain is not broken`() {
        val middleware: Middleware<Int> = { state: Int, action: Action, chain: DispatchChain<Int> ->
            chain.next(state, action)
        }

        val store = Store(0, listOf(middleware)) { _, _ -> 42 }

        store.dispatch(object: Action {})

        Assertions.assertEquals(42, store.state)
    }

    @Test
    fun `Should be possible to unsubscribe from the store`() {
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
    fun `Simple user store`() {
        class User(val name: String)
        class SetUserAction(val user: User): Action
        class ClearAction: Action

        val store = Store<User?>(null) { state, action ->
            when(action) {
                is SetUserAction -> action.user
                is ClearAction -> null
                else -> state
            }
        }

        store.dispatch(SetUserAction(User("Bruno")))

        Assertions.assertEquals("Bruno", store.state?.name)

        store.dispatch(ClearAction())

        Assertions.assertEquals(null, store.state)
    }

    @Test
    fun `A dispatch should run inside the middleware chain`() {
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
    fun `Complex todo store`() {
        data class Todo(val text: String)
        data class TodoState(var newTodoText: String, val todos: MutableList<Todo>)

        data class UpdateNewTodoText(val newTodoText: String): Action
        data class AddTodo(val text: String): Action

        val logger = mutableListOf<String>()

        val loggingMiddleware = MonitoringMiddleware<TodoState>({ state, action ->
            logger.add("before $action: $state")
        }, { state, action ->
            logger.add("after $action: $state")
        })

        val state = TodoState("", mutableListOf())

        val store = Store(state, listOf(loggingMiddleware)) { todoState, action ->
            when(action) {
                is UpdateNewTodoText -> todoState.apply { newTodoText = action.newTodoText }
                is AddTodo -> todoState
                    .apply { todos.add(Todo(newTodoText)) }
                    .apply { newTodoText = "" }
                else -> todoState
            }
        }

        store.dispatch(UpdateNewTodoText("Something"))
        Assertions.assertEquals("Something", store.state.newTodoText)

        store.dispatch(AddTodo(store.state.newTodoText))
        Assertions.assertEquals("", store.state.newTodoText)
        Assertions.assertEquals("Something", store.state.todos.first().text)

        Assertions.assertEquals(mutableListOf(
            "before UpdateNewTodoText(newTodoText=Something): TodoState(newTodoText=, todos=[])",
            "after UpdateNewTodoText(newTodoText=Something): TodoState(newTodoText=Something, todos=[])",
            "before AddTodo(text=Something): TodoState(newTodoText=Something, todos=[])",
            "after AddTodo(text=Something): TodoState(newTodoText=, todos=[Todo(text=Something)])"
        ), logger)

    }
}