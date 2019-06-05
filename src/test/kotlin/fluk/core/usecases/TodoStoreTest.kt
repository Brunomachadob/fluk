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
    data class UpdateNewTodoTextAction(val newTodoText: String): Action
    data class AddTodoAction(val text: String): Action

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
                is UpdateNewTodoTextAction -> todoState.apply { newTodoText = action.newTodoText }
                is AddTodoAction -> todoState
                    .apply { todos.add(Todo(newTodoText)) }
                    .apply { newTodoText = "" }
                else -> todoState
            }
        }

        val newTodoTextSelector = store.selector { it.newTodoText }
        val firstTodoTextSelector = store.selector { it.todos.first().text }

        store.dispatch(UpdateNewTodoTextAction("Something"))
        Assertions.assertEquals("Something", newTodoTextSelector())

        store.dispatch(AddTodoAction(newTodoTextSelector()))
        Assertions.assertEquals("", newTodoTextSelector())
        Assertions.assertEquals("Something", firstTodoTextSelector())

        Assertions.assertEquals(mutableListOf(
            "before UpdateNewTodoTextAction(newTodoText=Something): TodoState(newTodoText=, todos=[])",
            "after UpdateNewTodoTextAction(newTodoText=Something): TodoState(newTodoText=Something, todos=[])",
            "before AddTodoAction(text=Something): TodoState(newTodoText=Something, todos=[])",
            "after AddTodoAction(text=Something): TodoState(newTodoText=, todos=[Todo(text=Something)])"
        ), logger)
    }
}