# Fluk
[![Maintainability](https://api.codeclimate.com/v1/badges/2ee40d3c924df3493001/maintainability)](https://codeclimate.com/github/Brunomachadob/fluk/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/2ee40d3c924df3493001/test_coverage)](https://codeclimate.com/github/Brunomachadob/fluk/test_coverage)
---

[Flux](https://facebook.github.io/flux/) architecture implementation for Kotlin


The main goals of this is implementation is to be `simple`, `easy to use` and `fun to work with`


## Simple usage example

```kotlin
// Model/State
class User(val name: String)

// Actions
class SetUserAction(val user: User): Action
class ClearAction: Action
    
val store = Store<User?>(null) { state, action ->
    when (action) {
        is SetUserAction -> action.user
        is ClearAction -> null
        else -> state
    }
}

val userNameSelector = store.selector { it?.name }

store.dispatch(SetUserAction(User("John Doe")))

Assertions.assertEquals("John Doe", userNameSelector())

store.dispatch(ClearAction())

Assertions.assertEquals(null, userNameSelector())
```

You can find more examples at [src/test/kotlin/fluk/core/usecases](https://github.com/Brunomachadob/fluk/tree/master/src/test/kotlin/fluk/core/usecases)

## Todo

- [X] Dispatch mechanism
- [X] Middlewares
- [X] Subscribers
- [X] Value watchers
- [X] Selectors
- [X] Time travel mechanism
- [X] Thread safe store
