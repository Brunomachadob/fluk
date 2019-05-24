package fluk.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class MiddlewareTest {

    @Test
    fun `NoOpMiddleware should not modify the state`() {
        val action = object: Action {}
        val middleware = NoOpMiddleware<Int>()

        val actual = middleware.dispatch(42, action)

        Assertions.assertEquals(42, actual)
    }

    @Test
    fun `should be possible to compose multiple middlewares`() {
        val action = object: Action {}

        val midSumBy1 = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, next: Middleware<Int>) = next.dispatch(state + 1, action)
        }

        val midTimes2 = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, next: Middleware<Int>) = next.dispatch(state * 2, action)
        }

        val composed = composeMiddlewares(midSumBy1, midTimes2)

        val actual = composed.dispatch(1, action)

        Assertions.assertEquals(4, actual)
    }
}