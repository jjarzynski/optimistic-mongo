package dev.jjrz.mongotx.before

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

@SpringBootTest
class MongoTxTest extends Specification {

    @Autowired LatestItemsRepository latest
    @Autowired HistoricalItemsRepository historical

    @Autowired ItemUpdater updater

    def setup() {
        latest.deleteAll()
        historical.deleteAll()
    }

    def "history not retained"() {
        when:
        updater.addItem(1, '0')

        and:
        def integers = (1..5).toList().collect { it as String }
        integers.collect { number -> CompletableFuture.supplyAsync { updater.updateItem(1, number) } }
                .collect { future -> future.get() }

        and:
        updater.updateItem(1, 'another one')

        then:
        with(historical.findAll().collect { it.value }) {
            it.containsAll(integers)
        }
    }
}
