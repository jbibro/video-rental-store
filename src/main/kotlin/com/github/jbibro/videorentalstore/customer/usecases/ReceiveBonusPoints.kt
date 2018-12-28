package com.github.jbibro.videorentalstore.customer.usecases

import com.github.jbibro.videorentalstore.customer.data.Customer
import com.github.jbibro.videorentalstore.events.RentalCreated
import com.github.jbibro.videorentalstore.events.RentedFilm
import com.github.jbibro.videorentalstore.film.data.Film
import com.github.jbibro.videorentalstore.film.data.FilmRepository
import com.github.jbibro.videorentalstore.film.data.Type.NEW_RELEASE
import com.mongodb.client.result.UpdateResult
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.core.publisher.Mono
import java.util.UUID

class ReceiveBonusPoints(private val mongoTemplate: ReactiveMongoTemplate,
                         private val filmRepository: FilmRepository) {
    @EventListener
    fun onRentalEvent(rental: RentalCreated) {
        rental.items.bonusPoints()
            .flatMap { saveBonusPoints(rental.customerId, it) }
            .subscribe()
    }

    private fun List<RentedFilm>.bonusPoints(): Mono<Int> {
        val filmOccurrences = this.groupingBy { it.filmId }.eachCount()
        return filmRepository
            .findAllById(this.map { it.filmId })
            .map { film -> film.bonusPoints() * filmOccurrences[film.id]!! }
            .reduce(0, Integer::sum)
    }

    private fun Film.bonusPoints() = when (this.type) {
        NEW_RELEASE -> 2
        else -> 1
    }

    private fun saveBonusPoints(customerId: UUID, bonusPoints: Int): Mono<UpdateResult> =
        mongoTemplate
            .updateFirst(
                query(where("id").isEqualTo(customerId)),
                Update().inc("bonusPoints", bonusPoints),
                Customer::class.java
            )
}