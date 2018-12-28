package com.github.jbibro.videorentalstore

import com.github.jbibro.videorentalstore.customer.data.Customer
import com.github.jbibro.videorentalstore.film.data.Film
import com.github.jbibro.videorentalstore.rental.data.Rental
import com.github.jbibro.videorentalstore.rental.usecases.RentalResponse
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.test
import java.time.LocalDateTime.now
import java.util.UUID
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApiTest {

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var mongoTemplate: ReactiveMongoTemplate

    @BeforeEach
    fun beforeEach() {
        pulpFictionId = createFilm(
            """
                {
                    "title": "Pulp Fiction",
                    "type": "OLD"
                }
            """
        )

        spidermanId = createFilm(
            """
                {
                    "title": "Spiderman",
                    "type": "NEW_RELEASE"
                }
            """
        )
        customerId = createCustomer()
    }

    @Test
    fun `create rental`() {
        // rent spiderman for 2 days and pulp fiction for 3
        rent(listOf(spidermanId to 2, pulpFictionId to 3))
            .expectStatus()
            .isCreated
            .expectBody().json(
                """
                   {
                        "customerId": "$customerId",
                        "regularPrice": 110,
                        "surcharge": 0,
                        "items": [
                            {
                                "filmId": "$pulpFictionId",
                                "days": 3,
                                "regularPrice": 30,
                                "surcharge": 0,
                                "returnedAt": null
                            },
                            {
                                "filmId": "$spidermanId",
                                "days": 2,
                                "regularPrice": 80,
                                "surcharge": 0,
                                "returnedAt": null
                            }
                        ]
                    }
                """
            )
    }

    @Test
    fun `customer gets bonus points`() {
        rent(listOf(spidermanId to 2, pulpFictionId to 3))

        // customer gets bonus points
        await().atMost(3, TimeUnit.SECONDS).untilAsserted {
            webClient
                .get()
                .uri("/api/customers/{id}", customerId)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.bonusPoints").isEqualTo(3)
        }
    }

    @Test
    fun `return film`() {
        //create rental
        val rental = rent(listOf(spidermanId to 2, pulpFictionId to 3))
            .expectBody(RentalResponse::class.java)
            .returnResult()
            .responseBody!!

        val rentedSpidermanId = rental.items.first { it.filmId == spidermanId }.id

        // return spiderman
        returnFilm(rental.id, rentedSpidermanId)
            .expectBody()
            .jsonPath("$.items[?(@.id=='$rentedSpidermanId')].returnedAt").exists()
    }

    @Test
    fun `test surcharge if film is returned after due date`() {
        // I rent spiderman for 2 days
        val rental = rent(listOf(spidermanId to 2, pulpFictionId to 3))
            .expectBody(RentalResponse::class.java)
            .returnResult()
            .responseBody!!

        // modify rental date directly in mongo, rented 3 days and 1 hour ago
        mongoTemplate
            .updateMulti(
                Query.query(Criteria.where("id").isEqualTo(rental.id).and("rentedFilms.film.id").isEqualTo(spidermanId)),
                Update.update("rentedFilms.$.rentedAt", now().minusDays(3).minusHours(1)),
                Rental::class.java
            )
            .test()
            .expectNextCount(1)
            .verifyComplete()

        val rentedSpidermanId = rental.items.first { it.filmId == spidermanId }.id

        // return spiderman
        returnFilm(rental.id, rentedSpidermanId)
            .expectBody()
            .jsonPath("$.surcharge").isEqualTo(80) // 2 days late
            .jsonPath("$.items[?(@.filmId=='$spidermanId')].surcharge").isEqualTo(80)
            .jsonPath("$.items[?(@.filmId=='$pulpFictionId')].surcharge").isEqualTo(0)
    }


    private fun createCustomer() =
        webClient
            .post()
            .uri("/api/customers")
            .contentType(APPLICATION_JSON)
            .syncBody(
                """
                    {
                        "name": "John"
                    }

                """
            )
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody(Customer::class.java)
            .returnResult()
            .responseBody!!
            .id

    private fun createFilm(json: String) =
        webClient
            .post()
            .uri("/api/films")
            .contentType(APPLICATION_JSON)
            .syncBody(json)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody(Film::class.java)
            .returnResult()
            .responseBody!!
            .id


    private fun rent(films: List<Pair<UUID, Int>>) =
        webClient
            .post()
            .uri("/api/rentals")
            .contentType(APPLICATION_JSON)
            .syncBody(
                """
                    {
                        "customerId": "$customerId",
                        "films": [
                            ${films.joinToString(",") {
                    """
                                {
                                    "id": "${it.first}",
                                    "days": ${it.second}
                                }
                            """
                }
                }
                        ]
                    }

                    """
            )
            .exchange()

    private fun returnFilm(rentalId: UUID, filmId: UUID) =
        webClient
            .put()
            .uri("/api/rentals/{id}", rentalId)
            .contentType(APPLICATION_JSON)
            .syncBody(
                """
                        {
                           "items": ["$filmId"]
                        }
                    """
            )
            .exchange()

    companion object {
        private var customerId = UUID.randomUUID()
        private var pulpFictionId = UUID.randomUUID()
        private var spidermanId = UUID.randomUUID()
    }

}