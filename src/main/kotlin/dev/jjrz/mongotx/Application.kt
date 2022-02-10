package dev.jjrz.mongotx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class MongoTxApplication

fun main(args: Array<String>) {
	runApplication<MongoTxApplication>(*args)
}
