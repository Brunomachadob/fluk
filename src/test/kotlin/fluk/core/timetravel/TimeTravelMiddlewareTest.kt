package fluk.core.timetravel

import fluk.core.Action
import fluk.core.DispatchChain
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

internal class TimeTravelMiddlewareTest {

    data class User(val name: String)

    @Test
    fun `it should throw an error if TimeTravelUpdateAction is used publicly`() {

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            val middleware = TimeTravelMiddleware(0)
            val dispatchChain = DispatchChain(listOf(middleware))

            dispatchChain.next(0, TimeTravelUpdateAction(1))
        }
    }

    @Test
    fun `it should keep track of the states`() {
        val middleware = TimeTravelMiddleware(User("John"))
        val dispatchChain = DispatchChain(listOf(middleware))

        Assertions.assertEquals(1, middleware.statesSelector().size)
        Assertions.assertEquals("John", middleware.currentStateSelector().name)

        val actual = dispatchChain.next(User("John Doe"), object: Action {})

        Assertions.assertEquals(User("John Doe"), actual)
        Assertions.assertEquals(User("John Doe"), middleware.currentStateSelector())
        Assertions.assertEquals(mutableListOf(
            User("John"),
            User("John Doe")
        ), middleware.statesSelector())
    }

    @Test
    fun `it should be possible to travel back in time`() {
        val middleware = TimeTravelMiddleware(User("John"))

        var state = DispatchChain(listOf(middleware)).next(User("John Doe"), object: Action {})
        state = DispatchChain(listOf(middleware)).next(state, TimeTravelAction(0))

        Assertions.assertEquals(User("John"), state)
        Assertions.assertEquals(User("John"), middleware.currentStateSelector())
        Assertions.assertEquals(mutableListOf(
            User("John"),
            User("John Doe")
        ), middleware.statesSelector())
    }

    @Test
    fun `it should be possible to reset the store after going back in time`() {
        val middleware = TimeTravelMiddleware(User("John"))

        var state = DispatchChain(listOf(middleware)).next(User("John Doe"), object: Action {})
        state = DispatchChain(listOf(middleware)).next(state, TimeTravelAction(0))
        state = DispatchChain(listOf(middleware)).next(state, TimeTravelResetAction())

        Assertions.assertEquals(User("John Doe"), state)
        Assertions.assertEquals(User("John Doe"), middleware.currentStateSelector())
        Assertions.assertEquals(mutableListOf(
            User("John"),
            User("John Doe")
        ), middleware.statesSelector())
    }

    @Test
    fun `it should keep at max n items in the state history`() {
        val maxItems = 5
        val middleware = TimeTravelMiddleware(0, maxItems)

        for (i in 1..maxItems+1) {
            DispatchChain(listOf(middleware)).next(i, object: Action {})
        }

        Assertions.assertEquals(mutableListOf(2, 3, 4, 5, 6), middleware.statesSelector())
    }
}