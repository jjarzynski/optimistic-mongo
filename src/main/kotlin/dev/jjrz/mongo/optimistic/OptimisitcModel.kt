package dev.jjrz.mongo.optimistic

import dev.jjrz.mongo.Item
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

@Document("items")
data class OptimisticItem(
    @Id val id: Int,
    val item: Item,

    @Version val version: Int = 0,
) {
    fun next(value: String) = Pair(
        copy(item = Item(id, value)),
        item
    )
}

interface OptimisticRepository : MongoRepository<OptimisticItem, Any> {
    fun findById(id: Int): OptimisticItem?
}
