package dev.jjrz.mongo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class OptimisticMongoApplication

fun main(args: Array<String>) {
	runApplication<OptimisticMongoApplication>(*args)
}
