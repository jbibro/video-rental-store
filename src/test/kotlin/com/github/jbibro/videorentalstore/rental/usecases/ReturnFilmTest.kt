package com.github.jbibro.videorentalstore.rental.usecases

import com.github.jbibro.videorentalstore.customer.data.Customer
import com.github.jbibro.videorentalstore.customer.data.CustomerRepository
import com.github.jbibro.videorentalstore.film.data.Film
import com.github.jbibro.videorentalstore.film.data.Type.NEW_RELEASE
import com.github.jbibro.videorentalstore.film.data.Type.OLD
import com.github.jbibro.videorentalstore.rental.data.Rental
import com.github.jbibro.videorentalstore.rental.data.RentalRepository
import com.github.jbibro.videorentalstore.rental.data.RentedFilm
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import reactor.test.test
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.UUID.randomUUID

@DataMongoTest
class ReturnFilmTest {

    @Autowired
    lateinit var rentalRepository: RentalRepository

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var returnFilm: ReturnFilm

    @BeforeEach
    fun beforeEach() {
        customerRepository.save(customer)
            .test()
            .expectNextCount(1)
            .verifyComplete()

        rentalRepository.save(rental)
            .test()
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `return single film`() {
        //then
        returnFilm.execute(ReturnFilmInput(rentalId = rental.id, items = listOf(pulpFiction.id)))
            .test()
            .expectNextMatches {
                it.items.count { item -> item.returnedAt != null } == 1
            }
            .verifyComplete()
    }

    @Test
    fun `return two films at the same time`() {
        //then
        returnFilm.execute(ReturnFilmInput(rentalId = rental.id, items = listOf(pulpFiction.id, spiderman.id)))
            .test()
            .expectNextMatches {
                it.items.count { item -> item.returnedAt != null } == 2
            }
            .verifyComplete()
    }

    @Test
    fun `it's not possible to return same film twice`() {
        // given I return pulp fiction
        var returnedDate: LocalDateTime? = null
        returnFilm.execute(ReturnFilmInput(rentalId = rental.id, items = listOf(pulpFiction.id)))
            .test()
            .consumeNextWith { returnedDate = it.items.first { item -> item.id == pulpFiction.id }.returnedAt!! }
            .verifyComplete()

        // then I can't return it again (returnedDate stays the same)
        returnFilm.execute(ReturnFilmInput(rentalId = rental.id, items = listOf(pulpFiction.id)))
            .test()
            .expectNextMatches{ it.items.first { item -> item.id == pulpFiction.id }.returnedAt!! == returnedDate }
            .verifyComplete()
    }
    companion object {
        private val spiderman = RentedFilm(
            id = randomUUID(),
            film = Film(
                id = randomUUID(),
                title = "Spiderman 2",
                type = NEW_RELEASE
            ),
            days = 3
        )
        private val pulpFiction = RentedFilm(
            id = randomUUID(),
            film = Film(
                id = randomUUID(),
                title = "Pulp fiction",
                type = OLD
            ),
            days = 3
        )

        private val customer = Customer(name = "John")

        private val rental = Rental(
            id = randomUUID(),
            customer = customer,
            rentedAt = now(),
            rentedFilms = listOf(
                spiderman,
                pulpFiction
            )
        )
    }
}