package fluk.core.usecases

import fluk.core.Action
import fluk.core.MonitoringMiddleware
import fluk.core.Store
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TodoStoreTest {

    // Model
    data class Todo(val text: String)

    // State
    data class TodoState(var newTodoText: String, val todos: MutableList<Todo>)

    // Actions
    data class UpdateNewTodoText(val newTodoText: String): Action
    data class AddTodo(val text: String): Action

    @Test
    fun `Todo store test case`() {


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