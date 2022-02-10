package dev.jjrz.mongotx

import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemUpdaterTx(
    private val latestUpdater: LatestUpdater,
    private val latest: LatestItemsRepositoryTx,
    private val historical: HistoricalItemsRepositoryTx,
) {

    fun addItem(id: Int, value: String) = LatestItemTx(id, ItemTx(id, value))
        .let { latest.save(it) }

    fun updateItem(id: Int, text: String) = latestUpdater.update(id, text)
        ?.let { historical.save(it) }
}

@Service
class LatestUpdater(private val latest: LatestItemsRepositoryTx) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Retryable(value = [DataAccessException::class], maxAttempts = 10)
    fun update(id: Int, value: String) = latest.findById(id)
        ?.next(value)
        ?.let { (newLatest, newHistorical) -> update(newLatest, newHistorical) }

    private fun update(newLatest: LatestItemTx, newHistorical: ItemTx): ItemTx {
        log.info("(??) from \"${newHistorical.value}\" to \"${newLatest.item.value}\"")
        latest.save(newLatest)
        log.info("(ok) from \"${newHistorical.value}\" to \"${newLatest.item.value}\"")
        return newHistorical
    }
}
