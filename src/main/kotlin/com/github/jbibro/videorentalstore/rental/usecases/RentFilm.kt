package com.github.jbibro.videorentalstore.rental.usecases

import com.github.jbibro.videorentalstore.customer.data.CustomerRepository
import com.github.jbibro.videorentalstore.film.data.FilmRepository
import com.github.jbibro.videorentalstore.rental.ErrorCode.CUSTOMER_NOT_FOUND
import com.github.jbibro.videorentalstore.rental.ErrorCode.FILM_NOT_FOUND
import com.github.jbibro.videorentalstore.rental.VideoRentalException
import com.github.jbibro.videorentalstore.rental.data.Rental
import com.github.jbibro.videorentalstore.rental.data.RentalRepository
import com.github.jbibro.videorentalstore.rental.data.RentedFilm
import org.springframework.context.ApplicationEventPublisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

class RentFilm(private val rentalRepository: RentalRepository,
               private val filmRepository: FilmRepository,
               private val customerRepository: CustomerRepository,
               private val eventPublisher: ApplicationEventPublisher
) {
    fun execute(input: RentFilmsInput): Mono<RentalResponse> {
        val customer = findCustomer(input.customerId)

        val rentedFilms = Flux
            .fromIterable(input.films)
            .flatMap { (filmId, days) ->
                findFilm(filmId)
                    .map { film -> RentedFilm(film = film, days = days) }
            }
            .collectList()

        return Mono.zip(customer, rentedFilms)
            .map { Rental(customer = it.t1, rentedFilms = it.t2) }
            .flatMap { rentalRepository.save(it) }
            .map { it.asResponse() }
            .doOnSuccess { eventPublisher.publishEvent(it.asEvent()) }
    }

    private fun findCustomer(id: UUID) =
        customerRepository
            .findById(id)
            .switchIfEmpty(Mono.error(VideoRentalException(CUSTOMER_NOT_FOUND)))

    private fun findFilm(id: UUID) =
        filmRepository
            .findById(id)
            .switchIfEmpty(Mono.error(VideoRentalException(FILM_NOT_FOUND, "Film Id - $id")))
}

data class RentFilmsInput(
    val customerId: UUID,
    val films: List<FilmAndRentalPeriod>
)

data class FilmAndRentalPeriod(
    val id: UUID,
    val days: Int
)