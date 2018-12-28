package com.github.jbibro.videorentalstore.rental.data

import com.github.jbibro.videorentalstore.customer.data.Customer
import com.github.jbibro.videorentalstore.film.data.Film
import com.github.jbibro.videorentalstore.film.data.Type
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static com.github.jbibro.videorentalstore.film.data.Type.*
import static java.time.LocalDateTime.now
import static java.util.UUID.randomUUID

class RentalSpec extends Specification {
    @Unroll
    def "#type film costs #price for #days days"() {
        given:
        def rentedFilm = rentedFilm(type, days)

        expect:
        rentedFilm.regularPrice() == price

        where:
        type        | days || price
        NEW_RELEASE | 0    || 0
        NEW_RELEASE | 2    || 80

        REGULAR     | 0    || 0
        REGULAR     | 3    || 30
        REGULAR     | 5    || 90

        OLD         | 0    || 0
        OLD         | 5    || 30
        OLD         | 7    || 90
    }

    @Unroll
    def "surcharge for #type is #surcharge for #extraDays days late return"() {
        given:
        def days = 5 // rented for 5 days
        def rentedAt = LocalDateTime.of(2018, 1, 1, 10, 0) // on 01.01.2018 10:00
        def returnedAt = rentedAt.plusDays(days + extraDays) // returned 5 + extraDays later
        def rentedFilm = rentedFilm(type, days, rentedAt, returnedAt)


        expect:
        rentedFilm.surcharge() == surcharge

        where:
        type        | extraDays || surcharge
        NEW_RELEASE | 0         || 0
        NEW_RELEASE | 1         || 40
        NEW_RELEASE | 2         || 80

        REGULAR     | 0         || 0
        REGULAR     | 1         || 30
        REGULAR     | 2         || 60

        OLD         | 0         || 0
        OLD         | 1         || 30
        OLD         | 2         || 60
    }

    def "part of rental day should count as whole day"() {
        given: "I rent for 1 day"
        def days = 1
        def rentedAt = LocalDateTime.of(2018, 1, 1, 10, 0)

        and: "I return 2 hours after due day"
        def returnedAt = rentedAt.plusDays(days).plusHours(2)

        when:
        def surcharge = rentedFilm(NEW_RELEASE, days, rentedAt, returnedAt).surcharge()

        then: "surcharge should be applied for 1 extra day"
        surcharge == 40
    }

    def "surcharge should be calculated for not returned film"() {
        given: "I rent for 1 day, day and 10 minutes ago"
        def days = 1
        def rentedAt = now().minusDays(1).minusMinutes(10)

        and: "I haven't returned yet"
        def returnedAt = null

        when:
        def surcharge = rentedFilm(NEW_RELEASE, days, rentedAt, returnedAt).surcharge()

        then: "surcharge should be applied for 1 extra day"
        surcharge == 40
    }

    def "sums prices for all films"() {
        given:
        def first = rentedFilm(NEW_RELEASE, 1) // 40
        def second = rentedFilm(REGULAR, 2) // 30
        def rental = new Rental(randomUUID(), customer(), [first, second], now())

        when:
        def price = rental.regularPrice()

        then:
        price == 40 + 30
    }

    def rentedFilm(Type type, int days) {
        return new RentedFilm(randomUUID(), new Film(randomUUID(), "Spiderman", type), days, now(), now())
    }

    def rentedFilm(Type type, int days, LocalDateTime rentedAt, LocalDateTime returnedAt) {
        return new RentedFilm(randomUUID(), new Film(randomUUID(), "Spiderman", type), days, rentedAt, returnedAt)
    }

    def customer() {
        new Customer(randomUUID(), "John", 0)
    }
}
