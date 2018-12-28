package com.github.jbibro.videorentalstore.rental.data

import com.github.jbibro.videorentalstore.customer.data.Customer
import com.github.jbibro.videorentalstore.film.data.Film
import com.github.jbibro.videorentalstore.film.data.Type
import com.github.jbibro.videorentalstore.film.data.Type.NEW_RELEASE
import com.github.jbibro.videorentalstore.film.data.Type.OLD
import com.github.jbibro.videorentalstore.film.data.Type.REGULAR
import com.github.jbibro.videorentalstore.rental.data.Price.BASIC
import com.github.jbibro.videorentalstore.rental.data.Price.PREMIUM
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.UUID

@Document(collection = "rentals")
@TypeAlias("rental")
data class Rental(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val customer: Customer,
    val rentedFilms: List<RentedFilm>,
    val rentedAt: LocalDateTime = now()
) {
    fun regularPrice() = rentedFilms.sumBy { it.regularPrice() }
    fun surcharge() = rentedFilms.sumBy { it.surcharge() }
}

data class RentedFilm(
    val id: UUID = UUID.randomUUID(),
    val film: Film,
    val days: Int,
    val rentedAt: LocalDateTime = now(),
    val returnedAt: LocalDateTime? = null
) {
    fun regularPrice() = film.type.price(days)
    fun surcharge() = film.type.surcharge(overdueDays())
    private fun overdueDays() = (duration() - days).coerceAtLeast(0)
    private fun duration() = Duration.between(rentedAt, returnedAt ?: now()).days()
}

private fun Type.price(days: Int) =
    if (days == 0) 0 else
        when (this) {
            NEW_RELEASE -> PREMIUM.amount * days
            REGULAR -> BASIC.amount + (days - 3).coerceAtLeast(0) * BASIC.amount
            OLD -> BASIC.amount + (days - 5).coerceAtLeast(0) * BASIC.amount
        }

private fun Type.surcharge(overdueDays: Int) = when (this) {
    NEW_RELEASE -> PREMIUM.amount * overdueDays
    else -> BASIC.amount * overdueDays
}

private fun Duration.days(): Int {
    val addExtraDay = this.toHours() % 24 > 0 || this.toMinutes() % 60 > 0
    return this.toDays().toInt() + if (addExtraDay) 1 else 0
}