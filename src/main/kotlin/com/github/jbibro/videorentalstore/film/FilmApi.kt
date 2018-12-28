package com.github.jbibro.videorentalstore.film

import org.springframework.web.reactive.function.server.router

class FilmApi(private val handler: FilmHandler) {
    fun router() = router {
        "/api".nest {
            GET("/films") { handler.findAll() }
            GET("/films/{id}", handler::findOne)
            POST("/films", handler::create)
        }
    }
}