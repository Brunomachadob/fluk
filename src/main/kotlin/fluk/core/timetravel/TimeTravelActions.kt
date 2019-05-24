package fluk.core.timetravel

import fluk.core.Action

internal class TimeTravelUpdateAction<T>(val newState: T): Action
class TimeTravelAction(val index: Int): Action
class TimeTravelResetAction: Action