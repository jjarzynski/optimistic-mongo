# optimistic-mongo

Optimistic locking solution for write skews

## Example

Let's say we have two collections: `latest` and `historical`. When an item gets updated, there are two writes involved. The first updates the document in collection `latest`. The second saves the previous value as a new document to collection `historical`.

The diagram below shows all three steps involved in an update performed by a client named Alice:

1. Fetch previous value
2. Update `latest`
3. Save previous value to `historical`

```mermaid
sequenceDiagram
    autonumber
    actor A as Alice
    participant L as latest
    participant H as historical

    L-->>A: retrieve "0"
    A->>L: update to "1"
    A->>H: save "0"
```

## Problem

Issues arise if two clients start performing updates at the same time.

In the scenario below, Bobby retrieves value "0" right after Alice. He does that before Alice manages to complete her update. Unaware of the new value sent by Alice, Bobby updates the document in collection `latest`, and then saves "0" to collection `historical`. As far as Bobby knows, "0" is still the previous value.

We call this type of errors _skewed writes_. Bobby does everything by the book. His only problem is his outdated (hence _skewed_) information.

```mermaid
sequenceDiagram
    autonumber
    actor A as Alice
    participant L as latest
    participant H as historical
    actor B as Bobby

    L-->>A: A1. retrieve "0"
    L-->>B: B1. retrieve "0"
    
    A->>L: A2. update to "1"
    A->>H: A3. save "0"
    note over H: values: "0"
    
    B->>L: B2. update to "2"
    B->>H: B3. save "0"
    note over H: values: "0", "0"
    note over H: should be: "0", "1"
```

(...)

```kotlin
@Document("latest")
data class LatestItem(
    @Id val id: Int,
    val item: Item,
) {
    fun next(value: String) = Pair(
        copy(item = Item(id, value)),
        item
    )
}

@Document("historical")
data class Item(
    val itemId: Int,
    val value: String,
)
```

(...)

`ItemUpdaterTest`  
`ItemUpdater#updateItem`

```groovy
def "history retained"() {
    when:
    updater.addItem(1, '0')

    and:
    def integers = (1..5).toList().collect { it as String }
    integers.collect { number -> CompletableFuture.supplyAsync { updater.updateItem(1, number) } }
            .collect { future -> future.get() }

    and:
    updater.updateItem(1, 'final')

    then:
    with(historical.findAll().collect { it.value }) {
        it.containsAll(integers)
    }
}
```

(...)

```
19:14:53.007 : (??) from "0" to "5"
19:14:53.008 : (??) from "0" to "3"
19:14:53.007 : (??) from "0" to "4"
19:14:53.007 : (??) from "0" to "1"
19:14:53.008 : (??) from "0" to "2"
19:14:53.024 : (ok) from "0" to "2"
19:14:53.024 : (ok) from "0" to "3"
19:14:53.026 : (ok) from "0" to "4"
19:14:53.026 : (ok) from "0" to "5"
19:14:53.031 : (ok) from "0" to "1"
```

## Solution

Introduce an integer field annotated with `@Version`:

```kotlin
@Document("latest")
data class OptimisticItem(
    @Id val id: Int,
    val item: Item,

    @Version val version: Int = 0,
)
```

Pull out the fetch and update code to a method, so it can be retried:

```kotlin
@Retryable(value = [DataAccessException::class], maxAttempts = 10)
fun update(id: Int, value: String) = latest.findById(id)
    ?.next(value)
    ?.let { (newLatest, newHistorical) -> update(newLatest, newHistorical) }
```

`OptimisticUpdater`  
`RetryableUpdater`

(...)

```
19:27:24.274 : (??) from "0" to "2"
19:27:24.275 : (??) from "0" to "3"
19:27:24.274 : (??) from "0" to "5"
19:27:24.275 : (??) from "0" to "4"
19:27:24.275 : (??) from "0" to "1"
19:27:24.327 : (ok) from "0" to "4"

19:27:25.341 : (??) from "4" to "3"
19:27:25.341 : (??) from "4" to "5"
19:27:25.341 : (??) from "4" to "1"
19:27:25.343 : (??) from "4" to "2"
19:27:25.354 : (ok) from "4" to "1"

19:27:26.361 : (??) from "1" to "2"
19:27:26.361 : (??) from "1" to "3"
19:27:26.365 : (??) from "1" to "5"
19:27:26.365 : (ok) from "1" to "3"

19:27:27.381 : (??) from "3" to "5"
19:27:27.381 : (??) from "3" to "2"
19:27:27.386 : (ok) from "3" to "5"

19:27:28.397 : (??) from "5" to "2"
19:27:28.400 : (ok) from "5" to "2"

19:27:28.404 : (??) from "2" to "final"
19:27:28.406 : (ok) from "2" to "final"
```
