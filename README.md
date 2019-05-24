# Fluk

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

Assertions.assertEquals(null, store.state)
```

You can find more examples at [src/test/kotlin/fluk/core/usecases](https://github.com/Brunomachadob/fluk/tree/master/src/test/kotlin/fluk/core/usecases)

## Todo

- [X] Dispatch mechanism
- [X] Middlewares
- [X] Selectors
- [X] Time travel mechanism
- [ ] Thread safe store
