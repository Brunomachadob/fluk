package fluk.core.timetravel

class TimeTravelState<T>(initialState: T) {
    var currentState = initialState
        internal set

    var states = mutableListOf(currentState)
        internal set
}