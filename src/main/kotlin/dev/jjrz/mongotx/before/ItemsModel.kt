package dev.jjrz.mongotx.before

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

@Document("items")
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
data class Item(val itemId: Int, val value: String)

interface LatestItemsRepository : MongoRepository<LatestItem, Any> {
    fun findById(id: Int): LatestItem?
}

interface HistoricalItemsRepository : MongoRepository<Item, Any>
