package com.github.jbibro.videorentalstore.rental.usecases

import com.github.jbibro.videorentalstore.customer.data.Customer
import com.github.jbibro.videorentalstore.customer.data.CustomerRepository
import com.github.jbibro.videorentalstore.events.RentalCreated
import com.github.jbibro.videorentalstore.film.data.Film
import com.github.jbibro.videorentalstore.film.data.FilmRepository
import com.github.jbibro.videorentalstore.film.data.Type.NEW_RELEASE
import com.github.jbibro.videorentalstore.rental.ErrorCode.CUSTOMER_NOT_FOUND
import com.github.jbibro.videorentalstore.rental.ErrorCode.FILM_NOT_FOUND
import com.github.jbibro.videorentalstore.rental.VideoRentalException
import com.github.jbibro.videorentalstore.rental.data.Price
import com.github.jbibro.videorentalstore.rental.data.RentalRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.test.test
import java.util.UUID

@ExtendWith(SpringExtension::class)
@DataMongoTest
class RentFilmTest {
    lateinit var rentFilm: RentFilm

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var filmRepository: FilmRepository

    @Autowired
    lateinit var rentalRepository: RentalRepository

    @MockBean
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @BeforeEach
    fun before() {
        rentFilm = RentFilm(rentalRepository, filmRepository, customerRepository, applicationEventPublisher)

        customerRepository.save(customer)
            .test()
            .expectNextCount(1)
            .verifyComplete()

        filmRepository.save(film)
            .test()
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `persists new rental`() {
        //when
        rentFilm.execute(
            RentFilmsInput(
                customerId = customer.id,
                films = listOf(FilmAndRentalPeriod(id = film.id, days = days))
            ))
            .test()
            .expectNextCount(1)
            .verifyComplete()

        //then
        rentalRepository.findAll()
            .test()
            .expectNextMatches {
                    it.customer.id == customer.id &&
                    it.regularPrice() == Price.PREMIUM.amount * days &&
                    it.surcharge() == 0 &&
                    it.rentedFilms.size == 1
                    it.rentedFilms.any { rentedFilm ->
                            rentedFilm.days == days &&
                            rentedFilm.film == film &&
                            rentedFilm.regularPrice() == Price.PREMIUM.amount * days &&
                            rentedFilm.surcharge() == 0
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `return error when customer not found`() {
        //when
        rentFilm.execute(
            RentFilmsInput(
                customerId = UUID.randomUUID(), //not existing customer
                films = listOf(FilmAndRentalPeriod(id = film.id, days = days))
            ))
            .test()
            .expectErrorMatches { it is VideoRentalException && it.errorCode == CUSTOMER_NOT_FOUND }
            .verify()
    }

    @Test
    fun `return error when film not found`() {
        //when
        rentFilm.execute(
            RentFilmsInput(
                customerId = customer.id,
                films = listOf(FilmAndRentalPeriod(id = UUID.randomUUID(), days = days)) //not existing film
            ))
            .test()
            .expectErrorMatches { it is VideoRentalException && it.errorCode == FILM_NOT_FOUND }
            .verify()
    }

    @Test
    fun `event is published`() {
        //when
        rentFilm.execute(
            RentFilmsInput(
                customerId = customer.id,
                films = listOf(FilmAndRentalPeriod(id = film.id, days = days))
            ))
            .test()
            .expectNextCount(1)
            .verifyComplete()

        //then
        verify(applicationEventPublisher).publishEvent(ArgumentMatchers.any(RentalCreated::class.java))
    }

    companion object {
        private const val days = 3
        private val customer = Customer(
            id = UUID.randomUUID(),
            name = "John"
        )

        private val film = Film(
            id = UUID.randomUUID(),
            title = "Spiderman",
            type = NEW_RELEASE
        )
    }
}