package fluk.core.usecases

import fluk.core.Action
import fluk.core.Store
import fluk.core.timetravel.TimeTravelMiddleware
import fluk.core.timetravel.TimeTravelResetAction
import fluk.core.timetravel.TimeTravelAction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class UserStoreTimeTravelTest {

    // Model/State
    class User(val name: String)

    // Actions
    class SetUserAction(val user: User): Action
    class ClearAction: Action

    @Test
    fun `User store with time travel test case`() {
        val store = Store(null, listOf(TimeTravelMiddleware<User?>(null))) { state, action ->
            when (action) {
                is SetUserAction -> action.user
                is ClearAction -> null
                else -> state
            }
        }

        val userSelector = store.selector { it }
        val userNameSelector = store.selector { it?.name }

        store.dispatch(SetUserAction(User("John Doe")))

        Assertions.assertEquals("John Doe", userNameSelector())

        store.dispatch(ClearAction())

        Assertions.assertEquals(null, userSelector())

        store.dispatch(TimeTravelAction(1))

        Assertions.assertEquals("John Doe", userNameSelector())

        store.dispatch(TimeTravelResetAction())

        Assertions.assertEquals(null, userSelector())
    }
}