package com.github.jbibro.videorentalstore.film

import com.github.jbibro.videorentalstore.film.data.Film
import com.github.jbibro.videorentalstore.film.data.FilmRepository
import com.github.jbibro.videorentalstore.film.data.Type
import com.github.jbibro.videorentalstore.rental.ErrorCode
import com.github.jbibro.videorentalstore.rental.VideoRentalException
import com.github.jbibro.videorentalstore.rental.asException
import com.github.jbibro.videorentalstore.rental.toServerResponse
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.util.UUID

class FilmHandler(private val repository: FilmRepository) {
    fun findAll() = ServerResponse.ok().body(repository.findAll())

    fun findOne(request: ServerRequest) =
        repository.findById(UUID.fromString(request.pathVariable("id")))
            .switchIfEmpty(Mono.error(VideoRentalException(ErrorCode.CUSTOMER_NOT_FOUND)))
            .flatMap { ServerResponse.ok().syncBody(it) }
            .onErrorResume(
                { it is VideoRentalException },
                { it -> it.asException<VideoRentalException>().toServerResponse() }
            )

    fun create(request: ServerRequest) =
        request.bodyToMono<FilmInput>()
            .flatMap { repository.save(Film(title = it.title, type = it.type)) }
            .flatMap { ServerResponse.status(CREATED).syncBody(it) }
}

data class FilmInput(
    val title: String,
    val type: Type
)