package fluk.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class MiddlewareTest {

    @Test
    fun `should be possible to compose multiple middlewares`() {
        val midSumBy1: Middleware<Int> = { state: Int, action: Action, chain: DispatchChain<Int> ->
            chain.next(state + 1, action)
        }

        val midTimes2: Middleware<Int> = { state: Int, action: Action, chain: DispatchChain<Int> ->
            chain.next(state * 2, action)
        }

        val middlewareChain = DispatchChain(listOf(midSumBy1, midTimes2))

        val actual = middlewareChain.next(1, object: Action {})

        Assertions.assertEquals(4, actual)
    }

    @Test
    fun `Middlewares should run chained`() {
        val mid1: Middleware<Int> = { state: Int, action: Action, chain: DispatchChain<Int> ->
            Assertions.assertEquals(1, state)
            chain.next(state + 1, action).also {
                Assertions.assertEquals(3, it)
            }
        }

        val mid2: Middleware<Int> = { state: Int, action: Action, chain: DispatchChain<Int> ->
            Assertions.assertEquals(2, state)
            chain.next(state + 1, action).also {
                Assertions.assertEquals(3, it)
            }
        }

        val middlewareChain = DispatchChain(listOf(mid1, mid2))

        val actual = middlewareChain.next(1, object: Action {})

        Assertions.assertEquals(3, actual)
    }
}