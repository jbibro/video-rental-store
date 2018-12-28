package com.github.jbibro.videorentalstore.customer

import com.github.jbibro.videorentalstore.customer.data.Customer
import com.github.jbibro.videorentalstore.customer.data.CustomerRepository
import com.github.jbibro.videorentalstore.rental.ErrorCode.CUSTOMER_NOT_FOUND
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

class CustomerHandler(private val repository: CustomerRepository) {
    fun findAll() = ServerResponse.ok().body(repository.findAll())

    fun findOne(request: ServerRequest) =
        repository.findById(UUID.fromString(request.pathVariable("id")))
            .switchIfEmpty(Mono.error(VideoRentalException(CUSTOMER_NOT_FOUND)))
            .flatMap { ServerResponse.ok().syncBody(it) }
            .onErrorResume(
                { it is VideoRentalException },
                { it -> it.asException<VideoRentalException>().toServerResponse() }
            )

    fun create(request: ServerRequest) =
        request.bodyToMono<CustomerInput>()
            .flatMap { repository.save(Customer(name = it.name)) }
            .flatMap { ServerResponse.status(CREATED).syncBody(it) }
}

data class CustomerInput(
    val name: String
)