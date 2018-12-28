package com.github.jbibro.videorentalstore.events

import java.time.LocalDateTime
import java.util.UUID

data class RentalCreated(
    val id: UUID,
    val customerId: UUID,
    val rentedAt: LocalDateTime,
    val regularPrice: Int,
    val surcharge: Int,
    val items: List<RentedFilm>
)

data class RentedFilm(
    val id: UUID,
    val filmId: UUID,
    val days: Int,
    val regularPrice: Int,
    val surcharge: Int,
    val returnedAt: LocalDateTime? = null
)