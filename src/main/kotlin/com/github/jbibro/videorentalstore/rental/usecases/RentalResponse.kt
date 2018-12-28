package com.github.jbibro.videorentalstore.rental.usecases

import com.github.jbibro.videorentalstore.events.RentalCreated
import com.github.jbibro.videorentalstore.events.RentedFilm
import com.github.jbibro.videorentalstore.rental.data.Rental
import java.time.LocalDateTime
import java.util.UUID

data class RentalResponse(
    val id: UUID,
    val customerId: UUID,
    val rentedAt: LocalDateTime,
    val regularPrice: Int,
    val surcharge: Int,
    val items: List<RentedFilmResponse>
)

data class RentedFilmResponse(
    val id: UUID,
    val filmId: UUID,
    val days: Int,
    val regularPrice: Int,
    val surcharge: Int,
    val returnedAt: LocalDateTime? = null
)

fun Rental.asResponse() = RentalResponse(
    id = this.id,
    customerId = this.customer.id,
    rentedAt = this.rentedAt,
    regularPrice = this.regularPrice(),
    surcharge = this.surcharge(),
    items = this.rentedFilms.map {
        RentedFilmResponse(
            id = it.id,
            filmId = it.film.id,
            days = it.days,
            regularPrice = it.regularPrice(),
            surcharge = it.surcharge(),
            returnedAt = it.returnedAt
        )
    }
)

fun RentalResponse.asEvent() = RentalCreated(
    id = this.id,
    customerId = this.customerId,
    rentedAt = this.rentedAt,
    regularPrice = this.regularPrice,
    surcharge = this.surcharge,
    items = this.items.map {
        RentedFilm(
            id = it.id,
            filmId = it.filmId,
            days = it.days,
            regularPrice = it.regularPrice,
            surcharge = it.surcharge,
            returnedAt = it.returnedAt
        )
    }
)