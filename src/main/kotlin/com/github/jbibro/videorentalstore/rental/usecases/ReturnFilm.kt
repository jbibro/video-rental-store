package com.github.jbibro.videorentalstore.rental.usecases

import com.github.jbibro.videorentalstore.rental.ErrorCode.RENTAL_NOT_FOUND
import com.github.jbibro.videorentalstore.rental.VideoRentalException
import com.github.jbibro.videorentalstore.rental.data.RentalRepository
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.exists
import com.mongodb.client.model.Filters.not
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.set
import com.mongodb.reactivestreams.client.MongoDatabase
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.time.LocalDateTime.now
import java.util.UUID


class ReturnFilm(
    private val mongoDatabase: MongoDatabase, // spring data mongodb doesn't support arrayFilters yet
    private val rentalRepository: RentalRepository
) {
    fun execute(input: ReturnFilmInput) =
        mongoDatabase
            .getCollection("rentals")
            .updateOne(
                eq("_id", input.rentalId),
                set("rentedFilms.$[element].returnedAt", now()),
                UpdateOptions().arrayFilters(listOf(
                    and(
                        not(exists("element.returnedAt")),
                        `in`("element._id", input.items)
                    )
                ))
            )
            .toMono()
            .flatMap {
                when (it.matchedCount) {
                    0L -> Mono.error(VideoRentalException(RENTAL_NOT_FOUND))
                    else -> Mono.just(it)
                }
            }
            .log()
            .then(rentalRepository.findById(input.rentalId))
            .map { it.asResponse() }
}

data class ReturnFilmInput(
    val rentalId: UUID,
    val items: List<UUID>
)
