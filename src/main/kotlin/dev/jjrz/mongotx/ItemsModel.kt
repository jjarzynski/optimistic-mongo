package dev.jjrz.mongotx

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

@Document("items")
data class LatestItemTx(
    @Id val id: Int,
    val item: ItemTx,

    @Version val version: Int = 0,
) {
    fun next(value: String) = Pair(
        copy(item = ItemTx(id, value)),
        item
    )
}

@Document("historical")
data class ItemTx(val itemId: Int, val value: String)

interface LatestItemsRepositoryTx : MongoRepository<LatestItemTx, Any> {
    fun findById(id: Int): LatestItemTx?
}

interface HistoricalItemsRepositoryTx : MongoRepository<ItemTx, Any>
