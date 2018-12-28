package com.github.jbibro.videorentalstore.customer.usecases

import com.github.jbibro.videorentalstore.customer.data.Customer
import com.github.jbibro.videorentalstore.customer.data.CustomerRepository
import com.github.jbibro.videorentalstore.events.RentalCreated
import com.github.jbibro.videorentalstore.events.RentedFilm
import com.github.jbibro.videorentalstore.film.data.Film
import com.github.jbibro.videorentalstore.film.data.FilmRepository
import com.github.jbibro.videorentalstore.film.data.Type.NEW_RELEASE
import com.github.jbibro.videorentalstore.film.data.Type.OLD
import com.github.jbibro.videorentalstore.rental.usecases.RentalResponse
import com.github.jbibro.videorentalstore.rental.usecases.RentedFilmResponse
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import reactor.test.test
import java.time.LocalDateTime
import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit.SECONDS

@DataMongoTest
class ReceiveBonusPointsTest {

    @Autowired
    lateinit var filmRepository: FilmRepository

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var receiveBonusPoints: ReceiveBonusPoints

    @BeforeEach
    fun before() {
        customerRepository.save(customer)
            .test()
            .expectNextCount(1)
            .verifyComplete()

        filmRepository.saveAll(listOf(newRelease, oldFilm))
            .test()
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `adds bonus points`() {
        //given
        val event = RentalCreated(
            id = randomUUID(),
            rentedAt = LocalDateTime.now(),
            customerId = customer.id,
            regularPrice = 0,
            surcharge = 0,
            items = listOf(
                RentedFilm(id = randomUUID(), filmId = newRelease.id, days = 0, regularPrice = 0, surcharge = 0), // gives 2 points
                RentedFilm(id = randomUUID(), filmId = oldFilm.id, days = 0, regularPrice = 0, surcharge = 0) // gives 1 point
            )
        )

        //when
        receiveBonusPoints.onRentalEvent(event)

        //then
        await().atMost(3, SECONDS).until {
            customerRepository.findById(customer.id).block()!!.bonusPoints == 3
        }
    }

    @Test
    fun `the same film should be counted twice`() {
        //given
        val event = RentalCreated(
            id = randomUUID(),
            rentedAt = LocalDateTime.now(),
            customerId = customer.id,
            regularPrice = 0,
            surcharge = 0,
            items = listOf(
                RentedFilm(id = randomUUID(), filmId = oldFilm.id, days = 0, regularPrice = 0, surcharge = 0), // gives 1 points
                RentedFilm(id = randomUUID(), filmId = oldFilm.id, days = 0, regularPrice = 0, surcharge = 0) // gives 1 point
            )
        )

        //when
        receiveBonusPoints.onRentalEvent(event)

        //then
        await().atMost(3, SECONDS).until {
            customerRepository.findById(customer.id).block()!!.bonusPoints == 2
        }
    }

    companion object {
        private val customer = Customer(
            id = randomUUID(),
            name = "John"
        )
        private val newRelease = Film(
            id = randomUUID(),
            title = "Spiderman 2",
            type = NEW_RELEASE
        )
        private val oldFilm = Film(
            id = randomUUID(),
            title = "Pulp fiction",
            type = OLD
        )
    }
}