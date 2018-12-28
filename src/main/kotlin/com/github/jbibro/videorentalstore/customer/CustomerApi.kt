package com.github.jbibro.videorentalstore.customer

import org.springframework.web.reactive.function.server.router

class CustomerApi(private val handler: CustomerHandler) {
    fun router() = router {
        "/api".nest {
            GET("/customers") { handler.findAll() }
            GET("/customers/{id}", handler::findOne)
            POST("/customers", handler::create)
        }
    }
}