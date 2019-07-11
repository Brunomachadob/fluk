package fluk.core.timetravel

import fluk.core.Action
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TimeTravelReducerTest {

    @Test
    fun `it should return the same state with an invalid action `() {
        val state = TimeTravelState(0)
        val reducer = TimeTravelReducer<Int>(1)

        reducer(state, object: Action {})

        Assertions.assertEquals(0, state.currentState)
        Assertions.assertEquals(1, state.states.size)
    }

    @Test
    fun `it should save the last state and stack the old one`() {
        val state = TimeTravelState(0)
        val reducer = TimeTravelReducer<Int>(1)

        reducer(state, TimeTravelUpdateAction(1))

        Assertions.assertEquals(1, state.currentState)
        Assertions.assertEquals(1, state.states.first())
    }

    @Test
    fun `it should hold only a limited amount of states`() {
        val state = TimeTravelState(0)
        val reducer = TimeTravelReducer<Int>(2)

        reducer(state, TimeTravelUpdateAction(1))
        reducer(state, TimeTravelUpdateAction(2))

        Assertions.assertEquals(2, state.currentState)
        Assertions.assertEquals(1, state.states.first())
    }

    @Test
    fun `it should be possible to restore a state from history`() {
        val state = TimeTravelState(0)
        val reducer = TimeTravelReducer<Int>(5)

        reducer(state, TimeTravelUpdateAction(1))
        reducer(state, TimeTravelUpdateAction(2))

        reducer(state, TimeTravelAction(0))

        Assertions.assertEquals(0, state.currentState)
        Assertions.assertEquals(0, state.states.first())
        Assertions.assertEquals(2, state.states.last())
    }

    @Test
    fun `it should be possible to reset the state after using history`() {
        val state = TimeTravelState(0)
        val reducer = TimeTravelReducer<Int>(5)

        reducer(state, TimeTravelUpdateAction(1))
        reducer(state, TimeTravelUpdateAction(2))

        reducer(state, TimeTravelAction(0))

        reducer(state, TimeTravelResetAction())

        Assertions.assertEquals(2, state.currentState)
        Assertions.assertEquals(0, state.states.first())
        Assertions.assertEquals(2, state.states.last())
    }


}