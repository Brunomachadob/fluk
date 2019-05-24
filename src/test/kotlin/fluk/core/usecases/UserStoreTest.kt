package fluk.core.usecases

import fluk.core.Action
import fluk.core.Store
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class UserStoreTest {

    // Model/State
    class User(val name: String)

    // Actions
    class SetUserAction(val user: User): Action
    class ClearAction: Action

    @Test
    fun `User store test case`() {
        val store = Store<User?>(null) { state, action ->
            when (action) {
                is SetUserAction -> action.user
                is ClearAction -> null
                else -> state
            }
        }

        store.dispatch(SetUserAction(User("John Doe")))

        Assertions.assertEquals("John Doe", store.state?.name)

        store.dispatch(ClearAction())

        Assertions.assertEquals(null, store.state)
    }
}