package fluk.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class MiddlewareTest {

    @Test
    fun `should be possible to compose multiple middlewares`() {
        val action = object: Action {}

        val midSumBy1 = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, chain: DispatchChain<Int>) = chain.next(state + 1, action)
        }

        val midTimes2 = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, chain: DispatchChain<Int>) = chain.next(state * 2, action)
        }

        val middlewareChain = DispatchChain(listOf(midSumBy1, midTimes2))

        val actual = middlewareChain.next(1, action)

        Assertions.assertEquals(4, actual)
    }

    @Test
    fun `Middlewares should run chained`() {
        val action = object: Action {}

        val mid1 = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, chain: DispatchChain<Int>): Int {
                Assertions.assertEquals(1, state)
                return chain.next(state + 1, action).also {
                    Assertions.assertEquals(3, it)
                }
            }
        }

        val mid2 = object : Middleware<Int> {
            override fun dispatch(state: Int, action: Action, chain: DispatchChain<Int>): Int {
                Assertions.assertEquals(2, state)
                return chain.next(state + 1, action).also {
                    Assertions.assertEquals(3, it)
                }
            }
        }

        val middlewareChain = DispatchChain(listOf(mid1, mid2))

        val actual = middlewareChain.next(1, action)

        Assertions.assertEquals(3, actual)
    }
}