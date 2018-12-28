package com.github.jbibro.videorentalstore.rental

import com.github.jbibro.videorentalstore.rental.ErrorCode.INVALID_RENTAL_PERIOD
import com.github.jbibro.videorentalstore.rental.data.RentalRepository
import com.github.jbibro.videorentalstore.rental.usecases.RentFilm
import com.github.jbibro.videorentalstore.rental.usecases.RentFilmsInput
import com.github.jbibro.videorentalstore.rental.usecases.ReturnFilm
import com.github.jbibro.videorentalstore.rental.usecases.ReturnFilmInput
import com.github.jbibro.videorentalstore.rental.usecases.asResponse
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.util.UUID

class RentalHandler(private val rentFilm: RentFilm,
                    private val returnFilm: ReturnFilm,
                    private val repository: RentalRepository
) {
    fun create(request: ServerRequest) =
        request
            .bodyToMono<RentFilmsInput>()
            .flatMap {
                when (it.films.any { film -> film.days <= 0 }) {
                    true -> Mono.error(VideoRentalException(INVALID_RENTAL_PERIOD))
                    false -> Mono.just(it)
                }
            }
            .flatMap(rentFilm::execute)
            .flatMap { ServerResponse.status(CREATED).syncBody(it) }
            .onErrorResume(
                { it is VideoRentalException },
                { it -> it.asException<VideoRentalException>().toServerResponse() }
            )

    fun returnFilm(request: ServerRequest) =
        request
            .bodyToMono<ItemsToReturn>()
            .map { ReturnFilmInput(rentalId = UUID.fromString(request.pathVariable("id")), items = it.items) }
            .flatMap(returnFilm::execute)
            .flatMap { ServerResponse.ok().syncBody(it) }
            .onErrorResume(
                { it is VideoRentalException },
                { it -> it.asException<VideoRentalException>().toServerResponse() }
            )

    fun findAll() = ServerResponse.ok().body(repository.findAll().map { it.asResponse() })
}

data class ItemsToReturn(
    val items: List<UUID>
)
