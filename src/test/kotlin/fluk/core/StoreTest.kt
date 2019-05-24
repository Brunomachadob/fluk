package fluk.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class StoreTest {

    @Test
    fun `A store should persist initial state`() {
        val store = Store(42) { _, _ -> 0 }

        Assertions.assertEquals(42, store.state)
    }

    @Test
    fun `A store should mutate state after a dispatch`() {
        val store = Store(0) { _, _ -> 42 }

        store.dispatch(object : Action {})

        Assertions.assertEquals(42, store.state)
    }

    @Test
    fun `Subscribers should be notified after a dispatch`() {
        val store = Store(0) { _, _ -> 42 }
        var stateInListener: Int? = null

        store.subscribe { stateInListener = it  }

        store.dispatch(object : Action {})

        Assertions.assertEquals(42, store.state)
        Assertions.assertEquals(42, stateInListener)
    }

    @Test
    fun `A middleware can break the dispatch chain and override the value`() {
        val action = object: Action {}

        val middleware = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, next: Middleware<Int>) = 43
        }

        val store = Store(0, middleware) { _, _ -> 42 }

        store.dispatch(action)

        Assertions.assertEquals(43, store.state)
    }

    @Test
    fun `Composed middlewares`() {
        val action = object: Action {}

        val middleware1 = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, next: Middleware<Int>) = next.dispatch(state, action)
        }

        val middleware2 = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, next: Middleware<Int>) = next.dispatch(state, action)
        }

        val store = Store(0, composeMiddlewares(middleware1, middleware2)) { _, _ -> 42 }

        store.dispatch(action)

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
        val middleware = AssertionMiddleware<Int>({
            Assertions.assertEquals(0, it)
        }, {
            Assertions.assertEquals(1, it)
        })

        val store = Store(0, middleware) { state, _ -> state + 1 }

        store.dispatch(object : Action {})

        Assertions.assertEquals(middleware.called, true)
        Assertions.assertEquals(1, store.state)

    }

    @Test
    fun `Complex todo store`() {
        class Todo(val text: String)
        class TodoState(var newTodoText: String, val todos: MutableList<Todo>)

        class UpdateNewTodoText(val newTodoText: String): Action
        class AddTodo: Action

        val store = Store(TodoState("", mutableListOf())) { todoState, action ->
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

        store.dispatch(AddTodo())
        Assertions.assertEquals("", store.state.newTodoText)
        Assertions.assertEquals("Something", store.state.todos.first().text)

    }
}