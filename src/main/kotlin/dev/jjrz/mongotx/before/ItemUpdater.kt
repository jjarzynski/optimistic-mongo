package dev.jjrz.mongotx.before

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ItemUpdater(
    private val latest: LatestItemsRepository,
    private val historical: HistoricalItemsRepository,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun addItem(id: Int, value: String) = LatestItem(id, Item(id, value))
        .let { latest.save(it) }

    fun updateItem(id: Int, text: String) = latest.findById(id)
        ?.next(text)
        ?.let { (newLatest, newHistorical) -> update(newLatest, newHistorical) }
        ?.let { historical.save(it) }

    private fun update(newLatest: LatestItem, newHistorical: Item): Item {

        latest.save(newLatest)
        log.info("updated from \"${newHistorical.value}\" to \"${newLatest.item.value}\"")

        return newHistorical
    }
}
