package dev.jjrz.mongo.optimistic

import dev.jjrz.mongo.HistoricalItemsRepository
import dev.jjrz.mongo.Item
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class OptimisticUpdater(
    private val latestUpdater: RetryableUpdater,
    private val latest: OptimisticRepository,
    private val historical: HistoricalItemsRepository,
) {

    fun addItem(id: Int, value: String) = OptimisticItem(id, Item(id, value))
        .let { latest.save(it) }

    fun updateItem(id: Int, text: String) = latestUpdater.update(id, text)
        ?.let { historical.save(it) }
}

@Service
class RetryableUpdater(private val latest: OptimisticRepository) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Retryable(value = [DataAccessException::class], maxAttempts = 10)
    fun update(id: Int, value: String) = latest.findById(id)
        ?.next(value)
        ?.let { (newLatest, newHistorical) -> update(newLatest, newHistorical) }

    @Suppress("DuplicatedCode")
    private fun update(newLatest: OptimisticItem, newHistorical: Item): Item {
        log.info("(??) from \"${newHistorical.value}\" to \"${newLatest.item.value}\"")
        latest.save(newLatest)
        log.info("(ok) from \"${newHistorical.value}\" to \"${newLatest.item.value}\"")
        return newHistorical
    }
}
