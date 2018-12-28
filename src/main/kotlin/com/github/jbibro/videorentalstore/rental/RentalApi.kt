package com.github.jbibro.videorentalstore.rental

import org.springframework.web.reactive.function.server.router

class RentalApi(private val handler: RentalHandler) {
    fun router() = router {
        "/api".nest {
            GET("/rentals") { handler.findAll() }
            POST("/rentals", handler::create)
            PUT("/rentals/{id}", handler::returnFilm)
        }
    }
}